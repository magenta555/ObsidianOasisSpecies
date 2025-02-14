package com.github.rol;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Abilities {
    private static final Map<UUID, Long> cooldowns = new HashMap<>(); // Map to track cooldowns for each player

    // Abstract method to get the configuration file
    protected abstract FileConfiguration getConfig();

    // Abstract method to get the player associated with this ability
    protected abstract Player getPlayer();

    // Method to start a cooldown for a player
    protected void startCooldown(UUID playerId, long cooldownSeconds) {
        cooldowns.put(playerId, System.currentTimeMillis() + (cooldownSeconds * 1000)); // Set the end time for the cooldown
    }

    // Method to check if a player is currently on cooldown
    protected boolean isOnCooldown(UUID playerId) {
        return cooldowns.containsKey(playerId) && cooldowns.get(playerId) > System.currentTimeMillis(); // Return true if still on cooldown
    }

    // Method to get the remaining cooldown time for a player
    protected long getRemainingCooldown(UUID playerId) {
        if (cooldowns.containsKey(playerId)) {
            long endTime = cooldowns.get(playerId); // Get the end time of the cooldown
            long timeLeft = (endTime - System.currentTimeMillis()) / 1000; // Calculate remaining time in seconds
            return Math.max(0, timeLeft); // Return remaining time, ensuring it's not negative
        }
        return 0; // Return 0 if no cooldown exists
    }

    // Method to apply or remove a potion effect based on configuration and time of day
    protected void applyPotionEffect(PotionEffectType effectType, String configPath, boolean isNight) {
        FileConfiguration config = getConfig(); // Get the configuration file
        boolean enabled = config.getBoolean(configPath + ".enabled", true); // Check if the effect is enabled in config
        int amplifier = config.getInt(configPath + ".amplifier"); // Get the amplifier level from config

        if (enabled && isNight) { // Apply effect if enabled and it's night time
            PotionEffect nightEffect = new PotionEffect(effectType, 11 * 20, amplifier, false, false, true); // Create potion effect with duration and amplifier
            getPlayer().addPotionEffect(nightEffect); // Apply the potion effect to the player
        } else {
            getPlayer().removePotionEffect(effectType); // Remove potion effect if conditions are not met
        }
    }

    // Method to
