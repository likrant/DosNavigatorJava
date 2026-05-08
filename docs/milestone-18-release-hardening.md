# Майлстоун 18: Релизная стабилизация и поставка

## Цель

Собрать перенос в надежный продукт: быстрый, восстанавливающий терминал, воспроизводимо собираемый и документированный для пользователей старого DN.

## Функционал

- Performance pass: большие каталоги, большие файлы, archive listings, find results, async operations.
- Robust error handling: retry/cancel, corrupted config fallback, migration rollback, operation recovery.
- Terminal recovery on crash and signal handling.
- Packaging: shaded jar, Windows `jpackage`, portable distribution, checksums.
- Upgrade/migration guide from legacy DN.
- User documentation for menus, settings, resources, plugins and compatibility limits.
- Developer documentation for architecture and extension APIs.
- License and attribution compliance.
- Release notes with implemented/partial/replaced/unsupported matrix.

## Источники Pascal для сверки

- `LICENSE`, `readme.md`, `DOC/*`: license/docs/release context.
- `version.pas`: versioning concepts.
- `scripts/build-windows-release.ps1`, `pom.xml`: Java release path.
- `DN1.PAS`: crash/fatal error behavior.

## Результат

- Release artifacts are reproducible.
- Terminal is restored after normal exit and crash paths.
- Users can migrate an old DN profile and understand exactly what changed.
- No feature ships with unknown compatibility status.
