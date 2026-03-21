<!-- Copyright 2021-2025 DeNCS -->
<!-- Licensed under the MIT License (see LICENSE). -->
<!-- See LICENSE file in the project root for full license information. -->

# DeNCS - KotOR Script Decompiler

## Welcome

**DeNCS** is a tool that converts compiled KotOR game scripts (`.ncs` files) back into readable source code (`.nss` files).

**What does this mean?** If you have a compiled script file from Knights of the Old Republic or Knights of the Old Republic II, this tool can turn it back into the original source code that you can read and edit. It's like having a translator that converts the game's internal script format back into human-readable code.

This version includes:
- **A simple graphical program** (GUI) - Just double-click to open and use
- **Command-line tools** (CLI) - For advanced users who want to automate tasks
- **Works with both games** - KotOR 1 and KotOR 2/TSL are fully supported

---

## 🚀 Quick Start Guide

### For Most Users: Use the Graphical Program (GUI)

**This is the easiest way to use DeNCS!**

1. **Find the `DeNCS` folder** in your download
2. **Double-click the program:**
   - **Windows:** Double-click `DeNCS.exe`
   - **macOS:** Double-click `DeNCS.app`
   - **Linux:** Double-click `DeNCS` (or run `./DeNCS` from terminal)
3. **That's it!** The program will open and you can:
   - Drag and drop `.ncs` files to decompile them
   - Open `.nss` files to view or edit them
   - Use File → Open to browse for files
   - Edit code with syntax highlighting
   - Save your changes

**No Java installation needed** - everything is included! The program is completely self-contained.

### For Advanced Users: Command-Line Interface (CLI)

If you prefer using the command line or want to automate tasks:

1. **Find the `DeNCSCLI` folder** in your download
2. **Open Command Prompt (Windows) or Terminal (Mac/Linux)**
3. **Navigate to the folder** and run:
   - **Windows:** `DeNCSCLI.exe --help` (to see all options)
   - **macOS/Linux:** `./DeNCSCLI --help`

See the examples below for common usage.

### Alternative: Using the JAR File

If you already have Java installed on your computer, you can use the JAR file instead:

```bash
java -jar DeNCSCLI.jar [options]
```

**Note:** Most users should use the self-contained executable instead - it's easier and doesn't require Java!

---

## 📖 How to Use

### Using the Graphical Program (Recommended for Beginners)

1. **Open the program** by double-clicking `DeNCS.exe` (Windows) or `DeNCS.app` (Mac)
2. **Open a file:**
   - Drag and drop a `.ncs` or `.nss` file onto the window, OR
   - Click File → Open and browse for your file
3. **View the code:** The decompiled source code will appear in the main window
4. **Edit if needed:** You can edit the code directly - syntax highlighting makes it easy to read
5. **Save your work:** Press Ctrl+S (Windows/Linux) or Cmd+S (Mac) to save

**Tips:**
- You can open multiple files at once - each appears in its own tab
- The program automatically detects whether you're working with KotOR 1 or KotOR 2 scripts
- If you see an asterisk (*) on a tab, that file has unsaved changes

### Using the Command Line (For Advanced Users)

**First, open Terminal/Command Prompt:**

**Windows:**
1. Press `Windows Key + R`
2. Type `cmd` and press Enter
3. Navigate to the folder where DeNCS is located:

   ```powershell
   cd C:\path\to\DeNCS
   ```

**macOS/Linux:**
1. Open Terminal
2. Navigate to the folder where DeNCS is located:

   ```bash
   cd /path/to/DeNCS
   ```

### Command-Line Examples

**Note:** Make sure you're in the `DeNCSCLI` folder, or use the full path to the executable.

#### Decompile a Single File (KotOR 2 / TSL)

**Windows:**
```powershell
# From within DeNCSCLI folder:
.\DeNCSCLI.exe -i "script.ncs" -o "script.nss" --k2

# Or from parent directory:
.\DeNCSCLI\DeNCSCLI.exe -i "script.ncs" -o "script.nss" --k2
```

**macOS/Linux:**
```bash
# From within DeNCSCLI folder:
./DeNCSCLI -i "script.ncs" -o "script.nss" --k2

# Or from parent directory:
./DeNCSCLI/DeNCSCLI -i "script.ncs" -o "script.nss" --k2
```

This will:

- Read `script.ncs`
- Create `script.nss` with the decompiled code
- Use KotOR 2 definitions (TSL)

#### Decompile a Single File (KotOR 1)

**Windows:**
```powershell
.\DeNCSCLI.exe -i "script.ncs" -o "script.nss" --k1
```

**macOS/Linux:**
```bash
./DeNCSCLI -i "script.ncs" -o "script.nss" --k1
```

#### Decompile an Entire Folder

**Windows:**
```powershell
.\DeNCSCLI.exe -i "scripts_folder" -r --k2 -O "output_folder"
```

**macOS/Linux:**
```bash
./DeNCSCLI -i "scripts_folder" -r --k2 -O "output_folder"
```

This will:

- Process all `.ncs` files in `scripts_folder`
- Include all subfolders (`-r` means recursive)
- Save results to `output_folder`
- Use KotOR 2 definitions (`--k2`)

#### View Decompiled Code in Console

**Windows:**
```powershell
.\DeNCSCLI.exe -i "script.ncs" --stdout --k2
```

**macOS/Linux:**
```bash
./DeNCSCLI -i "script.ncs" --stdout --k2
```

This displays the code directly in the terminal/command window instead of saving to a file.

---

## 🎮 Game Mode Selection

DeNCS works with both KotOR games! The program usually detects which game automatically, but you can specify:

- **`--k1`** - For Knights of the Old Republic (KotOR 1)
- **`--k2`** or **`--tsl`** - For Knights of the Old Republic II: The Sith Lords (TSL)

**In the GUI:** The program automatically detects the game based on the script. You can also change it in Settings if needed.

**In the CLI:** If you don't specify, it defaults to KotOR 1 mode. Add `--k2` to your command for KotOR 2 scripts.

---

## 📁 Required Files

**Good news:** All required files are automatically included! You don't need to do anything.

The program includes:
- `k1_nwscript.nss` - Definitions for KotOR 1 scripts
- `tsl_nwscript.nss` - Definitions for KotOR 2/TSL scripts

These files are automatically found by the program - no setup needed! They're included in the `tools` folder in the distribution.

---

## 🔧 Common Options

| Option | Description | Example |
|--------|-------------|---------|
| `-i`, `--input` | Input file or folder | `-i "script.ncs"` |
| `-o`, `--output` | Output file name | `-o "output.nss"` |
| `-O`, `--out-dir` | Output folder | `-O "results"` |
| `-r`, `--recursive` | Process subfolders | `-r` |
| `--k1` | Use KotOR 1 mode | `--k1` |
| `--k2`, `--tsl` | Use KotOR 2/TSL mode | `--k2` |
| `--stdout` | Show output in console | `--stdout` |
| `--overwrite` | Overwrite existing files | `--overwrite` |
| `--quiet` | Less verbose output | `--quiet` |
| `--help` | Show help message | `--help` |

---

## 📝 More Examples

### Example 1: Batch Decompile

**Windows:** Create a text file named `decompile.bat` with this content:

```batch
@echo off
cd /d "%~dp0DeNCSCLI"
DeNCSCLI.exe -i "C:\KotOR\scripts" -r --k2 -O "C:\KotOR\decompiled"
pause
```

**macOS/Linux:** Create a shell script named `decompile.sh` with this content:

```bash
#!/bin/bash
cd "$(dirname "$0")/DeNCSCLI"
./DeNCSCLI -i "/path/to/scripts" -r --k2 -O "/path/to/decompiled"
```

Make it executable: `chmod +x decompile.sh`

This script changes to the DeNCSCLI directory first, then runs the executable.

### Example 2: Process Multiple Files

**Windows:**
```powershell
.\DeNCSCLI.exe -i file1.ncs -i file2.ncs -i file3.ncs --k2 -O output
```

**macOS/Linux:**
```bash
./DeNCSCLI -i file1.ncs -i file2.ncs -i file3.ncs --k2 -O output
```

### Example 3: Add Custom Suffix

**Windows:**
```powershell
.\DeNCSCLI.exe -i script.ncs --suffix "_decompiled" --k2
```

**macOS/Linux:**
```bash
./DeNCSCLI -i script.ncs --suffix "_decompiled" --k2
```

This creates `script_decompiled.nss` instead of `script.nss`.

---

## ❓ Troubleshooting

### "Error: nwscript file not found"

**What this means:** The program can't find the game definition files it needs.

**How to fix:**
- Make sure you haven't deleted or moved the `tools` folder
- The `tools` folder should be in the same directory as the executable
- If using the JAR version, make sure `k1_nwscript.nss` or `tsl_nwscript.nss` is in the `tools/` folder

### "No .ncs files found"

**What this means:** The program couldn't find any script files in the location you specified.

**How to fix:**
- Check that the file path is correct
- Make sure the files actually have the `.ncs` extension
- Try navigating to the folder first and then selecting the file

### Program won't start

**Windows:**
- Windows might be blocking the program. Right-click `DeNCS.exe` → Properties → Check "Unblock" → Apply
- Try running as Administrator if you get permission errors

**macOS:**
- You may need to allow the app in System Preferences → Security & Privacy
- Right-click the app and select "Open" the first time

**Linux:**
- Make sure the executable has permission to run: `chmod +x DeNCSCLI/DeNCSCLI`

### Other Issues

**The GUI looks strange or buttons don't work:**
- Make sure you're using a recent version of Windows/macOS/Linux
- Try restarting the program

**Can't save files:**
- Make sure you have write permissions in the folder you're trying to save to
- Try saving to a different location (like your Desktop)

---

## 📚 Getting Help

**For the GUI:**
- Check the Help menu in the program
- Look at the status bar at the bottom for hints and information

**For the Command Line:**
- Run `.\DeNCSCLI.exe --help` (Windows) or `./DeNCSCLI --help` (Mac/Linux) for all available options
- Run `.\DeNCSCLI.exe --version` (Windows) or `./DeNCSCLI --version` (Mac/Linux) for version information

**Online Resources:**
- Visit [https://bolabaden.org](https://bolabaden.org) for more information and resources
- Check the included documentation files (README-CLI.md, README-USER.md) for detailed guides

---

## 🎯 What Can You Do With DeNCS?

**Basic Usage:**
- Decompile `.ncs` files to see the original source code
- View and read script code from your favorite KotOR games
- Understand how game scripts work

**Advanced Usage:**
- Edit decompiled scripts and recompile them
- Create mods for KotOR 1 and KotOR 2
- Analyze game scripts for research or learning
- Batch process entire folders of scripts

**Round-Trip Feature:**
- Open both `.ncs` (compiled) and `.nss` (source) files
- Edit source code and compile it back
- Verify your changes by comparing bytecode
- See side-by-side views of original and decompiled code

For detailed technical documentation, see `README-TECHNICAL.md` included in this package.

---

## 🙏 Credits

**Original Developers:**

- JdNoa - Script Decompiler
- Dashus - GUI

**Current Maintainer:**

- th3w1zard1

**Website:** [https://bolabaden.org](https://bolabaden.org)

**Source Code:** [https://github.com/bolabaden](https://github.com/bolabaden)

---

## 📄 License

This software is provided under the MIT License (see LICENSE).
See LICENSE file in the project root for full license information.

---

**Enjoy decompiling!** 🎮✨
