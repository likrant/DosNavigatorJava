# План переноса Dos Navigator на Java-движок

## Цель

Перенести основную пользовательскую функциональность Pascal-версии Dos Navigator в Java так, чтобы приложение оставалось текстовым двухпанельным файловым менеджером с тем же визуальным языком: верхнее меню, нижняя строка подсказок/горячих клавиш, Turbo Vision-окна и диалоги, цветовые схемы, F-клавиши, панели, просмотрщик, редактор, архивы и служебные инструменты.

Новый код не должен повторять Pascal-классы один к одному. Граница совместимости задается поведением и экранной моделью, а не внутренними именами типов.

## Что есть сейчас

Java-проект уже запускает минимальный TUI-прототип:

- `org.dosnavigator.app.DosNavigatorApp` рисует меню, две панели и status line.
- `org.dosnavigator.terminal` инкапсулирует JLine-терминал, цвета и клавиши.
- `org.dosnavigator.panels.FilePanel` умеет читать каталог, перемещать курсор, открывать директории.
- `org.dosnavigator.fs.LocalFileSystemService` дает локальный список файлов.

Это хороший стартовый вертикальный срез, но он пока покрывает только малую часть DN: нет общей оконной модели, команд, меню, диалогов, конфигурации, выделений, операций с файлами, просмотрщика, редактора, архивов и виртуальных панелей.

## Логические блоки Pascal-кода

- Старт и жизненный цикл: `dn.pas`, `DN1.PAS`, `DNAPP.PAS`, `U_MYAPP.PAS`.
- Turbo Vision-платформа: `views.pas`, `dialogs.pas`, `menus.pas`, `objects.pas`, `streams.pas`, `collect.pas`, `gauges.pas`.
- Команды, ресурсы, строки, help context: `Commands.pas`, `RSTRINGS.PAS`, `HELPKERN.PAS`, `helpfile.pas`, `DNStdDlg.pas`.
- Панели и drive-абстракция: `flpanel.pas`, `flpanelx.pas`, `filescol.pas`, `drives.pas`, `diskinfo.pas`, `DblWnd.pas`, `XDblWnd.pas`.
- Файловые операции: `FILECOPY.PAS`, `eraser.pas`, `filediz.pas`, `filetype.pas`, `filelst.pas`.
- Поиск и виртуальные панели: `filefind.pas`, `tree.pas`, `FSTORAGE.PAS`, `listmakr.pas`, `netbrwsr.pas`.
- Просмотрщик: `FVIEWER.PAS`, `highlite.pas`, `decoder.pas`, `XCode.pas`.
- Редактор: `MICROED.PAS`, `MICROED2.PAS`, `EDITOR.PAS`, `edwin.pas`, `ed2.pas`, `macro.pas`.
- Архивы: `ARCHIVER.PAS`, `ARCHDET.PAS`, `archread.pas`, `arcview.pas`, `archset.pas`, `arc_*.pas`, `ARC_*.PAS`.
- Настройки, локализация, история: `dnini.pas`, `dnini_p.pas`, `setups.pas`, `colors.pas`, `colorsel.pas`, `U_KEYMAP.PAS`, `histries.pas`, `histlist.pas`.
- Пользовательские интеграции и инструменты: `USERMENU.PAS`, `cmdline.pas`, `dnexec.pas`, `asciitab.pas`, `Calculat.pas`, `ccalc.pas`, `calendar.pas`, `PRINTMAN.PAS`.
- Плагины и расширения: `plugin.pas`, `plugman.pas`, `plugrez.pas`, `modules.pas`, `_dnfuncs.pas`.

## Архитектурное направление Java

- Сохранить JLine как низкоуровневый терминальный драйвер, но поверх него построить собственный retained-mode TUI: `Application`, `Desktop`, `View`, `Window`, `Dialog`, `MenuBar`, `StatusLine`, `CommandBus`.
- Развести модель файловой системы и UI: панели работают с `Drive`/`VirtualFileSystem`, а не напрямую с `java.nio.file.Path`.
- Все длительные операции выполнять через единый `OperationRunner` с прогрессом, отменой и восстановлением панелей.
- Ресурсы, строки, палитры, меню и keymap хранить в читаемых Java-форматах (`properties`, `json`, `toml` или `ini`), но предусмотреть импорт части старых `dn.ini`, `dn.mnu`, `*.pal`, `*.xlt`.
- Для архивов использовать Java-библиотеки там, где они надежнее встроенного Pascal-кода, и process-wrapper для форматов, где нужен внешний архиватор.
- Поддерживать pixel-perfect в рамках текста: символы рамок, раскладка F-key строки, цвета, активные/пассивные панели, modal windows, фокус и клавиатурные сценарии должны совпадать с DN.

## Обязательные области совместимости

- Верхнее меню должно быть перенесено полностью для основного файлового менеджера: структура меню, вложенность, hotkeys, enabled/disabled состояния, default menu items и команды из `dn.mnu`/ресурсов.
- Нижняя строка F-клавиш должна быть контекстной и полной: отдельные наборы для файловых панелей, viewer, editor, диалогов, архивных/поисковых/tree/list панелей.
- Viewer и editor должны иметь собственные меню и status line, а не только общее меню файлового менеджера.
- Все настройки, которые влияют на пользовательский опыт, должны быть либо реализованы, либо явно отмечены в compatibility matrix как отложенные: панели, сортировки, цвета, палитры, editor/viewer options, архиваторы, подтверждения, mouse/terminal, startup, histories, user menu, file associations, language/help.
- Обратная совместимость нужна для практических пользовательских данных: палитры, истории, общее сохранение настроек, языковые/диалоговые/help ресурсы, user menu, key/codepage tables и базовые `dn.ini`/`DN.CFG` поля.
- Если старый формат невозможно безопасно использовать напрямую, Java-версия должна иметь importer/migrator с сохранением исходного файла и понятным отчетом о неперенесенных полях.

## Майлстоуны

1. [Основа TUI и запускаемый DN shell](milestone-01-tui-foundation.md)
2. [Панели, файловая модель и навигация](milestone-02-panels-navigation.md)
3. [Команды, меню, диалоги, настройки](milestone-03-commands-resources-config.md)
4. [Файловые операции](milestone-04-file-operations.md)
5. [Просмотрщик файлов](milestone-05-viewer.md)
6. [Редактор файлов](milestone-06-editor.md)
7. [Поиск, дерево и виртуальные панели](milestone-07-search-tree-virtual-panels.md)
8. [Архивы](milestone-08-archives.md)
9. [Пользовательские интеграции и инструменты](milestone-09-integrations-tools.md)
10. [Совместимость ресурсов и миграция настроек](milestone-10-compatibility-release.md)
11. [Специализированные просмотрщики и форматы данных](milestone-11-specialized-viewers-data-formats.md)
12. [Справка, локализация и ресурсный редактор](milestone-12-help-localization-resources.md)
13. [Системные интеграции, метаданные и legacy storage](milestone-13-system-integrations-legacy-storage.md)
14. [Сеть, удаленные панели, modem/terminal и phonebook](milestone-14-network-remote-terminal-phonebook.md)
15. [Плагины и расширяемость](milestone-15-plugins-extension-api.md)
16. [Дополнительные приложения DN](milestone-16-extra-applications.md)
17. [Матрица покрытия и regression suite](milestone-17-coverage-regression-suite.md)
18. [Релизная стабилизация и поставка](milestone-18-release-hardening.md)

Каждый майлстоун должен заканчиваться запускаемым приложением, где новая функциональность доступна из UI и не ломает уже реализованные сценарии.

Полный реестр покрытия ведется отдельно: [functionality-coverage.md](functionality-coverage.md). В финальной версии не должно быть неучтенных зон: каждая команда, ресурс, настройка и пользовательский модуль из Pascal-версии получает статус `implemented`, `compatible via importer`, `replaced by adapter`, `unsupported by platform with documented substitute` или `removed by explicit product decision`.
