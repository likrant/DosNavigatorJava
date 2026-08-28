---
name: legacy-dn-reference
description: Capture legacy DOS Navigator behavior in Windows Sandbox and turn it into evidence-backed Java regression tests for this project.
---

# Legacy DN Reference

Use this skill for legacy DN/2 visual captures, keyboard-behavior research, and Java parity test plans in this repository. Do not use it for ordinary Java implementation work that does not depend on the legacy application.

## Required workflow

1. Run legacy DN only through the prepared Windows Sandbox package. Generate it with `scripts/prepare-legacy-sandbox-capture.ps1`; launch the resulting `legacy-capture.wsb` only after the previous Sandbox instance has fully closed.
2. Preserve the shortcut-based launcher and the `ru-RU` system-locale bootstrap. They reproduce the legacy console profile and Russian OEM text. Do not launch `dn.exe` directly when capturing evidence.
3. Capture only the scoped mapped runtime and artifact folders. Networking, clipboard, printers, audio, and video redirection must remain disabled in the `.wsb` file.
4. For menu research, use no arrow keys. Start every route with `F10`, then resolve the highlighted letters from the active language resource before sending keys. Mnemonics are language-dependent: do not reuse a Russian route for English or another language.
5. Close a form with `Esc` without saving. `Esc` at a top menu or first-level drop-down closes the whole menu; `Esc` from a nested submenu returns one level up.
6. Do not invoke file mutations, archive operations, OS command execution, state/palette persistence, format-like operations, or external programs. For a potentially mutating command, capture the menu only and mark it excluded.
7. Treat a route as confirmed only after inspecting its newest screenshot and verifying that it shows the requested form, not merely a highlighted menu item.

## References

- Read [Sandbox procedure](references/sandbox-procedure.md) before launching or changing capture automation.
- Read [Command catalog](references/command-catalog.md) when deriving routes or writing Java tests.
- Read `DosNavigator-master/advance6.pas` and the active `DosNavigator-master/RESOURCE/<LANGUAGE>/dn.dnr` when adding a language route. `HotKey()` extracts the character enclosed by `~` in the localized resource text.
- Run `scripts/build-legacy-ui-reference.ps1` after a validated form and menu capture to publish test PNGs plus decoded resource-based text and geometry metadata under `tests/legacy/ui-reference`.
