# Майлстоун 16: Дополнительные приложения DN

## Цель

Перенести встроенные приложения и optional modules, чтобы Java-версия была заменой DN как среды, а не только file manager/editor/viewer.

## Функционал

- ASCII table/chart with copy/insert behavior.
- Calculator and conversion calculator.
- Calendar window.
- Print manager UI or modern print/export substitute.
- Tetris/game module if parity target includes shipped entertainment modules.
- Spreadsheet module if target build includes it.
- DB/watch companion features not covered by DBF viewer milestone.
- Integration with menu, command ids, status line, help and persisted window state.

## Источники Pascal для сверки

- `asciitab.pas`: ASCII chart.
- `Calculat.pas`, `ccalc.pas`: calculators.
- `calendar.pas`: calendar.
- `PRINTMAN.PAS`: print manager.
- `tetris.pas`: game module.
- `Calc.PAS`, `cellscol.pas`: spreadsheet module.
- `DBWATCH.PAS`: DB watch companion.
- `regall.pas`: serializable objects for optional modules.

## Результат

- Optional/user-visible applications have explicit implementation or compatibility status.
- Menu/status/help coverage includes each included application.
- Persisted windows for these tools restore where safe.
