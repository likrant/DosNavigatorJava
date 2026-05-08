# Реестр полного покрытия Dos Navigator

## Принцип

Цель проекта - полная пользовательская замена Dos Navigator, а не частичный файловый менеджер с похожими панелями. Поэтому каждая старая область должна получить явный статус:

- `implemented`: реализовано в Java как пользовательская функция.
- `compatible via importer`: старый файл/настройка/ресурс переносится мигратором.
- `replaced by adapter`: поведение сохранено через Java API или внешний процесс.
- `unsupported by platform with documented substitute`: невозможно или небезопасно на современной платформе, но есть понятная замена.
- `removed by explicit product decision`: исключено только после отдельного решения, с причиной и пользовательским обходным путем.

Статус `unknown`, `later`, `nice to have` или пустое место недопустимы для финальной замены.

## Обязательный инвентарь

### Core TUI

- Application lifecycle, restart-on-exit behavior where practical, fatal error handling.
- Desktop, background, windows, dialogs, controls, scrollbars, gauges.
- Full keyboard model: F1-F12, Shift/Ctrl/Alt variants, Insert/Delete, Gray keys, navigation keys.
- Mouse support where terminal exposes it.
- Menu bar and status line per context.
- Context help and help ids.

### File Manager

- Two-panel layout, active/passive panel state, swap panels, hide/show/zoom panels.
- Disk/local path navigation, parent/root behavior, drive selection.
- File records: long/short names, size, dates, attrs, owner/group where available, DIZ.
- Sorting, filtering, masks, quick search, selections and selected totals.
- Panel modes, panel setup, sort setup, reread/refresh/autorefresh.
- Disk info and current directory info.

### File Operations

- Copy, move, rename, mkdir, delete, wipe/trash strategy where applicable.
- Recursive operations, conflict dialogs, masks, overwrite/skip/rename/all variants.
- Progress, speed, elapsed/remaining, cancel, retry.
- Preserve timestamps, attrs, descriptions and metadata where platform supports them.
- Compare directories, make list, print/export list.

### Viewer

- Text, hex and dump modes.
- Large file random access.
- Search, reverse/continue search, goto line/address, markers.
- Codepages and XLT tables.
- Viewer menu, viewer status line, viewer history and settings.
- Specialized view modes: DBF/WKZ where old DN exposes them.

### Editor

- Open/new/save/save as/reload/lock/read-only handling.
- Navigation, insert/overwrite, tabs, margins, autoindent, wrap.
- Undo/redo, blocks including vertical blocks, clipboard.
- Search/replace, macros, highlighter.
- Editor menu, editor status line, editor histories and settings.

### Archives

- Archive detection by signature and extension.
- Archive as virtual panel/drive.
- List, view, extract, add, delete, test where backend supports it.
- ZIP, 7Z, TAR, TGZ/GZ, BZ2, RAR read/extract minimum.
- Legacy formats via external tools/adapters: ACE, ARJ, LHA, HA, CAB, ARC, ZOO, UC2, AIN, SQZ, HYP, HPK, UFA, ZXZ and others from `ARCHDET.PAS`.
- Archive setup compatibility.

### Virtual And Remote Panels

- Find results panel.
- Directory tree panel/dialog.
- Temp/list panel.
- Archive panel.
- Network/UNC panel and network info where platform allows it.
- Arvid/link/removable legacy drive behavior: implement if meaningful, otherwise adapter/substitute with explicit status.

### Resources And Configuration

- `dn.ini`, `DN.CFG`, `DN.DSK`, `DN.HIS`.
- `dn.mnu`, `dn.ext`, `dn.edt`, `dn.vwr`, `dn.hgl`, `dnhgl.grp`.
- Palettes `*.pal`.
- Language/dialog files `*.LNG`, `*.DLG`.
- Help `*.hlp`, `dnhelp.htx`.
- XLT/codepage files.
- Startup options, confirmations, mouse/terminal, editor/viewer, archive, print, user menu and history settings.

### Tools And Applications

- Command line and shell execution.
- User menu and quick run.
- File associations.
- ASCII table.
- Calculator and conversion calculator.
- Calendar.
- Print manager or modern print/export substitute.
- DBF viewer/editor behavior.
- Spreadsheet/WKZ behavior if shipped/enabled in target build.
- UUEncode/UUDecode tools.
- Phonebook.
- Terminal/modem emulator: implement via modern serial/telnet/ssh strategy or explicitly substitute.
- Tetris/game module if full DN parity includes optional entertainment modules.

### Plugins

- Java extension API for menu contributors, tools, archive backends and virtual drives.
- Import/recognize old plugin configuration.
- Old `.vpi/.dll` direct binary loading is not assumed portable; if not supported, document adapter strategy.

## Definition Of Done

- Every command id range in `Commands.pas` is mapped to a Java command, compatibility substitute or explicit removal.
- Every resource id used by menus/dialogs/status/help is present in the Java resource catalog for supported languages.
- Every serializable object registered in `regall.pas` is mapped to a Java feature, migrated state object or explicit compatibility note.
- Every shipped config/resource file under `EXE.D32`, `EXE.WLF`, `EXE.OLF` and `RESOURCE/*` is covered by a migration test or documented as not user-owned.
- The regression suite includes visual snapshots for file manager, viewer, editor, dialogs, archive panel, tree, find panel and settings screens.
