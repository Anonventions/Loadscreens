package org.anonventions.loadscreens.util;

import org.anonventions.loadscreens.core.Loadscreens;
import org.anonventions.loadscreens.depends.PlaceholderManager;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.key.Key;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LoadscreenManager {
    private static final Map<UUID, LoadscreenSession> activeSessions = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastLoadscreen = new ConcurrentHashMap<>();

    // Main method with type support
    public static void showLoadscreen(Player player, String type) {
        showLoadscreen(player, type, 0);
    }

    public static void showLoadscreen(Player player, String type, int customDelay) {
        var config = Loadscreens.getInstance().getConfig();

        if (!config.getBoolean("global.enabled", true)) return;

        // Check if specific type is enabled
        if (!config.getBoolean("loadscreen_types." + type + ".enabled", false)) {
            if (config.getBoolean("global.debug", false)) {
                Loadscreens.getInstance().getLogger().info("Loadscreen type '" + type + "' is disabled");
            }
            return;
        }

        // Get delay from config (in seconds) and convert to ticks, or use custom delay
        int delayTicks;
        if (customDelay > 0) {
            delayTicks = customDelay;
        } else {
            double delaySeconds = config.getDouble("loadscreen_types." + type + ".timer_delay", 0.0);
            delayTicks = (int) (delaySeconds * 20); // Convert seconds to ticks (20 ticks = 1 second)
        }

        // Schedule the loadscreen
        if (delayTicks > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    showLoadscreenNow(player, type);
                }
            }.runTaskLater(Loadscreens.getInstance(), delayTicks);
        } else {
            showLoadscreenNow(player, type);
        }
    }

    private static void showLoadscreenNow(Player player, String type) {
        var config = Loadscreens.getInstance().getConfig();
        String basePath = "loadscreen_types." + type + ".";

        // Check cooldown
        long cooldown = config.getLong(basePath + "cooldown_seconds", 0) * 1000L;
        if (cooldown > 0 && lastLoadscreen.containsKey(player.getUniqueId())) {
            long timeSince = System.currentTimeMillis() - lastLoadscreen.get(player.getUniqueId());
            if (timeSince < cooldown) return;
        }

        // Check permission
        if (config.getBoolean(basePath + "require_permission", false) &&
                !player.hasPermission(config.getString(basePath + "permission_node", "loadscreens.view"))) {
            return;
        }

        // Check first join only
        if (config.getBoolean(basePath + "first_join_only", false) && player.hasPlayedBefore()) {
            return;
        }

        // Check max concurrent sessions
        int maxSessions = config.getInt("global.max_concurrent_sessions", 50);
        if (activeSessions.size() >= maxSessions) {
            Loadscreens.getInstance().getLogger().warning("Max concurrent sessions reached, skipping loadscreen for " + player.getName());
            return;
        }

        // Stop existing session
        stopLoadscreen(player);

        // Create new session
        LoadscreenSession session = new LoadscreenSession(player, config, type, basePath);
        activeSessions.put(player.getUniqueId(), session);
        lastLoadscreen.put(player.getUniqueId(), System.currentTimeMillis());

        session.start();
    }

    // Legacy method for backward compatibility
    public static void showLoadscreen(Player player) {
        showLoadscreen(player, "join");
    }

    public static void stopLoadscreen(Player player) {
        LoadscreenSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            session.stop();
        }
    }

    public static void cleanupPlayerData(Player player) {
        // Remove active session
        stopLoadscreen(player);

        // Remove cooldown data to prevent interference on rejoin
        lastLoadscreen.remove(player.getUniqueId());

        if (Loadscreens.getInstance().getConfig().getBoolean("global.debug", false)) {
            Loadscreens.getInstance().getLogger().info("Cleaned up all data for " + player.getName());
        }
    }

    public static void stopAllLoadscreens() {
        for (LoadscreenSession session : activeSessions.values()) {
            session.stop();
        }
        activeSessions.clear();
    }

    public static boolean hasActiveLoadscreen(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public static int getActiveSessionCount() {
        return activeSessions.size();
    }

    // Session class with enhanced fade effects
    private static class LoadscreenSession {
        private final Player player;
        private final List<String> frames;
        private final String type;
        private final String basePath;
        private final boolean debug;

        // Configuration values
        private final int frameInterval, duration, fadeInDuration, fadeOutDuration;
        private final int fadeInOpacityStart, fadeOutOpacityEnd;
        private final boolean fadeSmooth;
        private final double scale, yOffset, zOffset, xOffset;
        private final int opacity, brightness;
        private final boolean freezePlayer, hideUI, hideChat, preventInteraction, blockPackets;
        private final boolean makeInvisible, lockPosition;
        private final String font;

        // Visual effects
        private final boolean pulseEffect, wobbleEffect, rainbowText, typewriterEffect;
        private final double pulseIntensity, pulseSpeed, wobbleIntensity, wobbleSpeed, rainbowSpeed;
        private final int typewriterSpeed;

        // Advanced settings
        private TextDisplay.Billboard billboard;
        private final boolean allowNegativeScale;
        private final double rotationX, rotationY, rotationZ;
        private final boolean glowOverride;
        private final int glowR, glowG, glowB, glowA;

        // Sound settings
        private final boolean playSound, soundPerFrame;
        private final Sound sound;
        private final float soundVolume, soundPitch;

        // New fields for look direction
        private final double lookYaw, lookPitch;

        private TextDisplay display;
        private BukkitRunnable animationTask;
        private BukkitRunnable positionLockTask;
        private List<String> parsedFrames;
        private Location originalLocation;
        private float originalYaw, originalPitch;

        // Store original player state for proper restoration
        private org.bukkit.GameMode originalGameMode;
        private float originalWalkSpeed, originalFlySpeed;
        private boolean originalAllowFlight;

        public LoadscreenSession(Player player, org.bukkit.configuration.file.FileConfiguration config, String type, String basePath) {
            this.player = player;
            this.type = type;
            this.basePath = basePath;
            this.debug = config.getBoolean("global.debug", false);

            // Load configuration
            this.frames = config.getStringList(basePath + "frames");
            this.frameInterval = config.getInt(basePath + "frame_interval", 4);
            this.duration = config.getInt(basePath + "duration", 100);
            this.fadeInDuration = config.getInt(basePath + "fade_in_duration", 10);
            this.fadeOutDuration = config.getInt(basePath + "fade_out_duration", 10);
            this.fadeInOpacityStart = config.getInt(basePath + "fade_in_opacity_start", 0);
            this.fadeOutOpacityEnd = config.getInt(basePath + "fade_out_opacity_end", 0);
            this.fadeSmooth = config.getBoolean(basePath + "fade_smooth", true);
            this.scale = config.getDouble(basePath + "scale", 1.0);
            this.yOffset = config.getDouble(basePath + "y_offset", 0.0);
            this.zOffset = config.getDouble(basePath + "z_offset", 2.5);
            this.xOffset = config.getDouble(basePath + "x_offset", 0.0);
            this.opacity = Math.max(0, Math.min(255, config.getInt(basePath + "opacity", 255)));
            this.brightness = Math.max(0, Math.min(15, config.getInt(basePath + "brightness", 15)));
            this.freezePlayer = config.getBoolean(basePath + "freeze_player", false);
            this.hideUI = config.getBoolean(basePath + "hide_ui", false);
            this.hideChat = config.getBoolean(basePath + "hide_chat", false);
            this.preventInteraction = config.getBoolean(basePath + "prevent_interaction", false);
            this.blockPackets = config.getBoolean(basePath + "block_packets", false);
            this.makeInvisible = config.getBoolean(basePath + "make_invisible", false);
            this.lockPosition = config.getBoolean(basePath + "lock_position", false);
            this.font = config.getString(basePath + "font", "minecraft:default");

            // Visual effects
            this.pulseEffect = config.getBoolean(basePath + "pulse_effect", false);
            this.wobbleEffect = config.getBoolean(basePath + "wobble_effect", false);
            this.rainbowText = config.getBoolean(basePath + "rainbow_text", false);
            this.typewriterEffect = config.getBoolean(basePath + "typewriter_effect", false);
            this.pulseIntensity = config.getDouble(basePath + "pulse_intensity", 0.2);
            this.pulseSpeed = config.getDouble(basePath + "pulse_speed", 0.1);
            this.wobbleIntensity = config.getDouble(basePath + "wobble_intensity", 0.1);
            this.wobbleSpeed = config.getDouble(basePath + "wobble_speed", 0.2);
            this.rainbowSpeed = config.getDouble(basePath + "rainbow_speed", 0.05);
            this.typewriterSpeed = config.getInt(basePath + "typewriter_speed", 2);

            // Advanced settings
            this.allowNegativeScale = config.getBoolean(basePath + "allow_negative_scale", true);
            this.rotationX = config.getDouble(basePath + "rotation_x", 0.0);
            this.rotationY = config.getDouble(basePath + "rotation_y", 0.0);
            this.rotationZ = config.getDouble(basePath + "rotation_z", 0.0);
            this.glowOverride = config.getBoolean(basePath + "glow_color_override", false);
            this.glowR = config.getInt(basePath + "glow_color_red", 255);
            this.glowG = config.getInt(basePath + "glow_color_green", 255);
            this.glowB = config.getInt(basePath + "glow_color_blue", 255);
            this.glowA = config.getInt(basePath + "glow_color_alpha", 255);

            // Billboard
            String billboardStr = config.getString(basePath + "billboard", "CENTER").toUpperCase();
            try {
                this.billboard = TextDisplay.Billboard.valueOf(billboardStr);
            } catch (IllegalArgumentException e) {
                this.billboard = TextDisplay.Billboard.CENTER;
            }

            // Sound
            this.playSound = config.getBoolean(basePath + "play_sound", false);
            this.soundVolume = (float) config.getDouble(basePath + "sound_volume", 0.5);
            this.soundPitch = (float) config.getDouble(basePath + "sound_pitch", 1.0);
            this.soundPerFrame = config.getBoolean(basePath + "sound_per_frame", false);

            Sound tempSound;
            try {
                tempSound = Sound.valueOf(config.getString(basePath + "sound_name", "BLOCK_NOTE_BLOCK_PLING").toUpperCase().replace("MINECRAFT:", ""));
            } catch (IllegalArgumentException e) {
                tempSound = Sound.BLOCK_NOTE_BLOCK_PLING;
            }
            this.sound = tempSound;

            // New fields for look direction
            this.lookYaw = config.getDouble(basePath + "look_yaw", 0.0);
            this.lookPitch = config.getDouble(basePath + "look_pitch", 0.0);
        }

        public void start() {
            if (frames.isEmpty()) {
                if (debug) Loadscreens.getInstance().getLogger().warning("No frames configured for type: " + type);
                return;
            }

            // Store original position and rotation
            originalLocation = player.getLocation().clone();
            originalYaw = player.getLocation().getYaw();
            originalPitch = player.getLocation().getPitch();

            // Store original player state for proper restoration
            originalGameMode = player.getGameMode();
            originalWalkSpeed = player.getWalkSpeed();
            originalFlySpeed = player.getFlySpeed();
            originalAllowFlight = player.getAllowFlight();

            if (debug) {
                Loadscreens.getInstance().getLogger().info("Stored original state for " + player.getName() +
                    " - GameMode: " + originalGameMode + ", WalkSpeed: " + originalWalkSpeed +
                    ", FlySpeed: " + originalFlySpeed + ", AllowFlight: " + originalAllowFlight);
            }

            // Only change look direction if specified in config, don't teleport to find "safe" location
            if (lookYaw != 0.0 || lookPitch != 0.0) {
                Location lookLoc = player.getLocation().clone();
                lookLoc.setYaw((float) lookYaw);
                lookLoc.setPitch((float) lookPitch);
                player.teleport(lookLoc);

                if (debug) {
                    Loadscreens.getInstance().getLogger().info("Forced " + player.getName() + " to look at yaw: " + lookYaw + ", pitch: " + lookPitch);
                }
            }

            // Parse placeholders in frames
            parsePlaceholders();

            // ALWAYS apply packet blocking to prevent mouse movement
            if (Loadscreens.getInstance().isPacketEventsEnabled()) {
                Loadscreens.getInstance().getPacketManager().blockPackets(player);
                if (debug) {
                    Loadscreens.getInstance().getLogger().info("Blocked all input packets for " + player.getName());
                }
            }

            // Create display in front of player's new view
            createDisplayInFrontOfPlayer();

            // Start animation
            startAnimation();

            // ALWAYS start position lock for maximum movement blocking
            startPositionLock();

            if (debug) {
                Loadscreens.getInstance().getLogger().info("Started loadscreen '" + type + "' for " + player.getName());
            }
        }

        private void parsePlaceholders() {
            parsedFrames = new java.util.ArrayList<>();

            if (Loadscreens.getInstance().isPlaceholderAPIEnabled()) {
                PlaceholderManager pm = Loadscreens.getInstance().getPlaceholderManager();
                for (String frame : frames) {
                    String parsed = pm.parsePlaceholders(player, frame);
                    parsedFrames.add(parsed);
                }
            } else {
                // Use only built-in placeholders
                PlaceholderManager pm = new PlaceholderManager();
                for (String frame : frames) {
                    String parsed = pm.parseBuiltinPlaceholders(player, frame);
                    parsedFrames.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', parsed));
                }
            }
        }

        private void applyPlayerRestrictions() {
            // No-op: always enforced above
        }

        private void startPositionLock() {
            // Run position locking with reasonable frequency and tolerance
            positionLockTask = new BukkitRunnable() {
                private int tickCounter = 0;

                @Override
                public void run() {
                    if (!LoadscreenManager.hasActiveLoadscreen(player) || !player.isOnline()) {
                        cancel();
                        return;
                    }

                    // Only check every 2 ticks (10 times per second) instead of every tick
                    tickCounter++;
                    if (tickCounter % 2 != 0) {
                        return;
                    }

                    Location currentLoc = player.getLocation();

                    // Use reasonable tolerance to prevent micro-corrections
                    boolean needsCorrection = false;
                    double rotationTolerance = 0.5; // Allow small movements before correcting

                    if (Math.abs(currentLoc.getYaw() - originalYaw) > rotationTolerance ||
                            Math.abs(currentLoc.getPitch() - originalPitch) > rotationTolerance) {
                        needsCorrection = true;
                    }

                    if (needsCorrection) {
                        // Create the exact lock location
                        Location lockLocation = originalLocation.clone();
                        lockLocation.setYaw(originalYaw);
                        lockLocation.setPitch(originalPitch);

                        // Force teleport immediately
                        player.teleport(lockLocation);

                        // Rate-limited debug logging (only once every 40 ticks = 2 seconds)
                        if (debug && tickCounter % 40 == 0) {
                            Loadscreens.getInstance().getLogger().info("Position lock correction for " + player.getName() +
                                    " - Target Yaw: " + originalYaw + ", Target Pitch: " + originalPitch);
                        }
                    }
                }
            };
            positionLockTask.runTaskTimer(Loadscreens.getInstance(), 0L, 1L);
        }

        // Helper to create display in front of player's current view
        private void createDisplayInFrontOfPlayer() {
            Location base = player.getLocation();
            Vector direction = base.getDirection().normalize();
            double distance = zOffset > 0 ? zOffset : 3.0;
            Location displayLoc = base.clone().add(direction.multiply(distance));
            displayLoc.add(xOffset, yOffset, 0);

            display = player.getWorld().spawn(displayLoc, TextDisplay.class, e -> {
                // Handle negative scale
                float finalScale = (float) scale;
                if (!allowNegativeScale && finalScale < 0) {
                    finalScale = Math.abs(finalScale);
                }

                // Create rotation quaternion
                Quaternionf rotation = new Quaternionf();
                if (rotationX != 0 || rotationY != 0 || rotationZ != 0) {
                    rotation.rotateXYZ(
                            (float) Math.toRadians(rotationX),
                            (float) Math.toRadians(rotationY),
                            (float) Math.toRadians(rotationZ)
                    );
                }

                e.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        rotation,
                        new Vector3f(finalScale, finalScale, finalScale),
                        new Quaternionf()
                ));

                e.setBillboard(billboard);
                e.setSeeThrough(!preventInteraction);
                e.setShadowed(false);
                e.setLineWidth(400);
                e.setDefaultBackground(false);

                // Set initial opacity for fade in
                byte initialOpacity = (byte) (fadeInDuration > 0 ? fadeInOpacityStart : opacity);
                e.setTextOpacity(initialOpacity);

                // Set glow color if overridden
                if (glowOverride) {
                    e.setGlowColorOverride(org.bukkit.Color.fromARGB(glowA, glowR, glowG, glowB));
                }

                // Set initial frame
                updateDisplayText(e, parsedFrames.get(0), 0, 0);
            });

            // Make display only visible to this player
            for (Player other : player.getWorld().getPlayers()) {
                if (!other.equals(player)) {
                    other.hideEntity(Loadscreens.getInstance(), display);
                }
            }
        }

        private void startAnimation() {
            animationTask = new BukkitRunnable() {
                int currentFrame = 0;
                int totalTicks = 0;

                @Override
                public void run() {
                    if (display == null || !display.isValid() || !player.isOnline()) {
                        // Use LoadscreenManager.stopLoadscreen to properly clean up
                        LoadscreenManager.stopLoadscreen(player);
                        return;
                    }

                    // Check if animation is complete
                    if (totalTicks >= duration) {
                        // Use LoadscreenManager.stopLoadscreen to properly clean up
                        LoadscreenManager.stopLoadscreen(player);
                        return;
                    }

                    // Update frame
                    String frameText = parsedFrames.get(currentFrame % parsedFrames.size());
                    updateDisplayText(display, frameText, currentFrame, totalTicks);

                    // Apply fade effects
                    applyFadeEffects(totalTicks);

                    // Play sound if enabled
                    if (playSound && soundPerFrame) {
                        player.playSound(player.getLocation(), sound, soundVolume, soundPitch);
                    }

                    // Move to next frame
                    currentFrame++;
                    totalTicks += frameInterval;
                }
            };

            animationTask.runTaskTimer(Loadscreens.getInstance(), 0, frameInterval);

            // Play initial sound
            if (playSound && !soundPerFrame) {
                player.playSound(player.getLocation(), sound, soundVolume, soundPitch);
            }
        }

        // Ensure these methods exist in LoadscreenSession
        private void updateDisplayText(TextDisplay display, String frameChar, int frameIndex, int totalTicks) {
            Component textComponent;
            if (typewriterEffect) {
                int revealedChars = Math.min(frameChar.length(), (totalTicks / frameInterval) * typewriterSpeed);
                if (revealedChars > 0) {
                    frameChar = frameChar.substring(0, revealedChars);
                } else {
                    frameChar = "";
                }
            }
            if (!font.equals("minecraft:default")) {
                textComponent = Component.text(frameChar).font(Key.key(font));
            } else {
                textComponent = Component.text(frameChar);
            }
            if (rainbowText) {
                float hue = (float) ((totalTicks * rainbowSpeed) % 1.0);
                int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
                textComponent = textComponent.color(TextColor.color(rgb));
            }
            display.text(textComponent);
            applyVisualEffects(display, frameIndex, totalTicks);
        }

        private void applyFadeEffects(int totalTicks) {
            if (!fadeSmooth || display == null) return;
            int currentOpacity = opacity;
            if (totalTicks < fadeInDuration) {
                if (fadeSmooth) {
                    double fadeProgress = (double) totalTicks / fadeInDuration;
                    currentOpacity = (int) (fadeInOpacityStart + (opacity - fadeInOpacityStart) * fadeProgress);
                }
            } else if (totalTicks >= duration - fadeOutDuration) {
                if (fadeSmooth) {
                    int fadeOutStart = duration - fadeOutDuration;
                    double fadeProgress = (double) (totalTicks - fadeOutStart) / fadeOutDuration;
                    currentOpacity = (int) (opacity - (opacity - fadeOutOpacityEnd) * fadeProgress);
                }
            }
            currentOpacity = Math.max(0, Math.min(255, currentOpacity));
            display.setTextOpacity((byte) currentOpacity);
            if (debug && totalTicks % 20 == 0) {
                Loadscreens.getInstance().getLogger().info("Fade opacity for " + player.getName() + ": " + currentOpacity);
            }
        }

        // Add missing applyVisualEffects method
        private void applyVisualEffects(TextDisplay display, int frameIndex, int totalTicks) {
            if (!pulseEffect && !wobbleEffect) return;

            Transformation current = display.getTransformation();
            Vector3f scale = new Vector3f(current.getScale());
            Vector3f translation = new Vector3f(current.getTranslation());

            // Pulse effect
            if (pulseEffect) {
                double pulseMultiplier = 1.0 + Math.sin(totalTicks * pulseSpeed) * pulseIntensity;
                scale.mul((float) pulseMultiplier);
            }

            // Wobble effect
            if (wobbleEffect) {
                float wobbleX = (float) (Math.sin(totalTicks * wobbleSpeed) * wobbleIntensity);
                float wobbleY = (float) (Math.cos(totalTicks * wobbleSpeed * 1.3) * wobbleIntensity);
                translation.add(wobbleX, wobbleY, 0);
            }

            display.setTransformation(new Transformation(
                    translation,
                    current.getLeftRotation(),
                    scale,
                    current.getRightRotation()
            ));
        }

        public void stop() {
            // Cancel animation task
            if (animationTask != null) {
                animationTask.cancel();
                animationTask = null;
            }

            // Cancel position lock task
            if (positionLockTask != null) {
                positionLockTask.cancel();
                positionLockTask = null;
            }

            // Remove display
            if (display != null) {
                display.remove();
                display = null;
            }

            // Always restore player state and log
            restorePlayerState();
            if (debug) {
                Loadscreens.getInstance().getLogger().info("Stopped loadscreen '" + type + "' for " + player.getName());
            }
        }

        private void restorePlayerState() {
            // FIRST: Restore player position to prevent ground-clipping
            if (originalLocation != null && player.isOnline()) {
                // Find a safe location near the original position
                Location safeLocation = findSafeLocation(originalLocation);

                // Restore original rotation
                safeLocation.setYaw(originalYaw);
                safeLocation.setPitch(originalPitch);

                // Teleport player to safe location
                player.teleport(safeLocation);

                if (debug) {
                    Loadscreens.getInstance().getLogger().info("Restored " + player.getName() +
                            " to safe location: " + safeLocation.getBlockX() + ", " + safeLocation.getBlockY() +
                            ", " + safeLocation.getBlockZ());
                }
            }

            // Remove ALL potion effects that might have been applied
            player.removePotionEffect(PotionEffectType.SLOWNESS);
            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.removePotionEffect(PotionEffectType.BLINDNESS);

            // Restore original player state instead of hardcoded values
            if (originalGameMode != null) {
                player.setGameMode(originalGameMode);
            }
            player.setWalkSpeed(originalWalkSpeed);
            player.setFlySpeed(originalFlySpeed);
            player.setAllowFlight(originalAllowFlight);

            if (debug) {
                Loadscreens.getInstance().getLogger().info("Restored original state for " + player.getName() +
                    " - GameMode: " + originalGameMode + ", WalkSpeed: " + originalWalkSpeed +
                    ", FlySpeed: " + originalFlySpeed + ", AllowFlight: " + originalAllowFlight);
            }

            // Unblock packets
            if (Loadscreens.getInstance().isPacketEventsEnabled()) {
                Loadscreens.getInstance().getPacketManager().unblockPackets(player);
            }

            // Clear velocity AFTER teleporting
            player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
        }

        private Location findSafeLocation(Location location) {
            // First, try the original location
            if (isSafeLocation(location)) {
                return location;
            }

            // If not safe, try to find a safe location nearby
            Location safeLoc = location.clone();

            // Try moving up to find solid ground
            for (int y = 0; y < 10; y++) {
                safeLoc.setY(location.getY() + y);
                if (isSafeLocation(safeLoc)) {
                    return safeLoc;
                }
            }

            // Try moving down to find solid ground
            for (int y = 1; y < 10; y++) {
                safeLoc.setY(location.getY() - y);
                if (isSafeLocation(safeLoc)) {
                    return safeLoc;
                }
            }

            // If still not found, use world spawn as fallback
            Location worldSpawn = location.getWorld().getSpawnLocation();
            if (isSafeLocation(worldSpawn)) {
                return worldSpawn;
            }

            // Last resort: return original location
            return location;
        }

        private boolean isSafeLocation(Location location) {
            if (location.getWorld() == null) {
                return false;
            }

            // Check if the block below is solid
            Location below = location.clone().subtract(0, 1, 0);
            if (below.getBlock().getType().isAir()) {
                return false;
            }

            // Check if the current block and the block above are not solid (so player can stand)
            if (!location.getBlock().getType().isAir() || !location.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                return false;
            }

            // Check for dangerous blocks (lava, fire, etc.)
            String blockType = below.getBlock().getType().toString();
            if (blockType.contains("LAVA") || blockType.contains("FIRE") || blockType.contains("MAGMA")) {
                return false;
            }

            return true;
        }
    }
}
