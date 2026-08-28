package org.dosnavigator.foundation;

import org.dosnavigator.command.CommandId;

import java.util.List;

/**
 * Initial direct resource slice from RESOURCE/<language>/dn.dnr, MENU dlgMainMenu.
 * Each mnemonic is the character enclosed by tildes in the source resource.
 */
final class LegacyMenuResources {
    private LegacyMenuResources() {
    }

    static List<LegacyMenuItem> mainMenu(LegacyLanguage language) {
        return switch (language) {
            case ENGLISH -> List.of(
                    LegacyMenuItem.submenu("File", 'f',
                            LegacyMenuItem.submenu("View", 'v',
                                    LegacyMenuItem.command("Main view", 'm', CommandId.REFRESH)),
                            LegacyMenuItem.command("About", 'a', CommandId.ABOUT)),
                    LegacyMenuItem.submenu("Options", 'o',
                            LegacyMenuItem.command("System setup", 's', CommandId.SYSTEM_SETUP),
                            LegacyMenuItem.command("Interface setup", 'i', CommandId.INTERFACE_SETUP)));
            case RUSSIAN -> List.of(
                    LegacyMenuItem.submenu("Файл", 'ф',
                            LegacyMenuItem.submenu("Смотреть", 'с',
                                    LegacyMenuItem.command("Основной просмотр", 'о', CommandId.REFRESH)),
                            LegacyMenuItem.command("О программе", 'п', CommandId.ABOUT)),
                    LegacyMenuItem.submenu("Настройки", 'н',
                            LegacyMenuItem.command("Системные установки", 'с', CommandId.SYSTEM_SETUP),
                            LegacyMenuItem.command("Установки интерфейса", 'и', CommandId.INTERFACE_SETUP)));
            case UKRAINIAN -> List.of(
                    LegacyMenuItem.submenu("Файл", 'ф',
                            LegacyMenuItem.submenu("Перегляд", 'п',
                                    LegacyMenuItem.command("Основний перегляд", 'о', CommandId.REFRESH)),
                            LegacyMenuItem.command("Про програму", 'р', CommandId.ABOUT)),
                    LegacyMenuItem.submenu("Налаштування", 'н',
                            LegacyMenuItem.command("Системні налаштування", 'с', CommandId.SYSTEM_SETUP),
                            LegacyMenuItem.command("Налаштування інтерфейсу", 'і', CommandId.INTERFACE_SETUP)));
        };
    }
}
