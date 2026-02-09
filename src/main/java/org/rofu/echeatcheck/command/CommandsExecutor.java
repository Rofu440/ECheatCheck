package org.rofu.echeatcheck.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CommandsExecutor implements CommandExecutor {

    private final CommandHandler commandHandler;

    public CommandsExecutor(@NotNull CommandHandler commandHandler) {
        this.commandHandler = Objects.requireNonNull(commandHandler, "Обработчик команд не может быть null");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        Objects.requireNonNull(sender, "Отправитель не может быть null");
        Objects.requireNonNull(command, "Команда не может быть null");
        Objects.requireNonNull(label, "Метка не может быть null");
        Objects.requireNonNull(args, "Аргументы не могут быть null");

        return commandHandler.handleCommand(sender, command, label, args);
    }
}