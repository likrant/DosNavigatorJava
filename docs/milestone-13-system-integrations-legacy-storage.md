# Майлстоун 13: Системные интеграции, метаданные и legacy storage

## Цель

Закрыть слой, который в DOS/OS2/Win32 DN был завязан на систему: атрибуты, long filenames, extended attributes, clipboard, printer, temp dirs, file watches and storage persistence.

## Функционал

- DOS-like attributes mapping: read-only, hidden, system/archive where platform supports it.
- Long/short filename display strategy and compatibility formatting.
- Extended attributes/resource forks: read/write where available, otherwise documented metadata sidecar/substitute.
- Clipboard integration for text, paths, selected lists, editor blocks.
- Temp directory validation and cleanup.
- File watcher/autorefresh panels.
- Persistent desktop/window state: panels, open viewer/editor windows, histories where safe.
- Printer integration foundation: print to system command, print to file, queue/status substitute.
- Robust terminal restore and crash log.

## Источники Pascal для сверки

- `ADVANCE2.PAS`, `lfnvp.pas`, `LIB.*\files.pas`: filenames and file metadata.
- `EAOper.pas`: OS/2 extended attributes.
- `winclpvp.pas`: clipboard.
- `DN1.PAS`: temp dir, config startup and fatal error behavior.
- `fnotify.001`, `LIB.*\fnotify.pas`: file notifications.
- `PRINTMAN.PAS`: print manager concepts.

## Результат

- Метаданные и системные отличия не теряются молча.
- Все platform-specific возможности имеют Java abstraction and fallback.
- Crash/terminal recovery покрыт тестами или manual verification scripts.
