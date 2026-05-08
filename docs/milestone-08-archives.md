# Майлстоун 08: Архивы

## Цель

Реализовать DN-поведение архивов: архив открывается как панель, файлы можно просматривать, извлекать, добавлять и удалять. Внутри Java не нужно переносить все Pascal-парсеры буквально, но пользовательский workflow должен совпасть.

## Функционал

- Detect archive по сигнатурам и расширениям.
- Open archive as virtual drive/panel с directory-like навигацией внутри архива.
- Read archive listing: name, packed/unpacked size, date/time, attrs, compression ratio.
- Extract selected/current files to passive panel/current dir with progress and overwrite dialogs.
- Add/compress selected files into existing/new archive.
- Delete from archive where backend supports it.
- View/edit temporary extracted file with write-back prompt where practical.
- Archive setup dialog: external commands/backends, default archiver, paths, command templates.
- Поддержать минимум ZIP, 7z, TAR, TGZ/GZ, BZ2, RAR read/extract depending on available library/tool.
- Для редких исторических форматов (`ACE`, `ARJ`, `LHA`, `HA`, `UC2`, etc.) сделать process-wrapper через внешний архиватор или read-only detection/list placeholder, если надежной Java-библиотеки нет.

## Источники Pascal для сверки

- `ARCHIVER.PAS`: archive commands, setup values, make archive, extract flow.
- `ARCHDET.PAS`: detection for supported formats.
- `archread.pas`: reading archive lists.
- `arcview.pas`: `TArcDrive`, archive virtual panel, extract/add/delete.
- `archset.pas`: archive configuration dialog.
- `arc_*.pas`, `ARC_*.PAS`: per-format metadata and signatures.

## Результат

- Enter/F3 на поддерживаемом архиве открывает virtual panel/listing.
- Extract/add flows работают из UI и обновляют панели.
- Unsupported archive дает понятное сообщение и не ломает панель.
- Есть тесты на ZIP/TAR/TGZ и backend contract для process-wrapper.
