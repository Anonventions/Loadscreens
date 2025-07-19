package org.anonventions.loadscreens.depends;

import org.anonventions.loadscreens.core.Loadscreens;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages placeholder parsing and caching for loadscreens.
 * 
 * <p>This class handles both built-in placeholders and PlaceholderAPI integration
 * with intelligent caching to improve performance. It supports custom placeholders
 * defined in the configuration and provides fallback behavior when PlaceholderAPI
 * is not available.</p>
 * 
 * <p>Built-in placeholders include:
 * <ul>
 *   <li>%player_name% - Player's name</li>
 *   <li>%player_displayname% - Player's display name</li>
 *   <li>%player_world% - Current world name</li>
 *   <li>%player_x%, %player_y%, %player_z% - Player coordinates</li>
 *   <li>%player_health%, %player_max_health% - Health information</li>
 *   <li>%player_food% - Food level</li>
 *   <li>%player_level% - Experience level</li>
 *   <li>%current_time% - Current timestamp</li>
 * </ul>
 * 
 * @author Anonventions
 * @since 1.0
 */

public class PlaceholderManager {
    /** Custom placeholder definitions from configuration */
    private final Map<String, String> customPlaceholders = new HashMap<>();
    
    /** Cache for tracking placeholder update times */
    private final Map<String, Long> placeholderCache = new ConcurrentHashMap<>();
    
    /** Cache for storing parsed placeholder values */
    private final Map<String, String> cachedValues = new ConcurrentHashMap<>();
    
    /** Maximum cache size to prevent memory issues */
    private static final int MAX_CACHE_SIZE = 1000;

    /**
     * Creates a new PlaceholderManager and loads custom placeholders.
     */
    public PlaceholderManager() {
        loadCustomPlaceholders();
    }

    /**
     * Loads custom placeholder definitions from the configuration.
     */
    private void loadCustomPlaceholders() {
        try {
            var config = Loadscreens.getInstance().getConfig();
            var customSection = config.getConfigurationSection("placeholders.custom_placeholders");

            if (customSection != null) {
                for (String key : customSection.getKeys(false)) {
                    String value = customSection.getString(key);
                    if (value != null) {
                        customPlaceholders.put(key, ChatColor.translateAlternateColorCodes('&', value));
                    }
                }
                Loadscreens.getInstance().getLogger().info("Loaded " + customPlaceholders.size() + " custom placeholders");
            }
        } catch (Exception e) {
            Loadscreens.getInstance().getLogger().warning("Failed to load custom placeholders: " + e.getMessage());
        }
    }

    /**
     * Parses placeholders in the given text for the specified player.
     * 
     * <p>This method processes placeholders in the following order:
     * <ol>
     *   <li>Custom placeholders from configuration</li>
     *   <li>PlaceholderAPI placeholders (if available)</li>
     *   <li>Color code translation</li>
     * </ol>
     * 
     * @param player the player for context-specific placeholders
     * @param text the text containing placeholders to parse
     * @return the text with placeholders replaced
     */
    public String parsePlaceholders(Player player, String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Input validation
        if (player == null) {
            Loadscreens.getInstance().getLogger().warning("Player is null in parsePlaceholders, skipping PlaceholderAPI");
            return parseCustomPlaceholders(text);
        }

        // Parse custom placeholders first
        String result = parseCustomPlaceholders(text);

        // Parse PlaceholderAPI placeholders if available
        if (Loadscreens.getInstance().isPlaceholderAPIEnabled()) {
            result = parsePlaceholderAPI(player, result);
        }

        // Fallback to basic color code translation
        return ChatColor.translateAlternateColorCodes('&', result);
    }
    
    /**
     * Parses custom placeholders defined in the configuration.
     */
    private String parseCustomPlaceholders(String text) {
        String result = text;
        for (Map.Entry<String, String> entry : customPlaceholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    /**
     * Parses PlaceholderAPI placeholders with caching.
     */
    private String parsePlaceholderAPI(Player player, String text) {
        // Check cache first
        String cacheKey = player.getUniqueId().toString() + ":" + text.hashCode();
        long cacheTime = Loadscreens.getInstance().getConfig().getLong("placeholders.cache_duration", 100) * 50L; // Convert to ms

        if (placeholderCache.containsKey(cacheKey)) {
            long lastUpdate = placeholderCache.get(cacheKey);
            if (System.currentTimeMillis() - lastUpdate < cacheTime) {
                return cachedValues.getOrDefault(cacheKey, text);
            }
        }

        // Clean cache if it gets too large
        if (placeholderCache.size() > MAX_CACHE_SIZE) {
            clearOldCacheEntries();
        }

        // Parse and cache using reflection to avoid compile-time dependency
        try {
            Class<?> placeholderAPIClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Object parsed = placeholderAPIClass.getMethod("setPlaceholders", Player.class, String.class)
                    .invoke(null, player, text);
            String parsedText = (String) parsed;

            // Cache the result
            placeholderCache.put(cacheKey, System.currentTimeMillis());
            cachedValues.put(cacheKey, parsedText);

            return parsedText;
        } catch (Exception e) {
            Loadscreens.getInstance().getLogger().warning("Failed to parse PlaceholderAPI placeholders: " + e.getMessage());
            return text;
        }
    }
    
    /**
     * Removes old cache entries to prevent memory leaks.
     */
    private void clearOldCacheEntries() {
        long currentTime = System.currentTimeMillis();
        long maxAge = 300000; // 5 minutes
        
        placeholderCache.entrySet().removeIf(entry -> {
            boolean isOld = currentTime - entry.getValue() > maxAge;
            if (isOld) {
                cachedValues.remove(entry.getKey());
            }
            return isOld;
        });
    }

    /**
     * Clears all cached placeholder values.
     */
    public void clearCache() {
        placeholderCache.clear();
        cachedValues.clear();
    }

    /**
     * Reloads custom placeholders from the configuration.
     */
    public void reloadCustomPlaceholders() {
        customPlaceholders.clear();
        loadCustomPlaceholders();
        // Clear cache since placeholders might have changed
        clearCache();
    }

    /**
     * Parses built-in placeholders that don't require external dependencies.
     * 
     * <p>This method provides basic placeholder functionality even when
     * PlaceholderAPI is not available.</p>
     * 
     * @param player the player for context
     * @param text the text to parse
     * @return the text with built-in placeholders replaced
     */
    public String parseBuiltinPlaceholders(Player player, String text) {
        if (player == null || text == null) {
            return text;
        }
        
        try {
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
                    .replace("%current_time%", String.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
            Loadscreens.getInstance().getLogger().warning("Error parsing built-in placeholders: " + e.getMessage());
            return text;
        }
    }
    
    /**
     * Gets the number of cached placeholder values.
     * 
     * @return the cache size
     */
    public int getCacheSize() {
        return cachedValues.size();
    }
    
    /**
     * Gets the number of custom placeholders loaded.
     * 
     * @return the number of custom placeholders
     */
    public int getCustomPlaceholderCount() {
        return customPlaceholders.size();
    }
}