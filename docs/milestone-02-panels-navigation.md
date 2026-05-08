# Майлстоун 02: Панели, файловая модель и навигация

## Цель

Довести две файловые панели до уровня базового DN: колонки, сортировки, выделения, быстрая навигация, drive abstraction. Это главный пользовательский экран, поэтому визуальное совпадение важнее внутренних совпадений с Pascal.

## Функционал

- Заменить прямую работу `FilePanel` с `Path` на модель `Drive`/`DirectoryModel`/`FileRecord`.
- Поддержать локальные диски, UNC/root paths, home/current directory, parent entry (`..`) и root behavior.
- Добавить режимы отображения панели: full/brief, long names, size/date/time/attrs, hidden/system flags.
- Реализовать сортировки DN: name, extension, size, date, creation date, last access date, unsorted, reverse, dirs by name.
- Добавить выделение файлов: Insert, Gray+, Gray-, Gray*, маски, invert, selected size/count.
- Реализовать quick search по имени, включая накопление ввода и timeout.
- Добавить панельную строку информации: текущий файл, размер, дата, атрибуты, свободное место.
- Реализовать смену диска/корня, backspace/up, Enter on file/directory.
- Сохранить независимое состояние левой/правой панели: каталог, sort mode, cursor, top index, selected set.

## Источники Pascal для сверки

- `flpanel.pas`, `flpanelx.pas`: UI и поведение файловых панелей.
- `filescol.pas`: коллекция файлов, сортировки, selected counters.
- `drives.pas`: `TDrive`, `GetDirectory`, `OpenDirectory`, `MakeDir`, `GetFreeSpace`.
- `diskinfo.pas`: отображение дисковой информации.
- `ADVANCE.PAS`, `ADVANCE2.PAS`: форматирование имен, маски, quick search.
- `Commands.pas`: `PanelCommands`, `cmSort*`, `cmPanelSelect`, `cmPanelReread`.

## Результат

- Приложение остается запускаемым и пригодным как двухпанельный файловый менеджер.
- Пользователь может ходить по каталогам, менять сортировку, выделять файлы масками, видеть корректные totals.
- Визуальная структура панелей соответствует DN: рамки, заголовки, активная панель, выделение, статус.
- Есть тесты на сортировку, маски, selection accounting и navigation edge cases.
