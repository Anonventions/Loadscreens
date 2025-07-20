package org.anonventions.loadscreens.command;

import org.anonventions.loadscreens.core.Loadscreens;
import org.anonventions.loadscreens.util.LoadscreenManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class LoadscreenCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("loadscreens.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                Loadscreens.getInstance().reloadConfig();
                // Reload placeholder manager if available
                if (Loadscreens.getInstance().isPlaceholderAPIEnabled()) {
                    Loadscreens.getInstance().getPlaceholderManager().reloadCustomPlaceholders();
                }
                sender.sendMessage(Component.text("Loadscreens config reloaded successfully!")
                        .color(NamedTextColor.GREEN));
                break;

            case "test":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Component.text("Only players can test loadscreens.")
                            .color(NamedTextColor.RED));
                    return true;
                }

                Player player = (Player) sender;
                String type = args.length > 1 ? args[1] : "join";

                LoadscreenManager.showLoadscreen(player, type);
                sender.sendMessage(Component.text("Loadscreen test sent! Type: " + type)
                        .color(NamedTextColor.GREEN));
                break;

            case "show":
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /loadscreen show <player> <type> [delay]")
                            .color(NamedTextColor.RED));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("Player not found: " + args[1])
                            .color(NamedTextColor.RED));
                    return true;
                }

                String showType = args[2];
                int delay = args.length > 3 ? parseInt(args[3], 0) : 0;

                LoadscreenManager.showLoadscreen(target, showType, delay);
                sender.sendMessage(Component.text("Loadscreen sent to " + target.getName() + " (Type: " + showType + ", Delay: " + delay + ")")
                        .color(NamedTextColor.GREEN));
                break;

            case "stop":
                if (args.length > 1) {
                    // Stop for specific player (admin only)
                    Player target2 = Bukkit.getPlayer(args[1]);
                    if (target2 == null) {
                        sender.sendMessage(Component.text("Player not found: " + args[1])
                                .color(NamedTextColor.RED));
                        return true;
                    }
                    LoadscreenManager.stopLoadscreen(target2);
                    sender.sendMessage(Component.text("Stopped loadscreen for " + target2.getName())
                            .color(NamedTextColor.GREEN));
                } else {
                    // Stop for self
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Component.text("Only players can stop their own loadscreens.")
                                .color(NamedTextColor.RED));
                        return true;
                    }

                    LoadscreenManager.stopLoadscreen((Player) sender);
                    sender.sendMessage(Component.text("Stopped your loadscreen.")
                            .color(NamedTextColor.GREEN));
                }
                break;

            case "leave":
            case "exit":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Component.text("Only players can use the leave command.")
                            .color(NamedTextColor.RED));
                    return true;
                }

                Player leavingPlayer = (Player) sender;
                var config = Loadscreens.getInstance().getConfig();

                // Check if leave loadscreen is enabled
                if (!config.getBoolean("loadscreen_types.leave.enabled", false)) {
                    sender.sendMessage(Component.text("Leave loadscreen is disabled.")
                            .color(NamedTextColor.RED));
                    return true;
                }

                // Show leave loadscreen and schedule disconnect
                showLeaveLoadscreenAndDisconnect(leavingPlayer);
                break;

            case "stopall":
                int count = LoadscreenManager.getActiveSessionCount();
                LoadscreenManager.stopAllLoadscreens();
                sender.sendMessage(Component.text("Stopped " + count + " active loadscreens!")
                        .color(NamedTextColor.GREEN));
                break;

            case "info":
                sendInfo(sender);
                break;

            case "stats":
                sendStats(sender);
                break;

            case "types":
                sendTypes(sender);
                break;

            case "version":
                sendVersion(sender);
                break;
            case "debug-packets":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Component.text("Only players can use debug-packets.")
                            .color(NamedTextColor.RED));
                    return true;
                }

                Player debugPlayer = (Player) sender;
                boolean isBlocked = false;

                if (Loadscreens.getInstance().isPacketEventsEnabled()) {
                    isBlocked = Loadscreens.getInstance().getPacketManager().isPlayerBlocked(debugPlayer);
                }

                sender.sendMessage(Component.text("=== PACKET DEBUG INFO ===").color(NamedTextColor.GOLD));
                sender.sendMessage(Component.text("Player: " + debugPlayer.getName()).color(NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Packets Blocked: " + isBlocked).color(NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Active Loadscreen: " + LoadscreenManager.hasActiveLoadscreen(debugPlayer)).color(NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Walk Speed: " + debugPlayer.getWalkSpeed()).color(NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Fly Speed: " + debugPlayer.getFlySpeed()).color(NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Game Mode: " + debugPlayer.getGameMode()).color(NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Location: " + debugPlayer.getLocation().getYaw() + ", " + debugPlayer.getLocation().getPitch()).color(NamedTextColor.YELLOW));
                break;
            case "debug":
                toggleDebug(sender);
                break;

            case "clear":
                clearCache(sender);
                break;

            case "hotreload":
                if (sender.hasPermission("loadscreens.admin")) {
                    Loadscreens.getInstance().hotReload();
                    sender.sendMessage(Component.text("Plugin hot reloaded successfully!")
                            .color(NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("No permission for hot reload.")
                            .color(NamedTextColor.RED));
                }
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("╔══════════════════════════════════════════════════════════════╗")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║                  LOADSCREENS COMMANDS                       ║")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("╠══════════════════════════════════════════════════════════════╣")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║ /loadscreen reload - Reload the config                      ║")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("║ /loadscreen test [type] - Test loadscreen                   ║")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("║ /loadscreen show <player> <type> [delay] - Show to player   ║")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("║ /loadscreen stop [player] - Stop loadscreen                 ║")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("║ /loadscreen stopall - Stop all loadscreens                  ║")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("║ /loadscreen info - Show plugin info                         ║")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("║ /loadscreen stats - Show session statistics                 ║")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("║ /loadscreen types - List available types                    ║")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("║ /loadscreen version - Show version info                     ║")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("║ /loadscreen debug - Toggle debug mode                       ║")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("║ /loadscreen clear - Clear placeholder cache                 ║")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("║ /loadscreen hotreload - Hot reload plugin                   ║")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("╚══════════════════════════════════════════════════════════════╝")
                .color(NamedTextColor.GOLD));
    }

    private void sendInfo(CommandSender sender) {
        var config = Loadscreens.getInstance().getConfig();
        sender.sendMessage(Component.text("╔══════════════════════════════════════════════════════════════╗")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║                    LOADSCREENS INFO                         ║")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("╠══════════════════════════════════════════════════════════════╣")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║ Version: 3.0 Ultimate Edition                               ║")
                .color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("║ Author: Anonventions                                        ║")
                .color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("║ Build Date: 2025-07-16 08:45:37 UTC                         ║")
                .color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("║ Enabled: " + config.getBoolean("global.enabled") + "                                             ║")
                .color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("║ PacketEvents: " + (Loadscreens.getInstance().isPacketEventsEnabled() ? "✓ Enabled" : "✗ Disabled") + "                                    ║")
                .color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("║ PlaceholderAPI: " + (Loadscreens.getInstance().isPlaceholderAPIEnabled() ? "✓ Enabled" : "✗ Disabled") + "                                  ║")
                .color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("║ Debug Mode: " + config.getBoolean("global.debug") + "                                         ║")
                .color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("║ Async Processing: " + config.getBoolean("global.async_processing") + "                                   ║")
                .color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("╚══════════════════════════════════════════════════════════════╝")
                .color(NamedTextColor.GOLD));
    }

    private void sendStats(CommandSender sender) {
        sender.sendMessage(Component.text("╔══════════════════════════════════════════════════════════════╗")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║                 LOADSCREEN STATISTICS                       ║")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("╠══════════════════════════════════════════════════════════════╣")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║ Active Sessions: " + LoadscreenManager.getActiveSessionCount() + "                                        ║")
                .color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("║ Max Concurrent: " + Loadscreens.getInstance().getConfig().getInt("global.max_concurrent_sessions") + "                                       ║")
                .color(NamedTextColor.GRAY));

        // Show memory usage
        long totalMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;

        sender.sendMessage(Component.text("║ Memory Usage: " + usedMemory + "MB / " + totalMemory + "MB                            ║")
                .color(NamedTextColor.GRAY));

        // PacketEvents stats
        if (Loadscreens.getInstance().isPacketEventsEnabled()) {
            int blockedPlayers = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (Loadscreens.getInstance().getPacketManager().isPlayerBlocked(player)) {
                    blockedPlayers++;
                }
            }
            sender.sendMessage(Component.text("║ Blocked Players: " + blockedPlayers + "                                        ║")
                    .color(NamedTextColor.GRAY));
        }

        sender.sendMessage(Component.text("╚══════════════════════════════════════════════════════════════╝")
                .color(NamedTextColor.GOLD));
    }

    private void sendTypes(CommandSender sender) {
        var config = Loadscreens.getInstance().getConfig();
        var typesSection = config.getConfigurationSection("loadscreen_types");

        sender.sendMessage(Component.text("╔══════════════════════════════════════════════════════════════╗")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║              AVAILABLE LOADSCREEN TYPES                     ║")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("╠══════════════════════════════════════════════════════════════╣")
                .color(NamedTextColor.GOLD));

        if (typesSection != null) {
            for (String type : typesSection.getKeys(false)) {
                boolean enabled = config.getBoolean("loadscreen_types." + type + ".enabled", false);
                NamedTextColor color = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;
                String status = enabled ? "✓" : "✗";

                int duration = config.getInt("loadscreen_types." + type + ".duration", 0);
                int frames = config.getStringList("loadscreen_types." + type + ".frames").size();
                int fadeIn = config.getInt("loadscreen_types." + type + ".fade_in_duration", 0);
                int fadeOut = config.getInt("loadscreen_types." + type + ".fade_out_duration", 0);

                sender.sendMessage(Component.text("║ " + status + " " + type + " (D:" + duration + "t F:" + frames + " FI:" + fadeIn + " FO:" + fadeOut + ")").color(color));
            }
        } else {
            sender.sendMessage(Component.text("║ No loadscreen types configured!                             ║")
                    .color(NamedTextColor.RED));
        }

        sender.sendMessage(Component.text("╚══════════════════════════════════════════════════════════════╝")
                .color(NamedTextColor.GOLD));
    }

    private void sendVersion(CommandSender sender) {
        sender.sendMessage(Component.text("╔══════════════════════════════════════════════════════════════╗")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║                   LOADSCREENS VERSION                       ║")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("╠══════════════════════════════════════════════════════════════╣")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("║ Plugin Version: 3.0 Ultimate Edition                        ║")
                .color(NamedTextColor.GREEN));
        sender.sendMessage(Component.text("║ Build: 2025-07-16 08:45:37 UTC                              ║")
                .color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("║ Author: Anonventions                                        ║")
                .color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("║ GitHub: https://github.com/Anonventions/Loadscreens         ║")
                .color(NamedTextColor.BLUE));

        // Minecraft version compatibility
        String mcVersion = Bukkit.getBukkitVersion();
        sender.sendMessage(Component.text("║ MC Version: " + mcVersion.substring(0, Math.min(mcVersion.length(), 20)) + "                           ║")
                .color(NamedTextColor.GRAY));

        // Dependency versions
        if (Loadscreens.getInstance().isPlaceholderAPIEnabled()) {
            sender.sendMessage(Component.text("║ PlaceholderAPI: ✓ Available                                 ║")
                    .color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("║ PlaceholderAPI: ✗ Not Found                                 ║")
                    .color(NamedTextColor.RED));
        }

        if (Loadscreens.getInstance().isPacketEventsEnabled()) {
            sender.sendMessage(Component.text("║ PacketEvents: ✓ Available                                   ║")
                    .color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("║ PacketEvents: ✗ Not Found                                   ║")
                    .color(NamedTextColor.RED));
        }

        sender.sendMessage(Component.text("╚══════════════════════════════════════════════════════════════╝")
                .color(NamedTextColor.GOLD));
    }

    private void toggleDebug(CommandSender sender) {
        var config = Loadscreens.getInstance().getConfig();
        boolean currentDebug = config.getBoolean("global.debug", false);
        boolean newDebug = !currentDebug;

        config.set("global.debug", newDebug);
        Loadscreens.getInstance().saveConfig();

        NamedTextColor color = newDebug ? NamedTextColor.GREEN : NamedTextColor.RED;
        String status = newDebug ? "enabled" : "disabled";

        sender.sendMessage(Component.text("Debug mode " + status + "!")
                .color(color));

        if (newDebug) {
            sender.sendMessage(Component.text("Debug information will now be logged to console.")
                    .color(NamedTextColor.YELLOW));
        }
    }

    private void clearCache(CommandSender sender) {
        if (Loadscreens.getInstance().isPlaceholderAPIEnabled()) {
            Loadscreens.getInstance().getPlaceholderManager().clearCache();
            sender.sendMessage(Component.text("Placeholder cache cleared successfully!")
                    .color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("PlaceholderAPI not available - no cache to clear.")
                    .color(NamedTextColor.YELLOW));
        }
    }

    private int parseInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("loadscreens.admin")) {
            return null;
        }

        if (args.length == 1) {
            return Arrays.asList("reload", "test", "show", "stop", "stopall", "info", "stats", "types", "version", "debug", "clear", "hotreload")
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "test":
                    return getLoadscreenTypes().stream()
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());

                case "show":
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());

                case "stop":
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("show")) {
            return getLoadscreenTypes().stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("show")) {
            return Arrays.asList("0", "5", "10", "20", "40", "60", "100")
                    .stream()
                    .filter(s -> s.startsWith(args[3]))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private List<String> getLoadscreenTypes() {
        List<String> types = new ArrayList<>();
        var config = Loadscreens.getInstance().getConfig();
        var typesSection = config.getConfigurationSection("loadscreen_types");

        if (typesSection != null) {
            types.addAll(typesSection.getKeys(false));
        }

        return types;
    }

    private void showLeaveLoadscreenAndDisconnect(Player player) {
        // Show the leave loadscreen
        LoadscreenManager.showLoadscreen(player, "leave");

        // Schedule the disconnect after the loadscreen duration
        var config = Loadscreens.getInstance().getConfig();
        int delay = config.getInt("loadscreen_types.leave.duration", 5) * 20; // Convert to ticks

        Bukkit.getScheduler().runTaskLater(Loadscreens.getInstance(), () -> {
            player.kick(Component.text("You have been disconnected.").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, true));
        }, delay);
    }
}

