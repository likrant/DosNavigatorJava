# Майлстоун 15: Плагины и расширяемость

## Цель

Заменить Pascal/VPI plugin model современной Java SPI, сохранив пользовательскую идею расширяемого DN: menu contributors, archive viewers, virtual drives, event catchers and tools.

## Функционал

- Java SPI for commands, menu entries, dialogs, archive backends, virtual drives, viewers/tools.
- Plugin discovery from application directory and user profile.
- Plugin manager UI: list, enable/disable, details, errors.
- Resource contribution: strings, menus, help topics.
- Permission/sandbox policy for plugins.
- Compatibility importer for old plugin configuration: identify old plugins, mark unsupported direct binary loading, suggest Java replacement/adapters.
- Process adapter API for legacy external tools.
- Versioning and stable extension contracts.

## Источники Pascal для сверки

- `plugin.pas`: plugin registration and archive/drive hooks.
- `plugman.pas`: plugin manager UI and install/uninstall.
- `plugrez.pas`: plugin resources.
- `modules.pas`: module loading.
- `_dnfuncs.pas`: exported function surface.

## Результат

- Плагины являются частью архитектуры, а не будущей неопределенностью.
- Старые binary plugins получают честный compatibility status.
- Java plugins can add commands, menu items, virtual panels and archive handlers.
