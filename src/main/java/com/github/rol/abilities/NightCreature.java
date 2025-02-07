package com.github.rol.abilities;

import com.github.rol.Rol;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implements the night creature's abilities, including a fireball and night-time effects.
 */
public class NightCreature {

    private final Rol plugin;
    private final Player player;

    // Cooldown handling
    private static final Map<UUID, Long> cooldowns = new HashMap<>(); // Static to persist across instances
    private static long cooldownSeconds; //This is now static

    /**
     * Constructor for the NightCreature class.
     *
     * @param plugin The main plugin instance. Used to access configuration.
     * @param player The player using the ability. Represents the player who is a night creature.
     */
    public NightCreature(Rol plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        loadConfig(); //Load configuration values
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        cooldownSeconds = config.getLong("nightcreature.fireball.cooldown", 20); //default cooldown seconds is 20
    }

    /**
     * Activates the fireball ability for the night creature, launching a fireball in the player's direction.
     * Includes cooldown management to prevent rapid firing.
     */
    public void activateNightCreatureAbility() {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (isOnCooldown(playerId)) {
            long timeLeft = getRemainingCooldown(playerId);
            player.sendMessage("[Rol] Fireball ability is on cooldown. " + timeLeft + " seconds remaining.");
            return; // Exit the method if the ability is on cooldown.
        }

        // Launch fireball
        Fireball fireball = player.getWorld().spawn(player.getLocation().add(0, 1.5, 0), Fireball.class);
        fireball.setShooter(player);
        Vector direction = player.getLocation().getDirection();
        fireball.setVelocity(direction.multiply(1.5));

        // Start cooldown
        startCooldown(playerId);
    }

    // Cooldown methods
    private void startCooldown(UUID playerId) {
        cooldowns.put(playerId, System.currentTimeMillis() + (cooldownSeconds * 1000));
    }

    private boolean isOnCooldown(UUID playerId) {
        return cooldowns.containsKey(playerId) && cooldowns.get(playerId) > System.currentTimeMillis();
    }

    private long getRemainingCooldown(UUID playerId) {
        if (cooldowns.containsKey(playerId)) {
            long endTime = cooldowns.get(playerId);
            long timeLeft = (endTime - System.currentTimeMillis()) / 1000;
            return Math.max(0, timeLeft);
        }
        return 0;
    }

    /**
     * Applies night creature effects to the player, such as night vision and strength, but only during the night.
     * Also sets the player's maximum health.
     */
    public void applyNightCreatureEffects() {
        FileConfiguration config = plugin.getConfig();

        boolean isNight = isNightTime(player);

        // Night Vision (only at night)
        applyPotionEffect(PotionEffectType.NIGHT_VISION, "nightcreature.nightVision", isNight);

        // Strength (only at night)
        applyPotionEffect(PotionEffectType.STRENGTH, "nightcreature.strength", isNight);


        // Max Health
        double maxHealth = config.getDouble("nightcreature.maxHealth");
        player.setHealthScale(maxHealth);
        player.setHealth(Math.min(player.getHealth(), maxHealth)); // Ensure current health doesn't exceed the new max.
    }

    private void applyPotionEffect(PotionEffectType effectType, String configPath, boolean isNight) {
        FileConfiguration config = plugin.getConfig();
        boolean enabled = config.getBoolean(configPath + ".enabled", true);
        int amplifier = config.getInt(configPath + ".amplifier");

        if (enabled && isNight) {
            PotionEffect nightEffect = new PotionEffect(effectType, 2, amplifier, false, false, true);
            player.addPotionEffect(nightEffect);
        } else {
            //remove effect if it exists
            player.removePotionEffect(effectType);
        }
    }

    /**
     * Checks if it is currently night time in the player's world.
     *
     * @param player The player whose world time is being checked.
     * @return True if it is night time; otherwise, false.
     */
    private boolean isNightTime(Player player) {
        long time = player.getWorld().getTime();
        return time > 12300 && time < 23850; // Define night time based on Minecraft's time system
    }

}
