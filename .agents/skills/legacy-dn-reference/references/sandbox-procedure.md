# Sandbox Procedure

## Package and lifecycle

Generate a fresh package from the repository root:

```powershell
.\scripts\prepare-legacy-sandbox-capture.ps1 -RunId legacy-<purpose>-YYYYMMDD
WindowsSandbox.exe "C:\dev\projects\DosNavigatorJava\target\windows-sandbox\legacy\legacy-<purpose>-YYYYMMDD\legacy-capture.wsb"
```

The package maps only these project-owned paths:

- Host `shared` to guest `C:\Harness`
- Host `shared\runtime` to guest `C:\DNW`

The mappings are writable so DN can run and place artifacts. No other host directory is exposed to the guest configuration. The bootstrap copies and starts the user's `Dos Navigator.lnk`, preserving its console settings. On a fresh Sandbox instance, the bootstrap sets system locale `ru-RU` and restarts the guest once; wait for the resumed run before checking artifacts.

Before generating or launching another package, close the existing Windows Sandbox window and wait for its process to exit. Do not run a second Sandbox package concurrently.

## Evidence check

For run `<run-id>`, inspect:

```text
target\windows-sandbox\legacy\<run-id>\shared\artifacts\bootstrap.log
target\windows-sandbox\legacy\<run-id>\shared\artifacts\report.md
target\windows-sandbox\legacy\<run-id>\shared\artifacts\*.png
```

Only use a run where `bootstrap.log` ends in `Bootstrap completed.`. Verify each target form visually before recording it in the Java plan.

## Keyboard protocol

Do not assume one mnemonic mapping for all interfaces. DN reads `ActiveLanguage` from `dn.ini`; the resource selected by that language marks a hotkey as `~x~`. Source implementation: `DosNavigator-master/advance6.pas`, function `HotKey`.

The current fixture has `ActiveLanguage=Russian`. Its observed top-level mapping is:

| Russian menu | Physical key | Route |
| --- | --- | --- |
| `Файл` | `A` | `F10 -> A` |
| `Диск` | `L` | `F10 -> L` |
| `Утилиты` | `E` | `F10 -> E` |
| `Панель` | `G` | `F10 -> G` |
| `Менеджер` | `V` | `F10 -> V` |
| `Настройки` | `Y` | `F10 -> Y` |
| `Окна` | `J` | `F10 -> J` |

For the current Russian fixture, `F10 -> Y -> R -> B` opens `Настройки -> Конфигурация -> Интерфейс`.

For English, read `RESOURCE/ENGLISH/dn.dnr` instead of transliterating Russian labels. The source-defined route to Interface Setup is `F10 -> O -> C -> I` (`Options -> Configuration -> Interface`). Capture it before calling it runtime-confirmed.
