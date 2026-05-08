# Майлстоун 10: Совместимость ресурсов и миграция настроек

## Цель

Сделать миграцию пользовательских данных полноценной частью продукта, а не вспомогательной утилитой. Java-версия должна уметь читать или переносить старые настройки, палитры, истории, языки, меню, help и resource-файлы так, чтобы существующий пользователь мог перейти без ручной настройки с нуля.

## Функционал

- Импорт старых ресурсов как обязательная область совместимости: `dn.ini`, `DN.CFG`, `DN.DSK`, `DN.HIS`, `dn.mnu`, palettes, language/dialog files, XLT/codepage tables, help text.
- Проверка полного соответствия верхнего меню и нижней F-key/status строки для файлового менеджера, viewer и editor.
- Проверка сохранения и обратной совместимости палитр, истории, общего config-save, language selection, dialog strings, help resources, user menu, file associations and archive setup.
- Config/resource migration tests на реальных старых файлах из `EXE.D32`, `EXE.WLF`, `EXE.OLF`, `RESOURCE/*` и пользовательских измененных копиях.
- Миграционный отчет: что перенесено, что заменено Java-аналогом, что требует внешнего инструмента, что невозможно на текущей платформе.
- Backup старых файлов перед записью новых.
- Двусторонняя стратегия там, где это реально: старые ресурсы читаются напрямую или конвертируются в новый формат без потери данных.
- Поддержка portable/profile modes: рядом с приложением, user home, explicit config dir.

## Источники Pascal для сверки

- `HELPKERN.PAS`, `helpfile.pas`, `dnhelp.pas`: help.
- `regall.pas`: inventory of serializable windows/modules.
- `DN1.PAS`, `dnini.pas`, `dnini_p.pas`, `copyini.pas`: config read/write.
- `colors.pas`, `colorsel.pas`, `EXE.*\COLORS\*.pal`: palettes.
- `histries.pas`, `histlist.pas`, `EXE.*\DN.HIS`: histories.
- `U_KEYMAP.PAS`, `EXE.*\XLT\*.XLT`: codepage tables.
- `dn.mnu`, `EXE.*\*.LNG`, `EXE.*\*.DLG`, `RESOURCE/*`: menus/languages/dialogs.

## Результат

- Старый профиль DN переносится в Java-профиль с отчетом и backup.
- Пользовательские палитры, истории, меню, языковые настройки, file associations, archive setup и основные UI-настройки восстанавливаются после запуска Java-версии.
- Неперенесенные поля не теряются молча: они перечислены в migration report.
