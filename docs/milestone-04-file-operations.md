# Майлстоун 04: Файловые операции

## Цель

Перенести основную практическую ценность DN: безопасное копирование, перемещение, удаление, создание каталогов, переименование и обновление панелей. После этапа Java-версия должна быть ежедневным двухпанельным файловым менеджером для локальных файлов.

## Функционал

- F5 copy, F6 move/rename, F7 mkdir, F8 delete, single rename.
- Операции над текущим файлом и выделенным набором файлов.
- Рекурсивное копирование каталогов с сохранением структуры.
- Диалоги конфликтов: overwrite, skip, append/resume where possible, rename, all variants.
- Маска назначения при копировании/перемещении, DN-подобное применение wildcard mask.
- Прогресс-окно: текущий файл, общий прогресс, bytes done/total, скорость, elapsed/remaining, cancel.
- Проверка свободного места, read-only/system/hidden confirmations.
- Обновление обеих панелей, дерева и virtual panels через command/event bus.
- Поддержка `descript.ion`: чтение, копирование, перенос, удаление описаний.
- Атрибуты и timestamps: preserve modified time, basic DOS-like attrs mapping where platform allows.
- Fail-safe поведение: операции не должны оставлять UI в подвисшем состоянии; отмена должна быть предсказуемой.

## Источники Pascal для сверки

- `FILECOPY.PAS`: copy/move queue, masks, dialogs, progress, collision handling.
- `eraser.pas`: delete flow, recursive delete, read-only confirmations.
- `drives.pas`: drive-level `CopyFiles`, `EraseFiles`, `MakeDir`.
- `filediz.pas`: `descript.ion`.
- `gauges.pas`, `gauge.pas`: progress UI.
- `Messages.pas`, `DNStdDlg.pas`: стандартные сообщения и подтверждения.

## Результат

- Пользователь может выполнять основные операции между левой и правой панелью.
- Каждая операция видна в DN-подобном modal progress dialog и может быть отменена.
- Панели корректно перечитываются после операций.
- Есть unit/integration-тесты на copy/move/delete/mkdir/mask/collision через временные каталоги.
