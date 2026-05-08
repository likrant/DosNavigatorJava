# Майлстоун 12: Справка, локализация и ресурсный слой

## Цель

Перенести не только функциональность, но и текстовую оболочку DN: языки, help, dialog resources, menu resources and resource editing/validation pipeline.

## Функционал

- Полный resource catalog для строк, меню, dialog layouts, help contexts, status hints.
- English/Russian/Ukrainian как минимум на уровне старых shipped resources.
- Импорт или конвертация `*.LNG`, `*.DLG`, `*.hlp`, `RESOURCE/*/dnhelp.htx`.
- F1/context help: index, topic lookup, navigation, search.
- Проверка полноты переводов: missing keys fail tests.
- Runtime language switching where old DN allowed it, otherwise restart-required mode with clear prompt.
- Resource validation tool: orphan strings, missing dialogs, command ids without labels.
- UI для выбора языка и help file.

## Источники Pascal для сверки

- `RSTRINGS.PAS`, `Commands.pas`: string/dialog/help ids.
- `HELPKERN.PAS`, `helpfile.pas`, `dnhelp.pas`: help system.
- `DNAPP.PAS`: `GetString`, `ExecResource`, `LoadResource`.
- `EXE.*\*.LNG`, `EXE.*\*.DLG`, `EXE.*\*.hlp`, `RESOURCE\*\dnhelp.htx`.

## Результат

- Все пользовательские строки берутся из resource catalog, а не захардкожены в коде.
- Справка работает из основного менеджера, viewer, editor and dialogs.
- Для каждого поддержанного языка есть тест полноты ресурсов.
