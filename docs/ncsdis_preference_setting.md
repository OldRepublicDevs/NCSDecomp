# ncsdis.exe Preference Setting Implementation

## Overview

Added a dedicated GUI preference setting to control whether ncsdis.exe is prioritized over nwnnsscomp.exe for pcode decompilation operations.

## Features

### GUI Controls

1. **Checkbox**: "Prefer ncsdis.exe for pcode decompilation (faster, no nwscript.nss required)"
   - Located in Settings → File/Directories tab
   - Automatically enabled/disabled based on whether ncsdis.exe exists
   - Defaults to checked (true) when ncsdis.exe is found
   - Real-time updates when ncsdis.exe path changes

2. **Path Field**: Text field for ncsdis.exe location
   - Shows full path to ncsdis.exe
   - Defaults to `tools/ncsdis.exe`
   - Document listener triggers real-time checkbox state updates

3. **Browse Button**: File picker for selecting ncsdis.exe
   - Filters to show only ncsdis.exe files
   - Updates checkbox state immediately after selection
   - Auto-saves settings on selection

### Automatic State Management

- **Checkbox Enabled**: ncsdis.exe exists at specified path
  - Tooltip: "If checked, ncsdis.exe will be used instead of nwnnsscomp for bytecode decompilation"
  
- **Checkbox Disabled** (greyed out): ncsdis.exe not found
  - Tooltip: "ncsdis.exe not found at specified path - checkbox disabled"
  - Automatically unchecks if previously checked
  - Auto-saves when state changes

### Real-Time Updates

All updates happen immediately without requiring settings dialog restart:

1. **Path field changes** → triggers `updateNcsdisCheckboxState()`
2. **File browse selection** → updates path → triggers state update
3. **Checkbox toggle** → saves settings immediately
4. **ncsdis.exe detection** → enables/disables checkbox
5. **Settings save** → persists to config file

## Implementation Details

### Files Modified

#### Settings.java (155 lines added)

**New Fields:**
```java
private JCheckBox preferNcsdisCheckBox;
private JTextField ncsdisPathField;
private JButton browseNcsdisButton;
```

**Key Methods:**
- `updateNcsdisCheckboxState()`: Real-time checkbox enable/disable logic
- `saveSettings()`: Persists ncsdis preference and path
- `loadSettingsIntoUI()`: Loads saved preferences on startup
- `browseNcsdisButton` handler: File picker for ncsdis.exe

**UI Layout Changes:**
- Added checkbox at gridy=3 (full width)
- Added ncsdis path field at gridy=4
- Browse button next to path field
- Shifted K1/K2 nwscript fields to gridy=5 and gridy=6

#### FileDecompiler.java (67 lines added)

**New Static Fields:**
```java
public static String ncsdisPath = null;
public static boolean preferNcsdis = true; // Default to true
```

**Modified Methods:**
- `getCompilerFile()`: Checks `preferNcsdis` flag first, returns ncsdis.exe if available
- `getNcsdisFile()`: Resolution logic for finding ncsdis.exe
  - Priority 1: explicit `ncsdisPath`
  - Priority 2: `tools/ncsdis.exe`
  - Priority 3: `ncsdis.exe` in CWD

**Resolution Flow:**
```
getCompilerFile() called
  ↓
if preferNcsdis == true
  ↓
getNcsdisFile()
  ↓
if ncsdis found && exists
  ↓
return ncsdis.exe
else
  ↓
fallback to nwnnsscomp resolution
```

### Configuration Properties

**Saved to ncsdecomp.conf:**
```properties
# ncsdis preference (boolean)
Prefer ncsdis=true

# ncsdis.exe path (string, can be relative or absolute)
ncsdis Path=G:/GitHub/HoloPatcher.NET/vendor/DeNCS/tools/ncsdis.exe
```

### Defaults

- **Prefer ncsdis**: `true` (enabled by default when ncsdis.exe exists)
- **ncsdis Path**: `tools/ncsdis.exe` (relative to app directory)

## User Experience

### First Launch (ncsdis.exe exists)

1. Settings dialog opens
2. ncsdis path auto-populated: `tools/ncsdis.exe`
3. Checkbox enabled and checked (ncsdis.exe found)
4. Pcode operations use ncsdis.exe by default

### First Launch (ncsdis.exe missing)

1. Settings dialog opens
2. ncsdis path shows default: `tools/ncsdis.exe`
3. Checkbox disabled (greyed out)
4. Tooltip: "ncsdis.exe not found at specified path - checkbox disabled"
5. Pcode operations fall back to nwnnsscomp.exe

### Changing Preference

**To disable ncsdis:**
1. Uncheck "Prefer ncsdis.exe" checkbox
2. Settings auto-save immediately
3. Pcode operations use nwnnsscomp.exe

**To re-enable ncsdis:**
1. Check "Prefer ncsdis.exe" checkbox (if enabled)
2. Settings auto-save immediately
3. Pcode operations use ncsdis.exe

**To change ncsdis path:**
1. Click Browse button
2. Select ncsdis.exe file
3. Path field updates
4. Checkbox state updates automatically
5. Settings auto-save immediately

## Benefits

### User Benefits
- ✓ Easy visual control of decompiler preference
- ✓ Clear indication when ncsdis.exe is unavailable
- ✓ No confusing behavior (checkbox disabled when not applicable)
- ✓ Immediate feedback (no restart required)
- ✓ Safe defaults (uses faster ncsdis when available)

### Developer Benefits
- ✓ Clean separation of concerns (Settings vs FileDecompiler)
- ✓ Consistent with existing Settings architecture
- ✓ Real-time validation prevents invalid states
- ✓ Auto-save reduces user friction
- ✓ Well-documented preference persistence

## Testing

### Manual Testing Checklist

- [ ] Fresh install: checkbox enabled when ncsdis.exe exists
- [ ] Fresh install: checkbox disabled when ncsdis.exe missing
- [ ] Browse button selects ncsdis.exe correctly
- [ ] Path field changes trigger checkbox state update
- [ ] Checkbox toggle saves immediately
- [ ] Settings persist across application restarts
- [ ] ncsdis.exe used when preference enabled
- [ ] nwnnsscomp.exe used when preference disabled
- [ ] Preference respected in CLI mode
- [ ] Preference respected in GUI mode

### Edge Cases Handled

- ✓ ncsdis.exe deleted after being configured → checkbox disables and unchecks
- ✓ Invalid path entered → checkbox disables
- ✓ Relative path entered → resolved relative to app directory
- ✓ ncsdis.exe moved to new location → browse to update path
- ✓ Both ncsdis and nwnnsscomp missing → appropriate error messages

## Future Enhancements

### Possible Improvements

1. **Status indicator**: Visual feedback showing which decompiler is active
2. **Compiler info**: Display detected compiler version in tooltip
3. **Auto-detection**: Button to search for ncsdis.exe automatically
4. **Performance stats**: Show time saved using ncsdis vs nwnnsscomp
5. **Advanced mode**: Allow fallback behavior configuration

## Commit Information

**Commit:** 3dea4c823864feb2409c70c88870e2159eb3a772
**Date:** 2025-12-14 01:29:07 -0600
**Files Changed:** 2 files, 218 insertions(+)

## See Also

- `docs/ncsdis_integration.md` - ncsdis.exe integration details
- `src/main/java/com/kotor/resource/formats/ncs/Settings.java` - Settings implementation
- `src/main/java/com/kotor/resource/formats/ncs/FileDecompiler.java` - Decompiler preference logic
