package org.rofu.echeatcheck.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;
import org.rofu.echeatcheck.core.CheatCheckFactory;
import org.rofu.echeatcheck.core.CheatCheckManager;
import org.rofu.echeatcheck.util.ColorUtil;
import org.rofu.echeatcheck.util.TimeFormatter;
import org.rofu.echeatcheck.util.Validator;

import java.util.*;
import java.util.logging.Logger;

public class CommandHandler {

    private final Logger logger;
    private final CheatCheckManager checkManager;
    private final CheatCheckFactory checkFactory;

    public CommandHandler(@NotNull Logger logger,
                          @NotNull CheatCheckManager checkManager,
                          @NotNull CheatCheckFactory checkFactory) {

        this.logger = Objects.requireNonNull(logger, "Логгер не может быть null");
        this.checkManager = Objects.requireNonNull(checkManager, "Менеджер проверок не может быть null");
        this.checkFactory = Objects.requireNonNull(checkFactory, "Фабрика проверок не может быть null");
    }

    public boolean handleCommand(@NotNull CommandSender sender,
                                 @NotNull Command command,
                                 @NotNull String label,
                                 @NotNull String[] args) {

        Objects.requireNonNull(sender, "Отправитель не может быть null");
        Objects.requireNonNull(command, "Команда не может быть null");
        Objects.requireNonNull(label, "Метка не может быть null");
        Objects.requireNonNull(args, "Аргументы не могут быть null");

        try {
            if (!sender.hasPermission("ecc.admin")) {
                sender.sendMessage(ColorUtil.colorize("&c[Ошибка] У вас нет разрешения на использование этой команды."));
                return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorUtil.colorize("&c[Ошибка] Эта команда доступна только игрокам."));
                return true;
            }

            Player admin = (Player) sender;

            if (args.length == 0) {
                showHelp(admin);
                return true;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "start":
                    return handleStartCommand(admin, args);
                case "stop":
                    return handleStopCommand(admin);
                case "addtime":
                    return handleAddTimeCommand(admin, args);
                case "freeze":
                    return handleFreezeCommand(admin);
                case "finish":
                    return handleFinishCommand(admin, args);
                default:
                    admin.sendMessage(ColorUtil.colorize("&c[Ошибка] Неизвестная подкоманда. Используйте /revise help"));
                    return true;
            }

        } catch (IllegalArgumentException e) {
            sender.sendMessage(ColorUtil.colorize("&c[Ошибка] Ошибка: " + e.getMessage()));
            return true;
        } catch (IllegalStateException e) {
            sender.sendMessage(ColorUtil.colorize("&c[Ошибка] Ошибка: " + e.getMessage()));
            return true;
        } catch (Exception e) {
            logger.severe("Ошибка при обработке команды: " + e.getMessage());
            sender.sendMessage(ColorUtil.colorize("&cВнутренняя ошибка. Обратитесь к администратору."));
            return true;
        }
    }

    private boolean handleStartCommand(@NotNull Player admin, @NotNull String[] args) {
        if (args.length < 2) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] Использование: /revise start <ник>"));
            return true;
        }

        String suspectName = args[1];
        Player suspect = Validator.findPlayer(suspectName);

        if (suspect == null) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] Игрок не найден или не в сети: " + suspectName));
            return true;
        }

        if (checkManager.isAdminBusy(admin)) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] Вы уже проводите проверку другого игрока."));
            return true;
        }

        if (checkManager.isSuspectInCheck(suspect)) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] Этот игрок уже находится на проверке."));
            return true;
        }

        if (admin.getUniqueId().equals(suspect.getUniqueId())) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] Вы не можете проверить самого себя."));
            return true;
        }

        try {
            checkFactory.createAndStartCheck(admin, suspect);
            admin.sendMessage(ColorUtil.colorize("&a[Успешно] Проверка игрока &6" + suspectName + " &aначата."));

        } catch (Exception e) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] Не удалось начать проверку: " + e.getMessage()));
        }

        return true;
    }

    private boolean handleStopCommand(@NotNull Player admin) {
        CheatCheck cheatCheck = checkManager.getCheckByAdmin(admin);

        if (cheatCheck == null) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] У вас нет активной проверки."));
            return true;
        }

        checkManager.stopCheck(cheatCheck);
        admin.sendMessage(ColorUtil.colorize("&a[Успешно] Проверка остановлена."));

        return true;
    }

    private boolean handleAddTimeCommand(@NotNull Player admin, @NotNull String[] args) {
        if (args.length < 2) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] Использование: /revise addtime <время>"));
            admin.sendMessage(ColorUtil.colorize("&7Примеры: 60, 1m, 2h, 1d"));
            return true;
        }

        CheatCheck cheatCheck = checkManager.getCheckByAdmin(admin);

        if (cheatCheck == null) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] У вас нет активной проверки."));
            return true;
        }

        String timeString = args[1];
        long secondsToAdd;

        try {
            secondsToAdd = TimeFormatter.parseTime(timeString);
        } catch (IllegalArgumentException e) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] Неверный формат времени: " + e.getMessage()));
            return true;
        }

        try {
            cheatCheck.addTime(secondsToAdd);

            String timeFormatted = TimeFormatter.formatSeconds(secondsToAdd);
            admin.sendMessage(ColorUtil.colorize("&a[Успешно] &fВремя проверки &aувеличено&f на &b" + timeFormatted));
        } catch (Exception e) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] Не удалось добавить время: " + e.getMessage()));
        }

        return true;
    }

    private boolean handleFreezeCommand(@NotNull Player admin) {
        CheatCheck cheatCheck = checkManager.getCheckByAdmin(admin);

        if (cheatCheck == null) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] У вас нет активной проверки."));
            return true;
        }

        try {
            cheatCheck.freeze();

            if (cheatCheck.getCurrentState().isFrozen()) {
                admin.sendMessage(ColorUtil.colorize("&a[Успешно] Проверка заморожена."));
            } else {
                admin.sendMessage(ColorUtil.colorize("&a[Успешно] Проверка возобновлена."));
            }

        } catch (Exception e) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] Не удалось заморозить проверку: " + e.getMessage()));
        }

        return true;
    }

    private boolean handleFinishCommand(@NotNull Player admin, @NotNull String[] args) {
        if (args.length < 2) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] Использование: /revise finish <причина>"));
            admin.sendMessage(ColorUtil.colorize("&e[Инфо] Или используйте шаблон: /revise finish killaura"));
            return true;
        }

        CheatCheck cheatCheck = checkManager.getCheckByAdmin(admin);

        if (cheatCheck == null) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] У вас нет активной проверки."));
            return true;
        }

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (reasonBuilder.length() > 0) {
                reasonBuilder.append(" ");
            }
            reasonBuilder.append(args[i]);
        }

        String reason = reasonBuilder.toString();

        try {
            cheatCheck.finish(reason);
            admin.sendMessage(ColorUtil.colorize("&7[&4!&7] &fИгрок &c" + cheatCheck.getSuspectName() +
                    " &cЗабанен&f по причине: &c" + reason));

        } catch (Exception e) {
            admin.sendMessage(ColorUtil.colorize("&c[Ошибка] Не удалось завершить проверку: " + e.getMessage()));
        }

        return true;
    }

    private void showHelp(@NotNull Player player) {
        player.sendMessage(" ");
        player.sendMessage(ColorUtil.colorize("&#929292П&#9A7C7Cр&#A36565о&#AB4F4Fв&#B43838е&#BC2222р&#C91B1Bк&#D71414а &#F20707н&#FF0000а &#D33A3AЧ&#BE5858и&#A87575т&#929292ы f- Справка"));
        player.sendMessage(ColorUtil.colorize("&#BC2222/revise start <ник> &7- Начать проверку игрока"));
        player.sendMessage(ColorUtil.colorize("&#BC2222/revise stop &7- Остановить текущую проверку"));
        player.sendMessage(ColorUtil.colorize("&#BC2222/revise addtime <время> &7- Продлить время проверки"));
        player.sendMessage(ColorUtil.colorize("&#BC2222/revise freeze &7- Заморозить/возобновить проверку"));
        player.sendMessage(ColorUtil.colorize("&#BC2222/revise finish <причина> &7- Забанить игрока по причине"));
    }

    @NotNull
    public List<String> getTabSuggestions(@NotNull CommandSender sender,
                                          @NotNull Command command,
                                          @NotNull String alias,
                                          @NotNull String[] args) {

        Objects.requireNonNull(sender, "Отправитель не может быть null");
        Objects.requireNonNull(command, "Команда не может быть null");
        Objects.requireNonNull(alias, "Алиас не может быть null");
        Objects.requireNonNull(args, "Аргументы не могут быть null");

        if (!sender.hasPermission("ecc.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("start", "stop", "addtime", "freeze", "finish", "help");
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start":
                if (args.length == 2) {
                    return getOnlinePlayerNames(sender);
                }
                break;

            case "addtime":
                if (args.length == 2) {
                    return Arrays.asList("60", "1m", "2m", "5m", "10m", "1h", "2h");
                }
                break;

            case "finish":
                if (args.length == 2) {
                    return Arrays.asList("killaura", "fly", "speed", "noknockback", "reach");
                } else if (args.length > 2) {
                    return Collections.emptyList();
                }
                break;
        }

        return Collections.emptyList();
    }

    @NotNull
    private List<String> getOnlinePlayerNames(@NotNull CommandSender sender) {
        List<String> names = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(sender)) {
                continue;
            }

            if (player.hasPermission("ecc.bypass")) {
                continue;
            }

            names.add(player.getName());
        }

        return names;
    }
}