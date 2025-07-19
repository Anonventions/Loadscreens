package org.anonventions.loadscreens.util;

import org.anonventions.loadscreens.core.Loadscreens;
import org.anonventions.loadscreens.depends.PlaceholderManager;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.key.Key;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
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

        // Get delay from config or use custom
        int delay = customDelay > 0 ? customDelay : config.getInt("loadscreen_types." + type + ".timer_delay", 0);

        // Schedule the loadscreen
        if (delay > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    showLoadscreenNow(player, type);
                }
            }.runTaskLater(Loadscreens.getInstance(), delay);
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

    // Session class with enhanced features
    private static class LoadscreenSession {
        private final Player player;
        private final List<String> frames;
        private final String type;
        private final String basePath;
        private final boolean debug;

        // Configuration values
        private final int frameInterval, duration, fadeInDuration, fadeOutDuration;
        private final double scale, yOffset, zOffset, xOffset;
        private final int opacity, brightness;
        private final boolean freezePlayer, hideUI, hideChat, preventInteraction, blockPackets;
        private final String font;

        // Visual effects
        private final boolean pulseEffect, wobbleEffect, rainbowText, typewriterEffect;
        private final double pulseIntensity, pulseSpeed, wobbleIntensity, wobbleSpeed, rainbowSpeed;
        private final int typewriterSpeed;

        // Advanced settings
        private final TextDisplay.Billboard billboard;
        private final boolean allowNegativeScale;
        private final double rotationX, rotationY, rotationZ;
        private final boolean glowOverride;
        private final int glowR, glowG, glowB, glowA;

        // Sound settings
        private final boolean playSound, soundPerFrame;
        private final Sound sound;
        private final float soundVolume, soundPitch;

        private TextDisplay display;
        private BukkitRunnable animationTask;
        private List<String> parsedFrames;

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
        }

        public void start() {
            if (frames.isEmpty()) {
                if (debug) Loadscreens.getInstance().getLogger().warning("No frames configured for type: " + type);
                return;
            }

            // Parse placeholders in frames
            parsePlaceholders();

            // Apply player restrictions
            applyPlayerRestrictions();

            // Create display
            createDisplay();

            // Start animation
            startAnimation();

            if (debug) {
                Loadscreens.getInstance().getLogger().info("Started loadscreen '" + type + "' for " + player.getName());
            }
        }

        private void parsePlaceholders() {
            parsedFrames = new java.util.ArrayList<>();

            if (Loadscreens.getInstance().isPlaceholderAPIEnabled()) {
                PlaceholderManager pm = Loadscreens.getInstance().getPlaceholderManager();
                for (String frame : frames) {
                    String parsed = pm.parseBuiltinPlaceholders(player, frame);
                    parsed = pm.parsePlaceholders(player, parsed);
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
            if (freezePlayer) {
                // Freeze player movement
                player.setWalkSpeed(0.0f);
                player.setFlySpeed(0.0f);
            }

            if (blockPackets && Loadscreens.getInstance().isPacketEventsEnabled()) {
                Loadscreens.getInstance().getPacketManager().blockPackets(player);
            }
        }

        private void createDisplay() {
            Location playerLoc = player.getEyeLocation();
            Location displayLoc = playerLoc.clone().add(
                    playerLoc.getDirection().multiply(zOffset)
            ).add(xOffset, yOffset, 0);

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

                // Set opacity and brightness
                if (opacity < 255) {
                    e.setTextOpacity((byte) opacity);
                }

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
                boolean fadingOut = false;

                @Override
                public void run() {
                    if (display == null || !display.isValid() || !player.isOnline()) {
                        stop();
                        return;
                    }

                    // Check if we should start fading out
                    if (!fadingOut && totalTicks >= duration - fadeOutDuration) {
                        fadingOut = true;
                    }

                    // Check if animation is complete
                    if (totalTicks >= duration) {
                        stop();
                        return;
                    }

                    // Update frame
                    String frameText = parsedFrames.get(currentFrame % parsedFrames.size());
                    updateDisplayText(display, frameText, currentFrame, totalTicks);

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

        private void updateDisplayText(TextDisplay display, String frameChar, int frameIndex, int totalTicks) {
            Component textComponent;

            // Apply typewriter effect
            if (typewriterEffect) {
                int revealedChars = Math.min(frameChar.length(), (totalTicks / frameInterval) * typewriterSpeed);
                if (revealedChars > 0) {
                    frameChar = frameChar.substring(0, revealedChars);
                } else {
                    frameChar = "";
                }
            }

            // Create base component
            if (!font.equals("minecraft:default")) {
                textComponent = Component.text(frameChar).font(Key.key(font));
            } else {
                textComponent = Component.text(frameChar);
            }

            // Apply rainbow effect
            if (rainbowText) {
                float hue = (float) ((totalTicks * rainbowSpeed) % 1.0);
                int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
                textComponent = textComponent.color(TextColor.color(rgb));
            }

            display.text(textComponent);

            // Apply visual effects
            applyVisualEffects(display, frameIndex, totalTicks);
        }

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
            // Cancel animation
            if (animationTask != null) {
                animationTask.cancel();
                animationTask = null;
            }

            // Remove display
            if (display != null) {
                display.remove();
                display = null;
            }

            // Restore player state
            restorePlayerState();

            if (debug) {
                Loadscreens.getInstance().getLogger().info("Stopped loadscreen '" + type + "' for " + player.getName());
            }
        }

        private void restorePlayerState() {
            // Restore movement
            if (freezePlayer) {
                player.setWalkSpeed(0.2f);  // Default walk speed
                player.setFlySpeed(0.1f);   // Default fly speed
            }

            // Unblock packets
            if (blockPackets && Loadscreens.getInstance().isPacketEventsEnabled()) {
                Loadscreens.getInstance().getPacketManager().unblockPackets(player);
            }
        }
    }
}