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
| `flpanel.pas` | `org.dosnavigator.panels.FilePanelWindow`, `app.DosNavigatorApp` | `FilePanelTest`, `TwoPanelFrameTest` | direct port | Two-panel frame, focus colours, scrolling and footer/status rendering are covered; DN drive line and full footer providers remain pending. |
| `flpanelx.pas` | `org.dosnavigator.panels.FilePanel`, `FilePanelWindow` | `FilePanelTest`, `TwoPanelFrameTest` | direct port | Navigation, sort, selection, Enter and Backspace run through the headless 160x50 frame; plugin/virtual panels remain pending. |
| `filescol.pas` | `org.dosnavigator.fs.FileRecord`, `DirectoryModel` | `FilePanelTest`, `TwoPanelFrameTest` | prototype to replace | Java record has deterministic basic metadata and ordering, but no DIZ, DOS attributes, duplicate removal, or legacy collection semantics. |
| `drives.pas` | `org.dosnavigator.fs.Drive`, `FileSystemService`, `LocalFileSystemService` | `FilePanelTest`, `TwoPanelFrameTest` | adapter | Host-neutral directory adapter is covered only for read/navigation. Copy, delete, archive, plugin and drive-specific operations remain excluded. |
| `filecopy.pas`, `eraser.pas`, `filelst.pas`, `filetype.pas` | `fs.FixtureFileOperations` | `FixtureFileOperationsTest` | adapter | Copy/move/rename/mkdir/delete and conflict/cancel paths work only below an explicitly authorised root. UI dialogs, masks, progress and panel reload are pending. |
| `FVIEWER.PAS`, `_fviewer.pas` | `viewer.ViewerDocument`, `ViewerFrame` | `ViewerFrameTest` | direct port | Initial read-only text/CP866/search/status-frame slice; binary, hex, large file, history and key-driven modes are pending. |
| `ARCHIVER.PAS`, `arcview.pas` | `archives.VirtualArchive` | `VirtualArchiveAndUtilitiesTest` | adapter | ZIP virtual list is read-only and platform-neutral; legacy archive formats, extraction and panel integration are pending. |
| `tree.pas` | `search.FileSearch` | `VirtualArchiveAndUtilitiesTest` | prototype to replace | Read-only fixture traversal only; tree state/window rendering is not ported. |
| `Calculat.pas`, `calendar.pas` | `tools.LegacyUtilities` | `VirtualArchiveAndUtilitiesTest` | adapter | Pure calculator/month-length primitives only; legacy dialogs, keyboard flow and cell frames are pending. |

## Current Java inventory

| Java area | Status | Note |
| --- | --- | --- |
| `Main` | adapter | Cross-platform CLI entry adapter. |
| `app.AppLayout`, `DosNavigatorApp` | prototype to replace | Existing layout/application scaffold, retained during direct port. |
| `command.Command`, `CommandHandler`, `CommandBus`, `CommandId` | adapter | Transitional command dispatch and nine legacy IDs. |
| `fs.DirectoryModel`, `Drive`, `FileRecord` | prototype to replace | Simplified host filesystem model. |
| `fs.FileSystemService`, `LocalFileSystemService` | adapter | Platform adapter boundary. |
| `fs.FixtureFileOperations` | adapter | Stage-4 safety boundary. It rejects traversal and symlink resolution outside its authorised disposable root. |
| `viewer.ViewerDocument`, `ViewerFrame` | direct port | Initial source-traceable, headless FVIEWER text slice. |
| `archives.VirtualArchive`, `search.FileSearch`, `tools.LegacyUtilities` | adapter | Stage-6 read-only/archive and pure utility slices; not a claim of full feature parity. |
| `panels.FilePanel`, `FilePanelWindow`, panel enums | prototype to replace | Prototype panel behavior; do not delete before `FlPanel*` direct port. |
| `terminal.Color`, `Key*`, `Mouse*`, `TerminalSize`, `TerminalKeyDecoder` | adapter | Terminal/input abstraction; key encoding does not yet match `Commands.pas`. |
| `terminal.TerminalDriver`, `TerminalSurface` | adapter | JLine terminal adapter. |
| `tui.Application`, `Desktop`, `Dialog`, `Group`, `RenderContext`, `View`, `Window` | prototype to replace | Minimal framework pending `views.pas` direct port. |
| `ui.Box`, `ColorPair`, `ColorPalette`, `MenuBarView`, `StatusLineView` | prototype to replace | Rendering prototype; resource palette/menu behavior remains unported. |
| `testing.TestTerminalSurface`, `TerminalSnapshot`, `IsolatedVisualHarnessMain` | keep as test infrastructure | Headless cell oracle and diagnostic harness; PNG is diagnostic only. |
| Existing JUnit tests | keep as test infrastructure | Preserve current prototype behavior during migration. |

`remove after replacement` is intentionally assigned to no current class: deleting scaffolding before a direct Pascal counterpart exists would violate the migration safety rule.
