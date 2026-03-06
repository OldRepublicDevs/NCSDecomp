# Cursor Cloud specific instructions

## Overview

NCSDecomp is a Java CLI decompiler for KotOR (Knights of the Old Republic) NCS script files. It converts compiled `.ncs` bytecode back to readable `.nss` source. See `README.md` for full build/run/test documentation.

## Build & Run

Standard Maven commands from repo root:

- **Compile**: `mvn compile`
- **Test**: `mvn test` (runs JUnit 5 round-trip tests against ~23k scripts; takes ~60s)
- **Package**: `mvn package` (produces fat JAR at `target/ncsdecomp-CLI-1.0.2.jar`)
- **CLI usage**: `java -jar target/ncsdecomp-CLI-1.0.2.jar -i <input.ncs> -o <output.nss> -g k1|k2`

## Non-obvious caveats

- The `.mvn/jvm.config` and `.mvn/maven.config` reference `.mvn/extensions/guice-hiddenclass-patch.jar`; this JAR is checked into the repo and must be present for Maven to run.
- Java source/target is 8 but runs on JDK 21. The "bootstrap class path" and "obsolete source/target" warnings during compilation are expected and harmless.
- Round-trip tests (`NCSDecompCLIRoundTripTest`) clone the `Vanilla_KOTOR_Script_Source` GitHub repo into `test-work/` on first run. This directory is gitignored.
- The round-trip tests use `nwnnsscomp.exe` (Windows binary in `tools/`) to compile `.nss` -> `.ncs`. On Linux, Wine is **not** required because the test harness has a built-in Java-based compiler path that handles this. Tests pass without Wine.
- The `tools/` directory contains Windows `.exe` files for bytecode disassembly (`ncsdis.exe`, `nwnnsscomp_kscript.exe`). These are only needed for manual bytecode inspection and require Wine on Linux.
- NWScript definition files (`k1_nwscript.nss`, `tsl_nwscript.nss`) live in both `tools/` and `src/main/resources/`. The CLI looks for them in `tools/` at runtime.
- All helper scripts in `scripts/` are PowerShell (`.ps1`). For Linux development, use Maven directly.
- Debug output goes to stderr; redirect with `2>/dev/null` for clean decompiler output.
