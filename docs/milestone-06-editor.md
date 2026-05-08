# Майлстоун 06: Редактор файлов

## Цель

Перенести F4 editor как рабочий встроенный текстовый редактор DN: навигация, редактирование, сохранение, undo, поиск/замена, блоки и clipboard.

## Функционал

- F4 открывает текущий файл; Shift+F4/New file открывает пустой буфер.
- Реализовать отдельное верхнее меню editor со всеми командами File/Edit/Search/Block/Options/Macro/Window/Help, применимыми к старому DN editor.
- Реализовать отдельную нижнюю F-key/status строку editor: save, block, search, replace, mode indicators, modified/read-only state.
- Text buffer с line collection, поддержка больших, но разумных файлов; отдельный read-only режим для огромных/binary файлов.
- Cursor movement, insert/overwrite mode, delete/backspace, enter, tabs, autoindent.
- Save, Save As, reload, modified marker, confirm on close.
- Undo/redo для базовых операций: insert/delete char, line, paste, block operations, replace.
- Search/replace: forward/backward, case-sensitive, prompt/all, selected scope.
- Blocks: mark stream block, vertical block, copy/cut/paste/delete, write block to file, read block from file.
- Clipboard integration через Java system clipboard при доступности и внутренний clipboard fallback.
- Editor options: tab size, replace tabs, right margin, autoindent, auto brackets/wrap where practical.
- Syntax highlighting минимум для Pascal/Java/text через новый highlighter API.
- File locking/read-only attributes where platform supports it.
- Editor history: recent files, cursor position, markers.
- Editor settings должны сохраняться и импортироваться: tab size, margins, wrap/autoindent, codepage, keymap, highlighter options, histories, macros where practical.

## Источники Pascal для сверки

- `MICROED.PAS`: `TFileEditor`, navigation, undo, blocks, search.
- `MICROED2.PAS`: open/save/load, file IO, locking, clipboard window.
- `EDITOR.PAS`, `edwin.pas`, `ed2.pas`: editor window and extended behavior.
- `macro.pas`: editor macros.
- `highlite.pas`: highlighting.
- `U_KEYMAP.PAS`: codepage conversion.

## Результат

- F4 editor usable для обычных текстовых файлов.
- Пользователь может изменить файл, сохранить, отменить изменения, выполнить поиск/замену и операции с блоком.
- Close с несохраненными изменениями показывает DN-подобный confirm dialog.
- Есть тесты text buffer, undo/redo, search/replace, save/load encodings.
- Верхнее меню и F-key строка editor соответствуют editor-контексту DN, а не основному файловому меню.
