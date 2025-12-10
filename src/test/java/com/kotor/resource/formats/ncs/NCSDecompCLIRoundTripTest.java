// Copyright 2021-2025 NCSDecomp
// Licensed under the Business Source License 1.1 (BSL 1.1).
// Visit https://bolabaden.org for more information and other ventures
// See LICENSE.txt file in the project root for full license information.

package com.kotor.resource.formats.ncs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * Exhaustive round-trip tests:
 *  1) Clone or use existing Vanilla_KOTOR_Script_Source repository
 *  2) Use nwnnsscomp.exe to compile each .nss -> .ncs (per game)
 *  3) Use NCSDecompCLI to decompile NCS -> NSS
 *  4) Compare normalized text with the original NSS
 *  5) Fast-fail on first failure
 *
 * All test artifacts are created in gitignored directories.
 */
public class NCSDecompCLIRoundTripTest {

   // Working directory (gitignored)
   private static final Path TEST_WORK_DIR = Paths.get(".").toAbsolutePath().normalize()
         .resolve("test-work");
   private static final Path VANILLA_REPO_DIR = TEST_WORK_DIR.resolve("Vanilla_KOTOR_Script_Source");
   private static final String VANILLA_REPO_URL = "https://github.com/KOTORCommunityPatches/Vanilla_KOTOR_Script_Source.git";

   // Paths relative to DeNCS directory
   private static final Path REPO_ROOT = Paths.get(".").toAbsolutePath().normalize();
   private static final Path NWN_COMPILER = REPO_ROOT.resolve("tools").resolve("nwnnsscomp.exe");
   private static final Path K1_NWSCRIPT = REPO_ROOT.resolve("src").resolve("main").resolve("resources").resolve("k1_nwscript.nss");
   private static final Path K2_NWSCRIPT = REPO_ROOT.resolve("src").resolve("main").resolve("resources").resolve("tsl_nwscript.nss");

   // Test output directories (gitignored)
   private static final Path WORK_ROOT = TEST_WORK_DIR.resolve("roundtrip-work");
   private static final Path PROFILE_OUTPUT = TEST_WORK_DIR.resolve("test_profile.txt");

   private static final Duration PROC_TIMEOUT = Duration.ofSeconds(25);

   // Performance tracking
   private static final Map<String, Long> operationTimes = new HashMap<>();
   private static long testStartTime;
   private static int totalTests = 0;
   private static int testsProcessed = 0;

   private static Path k1Scratch;
   private static Path k2Scratch;

   /**
    * Clone or update the Vanilla_KOTOR_Script_Source repository.
    */
   static void ensureVanillaRepo() throws IOException, InterruptedException {
      if (Files.exists(VANILLA_REPO_DIR) && Files.isDirectory(VANILLA_REPO_DIR)) {
         // Check if it's a valid git repo
         Path gitDir = VANILLA_REPO_DIR.resolve(".git");
         if (Files.exists(gitDir) && Files.isDirectory(gitDir)) {
            System.out.println("Using existing Vanilla_KOTOR_Script_Source repository at: " + VANILLA_REPO_DIR);
            // Optionally update: git pull
            return;
         } else {
            // Directory exists but isn't a git repo, remove it
            System.out.println("Removing non-git directory: " + VANILLA_REPO_DIR);
            deleteDirectory(VANILLA_REPO_DIR);
         }
      }

      // Clone the repository
      System.out.println("Cloning Vanilla_KOTOR_Script_Source repository...");
      System.out.println("  URL: " + VANILLA_REPO_URL);
      System.out.println("  Destination: " + VANILLA_REPO_DIR);

      Files.createDirectories(VANILLA_REPO_DIR.getParent());

      ProcessBuilder pb = new ProcessBuilder("git", "clone", VANILLA_REPO_URL, VANILLA_REPO_DIR.toString());
      pb.redirectErrorStream(true);
      Process proc = pb.start();

      // Capture output
      StringBuilder output = new StringBuilder();
      try (java.io.BufferedReader reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(proc.getInputStream()))) {
         String line;
         while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
         }
      }

      int exitCode = proc.waitFor();
      if (exitCode != 0) {
         throw new IOException("Failed to clone repository. Exit code: " + exitCode +
               "\nOutput: " + output.toString());
      }

      System.out.println("Repository cloned successfully.");
   }

   static void preflight() throws IOException, InterruptedException {
      System.out.println("=== Preflight Checks ===");

      // Ensure vanilla repo exists
      ensureVanillaRepo();

      // Check for required files
      if (!Files.isRegularFile(NWN_COMPILER)) {
         throw new IOException("nwnnsscomp.exe missing at: " + NWN_COMPILER);
      }
      System.out.println("✓ Found compiler: " + NWN_COMPILER);

      if (!Files.isRegularFile(K1_NWSCRIPT)) {
         throw new IOException("k1_nwscript.nss missing at: " + K1_NWSCRIPT);
      }
      System.out.println("✓ Found K1 nwscript: " + K1_NWSCRIPT);

      if (!Files.isRegularFile(K2_NWSCRIPT)) {
         throw new IOException("tsl_nwscript.nss missing at: " + K2_NWSCRIPT);
      }
      System.out.println("✓ Found TSL nwscript: " + K2_NWSCRIPT);

      // Verify vanilla repo structure
      Path k1Root = VANILLA_REPO_DIR.resolve("K1");
      Path tslRoot = VANILLA_REPO_DIR.resolve("TSL");
      if (!Files.exists(k1Root) || !Files.isDirectory(k1Root)) {
         throw new IOException("K1 directory not found in vanilla repo: " + k1Root);
      }
      if (!Files.exists(tslRoot) || !Files.isDirectory(tslRoot)) {
         throw new IOException("TSL directory not found in vanilla repo: " + tslRoot);
      }
      System.out.println("✓ Vanilla repo structure verified");

      // Prepare scratch directories
      k1Scratch = prepareScratch("k1", K1_NWSCRIPT);
      k2Scratch = prepareScratch("k2", K2_NWSCRIPT);

      // Copy nwscript files to current working directory for FileDecompiler
      Path cwd = Paths.get(System.getProperty("user.dir"));
      Path k1Nwscript = cwd.resolve("k1_nwscript.nss");
      Path k2Nwscript = cwd.resolve("tsl_nwscript.nss");

      if (!Files.exists(k1Nwscript) || !Files.isSameFile(K1_NWSCRIPT, k1Nwscript)) {
         Files.copy(K1_NWSCRIPT, k1Nwscript, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      }
      if (!Files.exists(k2Nwscript) || !Files.isSameFile(K2_NWSCRIPT, k2Nwscript)) {
         Files.copy(K2_NWSCRIPT, k2Nwscript, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      }

      System.out.println("=== Preflight Complete ===\n");
   }

   List<RoundTripCase> buildRoundTripCases() throws IOException {
      System.out.println("=== Discovering Test Files ===");

      List<TestItem> allFiles = new ArrayList<>();

      // K1 files
      Path k1Root = VANILLA_REPO_DIR.resolve("K1");
      if (Files.exists(k1Root)) {
         try (Stream<Path> paths = Files.walk(k1Root)) {
            paths.filter(p -> p.toString().toLowerCase().endsWith(".nss"))
                  .forEach(p -> allFiles.add(new TestItem(p, "k1", k1Scratch)));
         }
      }

      // TSL files
      Path tslVanilla = VANILLA_REPO_DIR.resolve("TSL").resolve("Vanilla");
      if (Files.exists(tslVanilla)) {
         try (Stream<Path> paths = Files.walk(tslVanilla)) {
            paths.filter(p -> p.toString().toLowerCase().endsWith(".nss"))
                  .forEach(p -> allFiles.add(new TestItem(p, "k2", k2Scratch)));
         }
      }

      Path tslTslrcm = VANILLA_REPO_DIR.resolve("TSL").resolve("TSLRCM");
      if (Files.exists(tslTslrcm)) {
         try (Stream<Path> paths = Files.walk(tslTslrcm)) {
            paths.filter(p -> p.toString().toLowerCase().endsWith(".nss"))
                  .forEach(p -> allFiles.add(new TestItem(p, "k2", k2Scratch)));
         }
      }

      System.out.println("Found " + allFiles.size() + " .nss files");

      // Shuffle for better distribution
      Collections.shuffle(allFiles);

      List<RoundTripCase> tests = new ArrayList<>();
      for (TestItem item : allFiles) {
         Path relPath = VANILLA_REPO_DIR.relativize(item.path);
         String displayName = item.gameFlag.equals("k1")
               ? "K1: " + relPath
               : "TSL: " + relPath;
         tests.add(new RoundTripCase(displayName, item));
      }

      totalTests = tests.size();
      System.out.println("=== Test Discovery Complete ===\n");

      return tests;
   }

   private static class TestItem {
      final Path path;
      final String gameFlag;
      final Path scratchRoot;

      TestItem(Path path, String gameFlag, Path scratchRoot) {
         this.path = path;
         this.gameFlag = gameFlag;
         this.scratchRoot = scratchRoot;
      }
   }

   private static class RoundTripCase {
      final String displayName;
      final TestItem item;

      RoundTripCase(String displayName, TestItem item) {
         this.displayName = displayName;
         this.item = item;
      }
   }

   private static void roundTripSingle(Path nssPath, String gameFlag, Path scratchRoot) throws Exception {
      long startTime = System.nanoTime();

      Path rel = VANILLA_REPO_DIR.relativize(nssPath);
      Path outDir = scratchRoot.resolve(rel.getParent() == null ? Paths.get("") : rel.getParent());
      Files.createDirectories(outDir);

      // Compile: NSS -> NCS
      Path compiled = outDir.resolve(stripExt(rel.getFileName().toString()) + ".ncs");
      long compileStart = System.nanoTime();
      runCompiler(nssPath, compiled, gameFlag, scratchRoot);
      long compileTime = System.nanoTime() - compileStart;
      operationTimes.merge("compile", compileTime, Long::sum);

      // Decompile: NCS -> NSS
      Path decompiled = outDir.resolve(stripExt(rel.getFileName().toString()) + ".dec.nss");
      long decompileStart = System.nanoTime();
      runDecompile(compiled, decompiled, gameFlag);
      long decompileTime = System.nanoTime() - decompileStart;
      operationTimes.merge("decompile", decompileTime, Long::sum);

      // Compare
      long compareStart = System.nanoTime();
      String original = normalizeNewlines(Files.readString(nssPath, StandardCharsets.UTF_8));
      String roundtrip = normalizeNewlines(Files.readString(decompiled, StandardCharsets.UTF_8));
      long compareTime = System.nanoTime() - compareStart;
      operationTimes.merge("compare", compareTime, Long::sum);

      if (!original.equals(roundtrip)) {
         String diff = formatUnifiedDiff(original, roundtrip);
         StringBuilder message = new StringBuilder("Round-trip mismatch for ").append(nssPath);
         if (diff != null) {
            message.append(System.lineSeparator()).append(diff);
         }
         throw new IllegalStateException(message.toString());
      }

      long totalTime = System.nanoTime() - startTime;
      operationTimes.merge("total", totalTime, Long::sum);
   }

   private static void runCompiler(Path nssPath, Path compiledOut, String gameFlag, Path workDir) throws Exception {
      // Ensure nwscript.nss is in the compiler's directory
      Path compilerDir = NWN_COMPILER.getParent();
      Path nwscriptSource = "k2".equals(gameFlag) ? K2_NWSCRIPT : K1_NWSCRIPT;
      Path compilerNwscript = compilerDir.resolve("nwscript.nss");
      if (!Files.exists(compilerNwscript) || !Files.isSameFile(nwscriptSource, compilerNwscript)) {
         Files.copy(nwscriptSource, compilerNwscript, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      }

      Files.createDirectories(compiledOut.getParent());

      java.io.File compilerFile = NWN_COMPILER.toAbsolutePath().toFile();
      java.io.File sourceFile = nssPath.toAbsolutePath().toFile();
      java.io.File outputFile = compiledOut.toAbsolutePath().toFile();
      boolean isK2 = "k2".equals(gameFlag);

      NwnnsscompConfig config = new NwnnsscompConfig(compilerFile, sourceFile, outputFile, isK2);
      String[] cmd = config.getCompileArgs(compilerFile.getAbsolutePath());

      ProcessBuilder pb = new ProcessBuilder(cmd);
      pb.redirectErrorStream(true);
      Process proc = pb.start();

      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream()));
      StringBuilder output = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
         output.append(line).append("\n");
      }

      boolean finished = proc.waitFor(PROC_TIMEOUT.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
      if (!finished) {
         proc.destroyForcibly();
         throw new RuntimeException("nwnnsscomp timed out for " + nssPath);
      }
      if (proc.exitValue() != 0 || !Files.isRegularFile(compiledOut)) {
         String errorMsg = "nwnnsscomp failed (exit=" + proc.exitValue() + ") for " + nssPath;
         if (output.length() > 0) {
            errorMsg += "\nCompiler output:\n" + output.toString();
         }
         throw new RuntimeException(errorMsg);
      }
   }

   private static void runDecompile(Path ncsPath, Path nssOut, String gameFlag) throws Exception {
      FileDecompiler.isK2Selected = "k2".equals(gameFlag);

      try {
         FileDecompiler fd = new FileDecompiler();
         File ncsFile = ncsPath.toFile();
         File nssFile = nssOut.toFile();

         Files.createDirectories(nssFile.getParentFile().toPath());

         fd.decompileToFile(ncsFile, nssFile, StandardCharsets.UTF_8, true);

         if (!Files.isRegularFile(nssOut)) {
            throw new RuntimeException("Decompile did not produce output: " + nssOut);
         }
      } catch (DecompilerException ex) {
         throw new RuntimeException("Decompile failed for " + ncsPath + ": " + ex.getMessage(), ex);
      }
   }

   private static Path prepareScratch(String gameLabel, Path nwscriptSource) throws IOException {
      Path scratch = WORK_ROOT.resolve(gameLabel);
      Files.createDirectories(scratch);
      Files.copy(nwscriptSource, scratch.resolve("nwscript.nss"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      return scratch;
   }

   private static String stripExt(String name) {
      int dot = name.lastIndexOf('.');
      return dot == -1 ? name : name.substring(0, dot);
   }

   private static String normalizeNewlines(String s) {
      String normalized = s.replace("\r\n", "\n").replace("\r", "\n");
      normalized = stripComments(normalized);

      String[] lines = normalized.split("\n", -1);
      StringBuilder result = new StringBuilder();
      boolean lastWasBlank = false;

      for (String line : lines) {
         String trimmed = line.replaceFirst("\\s+$", "");

         if (trimmed.isEmpty()) {
            if (!lastWasBlank) {
               result.append("\n");
               lastWasBlank = true;
            }
         } else {
            trimmed = trimmed.replace("\t", "    ").replaceAll(" +", " ");
            result.append(trimmed).append("\n");
            lastWasBlank = false;
         }
      }

      String finalResult = result.toString();
      while (finalResult.endsWith("\n")) {
         finalResult = finalResult.substring(0, finalResult.length() - 1);
      }

      return finalResult;
   }

   private static String stripComments(String code) {
      StringBuilder result = new StringBuilder();
      boolean inBlockComment = false;
      boolean inString = false;
      char[] chars = code.toCharArray();

      for (int i = 0; i < chars.length; i++) {
         if (inBlockComment) {
            if (i < chars.length - 1 && chars[i] == '*' && chars[i + 1] == '/') {
               inBlockComment = false;
               i++;
            }
            continue;
         }

         if (inString) {
            result.append(chars[i]);
            if (chars[i] == '"' && (i == 0 || chars[i - 1] != '\\')) {
               inString = false;
            }
            continue;
         }

         if (chars[i] == '"') {
            inString = true;
            result.append(chars[i]);
         } else if (i < chars.length - 1 && chars[i] == '/' && chars[i + 1] == '/') {
            while (i < chars.length && chars[i] != '\n') {
               i++;
            }
            if (i < chars.length) {
               result.append('\n');
            }
         } else if (i < chars.length - 1 && chars[i] == '/' && chars[i + 1] == '*') {
            inBlockComment = true;
            i++;
         } else {
            result.append(chars[i]);
         }
      }

      return result.toString();
   }

   private static String formatUnifiedDiff(String expected, String actual) {
      String[] expectedLines = expected.split("\n", -1);
      String[] actualLines = actual.split("\n", -1);

      DiffResult diffResult = computeDiff(expectedLines, actualLines);

      if (diffResult.isEmpty()) {
         return null;
      }

      StringBuilder diff = new StringBuilder();
      diff.append("    --- expected\n");
      diff.append("    +++ actual\n");

      int oldLineNum = 1;
      int newLineNum = 1;
      int firstOldLine = -1;
      int firstNewLine = -1;
      int lastOldLine = -1;
      int lastNewLine = -1;

      for (DiffLine line : diffResult.lines) {
         if (line.type == DiffLineType.REMOVED) {
            if (firstOldLine == -1) firstOldLine = oldLineNum;
            lastOldLine = oldLineNum;
            oldLineNum++;
         } else if (line.type == DiffLineType.ADDED) {
            if (firstNewLine == -1) firstNewLine = newLineNum;
            lastNewLine = newLineNum;
            newLineNum++;
         } else {
            if (firstOldLine != -1 && lastOldLine == oldLineNum - 1) {
               lastOldLine = oldLineNum;
            }
            if (firstNewLine != -1 && lastNewLine == newLineNum - 1) {
               lastNewLine = newLineNum;
            }
            oldLineNum++;
            newLineNum++;
         }
      }

      int oldStart, oldCount, newStart, newCount;
      if (firstOldLine == -1) {
         oldStart = 1;
         oldCount = expectedLines.length;
      } else {
         oldStart = firstOldLine;
         oldCount = lastOldLine - firstOldLine + 1;
      }

      if (firstNewLine == -1) {
         newStart = 1;
         newCount = actualLines.length;
      } else {
         newStart = firstNewLine;
         newCount = lastNewLine - firstNewLine + 1;
      }

      diff.append("    @@ -").append(oldStart);
      if (oldCount != 1) {
         diff.append(",").append(oldCount);
      }
      diff.append(" +").append(newStart);
      if (newCount != 1) {
         diff.append(",").append(newCount);
      }
      diff.append(" @@\n");

      for (DiffLine line : diffResult.lines) {
         switch (line.type) {
            case CONTEXT:
               diff.append("     ").append(line.content).append("\n");
               break;
            case REMOVED:
               diff.append("    -").append(line.content).append("\n");
               break;
            case ADDED:
               diff.append("    +").append(line.content).append("\n");
               break;
         }
      }

      return diff.toString();
   }

   private enum DiffLineType {
      CONTEXT, REMOVED, ADDED
   }

   private static class DiffLine {
      final DiffLineType type;
      final String content;

      DiffLine(DiffLineType type, String content) {
         this.type = type;
         this.content = content;
      }
   }

   private static class DiffResult {
      final List<DiffLine> lines = new ArrayList<>();

      boolean isEmpty() {
         return lines.stream().allMatch(l -> l.type == DiffLineType.CONTEXT);
      }
   }

   private static DiffResult computeDiff(String[] expected, String[] actual) {
      DiffResult result = new DiffResult();

      int m = expected.length;
      int n = actual.length;
      int[][] dp = new int[m + 1][n + 1];

      for (int i = 1; i <= m; i++) {
         for (int j = 1; j <= n; j++) {
            if (expected[i - 1].equals(actual[j - 1])) {
               dp[i][j] = dp[i - 1][j - 1] + 1;
            } else {
               dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
         }
      }

      int i = m, j = n;
      List<DiffLine> tempLines = new ArrayList<>();

      while (i > 0 || j > 0) {
         if (i > 0 && j > 0 && expected[i - 1].equals(actual[j - 1])) {
            tempLines.add(new DiffLine(DiffLineType.CONTEXT, expected[i - 1]));
            i--;
            j--;
         } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
            tempLines.add(new DiffLine(DiffLineType.ADDED, actual[j - 1]));
            j--;
         } else if (i > 0) {
            tempLines.add(new DiffLine(DiffLineType.REMOVED, expected[i - 1]));
            i--;
         }
      }

      for (int k = tempLines.size() - 1; k >= 0; k--) {
         result.lines.add(tempLines.get(k));
      }

      return result;
   }

   /**
    * Entry point for running the round-trip suite.
    */
   public static void main(String[] args) {
      NCSDecompCLIRoundTripTest runner = new NCSDecompCLIRoundTripTest();
      int exitCode = runner.runRoundTripSuite();
      if (exitCode != 0) {
         System.exit(exitCode);
      }
   }

   private int runRoundTripSuite() {
      testStartTime = System.nanoTime();

      try {
         preflight();
         List<RoundTripCase> tests = buildRoundTripCases();

         if (tests.isEmpty()) {
            System.err.println("ERROR: No test files found!");
            return 1;
         }

         System.out.println("=== Running Round-Trip Tests ===");
         System.out.println("Total tests: " + tests.size());
         System.out.println("Fast-fail: enabled (will stop on first failure)\n");

         for (RoundTripCase testCase : tests) {
            testsProcessed++;
            System.out.print(String.format("[%d/%d] %s ... ", testsProcessed, totalTests, testCase.displayName));

            try {
               roundTripSingle(testCase.item.path, testCase.item.gameFlag, testCase.item.scratchRoot);
               System.out.println("✓ PASSED");
            } catch (Exception ex) {
               System.out.println("✗ FAILED");
               System.out.println();
               System.out.println("═══════════════════════════════════════════════════════════");
               System.out.println("FAILURE: " + testCase.displayName);
               System.out.println("═══════════════════════════════════════════════════════════");
               System.out.println("Exception: " + ex.getClass().getSimpleName());
               String message = ex.getMessage();
               if (message != null && !message.isEmpty()) {
                  String diff = extractAndFormatDiff(message);
                  if (diff != null) {
                     System.out.println("\nDiff:");
                     System.out.println(diff);
                  } else {
                     System.out.println("Message: " + message);
                  }
               }
               if (ex.getCause() != null && ex.getCause() != ex) {
                  System.out.println("Cause: " + ex.getCause().getMessage());
               }
               System.out.println("═══════════════════════════════════════════════════════════");
               System.out.println();

               // Fast-fail: exit immediately on first failure
               printPerformanceSummary();
               return 1;
            }
         }

         System.out.println();
         System.out.println("═══════════════════════════════════════════════════════════");
         System.out.println("ALL TESTS PASSED!");
         System.out.println("═══════════════════════════════════════════════════════════");
         System.out.println("Tests run: " + tests.size());
         System.out.println("Tests passed: " + tests.size());
         System.out.println("Tests failed: 0");
         System.out.println();

         printPerformanceSummary();
         return 0;
      } catch (Exception e) {
         System.err.println("FATAL ERROR: " + e.getMessage());
         e.printStackTrace();
         printPerformanceSummary();
         return 1;
      }
   }

   private void printPerformanceSummary() {
      long totalTime = System.nanoTime() - testStartTime;

      System.out.println("═══════════════════════════════════════════════════════════");
      System.out.println("PERFORMANCE SUMMARY");
      System.out.println("═══════════════════════════════════════════════════════════");
      System.out.println(String.format("Total test time: %.2f seconds", totalTime / 1_000_000_000.0));
      System.out.println(String.format("Tests processed: %d / %d", testsProcessed, totalTests));

      if (testsProcessed > 0) {
         System.out.println();
         System.out.println("Operation breakdown (cumulative):");
         for (Map.Entry<String, Long> entry : operationTimes.entrySet()) {
            double seconds = entry.getValue() / 1_000_000_000.0;
            double percentage = (entry.getValue() * 100.0) / totalTime;
            System.out.println(String.format("  %-12s: %8.2f s (%5.1f%%)",
                  entry.getKey(), seconds, percentage));
         }

         System.out.println();
         System.out.println("Average per test:");
         double avgTotal = (operationTimes.getOrDefault("total", 0L) / 1_000_000_000.0) / testsProcessed;
         double avgCompile = (operationTimes.getOrDefault("compile", 0L) / 1_000_000_000.0) / testsProcessed;
         double avgDecompile = (operationTimes.getOrDefault("decompile", 0L) / 1_000_000_000.0) / testsProcessed;
         double avgCompare = (operationTimes.getOrDefault("compare", 0L) / 1_000_000_000.0) / testsProcessed;

         System.out.println(String.format("  Total:      %.3f s", avgTotal));
         System.out.println(String.format("  Compile:    %.3f s", avgCompile));
         System.out.println(String.format("  Decompile:  %.3f s", avgDecompile));
         System.out.println(String.format("  Compare:    %.3f s", avgCompare));
      }

      System.out.println();
      System.out.println("Profile log: " + PROFILE_OUTPUT);
      System.out.println("═══════════════════════════════════════════════════════════");
   }

   private String extractAndFormatDiff(String message) {
      int expectedStart = message.indexOf("expected: <");
      int butWasStart = message.indexOf(" but was: <");

      if (expectedStart == -1 || butWasStart == -1) {
         return null;
      }

      int expectedValueStart = expectedStart + "expected: <".length();
      int expectedValueEnd = message.indexOf(">", expectedValueStart);
      int actualValueStart = butWasStart + " but was: <".length();
      int actualValueEnd = message.lastIndexOf(">");

      if (expectedValueEnd == -1 || actualValueEnd == -1 || actualValueEnd <= actualValueStart) {
         return null;
      }

      String expected = message.substring(expectedValueStart, expectedValueEnd);
      String actual = message.substring(actualValueStart, actualValueEnd);

      return formatUnifiedDiff(expected, actual);
   }

   private static void deleteDirectory(Path dir) throws IOException {
      if (Files.exists(dir)) {
         try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                  .forEach(path -> {
                     try {
                        Files.delete(path);
                     } catch (IOException e) {
                        // Ignore deletion errors
                     }
                  });
         }
      }
   }
}
