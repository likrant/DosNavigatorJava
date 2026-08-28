# План изолированного тестового стенда для legacy DN и Java DN

## Цель

По состоянию на 28 августа 2026 года цель тестового стенда такая:

- запускать эталонный legacy Dos Navigator и новую Java-версию в изолированной среде;
- не допускать доступа тестов к реальной пользовательской системе;
- воспроизводить одинаковые сценарии ввода;
- снимать экран обеих версий в одном формате;
- сравнивать экран 1:1 по символам, координатам и цветам;
- использовать этот же комплект тестов затем для прогонки новой версии по мере миграции.

## Ключевое решение

Для этого проекта надо разделить:

- изоляцию выполнения;
- оркестрацию тестов;
- формат сценариев;
- формат эталона экрана;
- сравнение результатов.

Главная идея:

1. Legacy DN больше не запускается из `C:\DNW`, а только из временной копии [tests/legacy/DNW-reference](/abs/path/C:/dev/projects/DosNavigatorJava/tests/legacy/DNW-reference).
2. Java DN тоже запускается только на временном тестовом дереве.
3. Один и тот же сценарий подается в legacy-runner и java-runner.
4. На выходе оба раннера сохраняют `screen snapshot`.
5. Дифф идет по screen snapshot, а не по обычным GUI-скриншотам.

## Ограничения и выводы

### Почему не опираться только на Docker

Для Java-версии Docker подходит.

Для legacy DN Docker не является надежной основной средой, потому что:

- это старый текстовый Win32/DN runtime;
- нужен воспроизводимый console environment;
- нужны детерминированные ввод и захват буфера экрана;
- у приложения есть файловые операции, внешние команды и доступ к дискам.

Поэтому для legacy DN базовая изоляция должна быть Windows-native:

- `Windows Sandbox`, или
- `Hyper-V VM`, или
- отдельный disposable Windows runner.

Если потом окажется, что `dn.exe` стабильно гоняется в Wine или в другом слое, это можно добавить отдельно. Но исходный план надо строить на Windows.

### Почему нельзя тестировать на реальной файловой системе

Это файловый менеджер. Тесты могут:

- удалять файлы;
- переименовывать каталоги;
- перезаписывать контейнеры описаний;
- вызывать внешние программы;
- модифицировать конфиги и историю.

Поэтому каждый прогон обязан использовать отдельную одноразовую sandbox-копию.

## Базовая архитектура

Предлагаемый стенд состоит из пяти слоев:

### 1. Test assets

Хранятся в репозитории:

- `tests/legacy/DNW-reference`
- `tests/fixtures/fs/*`
- `tests/scenarios/*`
- `tests/expected/*`
- `tests/render/*`

### 2. Isolation runtime

Для каждого теста создается отдельная песочница:

- `sandbox/<run-id>/legacy-runtime`
- `sandbox/<run-id>/java-runtime`
- `sandbox/<run-id>/fixtures`
- `sandbox/<run-id>/artifacts`

Песочница должна удаляться целиком после прогона или сохраняться только при падении теста.

### 3. Runner layer

Два раннера:

- `legacy-runner`
- `java-runner`

Оба принимают один и тот же сценарий и выдают один и тот же формат результата.

### 4. Capture layer

Одинаковый формат screen snapshot:

- ширина;
- высота;
- массив ячеек;
- символ;
- foreground color;
- background color;
- дополнительные атрибуты, если доступны.

### 5. Comparator/reporting layer

Сравнивает:

- полный framebuffer;
- отдельные прямоугольные области;
- опционально текст без цветов;
- опционально цвета без текста.

И пишет:

- machine-readable diff;
- PNG/rendered view для отладки;
- краткий человекочитаемый отчет.

## Целевая структура каталогов

Рекомендуемая структура:

```text
tests/
  legacy/
    DNW-reference/
  fixtures/
    fs/
      smoke/
      panels/
      file-ops/
      descriptions/
      viewer/
      editor/
      archives/
  scenarios/
    smoke/
    panels/
    file-ops/
    viewer/
    editor/
    archives/
  expected/
    legacy/
    java/
  artifacts/
    .gitignore
tools/
  test-harness/
    legacy-runner/
    java-runner/
    snapshot-compare/
scripts/
  run-legacy-tests.ps1
  run-java-visual-tests.ps1
  run-parity-suite.ps1
docs/
  legacy-isolated-test-plan.md
```

## Формат сценария

Нужен единый декларативный формат. Практичнее всего JSON.

Пример:

```json
{
  "id": "panels-startup-default",
  "screen": {
    "width": 80,
    "height": 25
  },
  "locale": "ENGLISH",
  "palette": "Default.pal",
  "startupDir": "fixtures/fs/panels/basic",
  "timeoutMs": 10000,
  "steps": [
    { "type": "wait", "ms": 800 },
    { "type": "capture", "name": "startup" },
    { "type": "keys", "keys": ["TAB"] },
    { "type": "wait", "ms": 100 },
    { "type": "capture", "name": "after-tab" },
    { "type": "keys", "keys": ["F9"] },
    { "type": "wait", "ms": 100 },
    { "type": "capture", "name": "menu-open" },
    { "type": "keys", "keys": ["ESC"] }
  ]
}
```

### Обязательные поля сценария

- `id`
- `screen.width`
- `screen.height`
- `startupDir`
- `steps`

### Поддерживаемые шаги

- `wait`
- `keys`
- `text`
- `capture`
- `assertFileTree`
- `assertExitCode`
- `mouse` если будет реализовано

### Нормализация ввода

Нужен единый словарь клавиш:

- `UP`, `DOWN`, `LEFT`, `RIGHT`
- `ENTER`, `ESC`, `TAB`, `BACKSPACE`, `SPACE`
- `F1`..`F12`
- `SHIFT+F1`, `CTRL+F1`, `ALT+F1`
- `INS`, `DEL`, `HOME`, `END`, `PGUP`, `PGDN`
- `ALT+X`, `CTRL+Q`, `GRAY+`, `GRAY-`, `GRAY*`

Именно на этом уровне будет решаться совместимость старого и нового раннера по вводу.

## Формат screen snapshot

Для 1:1 сравнения нужен не PNG как источник истины, а JSON framebuffer.

Пример:

```json
{
  "snapshotId": "startup",
  "screen": {
    "width": 80,
    "height": 25
  },
  "palette": "Default.pal",
  "cells": [
    { "x": 0, "y": 0, "ch": "L", "fg": 15, "bg": 1, "attr": 0 },
    { "x": 1, "y": 0, "ch": "e", "fg": 15, "bg": 1, "attr": 0 }
  ],
  "cursor": {
    "x": 10,
    "y": 3,
    "visible": true
  },
  "meta": {
    "runner": "legacy",
    "scenarioId": "panels-startup-default",
    "capturedAt": "2026-08-28T12:00:00Z"
  }
}
```

### Обязательные свойства snapshot

- `screen.width`
- `screen.height`
- `cells`
- `meta.runner`
- `meta.scenarioId`

### Что считать цветом

Для начала достаточно DOS/console color index:

- `fg`: 0..15
- `bg`: 0..15

Если в legacy environment удастся снять дополнительные атрибуты, можно расширить:

- `bright`
- `blink`
- `underline`

Но базовая модель должна оставаться простой и общей для обеих версий.

## Изоляция: минимально безопасная реализация

### Вариант A. Disposable directory isolation

Минимальный обязательный уровень:

- не запускать ничего из `C:\DNW`;
- копировать `tests/legacy/DNW-reference` в `sandbox/<run-id>/legacy-runtime`;
- копировать фикстуры в `sandbox/<run-id>/fixtures`;
- запускать legacy DN с current directory внутри песочницы;
- не давать тестовым сценариям ссылок на реальные пользовательские пути.

Это уже достаточно, чтобы не повредить твою рабочую копию DN.

### Вариант B. Windows Sandbox / Hyper-V isolation

Рекомендуемый уровень для destructive file-op tests:

- подготовленный bootstrap script;
- передача в sandbox только репозитория или сжатого test bundle;
- выполнение внутри отдельной Windows instance;
- сбор артефактов обратно после завершения;
- уничтожение среды после прогона.

Это основной вариант для тестов копирования, удаления, переименования, архивации и запуска внешних программ.

### Вариант C. Split strategy

Самый прагматичный вариант:

- smoke/panel/viewer parity tests гонять локально на disposable directories;
- destructive suites гонять только в Windows Sandbox/VM;
- Java unit/regression tests гонять обычным Maven;
- visual parity suite гонять отдельной командой.

Именно этот split рекомендуется как основной.

## Реализация legacy-runner

### Задачи legacy-runner

- создать sandbox-копию runtime и fixtures;
- запустить `dn.exe`;
- подать последовательность клавиш;
- дождаться стабилизации экрана;
- снять screen snapshot;
- сохранить артефакты;
- завершить процесс;
- удалить sandbox или оставить при падении.

### Как запускать legacy DN

Ориентир:

- рабочая директория процесса должна быть внутри `sandbox/<run-id>/legacy-runtime`;
- стартовая папка и конфиги должны ссылаться только на sandbox-данные;
- перед запуском можно подменять `dn.ini` и связанные файлы на test-specific copies.

### Как подавать клавиши

Варианты по приоритету:

1. Win32 input injection в консольный процесс.
2. Автоматизация через Console API / pseudo console слой.
3. При невозможности, fallback через UI automation sendkeys внутри sandboxed session.

Первый вариант предпочтителен. Если он работает стабильно, остальные не нужны.

### Как снимать экран legacy DN

Нужен deterministic capture именно консольного буфера.

Варианты:

1. `ReadConsoleOutput` или аналогичный Win32 API, если `dn.exe` работает в классическом console buffer.
2. Слой-обертка, который запускает процесс в известном console host и читает буфер.
3. Fallback через эмулятор терминала с собственным буфером.

Требование:

- снимать не bitmap;
- снимать сетку ячеек;
- хранить и символы, и цвета.

### Выход legacy-runner

Для каждого сценария:

- `artifacts/<scenario-id>/legacy/startup.json`
- `artifacts/<scenario-id>/legacy/startup.png`
- `artifacts/<scenario-id>/legacy/process.log`
- `artifacts/<scenario-id>/legacy/fs-diff.json`

## Реализация java-runner

### Задачи java-runner

- поднять одноразовое тестовое дерево;
- запустить Java-приложение;
- подать тот же сценарий;
- снимать тот же формат snapshot;
- сравнивать с legacy snapshots.

### Как лучше реализовать захват экрана в Java

Не пытаться снимать настоящий терминал. Правильнее добавить test terminal backend.

Нужен `TestTerminalDriver`, который:

- рендерит в memory framebuffer;
- хранит `char + fg + bg` для каждой ячейки;
- умеет сериализовать snapshot в JSON;
- по желанию умеет рендерить PNG для отладки.

Это сделает тесты:

- быстрыми;
- детерминированными;
- независимыми от реального terminal host.

### Выход java-runner

- `artifacts/<scenario-id>/java/startup.json`
- `artifacts/<scenario-id>/java/startup.png`
- `artifacts/<scenario-id>/java/process.log`
- `artifacts/<scenario-id>/java/fs-diff.json`

## Comparator

### Основной режим

Строгий дифф:

- одинаковая ширина и высота;
- совпадение каждой ячейки по `ch`, `fg`, `bg`;
- совпадение курсора, если он участвует в сценарии.

### Дополнительные режимы

Нужны, чтобы внедрять систему поэтапно:

- `text-only`
- `color-only`
- `region-only`
- `ignore-cursor`
- `ignore-dynamic-footer`

Но финальная цель все равно strict parity.

### Артефакты diff

- `diff.json`
- `diff.txt`
- `legacy.png`
- `java.png`
- `overlay.png`

## Нормализация среды

Чтобы экраны совпадали, среду нужно жестко зафиксировать.

Обязательная нормализация:

- размер экрана `80x25` как базовый режим;
- одна и та же палитра;
- один и тот же language pack;
- фиксированная стартовая папка;
- фикстуры с контролируемыми именами, размерами и датами;
- отключение случайных и фоновых влияний;
- фиксация времени или маскирование областей, где показывается текущее время;
- отдельные сценарии для разных локалей, а не смешение локалей в одном наборе.

### Что нужно исключать или контролировать

- текущие дата и время;
- free space реального диска;
- user-specific history;
- порядок файлов, если он зависит от ОС;
- разница short/long names на разных файловых системах;
- системные атрибуты, которых нет в test fixture.

## Матрица тестов

Полный комплект надо набирать слоями.

### Набор 1. Boot and shell smoke

- старт в пустом каталоге;
- старт в каталоге с файлами;
- переключение активной панели;
- открытие/закрытие меню;
- выход без изменений.

### Набор 2. Panels parity

- курсор и перемещение;
- page up/down;
- home/end;
- brief/full modes;
- сортировки;
- hidden/system display;
- selections;
- footer/status information;
- переключение дисков, если применимо.

### Набор 3. File operations

- copy file;
- copy tree;
- move/rename;
- delete file;
- delete directory tree;
- overwrite dialogs;
- masks;
- description container updates;
- refresh after operation.

Эти тесты гонять только в усиленной изоляции.

### Набор 4. Viewer

- открытие текста;
- прокрутка;
- поиск;
- переход;
- режимы отображения;
- кодировки и XLT, если уже поддержаны.

### Набор 5. Editor

- open/save;
- insert/overwrite;
- cursor movement;
- block operations;
- search/replace;
- exit with prompt.

### Набор 6. Menus, dialogs, resources

- главное меню;
- контекстные диалоги;
- status line;
- resource strings;
- palette-dependent rendering.

### Набор 7. Config and migration

- запуск на тестовом `dn.ini`;
- включение/выключение опций;
- влияние настроек на экран;
- импорт или чтение legacy config.

## План внедрения

### Фаза 1. Подготовка эталона

- зафиксировать `tests/legacy/DNW-reference`;
- удалить из него все явно лишние временные данные, если они мешают;
- добавить `README` для reference runtime;
- описать, какие файлы считаются эталонными и какие можно перегенерировать.

### Фаза 2. Контур раннеров

- реализовать PowerShell orchestration;
- сделать создание и очистку sandbox;
- добавить простой запуск legacy DN;
- добавить простой запуск Java DN;
- сохранять process logs.

### Фаза 3. Захват экрана

- сделать `legacy screen capture`;
- сделать `TestTerminalDriver` для Java;
- ввести JSON snapshot schema;
- добавить PNG renderer для snapshot.

### Фаза 4. Сценарии и smoke suite

- описать 5-10 базовых сценариев;
- сохранить legacy snapshots;
- прогнать Java snapshots;
- внедрить comparator и первый отчет о расхождениях.

### Фаза 5. Core parity suite

- панели;
- меню;
- status/footer;
- базовые file operations;
- viewer.

### Фаза 6. Destructive and high-risk suite

- delete/move/copy tree;
- overwrite branches;
- config mutation scenarios;
- запуск только в sandbox/VM.

### Фаза 7. CI integration

- `mvn test` оставить для обычных unit tests;
- visual parity suite вынести в отдельную команду;
- destructive suite сделать отдельным job;
- сохранять artifacts при любом failure.

## Предлагаемые команды

Минимальный набор:

```powershell
.\scripts\run-legacy-tests.ps1 -Scenario smoke\panels-startup-default.json
.\scripts\run-java-visual-tests.ps1 -Scenario smoke\panels-startup-default.json
.\scripts\run-parity-suite.ps1 -Scenario smoke\panels-startup-default.json
.\scripts\run-parity-suite.ps1 -Suite smoke
.\scripts\run-parity-suite.ps1 -Suite file-ops -UseSandbox
```

## Что писать в репозиторий, а что нет

В репозиторий стоит коммитить:

- эталонный runtime `tests/legacy/DNW-reference`;
- test fixtures;
- scenario files;
- expected snapshots, если они стабильны;
- comparator code;
- runner scripts.

Не стоит коммитить:

- временные sandbox directories;
- process dumps;
- generated artifacts обычных прогонов;
- user-specific histories, если они дрейфуют между запусками.

## Основные риски

### 1. Legacy console capture окажется нестабильным

Если `dn.exe` плохо читается через стандартный console API, придется добавлять промежуточный host layer.

### 2. Цвета будут зависеть от console host

Это лечится фиксированной палитрой и захватом не GUI-пикселей, а текстового буфера.

### 3. В некоторых экранах есть динамические зоны

Нужны маски или region rules, но только локально для известных динамических полей.

### 4. File operation tests могут оказаться опасными

Поэтому destructive suites нельзя запускать без disposable sandbox.

### 5. Legacy и Java будут расходиться не только в UI, но и в данных

Это нормально. Для этого и нужен разделенный отчет:

- screen diff;
- file tree diff;
- log diff.

## Рекомендуемая стартовая реализация

Самый прагматичный первый инкремент:

1. Ввести `tools/test-harness` и PowerShell orchestration.
2. Реализовать sandbox copy для `DNW-reference`.
3. Реализовать `TestTerminalDriver` в Java.
4. Сделать `snapshot.json` и `snapshot.png`.
5. Сделать 3 smoke scenarios:
- startup;
- tab switch;
- menu open/close.
6. Научиться снимать эти три сценария с legacy build.
7. После этого начинать расширять матрицу.

## Итог

Полный комплект тестов для этого проекта реалистичен, если строить его как parity harness, а не как набор обычных unit tests.

Правильная целевая модель:

- legacy DN выступает как oracle;
- обе версии запускаются только изолированно;
- сценарии ввода общие;
- результат фиксируется как framebuffer;
- сравнение идет 1:1 по тексту и цветам;
- destructive проверки отделены от обычных визуальных тестов.

Именно такой стенд даст безопасную и измеримую миграцию старого DN в Java.
