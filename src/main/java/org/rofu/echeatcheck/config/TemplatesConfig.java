package org.rofu.echeatcheck.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class TemplatesConfig {

    private final Map<String, CheatTemplate> templates;

    public static class CheatTemplate {
        private final String name;
        private final String reason;
        private final String command;
        private final int banDuration;

        public CheatTemplate(@NotNull String name, @NotNull String reason, @NotNull String command, int banDuration) {
            this.name = Objects.requireNonNull(name, "Название шаблона не может быть null");
            this.reason = Objects.requireNonNull(reason, "Причина не может быть null");
            this.command = Objects.requireNonNull(command, "Команда не может быть null");
            this.banDuration = banDuration;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public String getReason() {
            return reason;
        }

        @NotNull
        public String getCommand() {
            return command;
        }

        public int getBanDuration() {
            return banDuration;
        }

        @NotNull
        public String apply(@NotNull String playerName) {
            return command.replace("%player%", playerName);
        }
    }

    public TemplatesConfig() {
        this.templates = new HashMap<>();
        loadDefaults();
    }

    private void loadDefaults() {
        addTemplate(new CheatTemplate(
                "killaura",
                "Использование KillAura",
                "ban %player% 30d Использование KillAura",
                30
        ));

        addTemplate(new CheatTemplate(
                "fly",
                "Использование Fly",
                "ban %player% 30d Использование Fly",
                30
        ));

        addTemplate(new CheatTemplate(
                "speed",
                "Использование Speed",
                "ban %player% 30d Использование Speed",
                30
        ));

        addTemplate(new CheatTemplate(
                "noknockback",
                "Использование NoKnockback",
                "ban %player% 30d Использование NoKnockback",
                30
        ));

        addTemplate(new CheatTemplate(
                "reach",
                "Использование Reach",
                "ban %player% 30d Использование Reach",
                30
        ));
    }

    public void loadFromConfig(@NotNull FileConfiguration config) {
        Objects.requireNonNull(config, "Конфигурация не может быть null");

        if (config.contains("templates")) {
            ConfigurationSection templatesSection = config.getConfigurationSection("templates");
            if (templatesSection != null) {
                for (String key : templatesSection.getKeys(false)) {
                    ConfigurationSection templateSection = templatesSection.getConfigurationSection(key);
                    if (templateSection != null) {
                        String reason = templateSection.getString("reason", "");
                        String command = templateSection.getString("command", "");

                        if (!reason.isEmpty() && !command.isEmpty()) {
                            int duration = extractBanDuration(command);
                            addTemplate(new CheatTemplate(key, reason, command, duration));
                        }
                    }
                }
            }
        }
    }

    private int extractBanDuration(@NotNull String command) {
        String[] parts = command.split("\\s+");
        for (String part : parts) {
            if (part.matches("\\d+[dhm]")) {
                String number = part.replaceAll("[^\\d]", "");
                try {
                    return Integer.parseInt(number);
                } catch (NumberFormatException e) {
                    return 30;
                }
            }
        }
        return 30;
    }

    public void addTemplate(@NotNull CheatTemplate template) {
        templates.put(template.getName().toLowerCase(), template);
    }

    @Nullable
    public CheatTemplate getTemplate(@NotNull String name) {
        return templates.get(name.toLowerCase());
    }

    @NotNull
    public Collection<CheatTemplate> getAllTemplates() {
        return Collections.unmodifiableCollection(templates.values());
    }

    @NotNull
    public Set<String> getTemplateNames() {
        return Collections.unmodifiableSet(templates.keySet());
    }

    public boolean hasTemplate(@NotNull String name) {
        return templates.containsKey(name.toLowerCase());
    }

    @Nullable
    public CheatTemplate findTemplateByReason(@NotNull String search) {
        String searchLower = search.toLowerCase();

        for (CheatTemplate template : templates.values()) {
            if (template.getReason().toLowerCase().contains(searchLower)) {
                return template;
            }
        }

        return null;
    }
}