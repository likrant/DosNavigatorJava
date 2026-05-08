# Майлстоун 01: Основа TUI и запускаемый DN shell

## Цель

Сделать Java-движок похожим на платформу `DNAPP.PAS`/`views.pas`/`menus.pas`: единый цикл событий, desktop, окна, базовые view, меню и статусная строка. После этапа приложение должно запускаться и выглядеть как каркас Dos Navigator, даже если функциональность панелей еще минимальна.

## Функционал

- Ввести `Application`/`Desktop`/`View`/`Group`/`Window`/`Dialog` как основу поверх текущего `TerminalDriver`.
- Реализовать retained-mode перерисовку: invalidation, z-order, фокус, modal loop, resize terminal.
- Расширить декодирование клавиш: F1-F12, Alt/Shift/Ctrl-модификаторы там, где JLine их отдает, Insert/Delete, Gray+/Gray-, Ctrl+PgUp/PgDn.
- Перенести визуальные примитивы: рамки DN, тени, заголовки, scrollbars, активное/пассивное состояние.
- Сделать верхнее меню и нижнюю F-key/status строку как отдельные view, а не как ручную отрисовку в `DosNavigatorApp`.
- Добавить центральный `CommandBus`: команды получают числовые/символьные id, но Java-код использует enum/value object.
- Ввести `ColorPalette` с базовой DN-схемой: panel, selected, menu, dialog, disabled, hotkey.
- Сохранить текущий сценарий запуска `mvn compile exec:java`.

## Источники Pascal для сверки

- `DNAPP.PAS`: `TProgram`, `TApplication`, `TDesktop`, `TBackground`, event loop.
- `views.pas`: `TView`, `TGroup`, `TWindow`, state/focus/visibility.
- `menus.pas`: `TMenuBar`, `TMenuBox`, `TStatusLine`, hotkeys.
- `dialogs.pas`: базовая модель modal dialogs и controls.
- `Commands.pas`: диапазоны команд и общая командная семантика.

## Результат

- Приложение открывает полноэкранный текстовый shell с desktop, menu bar, двумя framed-зонами панелей и status line.
- Tab переключает фокус, F10/Esc открывают/закрывают меню или выходят по DN-логике.
- Resize терминала не ломает раскладку.
- Есть smoke-тесты на layout, routing команд и базовые key mappings.
