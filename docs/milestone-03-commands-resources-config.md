# Майлстоун 03: Команды, меню, диалоги, настройки

## Цель

Сделать приложение управляемым как DN: команды доступны через F-клавиши, меню, диалоги, историю ввода и конфигурацию. Этот этап подготавливает UI-контракты для копирования, viewer/editor и остальных модулей.

## Функционал

- Описать полную карту базовых команд DN: quit, help, view, edit, copy, move, mkdir, delete, menu, drive, setup, reread.
- Реализовать полный menu bar основного файлового менеджера с подменю Left/File/Commands/Options/Right, вложенными пунктами, hotkeys, default items и состоянием enabled/disabled.
- Реализовать отдельные menu/status profiles для файловых панелей, viewer, editor, archive panel, find panel, tree panel и modal dialogs.
- Реализовать status line с полным набором F-key подсказок для каждого контекста, context hints и mouse/click-ready моделью, если терминал поддерживает mouse.
- Ввести ресурсную систему строк и диалогов: Java-описания вместо бинарных Pascal resources.
- Перенести языковые ресурсы минимум для English/Russian/Ukrainian: строки меню, диалогов, ошибок, status hints, help labels и F-key captions.
- Реализовать стандартные диалоги: message box, confirm, input line, file mask, copy/move target, sort setup, panel setup.
- Добавить history lists для путей, масок, команд и поисковых строк.
- Реализовать конфиг приложения: загрузка, сохранение, default config, portable mode рядом с exe/jar.
- Импортировать совместимые настройки из `dn.ini`, `DN.CFG`, `DN.DSK`, `DN.HIS`, `dn.mnu`, `*.pal`, `*.lng`, `*.dlg`, `*.hlp` или их resource-source эквивалентов.
- Добавить переключение палитр и настройки интерфейса/панелей/editor/viewer/archive/history/startup/confirmations/language/help.
- Реализовать migrator старой конфигурации: dry-run/report, backup исходных файлов, частичный перенос без потери непонятных полей.

## Источники Pascal для сверки

- `Commands.pas`: command ids, help contexts, dialog/string ids.
- `DNAPP.PAS`: `ExecResource`, `LoadResource`, `GetString`, menu/status initialization.
- `menus.pas`, `dialogs.pas`, `DNStdDlg.pas`: menu/dialog controls.
- `DN1.PAS`, `dnini.pas`, `dnini_p.pas`, `copyini.pas`: startup/config loading.
- `setups.pas`: setup dialogs.
- `colors.pas`, `colorsel.pas`, `colorvga.pas`: palettes and color editor.
- `histries.pas`, `histlist.pas`: histories.

## Результат

- Приложение запускается, показывает рабочие меню и стандартные DN-диалоги.
- Настройки панелей/цветов/сортировки/истории/языка/editor/viewer сохраняются между запусками.
- F-key строка соответствует доступным действиям во всех контекстах: панели, viewer, editor, archive, find/tree/list panels.
- Команды можно вызывать и из клавиатуры, и из меню, через один command layer.
- Старые пользовательские палитры, меню, истории и базовый config импортируются или получают явный compatibility report.
