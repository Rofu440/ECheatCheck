package org.rofu.echeatcheck.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.util.ColorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class MessagesConfig {

    private final Map<String, String> messages;

    public MessagesConfig() {
        this.messages = new HashMap<>();
        loadDefaults();
    }

    private void loadDefaults() {
        messages.put("no_permission", "&cУ вас нет разрешения на использование этой команды.");
        messages.put("player_only", "&cЭта команда доступна только игрокам.");
        messages.put("player_not_found", "&cИгрок не найден или не в сети.");
        messages.put("invalid_time_format", "&cНеверный формат времени. Используйте: 60, 1m, 2h, 1d");
        messages.put("time_too_long", "&cВремя не может превышать %max_time% секунд.");
        messages.put("already_in_check", "&cЭтот игрок уже находится на проверке.");
        messages.put("no_active_check", "&cУ вас нет активной проверки.");
        messages.put("admin_busy", "&cВы уже проводите проверку другого игрока.");
        messages.put("self_check", "&cВы не можете проверить самого себя.");
        messages.put("teleport_location_not_set", "&cТочка телепортации для проверки не настроена.");

        messages.put("check_started", "&aПроверка игрока %player% начата.");
        messages.put("check_stopped", "&aПроверка остановлена.");
        messages.put("time_added", "&aВремя проверки увеличено на %time%.");
        messages.put("check_frozen", "&aВремя проверки заморожено.");
        messages.put("check_finished", "&aИгрок %player% забанен по причине: %reason%");
        messages.put("check_resumed", "&aПроверка возобновлена.");

        messages.put("check_started_player", "&cВы были вызваны на проверку читов администратором %admin%.");
        messages.put("check_warning", "&cВыход из игры или игнор проверки приведёт к бану.");
        messages.put("check_confession", "&6Напишите 'у меня чит' - если используете читы.");
        messages.put("check_confession_note", "&7&o(за признание в читах - уменьшим срок бана)");
        messages.put("check_instructions", "&6Проверка выполняется через AnyDesk.");
        messages.put("check_download_link", "&6Ссылка на скачивание: &9https://anydesk.com/ru");
        messages.put("check_send_id", "&6После установки отправьте цифры Вашего рабочего места.");

        messages.put("time_format_seconds", "%d сек");
        messages.put("time_format_minutes", "%d мин %d сек");
        messages.put("time_format_hours", "%d ч %d мин");
        messages.put("time_format_days", "%d д %d ч");

        messages.put("bossbar_waiting", "&cОжидаем движение игрока..");
        messages.put("bossbar_timer_admin", "&eУ игрока осталось времени: &6%time%");
        messages.put("bossbar_timer_player", "&eУ вас осталось времени: &6%time%");
        messages.put("bossbar_frozen_admin", "&6Вы проверяете игрока: &c%player_name%");
        messages.put("bossbar_frozen_player", "&cВы находитесь на проверке");

        messages.put("actionbar_cheat_check", "&cДопенифся?)");

        messages.put("chat_admin_to_player", "&e[&6Вы &7» &6%player_name%&e] &7» &c%message%");
        messages.put("chat_player_to_admin", "&e[&6%player_name% &7» &6Вы&e] &7» &c%message%");
        messages.put("chat_suspect_to_admin", "&e[&6Вы &7» &6Проверяющий&e] &7» &c%message%");
        messages.put("chat_admin_to_suspect", "&e[&6Проверяющий&7» &6Вы&e] &7» &c%message%");
    }

    public void loadFromConfig(@NotNull FileConfiguration config) {
        Objects.requireNonNull(config, "Конфигурация не может быть null");
        if (config.contains("messages")) {
            for (String key : config.getConfigurationSection("messages").getKeys(false)) {
                String message = config.getString("messages." + key);
                if (message != null) {
                    messages.put(key, message);
                }
            }
        }
    }

    @NotNull
    public String getMessage(@NotNull String key) {
        return messages.getOrDefault(key, "&cСообщение не найдено: " + key);
    }

    @NotNull
    public String getMessage(@NotNull String key, @NotNull Map<String, String> replacements) {
        String message = getMessage(key);

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        return ColorUtil.colorize(message);
    }

    @NotNull
    public String getColoredMessage(@NotNull String key) {
        return ColorUtil.colorize(getMessage(key));
    }

    @NotNull
    public String getColoredMessage(@NotNull String key, @NotNull Map<String, String> replacements) {
        return ColorUtil.colorize(getMessage(key, replacements));
    }

    public boolean containsKey(@NotNull String key) {
        return messages.containsKey(key);
    }
}