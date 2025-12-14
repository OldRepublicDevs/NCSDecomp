# NCSDIS.exe Integration

## Overview

NCSDecomp now supports **ncsdis.exe** as an alternative pcode decompiler alongside nwnnsscomp variants. This integration provides:

- **No nwscript.nss dependency** - ncsdis works standalone
- **Simpler format** - Cleaner, more readable pcode output
- **Unified comparison** - Pcode normalization handles format differences transparently

## Pcode Format Differences

### ncsdis.exe Format

```
; 941 bytes, 153 instructions

_start:
  0000000D 1E 00 00000008             JSR main
  00000013 20 00                      RETN
  -------- -------------------------- ---
main:
  00000015 2C 01 10 00000000 00000000 STORESTATE sta_00000025 0 0
  0000001F 1D 00 00000029             JMP loc_00000048
```

**Characteristics:**
- Comment header with file stats
- Symbolic labels on separate lines (`_start:`, `main:`, `sta_XXXXX:`, `loc_XXXXX:`)
- Separator lines between sections: `-------- -------------------------- ---`
- Instructions indented (2 spaces)
- Single-word opcodes (e.g., `STORESTATE`)
- Minimal decimal formatting for numeric values

### nwnnsscomp_kscript.exe Format

```
00000008 42 000003AD              T 000003AD
0000000D 1E 00 00000008           JSR fn_00000015
00000013 20 00                    RETN
00000015 2C 10 00000000 00000000  STORE_STATE 10, 00000000, 00000000
0000001F 1D 00 00000029           JMP off_00000048
```

**Characteristics:**
- Header line with bytecode marker (`T 000003AD`)
- No separate label lines
- No separator lines
- Instructions at column 0
- Underscored opcodes (e.g., `STORE_STATE`)
- Zero-padded hex formatting
- Inline label references (`fn_XXXXX`, `off_XXXXX`)

## Implementation Details

### Code Changes

1. **KnownExternalCompilers.java**
   - Added `NCSDIS` enum with SHA256: `B1F398C2F64F4ACF2F39C417E7C7EB6F5483369BB95853C63A009F925A2E257C`
   - Command line: `ncsdis.exe <input.ncs> <output.pcode>` (no flags)
   - Release date: 2020-08-03

2. **CompilerUtil.java**
   - Added `ncsdis.exe` to `COMPILER_NAMES` priority list
   - Will be discovered in `tools/` directory automatically

3. **NwnnsscompConfig.java**
   - Special handling for NCSDIS in `getDecompileArgs()`
   - Returns simple argument array: `[executable, source, output]`
   - No `-d`, `-o`, or `-g` flags needed

4. **FileDecompiler.java**
   - Added `normalizePcodeLine()` method to handle format differences
   - Added `normalizeOperands()` method for semantic equivalence
   - Updated `comparePcodeFiles()` to use normalization before comparison

### Normalization Strategy

The pcode comparison now normalizes both formats to a canonical form:

**Normalization Rules:**
1. Skip comments, labels, separators, blank lines
2. Extract: `ADDRESS OPCODE OPERANDS`
3. Normalize opcodes: `STORESTATE` ↔ `STORE_STATE`
4. Normalize operands:
   - JSR/JMP: Extract hex address from labels
   - STORESTATE: Remove symbolic prefixes
   - ACTION: Keep action numbers only
   - CONSTI/CONSTF: Normalize hex format
   - CONSTS: Extract string content

**Example:**

ncsdis line:
```
  00000015 2C 01 10 00000000 00000000 STORESTATE sta_00000025 0 0
```

nwnnsscomp line:
```
00000015 2C 10 00000000 00000000  STORE_STATE 10, 00000000, 00000000
```

Both normalize to:
```
00000015 STORESTATE 10 00000000 00000000
```

## Usage

### CLI Mode

```bash
# Automatic detection (tries ncsdis.exe if available)
java -cp "target/classes;lib/*" com.kotor.resource.formats.ncs.NCSDecompCLI \
  -i input.ncs -o output.nss -g k1

# Explicit ncsdis path
java -cp "target/classes;lib/*" com.kotor.resource.formats.ncs.NCSDecompCLI \
  -i input.ncs -o output.nss -g k1 --compiler tools/ncsdis.exe
```

### GUI Mode

1. Settings → nwnnsscomp Path → Browse to `tools/` directory
2. Select `ncsdis.exe` from dropdown
3. Decompile NCS files as normal

### Round-Trip Testing

```bash
# Both formats work identically in round-trip tests
.\scripts\run_tests.ps1 -Game k1 -MaxScripts 100
```

The pcode comparison automatically handles format differences.

## Benefits

### Over nwnnsscomp

1. **No dependencies** - Works without nwscript.nss
2. **Simpler output** - More human-readable labels
3. **Faster** - Simpler tool, less overhead
4. **Standalone** - No registry requirements or game installation paths

### Compatibility

- ncsdis.exe and nwnnsscomp produce **semantically identical** bytecode
- Only formatting differs
- NCSDecomp handles both transparently
- Round-trip tests pass with either tool

## Testing

### Verification Scripts

1. **compare_pcode_formats.ps1**
   - Generates pcode with both tools
   - Shows side-by-side comparison
   - Highlights formatting differences

2. **test_pcode_normalization.ps1**
   - Verifies both formats can be generated
   - Compares file structure
   - Shows sample output

3. **test_ncsdis_recognition.ps1**
   - Verifies SHA256 hash matches
   - Confirms ncsdis.exe is recognized

### Test Results

```
ncsdis lines:     231 (includes labels, separators, comments)
nwnnsscomp lines: 154 (instructions only)

Both normalize to: ~153 actual instructions
```

## Future Work

- Identify ncsdis.exe author for attribution
- Consider making ncsdis the default disassembler
- Add ncsdis support to other tools in the ecosystem
- Document ncsdis.exe command-line options

## References

- Discord conversation: DarthParametric and Wizard, 2024-12-13
  - "ncsdis.exe pcode is the same bytecode but formatted slightly differently"
- ncsdis.exe: tools/ncsdis.exe (1,010,688 bytes, 2020-08-03)
- nwnnsscomp_kscript.exe: tools/nwnnsscomp_kscript.exe (266,240 bytes)

## See Also

- `test-work/pcode_format_differences.md` - Detailed format comparison
- `src/main/java/com/kotor/resource/formats/ncs/KnownExternalCompilers.java` - Compiler registry
- `src/main/java/com/kotor/resource/formats/ncs/FileDecompiler.java` - Pcode normalization logic
