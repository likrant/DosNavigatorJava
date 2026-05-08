# Майлстоун 17: Матрица покрытия и regression suite

## Цель

Доказать, что перенос не оставил серых зон. Этот майлстоун превращает `functionality-coverage.md` в исполняемую матрицу проверок.

## Функционал

- Command coverage generator: все команды из `Commands.pas` сопоставлены Java command/action/status.
- Resource coverage generator: все строки, dialog ids, help ids, menu ids mapped to Java resources.
- Serializable-object coverage: все записи `regall.pas` mapped to feature/state/migration status.
- Visual regression tests for 80x25, 80x50, 132x43: file manager, menus, dialogs, viewer, editor, archive, tree, find, settings.
- Behavioral tests: file operations, masks, sort, config migration, language switching, histories, archive operations.
- Golden sample workspace with files, archives, DBF, encoded text, codepages, old config/profile.
- Manual parity checklist for terminal-specific behavior that cannot be reliably automated.
- Release gate: no `unknown` coverage statuses.

## Источники Pascal для сверки

- `Commands.pas`: command and resource ids.
- `regall.pas`: object inventory.
- `EXE.*`, `RESOURCE/*`, `DOC/*`: shipped samples/resources.
- All milestone source modules for behavioral fixtures.

## Результат

- Coverage report is generated and checked in CI.
- Every legacy feature is accounted for.
- Regression suite catches visual and behavioral drift before release.
