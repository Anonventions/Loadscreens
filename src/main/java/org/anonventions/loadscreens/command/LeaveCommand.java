package org.anonventions.loadscreens.command;

import org.anonventions.loadscreens.core.Loadscreens;
import org.anonventions.loadscreens.util.LoadscreenManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LeaveCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        var config = Loadscreens.getInstance().getConfig();

        // Check if leave command feature is enabled
        if (!config.getBoolean("loadscreen_types.leave.allow_leave_command", true)) {
            sender.sendMessage(Component.text("Leave command is disabled on this server.")
                    .color(NamedTextColor.RED));
            return true;
        }

        // Check if leave loadscreen is enabled
        if (!config.getBoolean("loadscreen_types.leave.enabled", false)) {
            sender.sendMessage(Component.text("Leave loadscreen is disabled.")
                    .color(NamedTextColor.RED));
            return true;
        }

        // Show leave loadscreen and schedule disconnect
        showLeaveLoadscreenAndDisconnect(player);

        if (config.getBoolean("global.debug", false)) {
            Loadscreens.getInstance().getLogger().info("Player " + player.getName() + " used " + label + " command - showing leave loadscreen");
        }

        return true;
    }

    private void showLeaveLoadscreenAndDisconnect(Player player) {
        // Show the leave loadscreen
        LoadscreenManager.showLoadscreen(player, "leave");

        // Calculate disconnect delay based on leave loadscreen duration
        var config = Loadscreens.getInstance().getConfig();
        int duration = config.getInt("loadscreen_types.leave.duration", 40);
        int fadeOutDuration = config.getInt("loadscreen_types.leave.fade_out_duration", 10);
        int disconnectDelay = duration + fadeOutDuration + 5; // Extra 5 ticks for safety

        // Schedule the actual disconnect after the loadscreen completes
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    // Show leave loadscreen to nearby players if enabled
                    showLeaveLoadscreenToNearbyPlayers(player);

                    // Disconnect the player with a nice message
                    player.kick(Component.text("Thanks for playing! See you next time!")
                            .color(NamedTextColor.YELLOW));

                    if (config.getBoolean("global.debug", false)) {
                        Loadscreens.getInstance().getLogger().info("Disconnected " + player.getName() + " after leave loadscreen");
                    }
                }
            }
        }.runTaskLater(Loadscreens.getInstance(), disconnectDelay);
    }

    private void showLeaveLoadscreenToNearbyPlayers(Player leavingPlayer) {
        var config = Loadscreens.getInstance().getConfig();

        // Show leave loadscreen to other players if enabled
        if (config.getBoolean("loadscreen_types.leave.show_to_others", false)) {
            double range = config.getDouble("loadscreen_types.leave.show_range", 50.0);

            for (Player other : leavingPlayer.getWorld().getPlayers()) {
                if (!other.equals(leavingPlayer) &&
                    other.getLocation().distance(leavingPlayer.getLocation()) <= range) {
                    LoadscreenManager.showLoadscreen(other, "leave");
                }
            }
        }
    }
}
