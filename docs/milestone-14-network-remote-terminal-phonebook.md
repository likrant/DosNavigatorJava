# Майлстоун 14: Сеть, удаленные панели, modem/terminal и phonebook

## Цель

Закрыть сетевые и коммуникационные возможности старого DN, не притворяясь, что DOS modem API напрямую переносим на современную Java. Каждая функция получает реализацию или современную замену.

## Функционал

- UNC/local network paths as first-class directories.
- Network browser panel where platform/backend supports enumeration.
- Network info window or substitute for available network metadata.
- Remote panel strategy: SMB/SFTP/FTP adapters if chosen for Java replacement.
- Terminal/modem: modern serial/telnet/ssh session window or explicit compatibility substitute.
- Scrollback, copy to terminal/history/clipboard.
- Phonebook: records, groups, import, search, edit, delete, dial/open action mapped to modern terminal/URI command.
- Settings migration for modem/terminal/phonebook where old files exist.

## Источники Pascal для сверки

- `netbrwsr.pas`: network browser panel.
- `NetInfo` references in `Commands.pas`/`regall.pas`.
- `Terminal`/`ScrollBk`/`modemio` references under `Modem`.
- `phones.pas`, phone commands in `Commands.pas`.
- `uDialer` references in `regall.pas`.

## Результат

- Network/terminal/phonebook функции не остаются неопределенными.
- Unsupported legacy modem behavior has a documented modern replacement.
- Remote/network panels integrate with copy/move/view/edit where backend allows it.
