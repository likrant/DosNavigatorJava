# Майлстоун 09: Пользовательские интеграции и инструменты

## Цель

Вернуть DN как среду работы, а не только файловый менеджер: command line, user menu, external tools, macros, calculator/calendar/ascii table и служебные окна.

## Функционал

- Нижняя command line с history, запуском shell-команд и возвратом в TUI.
- External viewer/editor/open rules по расширениям и file types.
- User menu (`dn.mnu`-подобная модель): вложенные пункты, параметры, макросы текущего/пассивного файла, selected list.
- Quick run bindings и редактирование user menu из UI.
- Macro system для editor и глобальных команд: record/play или минимум playback из конфигурации.
- Clipboard commands для filenames/path/list.
- File type associations и description editing.
- Поддержка правил запуска из `dn.ext`, `dn.edt`, `dn.vwr` и их Java-эквивалентов.

## Источники Pascal для сверки

- `cmdline.pas`, `dnexec.pas`: command line and external execution.
- `USERMENU.PAS`: user menu parser/executor and macro substitutions.
- `filetype.pas`: extension/type associations.
- `macro.pas`: macro playback.
- `winclpvp.pas`: clipboard integration.

## Результат

- Пользователь может запускать внешние команды и возвращаться в DN screen.
- User menu способен запускать команды с текущим/выделенными файлами.
- Основные встроенные tools открываются как обычные окна и закрываются без потери состояния панелей.
- Истории и настройки интеграций сохраняются.
