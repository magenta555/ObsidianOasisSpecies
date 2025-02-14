package com.github.rol;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.SmallFireball;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import java.util.UUID;

public class NightCreature extends Abilities {
    private final Rol plugin; // Reference to the main plugin class
    private final Player player; // Reference to the player associated with this ability
    private static double fireballSpeed; // Speed of the fireball
    private static double fireballYield; // Yield of the fireball (explosion power)
    private static boolean fireballIncendiary; // Whether the fireball ignites blocks
    private static long cooldownSeconds; // Cooldown duration for the fireball ability

    // Constructor that initializes the NightCreature ability for a player
    public NightCreature(Rol plugin, Player player) {
        this.plugin = plugin; // Initialize plugin reference
        this.player = player; // Initialize player reference
        loadConfig(); // Load configuration settings for the ability
    }

    // Method to load configuration settings from the plugin's config file
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig(); // Get the configuration file
        cooldownSeconds = config.getLong("nightcreature.fireball.cooldown"); // Load cooldown duration
        fireballSpeed = config.getDouble("nightcreature.fireball.speed"); // Load fireball speed
        fireballYield = config.getDouble("nightcreature.fireball.yield"); // Load fireball yield
        fireballIncendiary = config.getBoolean("nightcreature.fireball.incendiary"); // Load incendiary setting
    }

    // Method to activate the Night Creature's fireball ability
    public void activateNightCreatureAbility() {
        UUID playerId = player.getUniqueId(); // Get the player's unique ID

        if (isOnCooldown(playerId)) { // Check if the player is on cooldown
            long remainingCooldown = getRemainingCooldown(playerId); // Get remaining cooldown time
            player.sendMessage("[Rol] Night Creature Fireball Cooldown: " + remainingCooldown); // Inform player of cooldown
            return; // Exit method if on cooldown
        }

        // Spawn a SmallFireball at the player's location, slightly above their head
        SmallFireball fireball = player.getWorld().spawn(player.getLocation().add(0, 1.5, 0), SmallFireball.class);
        fireball.setShooter(player); // Set the shooter of the fireball to the player

        Vector direction = player.getLocation().getDirection(); // Get the direction in which the player is looking
        fireball.setVelocity(direction.multiply(fireballSpeed)); // Set the velocity of the fireball based on player's direction and speed
        fireball.setYield((float) fireballYield); // Set the yield of the fireball explosion
        fireball.setIsIncendiary(fireballIncendiary); // Set whether the fireball ignites blocks

        startCooldown(playerId, cooldownSeconds); // Start cooldown for this ability for the player
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f); // Play sound effect for shooting a fireball
    }

    // Method to apply effects specific to Night Creatures based on time of day
    public void applyNightCreatureEffects() {
        boolean isNight = isNightTime(player); // Check if it is currently night time

        applyPotionEffect(PotionEffectType.NIGHT_VISION, "nightcreature.nightVision", isNight); // Apply night vision effect if it's night
        applyPotionEffect(PotionEffectType.STRENGTH, "nightcreature.strength", isNight); // Apply strength effect if it's night

        double maxHealth = plugin.getConfig().getDouble("nightcreature.maxHealth"); // Get maximum health from config
        applyMaxHealth(maxHealth); // Apply maximum health setting to the player
    }

    @Override
    protected FileConfiguration getConfig() {
        return plugin.getConfig(); // Return configuration file for abilities
    }

    @Override
    protected Player getPlayer() {
        return player; // Return reference to the associated player
    }
}
