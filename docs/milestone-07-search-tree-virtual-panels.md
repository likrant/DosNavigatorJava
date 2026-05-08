# Майлстоун 07: Поиск, дерево и виртуальные панели

## Цель

Расширить DN за пределы локального каталога: file find, directory tree, list/temp panels и directory compare. После этапа пользователь должен иметь основные виртуальные представления, которыми DN отличался от простого двухпанельного браузера.

## Функционал

- Find file dialog: masks, path, include/exclude dirs, size/date filters, text contains, case sensitivity.
- Find results как виртуальная панель: открыть файл, перейти к файлу в реальной панели, copy/move/delete из результатов.
- Directory tree panel/dialog: чтение дерева, collapse/expand, search in tree, change drive/root, синхронизация с активной панелью.
- Compare directories: mark newer/missing/different files между панелями.
- Temp/list panel: собрать список файлов из разных каталогов и работать с ним как с панелью.
- Make file list: экспорт списка текущей панели/выделения в файл.
- Reread/invalidation событий для локальных операций и virtual panels.
- Optional file watcher для auto-refresh panels.
- Disk/info panel improvements: totals, current dir info, selected info.

## Источники Pascal для сверки

- `filefind.pas`: search drive/panel and find commands.
- `tree.pas`: `TTreeView`, `TTreePanel`, tree cache, change tree.
- `FSTORAGE.PAS`, `filelst.pas`, `listmakr.pas`: list/temp storage.
- `flpanelx.pas`, `XDblWnd.pas`: panel coordination and compare.
- `diskinfo.pas`: info panel.
- `fnotify.001`, `LIB.*\fnotify.pas`: refresh/watch behavior.

## Результат

- Пользователь может найти файлы и работать с результатами как с панелью.
- Tree view открывается, позволяет перейти в каталог и обновляется после mkdir/delete.
- Directory compare выделяет различающиеся файлы.
- Приложение остается полностью запускаемым даже без включенного watcher.
