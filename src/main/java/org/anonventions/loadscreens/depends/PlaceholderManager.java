package org.anonventions.loadscreens.depends;

import me.clip.placeholderapi.PlaceholderAPI;
import org.anonventions.loadscreens.core.Loadscreens;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlaceholderManager {
    private final Map<String, String> customPlaceholders = new HashMap<>();
    private final Map<String, Long> placeholderCache = new ConcurrentHashMap<>();
    private final Map<String, String> cachedValues = new ConcurrentHashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public PlaceholderManager() {
        loadCustomPlaceholders();
    }

    private void loadCustomPlaceholders() {
        var config = Loadscreens.getInstance().getConfig();
        var customSection = config.getConfigurationSection("placeholders.custom_placeholders");

        if (customSection != null) {
            for (String key : customSection.getKeys(false)) {
                String value = customSection.getString(key);
                customPlaceholders.put(key, ChatColor.translateAlternateColorCodes('&', value));
            }
        }
    }

    public String parsePlaceholders(Player player, String text) {
        if (text == null || text.isEmpty()) return text;

        // Parse built-in placeholders first
        text = parseBuiltinPlaceholders(player, text);

        // Parse custom placeholders
        for (Map.Entry<String, String> entry : customPlaceholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }

        // Parse PlaceholderAPI placeholders if available
        if (Loadscreens.getInstance().isPlaceholderAPIEnabled()) {
            // Check cache first
            String cacheKey = player.getUniqueId().toString() + ":" + text;
            long cacheTime = Loadscreens.getInstance().getConfig().getLong("placeholders.cache_duration", 100) * 50L; // Convert to ms

            if (placeholderCache.containsKey(cacheKey)) {
                long lastUpdate = placeholderCache.get(cacheKey);
                if (System.currentTimeMillis() - lastUpdate < cacheTime) {
                    return cachedValues.getOrDefault(cacheKey, text);
                }
            }

            // Parse and cache
            try {
                String parsed = PlaceholderAPI.setPlaceholders(player, text);
                parsed = ChatColor.translateAlternateColorCodes('&', parsed);

                // Cache the result
                placeholderCache.put(cacheKey, System.currentTimeMillis());
                cachedValues.put(cacheKey, parsed);

                return parsed;
            } catch (Exception e) {
                Loadscreens.getInstance().getLogger().warning("Failed to parse placeholders: " + e.getMessage());
            }
        }

        // Fallback to basic color code translation
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public void clearCache() {
        placeholderCache.clear();
        cachedValues.clear();
    }

    public void reloadCustomPlaceholders() {
        customPlaceholders.clear();
        loadCustomPlaceholders();
    }

    // Built-in placeholders
    public String parseBuiltinPlaceholders(Player player, String text) {
        return text
                .replace("%player_name%", player.getName())
                .replace("%player_displayname%", player.getDisplayName())
                .replace("%player_world%", player.getWorld().getName())
                .replace("%player_x%", String.valueOf((int) player.getLocation().getX()))
                .replace("%player_y%", String.valueOf((int) player.getLocation().getY()))
                .replace("%player_z%", String.valueOf((int) player.getLocation().getZ()))
                .replace("%player_health%", String.valueOf((int) player.getHealth()))
                .replace("%player_max_health%", String.valueOf((int) player.getMaxHealth()))
                .replace("%player_food%", String.valueOf(player.getFoodLevel()))
                .replace("%player_level%", String.valueOf(player.getLevel()))
                .replace("%current_time%", dateFormat.format(new Date()))
                .replace("%server_name%", player.getServer().getName())
                .replace("%server_motd%", player.getServer().getMotd())
                .replace("%online_players%", String.valueOf(player.getServer().getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(player.getServer().getMaxPlayers()));
    }
}