# Майлстоун 05: Просмотрщик файлов

## Цель

Реализовать F3 viewer как отдельное DN-окно: быстрый просмотр больших файлов, текстовый и hex/dump режимы, поиск, кодировки, wrap и сохранение фрагментов.

## Функционал

- F3 открывает текущий файл в viewer window; Esc/F10 закрывают.
- Реализовать отдельное верхнее меню viewer со всеми командами просмотра, поиска, переходов, режимов, кодировок, сохранения фрагмента и настроек viewer.
- Реализовать отдельную нижнюю F-key/status строку viewer, совпадающую с DN-контекстом просмотра.
- Текстовый режим: scroll line/page/home/end, horizontal scroll, wrap/word wrap.
- Hex/dump mode: адрес, hex bytes, printable chars, cursor navigation.
- Переключение режимов text/hex/dump горячей клавишей, как в DN.
- Поиск forward/backward, case-sensitive toggle, whole word where applicable, continue/reverse search.
- Обработка больших файлов через buffered/random access без загрузки всего файла в память.
- Автоопределение или ручное переключение кодировок: UTF-8, CP866, Windows-1251, KOI8-R, ASCII/ANSI.
- Отображение control chars, tabs, CR/LF modes.
- Markers/bookmarks для позиций, goto line/address.
- Save viewed content/range to file where применимо.
- Viewer history: последние открытые файлы и позиции.
- Viewer settings должны сохраняться и импортироваться из старой конфигурации там, где есть соответствующие поля: mode, wrap, codepage, positions, history, colors.

## Источники Pascal для сверки

- `FVIEWER.PAS`: `TFileViewer`, `TFileWindow`, `TViewScroll`, text/hex/dump rendering, search.
- `highlite.pas`: подсветка/распознавание синтаксических элементов.
- `decoder.pas`, `XCode.pas`, `U_KEYMAP.PAS`: кодировки и конвертация.
- `Commands.pas`: `cmViewFile`, `cmViewText`, `cmViewHex`, `cmSearchFor`, `cmGotoCell`.
- `histries.pas`: history viewer state.

## Результат

- F3 viewer работает поверх панелей и не ломает состояние панелей.
- Большие файлы открываются быстро и скроллятся без заметной задержки.
- Text/hex search покрыт тестами на ASCII, UTF-8 и CP866/Windows-1251 samples.
- Экран viewer визуально совпадает с DN-стилем: frame, title, scrollbar, status hints.
- Верхнее меню и нижняя строка viewer полностью контекстные, без использования урезанного меню файловых панелей.
