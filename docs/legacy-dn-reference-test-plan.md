# Legacy DN Reference Test Plan

## Purpose

Use the screenshots captured from legacy DOS Navigator as visual and behavioral reference for the Java implementation. The reference runtime is executed only inside Windows Sandbox with networking, clipboard, audio, video, and printer redirection disabled.

## Reference Artifacts

For a run named `<run-id>`, use these files:

`target/windows-sandbox/legacy/<run-id>/shared/artifacts/report.md`

`target/windows-sandbox/legacy/<run-id>/shared/artifacts/*.png`

Use a completed run whose `bootstrap.log` ends with `Bootstrap completed.`.

Validated Russian form reference run:

`target/windows-sandbox/legacy/legacy-all-safe-forms-v2-20260828/shared/artifacts`

The run contains distinct, visually verified PNGs for the nine Configuration forms, three File Manager forms, three Panel forms, Colors, Calculator, and Calendar. `utility-ascii-table.png` from this run is not valid evidence and must be recaptured with the mnemonic route `F10 -> E -> ,`.

## Preconditions

1. Start the Java application with a deterministic fixture directory containing the legacy DN reference files.
2. Use a 120 x 50 terminal grid with the Terminal 8 x 12 visual profile for visual comparison.
3. Use Russian UI labels and OEM-compatible character rendering.
4. Do not compare desktop wallpaper, title-bar chrome, or taskbar pixels; compare DN client content only.

## Top-Level Menus

Do not use arrow keys for menu research. Press `F10`, then use the key marked by the active language resource. The current fixture is Russian and uses the routes below; English and other language packs require their own routes.

| Menu | Route | Artifact | Expected result |
| --- | --- | --- | --- |
| File | `F10 -> A` | `menu-file.png` | File operations menu is visible; no action runs until confirmation. |
| Disk | `F10 -> L` | `menu-disk.png` | Disk operations menu is visible. |
| Utilities | `F10 -> E` | `menu-utilities.png` | Utility actions menu is visible. |
| Panel | `F10 -> G` | `menu-panel.png` | Active-panel operations menu is visible. |
| Manager | `F10 -> V` | `menu-manager.png` | Manager/system commands menu is visible. |
| Settings | `F10 -> Y` | `menu-settings.png` | Settings menu is visible. |
| Windows | `F10 -> J` | `menu-windows.png` | Window and display commands menu is visible. |

## Settings Menu

Open `F10 -> Y`. Use `settings-item-00.png` through `settings-item-20.png` only as historical visual evidence for labels, separators, enabled state, accelerator hints, and Russian text. They must not be used as proof that a form was opened: the earlier run navigated with arrows.

The first four items open nested menus. Compare their initial state with:

- `settings-configuration-open.png`
- `settings-file-manager-open.png`
- `settings-panel-open.png`
- `settings-archivers-open.png`

## Configuration Forms

For each configuration item, start from the main panel again, open the configuration submenu through the route for `ActiveLanguage`, then select it through its verified mnemonic route and capture the form. Press `Esc` without confirming changes. In Russian, `F10 -> Y -> R -> B` is confirmed to open Interface Settings. In English, source resources define `F10 -> O -> C -> I`; recapture it before treating it as runtime evidence. Do not treat the legacy `settings-configuration-form-*.png` files as valid form evidence until each is recaptured with a verified mnemonic route.

| Configuration item index | Artifact | Java acceptance criteria |
| --- | --- | --- |
| System settings | `settings-system.png` | Form opens and can be cancelled with `Esc`; visually confirmed. |
| Load/unload process | `settings-startup.png` | Form opens and can be cancelled with `Esc`; visually confirmed. |
| Interface | `settings-interface.png`; Russian: `F10 -> Y -> R -> B`; English: `F10 -> O -> C -> I` | Form opens; checkboxes, radio controls, and buttons are present. |
| Confirmations | `settings-confirmations.png` | Form opens and can be cancelled with `Esc`; visually confirmed. |
| Screen savers | `settings-savers.png` | Form opens and can be cancelled with `Esc`; visually confirmed. |
| Printer | `settings-printer.png` | Form opens and can be cancelled with `Esc`; visually confirmed. |
| Country | `settings-country.png` | Form opens and can be cancelled with `Esc`; visually confirmed. |
| Mouse | `settings-mouse.png` | Form opens and can be cancelled with `Esc`; visually confirmed. |
| Editor/viewer | `settings-editor-viewer.png` | Form opens and can be cancelled with `Esc`; visually confirmed. |

## Deferred Panel Navigation

Do not send arrow keys in the current legacy-capture workflow. The existing `panel-down.png`, `panel-tab.png`, `panel-enter.png`, and `panel-backspace.png` are historical visual material only and must not be treated as current behavioral proof. Capture panel-navigation behavior in a separate, explicitly approved phase after the menu-form routes are complete.

## Safety Rules

- Automated reference capture must not activate commands that alter files, format disks, launch external utilities, save state, or save/load palettes.
- Every form opened during capture is closed with `Esc`; no `OK`, `Save`, or equivalent button is confirmed.
- Test fixtures must be copied per run. The Java test must never use a developer's working directory as a mutation target.
