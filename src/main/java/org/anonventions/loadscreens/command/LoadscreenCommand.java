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
import java.util.stream.Collectors;

/**
 * Command executor for the main /loadscreen command.
 * 
 * <p>This class handles all loadscreen-related commands including:
 * <ul>
 *   <li>reload - Reload the plugin configuration</li>
 *   <li>test - Test a loadscreen type</li>
 *   <li>show - Show a loadscreen to a specific player</li>
 *   <li>stop - Stop the sender's loadscreen</li>
 *   <li>stopall - Stop all active loadscreens</li>
 *   <li>info - Display plugin information</li>
 *   <li>stats - Display session statistics</li>
 *   <li>types - List available loadscreen types</li>
 * </ul>
 * 
 * @author Anonventions
 * @since 1.0
 */

public class LoadscreenCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permission check
        if (!sender.hasPermission("loadscreens.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        try {
            switch (subCommand) {
                case "reload":
                    handleReload(sender);
                    break;

                case "test":
                    handleTest(sender, args);
                    break;

                case "show":
                    handleShow(sender, args);
                    break;

                case "stop":
                    handleStop(sender);
                    break;

                case "stopall":
                    handleStopAll(sender);
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

                default:
                    sendHelp(sender);
                    break;
            }
        } catch (Exception e) {
            sender.sendMessage(Component.text("An error occurred while executing the command: " + e.getMessage())
                    .color(NamedTextColor.RED));
            Loadscreens.getInstance().getLogger().severe("Error in command execution: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Handles the reload subcommand.
     */
    private void handleReload(CommandSender sender) {
        Loadscreens.getInstance().reloadConfig();
        // Reload placeholder manager if available
        if (Loadscreens.getInstance().isPlaceholderAPIEnabled()) {
            Loadscreens.getInstance().getPlaceholderManager().reloadCustomPlaceholders();
        }
        sender.sendMessage(Component.text("Loadscreens config reloaded successfully!")
                .color(NamedTextColor.GREEN));
    }

    /**
     * Handles the test subcommand.
     */
    private void handleTest(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can test loadscreens.")
                    .color(NamedTextColor.RED));
            return;
        }

        Player player = (Player) sender;
        String type = args.length > 1 ? args[1].trim() : "join";
        
        if (type.isEmpty()) {
            sender.sendMessage(Component.text("Type cannot be empty.")
                    .color(NamedTextColor.RED));
            return;
        }

        LoadscreenManager.showLoadscreen(player, type);
        sender.sendMessage(Component.text("Loadscreen test sent! Type: " + type)
                .color(NamedTextColor.GREEN));
    }

    /**
     * Handles the show subcommand.
     */
    private void handleShow(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /loadscreen show <player> <type> [delay]")
                    .color(NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: " + args[1])
                    .color(NamedTextColor.RED));
            return;
        }

        String showType = args[2].trim();
        if (showType.isEmpty()) {
            sender.sendMessage(Component.text("Type cannot be empty.")
                    .color(NamedTextColor.RED));
            return;
        }
        
        int delay = args.length > 3 ? parseInt(args[3], 0) : 0;
        if (delay < 0) {
            sender.sendMessage(Component.text("Delay cannot be negative.")
                    .color(NamedTextColor.RED));
            return;
        }

        LoadscreenManager.showLoadscreen(target, showType, delay);
        sender.sendMessage(Component.text("Loadscreen sent to " + target.getName() + " (Type: " + showType + ", Delay: " + delay + ")")
                .color(NamedTextColor.GREEN));
    }

    /**
     * Handles the stop subcommand.
     */
    private void handleStop(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can stop their own loadscreens.")
                    .color(NamedTextColor.RED));
            return;
        }

        Player player = (Player) sender;
        if (LoadscreenManager.hasActiveLoadscreen(player)) {
            LoadscreenManager.stopLoadscreen(player);
            sender.sendMessage(Component.text("Loadscreen stopped!")
                    .color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("You don't have an active loadscreen.")
                    .color(NamedTextColor.YELLOW));
        }
    }

    /**
     * Handles the stopall subcommand.
     */
    private void handleStopAll(CommandSender sender) {
        int count = LoadscreenManager.getActiveSessionCount();
        LoadscreenManager.stopAllLoadscreens();
        sender.sendMessage(Component.text("Stopped " + count + " active loadscreens!")
                .color(NamedTextColor.GREEN));
    }

    /**
     * Displays the help message with all available commands.
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Loadscreens Commands ===")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));
        sender.sendMessage(Component.text("/loadscreen reload - Reload the config").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/loadscreen test [type] - Test loadscreen").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/loadscreen show <player> <type> [delay] - Show to player").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/loadscreen stop - Stop your loadscreen").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/loadscreen stopall - Stop all loadscreens").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/loadscreen info - Show plugin info").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/loadscreen stats - Show session statistics").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/loadscreen types - List available types").color(NamedTextColor.YELLOW));
    }

    /**
     * Displays plugin information including version and features.
     */
    private void sendInfo(CommandSender sender) {
        var config = Loadscreens.getInstance().getConfig();
        sender.sendMessage(Component.text("=== Loadscreens Info ===")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));
        sender.sendMessage(Component.text("Version: 3.0 Ultimate Edition").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Author: Anonventions").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Date: 2025-07-19").color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Enabled: " + config.getBoolean("global.enabled")).color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("PacketEvents: " + (Loadscreens.getInstance().isPacketEventsEnabled() ? "✓" : "✗")).color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("PlaceholderAPI: " + (Loadscreens.getInstance().isPlaceholderAPIEnabled() ? "✓" : "✗")).color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Debug Mode: " + config.getBoolean("global.debug")).color(NamedTextColor.GRAY));
    }

    /**
     * Displays current session statistics.
     */
    private void sendStats(CommandSender sender) {
        sender.sendMessage(Component.text("=== Loadscreen Statistics ===")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));
        sender.sendMessage(Component.text("Active Sessions: " + LoadscreenManager.getActiveSessionCount()).color(NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Max Concurrent: " + Loadscreens.getInstance().getConfig().getInt("global.max_concurrent_sessions")).color(NamedTextColor.GRAY));
    }

    /**
     * Displays all available loadscreen types and their status.
     */
    private void sendTypes(CommandSender sender) {
        var config = Loadscreens.getInstance().getConfig();
        var typesSection = config.getConfigurationSection("loadscreen_types");

        sender.sendMessage(Component.text("=== Available Loadscreen Types ===")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));

        if (typesSection != null) {
            for (String type : typesSection.getKeys(false)) {
                boolean enabled = config.getBoolean("loadscreen_types." + type + ".enabled", false);
                NamedTextColor color = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;
                String status = enabled ? "✓" : "✗";

                sender.sendMessage(Component.text(status + " " + type).color(color));
            }
        } else {
            sender.sendMessage(Component.text("No loadscreen types configured.").color(NamedTextColor.YELLOW));
        }
    }

    /**
     * Safely parses an integer from a string with a default fallback.
     * 
     * @param str the string to parse
     * @param defaultValue the default value if parsing fails
     * @return the parsed integer or the default value
     */
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
            return Arrays.asList("reload", "test", "show", "stop", "stopall", "info", "stats", "types")
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if ("test".equals(args[0].toLowerCase())) {
                // Tab complete loadscreen types for test command
                var config = Loadscreens.getInstance().getConfig();
                var typesSection = config.getConfigurationSection("loadscreen_types");
                if (typesSection != null) {
                    return typesSection.getKeys(false).stream()
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            } else if ("show".equals(args[0].toLowerCase())) {
                // Tab complete player names for show command
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && "show".equals(args[0].toLowerCase())) {
            // Tab complete loadscreen types for show command
            var config = Loadscreens.getInstance().getConfig();
            var typesSection = config.getConfigurationSection("loadscreen_types");
            if (typesSection != null) {
                return typesSection.getKeys(false).stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return null;
    }
}