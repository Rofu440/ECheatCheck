package org.rofu.echeatcheck.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class TabComplete implements TabCompleter {

    private final CommandHandler commandHandler;

    public TabComplete(@NotNull CommandHandler commandHandler) {
        this.commandHandler = Objects.requireNonNull(commandHandler, "Обработчик команд не может быть null");
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {

        Objects.requireNonNull(sender, "Отправитель не может быть null");
        Objects.requireNonNull(command, "Команда не может быть null");
        Objects.requireNonNull(alias, "Алиас не может быть null");
        Objects.requireNonNull(args, "Аргументы не могут быть null");

        return commandHandler.getTabSuggestions(sender, command, alias, args);
    }
}