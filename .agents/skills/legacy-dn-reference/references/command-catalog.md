# Command Catalog

Source screenshots: the user-provided menu captures from 2026-08-28 and `target/windows-sandbox/legacy/legacy-menu-coverage-20260828/shared/artifacts`. Yellow characters are language-resource mnemonics. The source uses `~x~` around the hotkey in `RESOURCE/<LANGUAGE>/dn.dnr`, then `HotKey()` returns that character. Routes must therefore be stored per `ActiveLanguage`.

## Confirmed navigation

| Target | Route | Status |
| --- | --- | --- |
| Russian Settings menu | `F10 -> Y` | Confirmed |
| Russian Configuration submenu | `F10 -> Y -> R` | Confirmed |
| Russian Interface settings | `F10 -> Y -> R -> B` | Confirmed form |
| English Interface settings | `F10 -> O -> C -> I` | Source-defined; runtime capture pending |
| Utilities menu | `F10 -> E` | Screenshot-backed |
| Panel menu | `F10 -> G` | Screenshot-backed |

The remaining Russian first-level menus use `F10` plus their observed keys: File `A`, Disk `L`, Manager `V`, Windows `J`. English top-level resources use `F`, `D`, `U`, `P`, `M`, `O`, and `W` for File, Disk, Utilities, Panel, Manager, Options, and Window.

## Deriving a mnemonic route

For a command visible in a menu screenshot, form its route as `F10 -> <top-level key for ActiveLanguage> -> <each nested localized hotkey>`. Never translate a route by label meaning. In a Russian resource, the emitted character maps to the physical QWERTY key below; in English, send the marked Latin character itself.

| Russian letter | Physical key | Russian letter | Physical key | Russian letter | Physical key |
| --- | --- | --- | --- | --- | --- |
| `Й` | `Q` | `Ц` | `W` | `У` | `E` |
| `К` | `R` | `Е` | `T` | `Н` | `Y` |
| `Г` | `U` | `Ш` | `I` | `Щ` | `O` |
| `З` | `P` | `Х` | `[` | `Ъ` | `]` |
| `Ф` | `A` | `Ы` | `S` | `В` | `D` |
| `А` | `F` | `П` | `G` | `Р` | `H` |
| `О` | `J` | `Л` | `K` | `Д` | `L` |
| `Ж` | `;` | `Э` | `'` | `Я` | `Z` |
| `Ч` | `X` | `С` | `C` | `М` | `V` |
| `И` | `B` | `Т` | `N` | `Ь` | `M` |
| `Б` | `,` | `Ю` | `.` | | |

Example: Russian `Настройки -> Конфигурация -> Интерфейс` maps `Н -> Y`, `К -> R`, `И -> B`, so the route is `F10 -> Y -> R -> B`. English `Options -> Configuration -> Interface` comes directly from `~O~ptions`, `~C~onfiguration`, and `~I~nterface`, so it is `F10 -> O -> C -> I`.

If a menu has two items with the same highlighted letter, record the visible direct shortcut if present and run a dedicated Sandbox capture for the mnemonic behavior. Do not infer whether DN cycles, selects the first match, or uses another disambiguator.

## Settings

`Настройки` contains submenus `Конфигурация`, `Менеджер файлов`, `Панель`, and `Архиваторы`, plus configuration-file commands, state persistence, and palette commands.

Capture these configuration forms individually, always cancel with `Esc`:

```text
Системные установки
Процесс загрузки/выгрузки
Интерфейс
Подтверждения
Экранные заставки
Принтер
Страна
Мышка
Редактор/просмотр
```

Under `Менеджер файлов`, capture only the three non-file forms:

```text
Настроить вид                 Alt-R
Настроить сортировку          Alt-S
Настроить фильтр              Alt-Del
```

Exclude file-path, INI, user-menu, viewer, editor, save/load state, and save/load palette commands until a dedicated fixture and mutation policy exists.

## Utilities

Visible direct commands from the menu capture:

| Command | Visible shortcut | Capture policy |
| --- | --- | --- |
| `Калькулятор` | `Ctrl-F6` | Open and cancel, safe candidate |
| `Календарь` | `Ctrl-F12` | Open and cancel, safe candidate |
| `ASCII Таблица` | `Ctrl-B` | Open and cancel, safe candidate |
| `Телефонная книжка` | `Shift-F3` | Exclude until persistence is isolated |
| `Электронная таблица` | `Shift-F11` | Exclude until persistence is isolated |
| `UU-кодирование` | `Ctrl-F7` | Exclude, operates on files |
| `UU-раскодирование` | `Ctrl-F8` | Exclude, operates on files |
| `Редактор переменных Окружения` | none shown | Exclude, changes guest environment |
| `Меню пользователя` | `F2` | Exclude, may execute configured commands |

The menu also shows command, viewer, and file-history memories. Capture their menu state only; do not activate them.

## Panel

Visible commands and direct shortcuts:

| Command | Visible shortcut | Capture policy |
| --- | --- | --- |
| `Создать список файлов` | `Alt-W` | Exclude, writes a file |
| `Ассоциировать список файлов` | `Ctrl-W` | Exclude, changes associations |
| `Сравнить каталоги` | `Ctrl-C` | Safe candidate, inspect result only |
| `Вычислить размер каталога` | `Ctrl-G` | Safe candidate, inspect result only |
| `Ветвь каталога` | `Ctrl-H` | Safe candidate, inspect result only |
| `Загрузить вид` | `Alt-_` | Exclude, loads persisted state |
| `Сортировать по` | `Alt-B` | Menu-only until sort fixture is defined |
| `Сменить диск` | `Alt-G` | Safe navigation candidate |
| `Сменить каталог` | `Alt-L` | Safe navigation candidate |
| `Перечитать диск` | `Ctrl-R` | Safe navigation candidate |
| `Быстрый переход` | `Alt-Shift-O` | Safe navigation candidate |

## Menus requiring capture-only treatment

- `Файл`: contains copy, move, delete, create-directory, archive, print, and exit operations.
- `Диск`: contains disk-image unpacking and other disk operations; capture the menu before selecting any item.
- `Менеджер`: capture commands and classify each against an isolated fixture before activation.
- `Окна`: do not activate OS command execution, language changes, games, or screen-cutting commands. Capture only clearly non-mutating display forms after visual confirmation.

The menu screenshots also establish these command families for Java menu parity:

| Menu | Visible command families |
| --- | --- |
| `Файл` | find, edit, copy, move/rename, create directory, archive/extract, print, catalog, delete, attributes, exit |
| `Диск` | disk-image processing and directory tree |
| `Менеджер` | new item, directory tree, information, quick view, description, panel placement |
| `Окна` | program/display windows, redraw, screen saver, language, DOS/output windows, notebook, text buffer, recycle bin, OS command, video modes, game |

## Recording a Java test

For every accepted command, store: start state, exact physical key sequence, screenshot name, expected form title/content, `Esc` return state, and a statement that no persistent action was confirmed. If two menu labels share a highlighted letter, do not guess a key sequence: verify that individual route in Sandbox and add its screenshot before marking it confirmed.
