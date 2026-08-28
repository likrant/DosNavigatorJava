# Phase 0 traceability

This inventory is source-first. `DosNavigator-master` is authoritative, followed by `tests/legacy/ui-reference`, then Java tests. Status is a migration classification, not a claim of functional parity.

## Base Pascal verticals

| Pascal unit | Java package/class | Tests | Status | Difference |
| --- | --- | --- | --- | --- |
| `Commands.pas` | `org.dosnavigator.command.CommandId`, `CommandBus`, `foundation.FoundationApp` | `CommandBusTest`, `CommandCatalogCoverageTest`, `FoundationAppTest` | adapter | Foundation routes F10/Esc/Enter and menu commands through shared `CommandId`; the catalog still tracks unported commands. |
| `DNAPP.PAS` | `foundation.FoundationApp` | `FoundationAppTest` | direct port | Headless event loop/state/frame slice is ported; native terminal lifecycle and full application services remain adapters. |
| `DN1.PAS` | `foundation.FoundationApp`, `LegacyMenuResources` | `FoundationAppTest` | direct port | Startup/menu/dialog vertical is present; panels remain deliberately outside this stage. |
| `views.pas` | `org.dosnavigator.tui.View`, `Group`, `Window`, `Desktop`, `foundation.FoundationApp` | `AppLayoutTest`, `FoundationAppTest` | direct port | Focus and z-order are tested for the foundation modal stack; full Turbo Vision event coverage remains pending. |
| `dialogs.pas` | `org.dosnavigator.tui.Dialog`, `foundation.FoundationApp` | `FoundationAppTest` | direct port | `dlgSystemSetup`, `dlgInterfaceSetup`, and about-modal frame slice are present; generic DNR dialog construction is pending. |
| `menus.pas` | `foundation.LegacyMenuResources`, `foundation.FoundationApp` | `FoundationAppTest` | direct port | Resource-derived top menu/submenu and EN/RU/UK mnemonics are ported for the foundation slice only. |
| `startup.pas` | `foundation.FoundationApp` | `FoundationAppTest` | adapter | Headless startup frame is portable; legacy configuration/startup sequence is absent. |
| `messages.pas` | `foundation.FoundationApp.DialogState` | `FoundationAppTest` | direct port | Initial dialog titles/messages are represented; full indexed message catalog is pending. |
| `flpanel.pas` | `org.dosnavigator.panels.FilePanelWindow` | `FilePanelTest` | prototype to replace | Java window has basic rendering only; no DN footer, drive line, or panel palettes. |
| `flpanelx.pas` | `org.dosnavigator.panels.FilePanel` | `FilePanelTest` | prototype to replace | Java model supports a subset of navigation/sort/select and lacks DN drive/plugin model. |
| `filescol.pas` | `org.dosnavigator.fs.FileRecord`, `DirectoryModel` | `FilePanelTest` | prototype to replace | Java record intentionally omits Pascal metadata, DIZ, DOS attributes, and collection semantics. |
| `drives.pas` | `org.dosnavigator.fs.Drive`, `FileSystemService`, `LocalFileSystemService` | `FilePanelTest` | adapter | Java filesystem adapter is host-neutral and does not implement legacy drives/archive/plugin operations. |

## Current Java inventory

| Java area | Status | Note |
| --- | --- | --- |
| `Main` | adapter | Cross-platform CLI entry adapter. |
| `app.AppLayout`, `DosNavigatorApp` | prototype to replace | Existing layout/application scaffold, retained during direct port. |
| `command.Command`, `CommandHandler`, `CommandBus`, `CommandId` | adapter | Transitional command dispatch and nine legacy IDs. |
| `fs.DirectoryModel`, `Drive`, `FileRecord` | prototype to replace | Simplified host filesystem model. |
| `fs.FileSystemService`, `LocalFileSystemService` | adapter | Platform adapter boundary. |
| `panels.FilePanel`, `FilePanelWindow`, panel enums | prototype to replace | Prototype panel behavior; do not delete before `FlPanel*` direct port. |
| `terminal.Color`, `Key*`, `Mouse*`, `TerminalSize`, `TerminalKeyDecoder` | adapter | Terminal/input abstraction; key encoding does not yet match `Commands.pas`. |
| `terminal.TerminalDriver`, `TerminalSurface` | adapter | JLine terminal adapter. |
| `tui.Application`, `Desktop`, `Dialog`, `Group`, `RenderContext`, `View`, `Window` | prototype to replace | Minimal framework pending `views.pas` direct port. |
| `ui.Box`, `ColorPair`, `ColorPalette`, `MenuBarView`, `StatusLineView` | prototype to replace | Rendering prototype; resource palette/menu behavior remains unported. |
| `testing.TestTerminalSurface`, `TerminalSnapshot`, `IsolatedVisualHarnessMain` | keep as test infrastructure | Headless cell oracle and diagnostic harness; PNG is diagnostic only. |
| Existing JUnit tests | keep as test infrastructure | Preserve current prototype behavior during migration. |

`remove after replacement` is intentionally assigned to no current class: deleting scaffolding before a direct Pascal counterpart exists would violate the migration safety rule.
