# Dos Navigator Java

Prototype Java rewrite of the classic Dos Navigator text-mode file manager.

## Requirements

- JDK 21 or newer
- Maven 3.9 or newer

## Run

```powershell
mvn compile exec:java
```

Run it from a real terminal with stdin/stdout attached to the console, for example Windows Terminal, PowerShell, or cmd.exe.
IDE run consoles and redirected sessions cannot run the raw-mode TUI.

Optional start directories:

```powershell
mvn compile exec:java -Dexec.args="C:\ C:\dev"
```

## Keys

- `Tab` switches active panel
- `Up` / `Down` moves selection
- `PageUp` / `PageDown` scrolls by page
- `Enter` opens selected directory
- `Backspace` goes to parent directory
- `F10` opens/closes the menu bar
- `Esc` closes the menu bar or exits when the menu is not open
- `Q` exits

The original Pascal sources are kept in `DosNavigator-master` as reference material.

## Windows Release

Requires a full JDK 21+ with `jpackage`.

```powershell
.\scripts\build-windows-release.ps1
```

For a non-snapshot release:

```powershell
.\scripts\build-windows-release.ps1 -Version 0.1.0 -PackageVersion 0.1.0
```

The script creates:

```text
target\release\windows\dnj\dnj.exe
target\release\windows\dnj-0.1.0-SNAPSHOT-windows-x64.zip
```
