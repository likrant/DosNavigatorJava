# Майлстоун 11: Специализированные просмотрщики и форматы данных

## Цель

Закрыть пользовательские форматы, которые в старом DN открывались не как обычный текст: DBF, WKZ/spreadsheet, DIZ/description, encoded files and structured data viewers.

## Функционал

- DBF viewer: таблица, поля, типы, memo where practical, search, goto row/column, edit field where старый DN это поддерживал.
- WKZ/spreadsheet viewer strategy: импорт или read-only viewer для форматов, реально поддержанных старой сборкой.
- Специализированные команды viewer: `cmViewDBF`, `cmViewWKZ`, `cmDBFView`, `cmWKZView`, `cmEditDBField`, `cmDBFGoto`.
- Интеграция DBF/WKZ с F3/F4/open rules и меню viewer/editor.
- `descript.ion`/file_id.diz отображение и редактирование описаний.
- UUEncode/UUDecode and decoder tools where present in old UI.
- Тестовые fixtures для DBF, текстовых кодировок и spreadsheet samples.

## Источники Pascal для сверки

- `DBVIEW.PAS`, `DBWATCH.PAS`: DBF viewer/editor behavior.
- `Calc.PAS`, `cellscol.pas`: spreadsheet/WKZ-related behavior.
- `filediz.pas`, `file_id.diz`: descriptions.
- `UUCODE.PAS`, `decoder.pas`, `pktview.pas`: encoded/special content.
- `Commands.pas`: DBF/WKZ/string/dialog command ids.

## Результат

- DBF/WKZ/encoded-file actions не являются серой зоной: они реализованы или имеют зафиксированный compatibility substitute.
- Viewer/editor menus показывают соответствующие команды только в применимых контекстах.
- Форматы покрыты fixtures и regression tests.
