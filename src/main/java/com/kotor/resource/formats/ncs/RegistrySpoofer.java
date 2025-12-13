// Copyright 2021-2025 NCSDecomp
// Licensed under the Business Source License 1.1 (BSL 1.1).
// See LICENSE.txt file in the project root for full license information.

package com.kotor.resource.formats.ncs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Temporarily spoofs Windows registry keys to point legacy compilers to the correct installation path.
 * <p>
 * Some legacy nwnnsscomp variants (KOTOR Tool, KOTOR Scripting Tool) read the game installation
 * path from the Windows registry instead of accepting command-line arguments. This class provides
 * a context manager pattern to temporarily set the registry keys during compilation/decompilation.
 * <p>
 * Registry paths:
 * <ul>
 *   <li>KotOR 1: HKEY_LOCAL_MACHINE\SOFTWARE\WOW6432Node\BioWare\SW\KOTOR (64-bit) or
 *       HKEY_LOCAL_MACHINE\SOFTWARE\BioWare\SW\KOTOR (32-bit)</li>
 *   <li>KotOR 2: HKEY_LOCAL_MACHINE\SOFTWARE\WOW6432Node\LucasArts\KotOR2 (64-bit) or
 *       HKEY_LOCAL_MACHINE\SOFTWARE\LucasArts\KotOR2 (32-bit)</li>
 * </ul>
 * Key name: "Path"
 * <p>
 * When registry modification fails due to insufficient privileges, this class will:
 * <ul>
 *   <li>First time: Prompt the user and attempt to elevate privileges via RunAs</li>
 *   <li>Subsequent times: Show an informational message suggesting to run as admin or use a different compiler</li>
 * </ul>
 */
public class RegistrySpoofer implements AutoCloseable {
   private final String registryPath;
   private final String keyName;
   private final String spoofedPath;
   private String originalValue;
   private boolean wasModified = false;
   private static final String ELEVATION_PROMPT_MARKER_FILE = "ncsdecomp_registry_elevation_shown.txt";

   /**
    * Creates a new registry spoofer for the specified game.
    *
    * @param installationPath The path to spoof in the registry (typically the tools directory)
    * @param isK2 true for KotOR 2 (TSL), false for KotOR 1
    * @throws UnsupportedOperationException If not running on Windows
    */
   public RegistrySpoofer(File installationPath, boolean isK2) {
      if (!System.getProperty("os.name").toLowerCase().contains("win")) {
         throw new UnsupportedOperationException("Registry spoofing is only supported on Windows");
      }

      this.spoofedPath = installationPath.getAbsolutePath();
      this.keyName = "Path";

      // Determine registry path based on game and architecture
      boolean is64Bit = is64BitArchitecture();
      if (isK2) {
         if (is64Bit) {
            this.registryPath = "HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\LucasArts\\KotOR2";
         } else {
            this.registryPath = "HKEY_LOCAL_MACHINE\\SOFTWARE\\LucasArts\\KotOR2";
         }
      } else {
         if (is64Bit) {
            this.registryPath = "HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\BioWare\\SW\\KOTOR";
         } else {
            this.registryPath = "HKEY_LOCAL_MACHINE\\SOFTWARE\\BioWare\\SW\\KOTOR";
         }
      }

      // Read original value
      this.originalValue = readRegistryValue(this.registryPath, this.keyName);
      System.err.println("DEBUG RegistrySpoofer: Created spoofer for " + (isK2 ? "K2" : "K1") +
            ", registryPath=" + this.registryPath + ", spoofedPath=" + this.spoofedPath +
            ", originalValue=" + (this.originalValue != null ? this.originalValue : "(null)"));
   }

   /**
    * Activates the registry spoof by setting the registry key to the spoofed path.
    * This should be called at the start of a try-with-resources block.
    * <p>
    * If permission is denied, this method will:
    * <ul>
    *   <li>First time: Prompt user and attempt elevation via RunAs</li>
    *   <li>Subsequent times: Show informational message and return without modifying registry</li>
    * </ul>
    *
    * @return this instance for method chaining
    * @throws SecurityException If registry modification fails and elevation is not possible or refused
    */
   public RegistrySpoofer activate() {
      if (this.originalValue != null && this.originalValue.equals(this.spoofedPath)) {
         System.err.println("DEBUG RegistrySpoofer: Registry value already matches spoofed path, skipping");
         return this;
      }

      try {
         writeRegistryValue(this.registryPath, this.keyName, this.spoofedPath);
         this.wasModified = true;
         System.err.println("DEBUG RegistrySpoofer: Successfully set registry key " + this.registryPath +
               "\\" + this.keyName + " to " + this.spoofedPath);
      } catch (SecurityException e) {
         System.err.println("DEBUG RegistrySpoofer: Failed to set registry key: " + e.getMessage());
         
         // Check if we've shown the elevation prompt before
         boolean hasShownPrompt = hasShownElevationPrompt();
         
         if (!hasShownPrompt) {
            // First time - prompt and attempt elevation
            if (attemptElevatedRegistryWrite()) {
               // Successfully set via elevation
               this.wasModified = true;
               markElevationPromptShown();
               System.err.println("DEBUG RegistrySpoofer: Successfully set registry key via elevation");
               return this;
            } else {
               // User refused or elevation failed
               markElevationPromptShown();
               throw new SecurityException("Permission denied. Administrator privileges required to spoof registry. " +
                     "Error: " + e.getMessage(), e);
            }
         } else {
            // Subsequent times - show informational message
            showSubsequentElevationMessage();
            // Don't throw - allow compilation to proceed (might fail, but we've warned the user)
            return this;
         }
      }
      return this;
   }

   /**
    * Restores the original registry value.
    * This is automatically called when the spoofer is closed in a try-with-resources block.
    */
   @Override
   public void close() {
      if (this.wasModified && this.originalValue != null && !this.originalValue.equals(this.spoofedPath)) {
         try {
            writeRegistryValue(this.registryPath, this.keyName, this.originalValue);
            System.err.println("DEBUG RegistrySpoofer: Restored registry key " + this.registryPath +
                  "\\" + this.keyName + " to original value: " + this.originalValue);
         } catch (Exception e) {
            System.err.println("DEBUG RegistrySpoofer: Failed to restore registry key: " + e.getMessage());
            // Don't throw - we've done our best to restore
         }
      } else if (this.wasModified && this.originalValue == null) {
         // Original value didn't exist - we could try to delete it, but that's more complex
         // Just log a warning
         System.err.println("DEBUG RegistrySpoofer: Registry key was created but original value was null. " +
               "Key will remain set to spoofed path.");
      }
   }

   /**
    * Gets the registry path being spoofed.
    */
   public String getRegistryPath() {
      return registryPath;
   }

   /**
    * Gets the original registry value before spoofing.
    */
   public String getOriginalValue() {
      return originalValue;
   }

   /**
    * Determines if the system is running 64-bit architecture.
    */
   private static boolean is64BitArchitecture() {
      String arch = System.getProperty("os.arch");
      return arch != null && (arch.contains("64") || arch.equals("amd64") || arch.equals("x86_64"));
   }

   /**
    * Reads a registry value using Windows registry commands.
    *
    * @param registryPath Full registry path (e.g., "HKEY_LOCAL_MACHINE\\SOFTWARE\\BioWare\\SW\\KOTOR")
    * @param keyName Name of the value to read
    * @return The registry value, or null if not found
    */
   private static String readRegistryValue(String registryPath, String keyName) {
      try {
         // Parse registry path
         int firstBackslash = registryPath.indexOf('\\');
         if (firstBackslash < 0) {
            return null;
         }
         String hive = registryPath.substring(0, firstBackslash);
         String keyPath = registryPath.substring(firstBackslash + 1);

         // Convert hive name to reg.exe format
         String regHive;
         if (hive.equals("HKEY_LOCAL_MACHINE")) {
            regHive = "HKLM";
         } else if (hive.equals("HKEY_CURRENT_USER")) {
            regHive = "HKCU";
         } else if (hive.equals("HKEY_CLASSES_ROOT")) {
            regHive = "HKCR";
         } else if (hive.equals("HKEY_USERS")) {
            regHive = "HKU";
         } else if (hive.equals("HKEY_CURRENT_CONFIG")) {
            regHive = "HKCC";
         } else {
            return null;
         }

         // Use reg.exe query to read the value
         ProcessBuilder pb = new ProcessBuilder("reg", "query", regHive + "\\" + keyPath, "/v", keyName);
         Process proc = pb.start();

         // Read output
         StringBuilder output = new StringBuilder();
         try (java.io.BufferedReader reader = new java.io.BufferedReader(
               new java.io.InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
               output.append(line).append("\n");
            }
         }

         // Read error output
         StringBuilder errorOutput = new StringBuilder();
         try (java.io.BufferedReader reader = new java.io.BufferedReader(
               new java.io.InputStreamReader(proc.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
               errorOutput.append(line).append("\n");
            }
         }

         int exitCode = proc.waitFor();
         if (exitCode != 0) {
            // Key doesn't exist or access denied
            return null;
         }

         // Parse output: "    Path    REG_SZ    C:\path\to\install"
         String outputStr = output.toString();
         String[] lines = outputStr.split("\n");
         for (String line : lines) {
            if (line.trim().startsWith(keyName)) {
               // Find the value (third column)
               String[] parts = line.trim().split("\\s+", 3);
               if (parts.length >= 3) {
                  return parts[2].trim();
               }
            }
         }
         return null;
      } catch (Exception e) {
         System.err.println("DEBUG RegistrySpoofer: Error reading registry value: " + e.getMessage());
         return null;
      }
   }

   /**
    * Writes a registry value using Windows registry commands.
    *
    * @param registryPath Full registry path (e.g., "HKEY_LOCAL_MACHINE\\SOFTWARE\\BioWare\\SW\\KOTOR")
    * @param keyName Name of the value to write
    * @param value The value to write
    * @throws SecurityException If registry modification fails due to permissions
    */
   private static void writeRegistryValue(String registryPath, String keyName, String value) {
      try {
         // Parse registry path
         int firstBackslash = registryPath.indexOf('\\');
         if (firstBackslash < 0) {
            throw new IllegalArgumentException("Invalid registry path: " + registryPath);
         }
         String hive = registryPath.substring(0, firstBackslash);
         String keyPath = registryPath.substring(firstBackslash + 1);

         // Convert hive name to reg.exe format
         String regHive;
         if (hive.equals("HKEY_LOCAL_MACHINE")) {
            regHive = "HKLM";
         } else if (hive.equals("HKEY_CURRENT_USER")) {
            regHive = "HKCU";
         } else if (hive.equals("HKEY_CLASSES_ROOT")) {
            regHive = "HKCR";
         } else if (hive.equals("HKEY_USERS")) {
            regHive = "HKU";
         } else if (hive.equals("HKEY_CURRENT_CONFIG")) {
            regHive = "HKCC";
         } else {
            throw new IllegalArgumentException("Unsupported registry hive: " + hive);
         }

         // Create the registry path if it doesn't exist
         ProcessBuilder createPb = new ProcessBuilder("reg", "add", regHive + "\\" + keyPath, "/f");
         Process createProc = createPb.start();
         createProc.waitFor(); // Ignore exit code - path might already exist

         // Set the registry value using reg.exe add (creates key if needed)
         // Note: reg.exe add requires /f flag to overwrite existing values
         ProcessBuilder pb = new ProcessBuilder("reg", "add", regHive + "\\" + keyPath,
               "/v", keyName, "/t", "REG_SZ", "/d", value, "/f");
         Process proc = pb.start();

         // Read error output to detect permission errors
         StringBuilder errorOutput = new StringBuilder();
         try (java.io.BufferedReader reader = new java.io.BufferedReader(
               new java.io.InputStreamReader(proc.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
               errorOutput.append(line).append("\n");
            }
         }

         int exitCode = proc.waitFor();
         if (exitCode != 0) {
            String errorMsg = errorOutput.toString();
            if (errorMsg.contains("access") || errorMsg.contains("denied") || errorMsg.contains("privilege")) {
               throw new SecurityException("Access denied. Administrator privileges required. " + errorMsg);
            }
            throw new RuntimeException("Failed to set registry value. Exit code: " + exitCode + ", Error: " + errorMsg);
         }
      } catch (SecurityException e) {
         throw e;
      } catch (Exception e) {
         if (e.getCause() instanceof SecurityException) {
            throw (SecurityException) e.getCause();
         }
         throw new RuntimeException("Error writing registry value: " + e.getMessage(), e);
      }
   }

   /**
    * Checks if we've shown the elevation prompt before by looking for a marker file.
    */
   private static boolean hasShownElevationPrompt() {
      try {
         Path markerPath = Paths.get(System.getProperty("user.dir"), ELEVATION_PROMPT_MARKER_FILE);
         return Files.exists(markerPath);
      } catch (Exception e) {
         return false;
      }
   }

   /**
    * Marks that we've shown the elevation prompt by creating a marker file.
    */
   private static void markElevationPromptShown() {
      try {
         Path markerPath = Paths.get(System.getProperty("user.dir"), ELEVATION_PROMPT_MARKER_FILE);
         Files.createFile(markerPath);
      } catch (Exception e) {
         System.err.println("DEBUG RegistrySpoofer: Failed to create elevation prompt marker: " + e.getMessage());
      }
   }

   /**
    * Attempts to set the registry value using an elevated process.
    * Shows a prompt to the user first.
    *
    * @return true if the registry was successfully set, false otherwise
    */
   private boolean attemptElevatedRegistryWrite() {
      // Show prompt to user
      String message = "NCSDecomp needs administrator privileges to set a Windows registry key.\n\n" +
            "This is required for the " + (registryPath.contains("KotOR2") ? "KotOR 2" : "KotOR 1") +
            " compiler (nwnnsscomp_ktool.exe or nwnnsscomp_kscript.exe) to work correctly.\n\n" +
            "The registry key will be temporarily set to:\n" +
            registryPath + "\\" + keyName + " = " + spoofedPath + "\n\n" +
            "Click 'Yes' to allow this (you'll see a Windows UAC prompt), or 'No' to cancel.";

      boolean userApproved = showPromptOnEDT("Registry Access Required", message);

      if (!userApproved) {
         System.err.println("DEBUG RegistrySpoofer: User declined elevation prompt");
         return false;
      }

      // Parse registry path for reg.exe
      int firstBackslash = registryPath.indexOf('\\');
      if (firstBackslash < 0) {
         return false;
      }
      String hive = registryPath.substring(0, firstBackslash);
      String keyPath = registryPath.substring(firstBackslash + 1);

      String regHive;
      if (hive.equals("HKEY_LOCAL_MACHINE")) {
         regHive = "HKLM";
      } else {
         return false;
      }

      try {
         // Create a temporary batch file to run the reg command elevated
         // This avoids complex PowerShell escaping issues
         File tempBatch = File.createTempFile("ncsdecomp_reg_spoof_", ".bat");
         tempBatch.deleteOnExit();
         
         // Write the batch file content
         // First create the key path, then set the value
         String batchContent = "@echo off\n" +
               "reg add \"" + regHive + "\\" + keyPath + "\" /f >nul 2>&1\n" +
               "reg add \"" + regHive + "\\" + keyPath + "\" /v \"" + keyName + "\" /t REG_SZ /d \"" +
               spoofedPath.replace("\"", "\\\"") + "\" /f\n";
         
         Files.write(tempBatch.toPath(), batchContent.getBytes("UTF-8"));
         
         // Run the batch file elevated using PowerShell
         String psCommand = "Start-Process -FilePath \"" + tempBatch.getAbsolutePath().replace("\\", "\\\\") +
               "\" -Verb RunAs -Wait -NoNewWindow";
         
         ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", psCommand);
         pb.redirectErrorStream(true);
         
         Process proc = pb.start();
         
         // Read output
         StringBuilder output = new StringBuilder();
         try (java.io.BufferedReader reader = new java.io.BufferedReader(
               new java.io.InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
               output.append(line).append("\n");
            }
         }

         int exitCode = proc.waitFor();
         
         // Clean up temp file
         try {
            tempBatch.delete();
         } catch (Exception e) {
            // Ignore cleanup errors
         }
         
         if (exitCode == 0) {
            System.err.println("DEBUG RegistrySpoofer: Successfully set registry via elevated process");
            return true;
         } else {
            System.err.println("DEBUG RegistrySpoofer: Elevated registry write failed. Exit code: " + exitCode +
                  ", Output: " + output.toString());
            return false;
         }
      } catch (Exception e) {
         System.err.println("DEBUG RegistrySpoofer: Exception during elevated registry write: " + e.getMessage());
         e.printStackTrace();
         return false;
      }
   }

   /**
    * Shows a prompt dialog on the EDT (Event Dispatch Thread).
    * If not on EDT, invokes it on EDT and waits for result.
    */
   private boolean showPromptOnEDT(String title, String message) {
      if (SwingUtilities.isEventDispatchThread()) {
         int result = JOptionPane.showConfirmDialog(
               null, message, title,
               JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
         return result == JOptionPane.YES_OPTION;
      } else {
      // Not on EDT - use invokeAndWait
         final boolean[] result = new boolean[1];
         try {
            SwingUtilities.invokeAndWait(() -> {
               int dialogResult = JOptionPane.showConfirmDialog(
                     null, message, title,
                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
               result[0] = (dialogResult == JOptionPane.YES_OPTION);
            });
            return result[0];
         } catch (Exception e) {
            System.err.println("DEBUG RegistrySpoofer: Exception showing prompt: " + e.getMessage());
            return false;
         }
      }
   }

   /**
    * Shows an informational message on subsequent elevation attempts.
    */
   private void showSubsequentElevationMessage() {
      String message = "NCSDecomp cannot set the Windows registry key (requires administrator privileges).\n\n" +
            "To avoid this message, you can either:\n" +
            "1. Run NCSDecomp as administrator, or\n" +
            "2. Use a different nwnnsscomp.exe compiler if available\n\n" +
            "Compilation will be attempted anyway, but may fail if the registry key is not set correctly.";

      if (SwingUtilities.isEventDispatchThread()) {
         JOptionPane.showMessageDialog(
               null, message, "Registry Access Required",
               JOptionPane.INFORMATION_MESSAGE);
      } else {
         try {
            SwingUtilities.invokeLater(() -> {
               JOptionPane.showMessageDialog(
                     null, message, "Registry Access Required",
                     JOptionPane.INFORMATION_MESSAGE);
            });
         } catch (Exception e) {
            System.err.println("DEBUG RegistrySpoofer: Exception showing subsequent message: " + e.getMessage());
         }
      }
   }
}
