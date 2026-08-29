# Автоматический план проверки разметки Java DN

## Назначение

Этот план определяет исполнимую проверку Java-версии против legacy evidence.
Он дополняет `upgrade-plan.md`: скриншоты полезны для review, но проверка
разметки не должна опираться на OCR, GUI-сессию или Windows Sandbox.

## Изоляция и платформы

- Harness создает disposable fixture tree через Java `Path`; он не работает с
  каталогами пользователя и не выполняет файловые операции вне фикстуры.
- Headless tests запускаются одинаково в Windows 11 и Linux с JDK 21.
- Windows Sandbox используется только для записи новых эталонов старого DN.
  Java-тесты не зависят от него.
- Проверка реального JLine terminal input/output является отдельным
  integration suite и запускается в CI на обеих ОС.

## Артефакт кадра

Каждый снимок должен сохранять три представления одного состояния:

1. `<id>.json` - обязательный golden oracle в UTF-8:
   размеры сетки, cursor, active view и для каждой клетки `glyph`, `fg`, `bg`,
   `attributes`, `x`, `y`.
2. `<id>.txt` - удобное текстовое представление сетки.
3. `<id>.png` - изображение, сгенерированное из JSON для ручного сравнения.

Сравнение JSON учитывает псевдографику, координаты, палитру, hotkey-символы,
focus/selection/disabled states и status line. Динамические поля разрешено
маскировать только по имени правила в сценарии.

## Сценарии

Каждый сценарий описывает исходное fixture-state, язык ресурсов, размер
терминала, последовательность `KeyStroke` и ожидаемые command/state/frame.

Минимальный обязательный набор:

- startup: frame, активная панель, заголовки, panel layout и status line;
- все верхние меню: `F10`, языковая мнемоника, submenu, команда, `Esc`;
- все доступные формы настроек, manager/panel/dialog forms и утилиты;
- navigation: arrows, Home/End/PageUp/PageDown, Enter, Backspace, Tab,
  selection keys и quick search;
- file operations: F5-F8, подтверждения, conflict paths, cancel и refresh;
- viewer/editor: все source-defined key bindings и смена меню/status profile;
- locale: English/Russian/Ukrainian labels и мнемоники ведут к одному
  `CommandId`, даже если физическая раскладка ОС другая.

Для каждого `KeyStroke` тест обязан проверять:

1. Входное состояние и активный view.
2. Разрешенный `CommandId` или точное отсутствие команды.
3. Новое логическое состояние.
4. Frame snapshot, если действие меняет интерфейс.

## Legacy reference

Проверенные legacy-наблюдения находятся в:

- `tests/legacy/ui-reference/screen-index.json`;
- `tests/legacy/ui-reference/screenshots/menus`;
- `tests/legacy/ui-reference/screenshots/forms`;
- `tests/legacy/ui-reference/resources/russian-ui-resources.dnr`.

Их используют для начальных golden frames, расположения диалогов, текстов,
псевдографики и палитры. При расхождении источник Pascal-кода имеет приоритет,
а новый/исправленный эталон записывается только после проверки в Sandbox.

## Запуск и CI

`LegacyVisualParityTest` is a mandatory Maven test. It renders the Java startup
Frame at 160x50 cells of 8x14 pixels, crops the checked-in legacy PNG to the
`logicalClient` rectangle, and requires an exact pixel match. Every run writes
`java.png`, `legacy-client.png`, and `diff.png` under
`target/visual-parity/00-startup`; no mismatch tolerance is permitted.

Локальный Windows запуск существующего harness:

```powershell
.\scripts\run-java-isolated-visual-test.ps1
```

Базовая кроссплатформенная команда:

```text
mvn test
```

CI обязан запускать обе команды, эквивалентные возможностям платформы, в
матрице `windows-latest` и `ubuntu-latest` с JDK 21. Артефакты snapshot diff,
TXT и PNG сохраняются при любом падении.

## Правила обновления golden files

- Golden JSON не обновляется автоматически при обычном запуске теста.
- Изменение требует ссылки на Pascal unit/resource или на новый legacy capture,
  а также явного описания причины diff.
- Если поведение невозможно повторить на одной из ОС, в coverage registry
  указывается адаптер или platform-specific substitute; молчаливое ослабление
  проверки запрещено.
