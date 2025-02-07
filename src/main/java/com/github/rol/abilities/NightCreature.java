// NightCreatureAbility.java
package com.github.rol.abilities;

import com.github.rol.Rol;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Implements the night creature's abilities, including a fireball and night-time effects.
 */
public class NightCreature {

    private final Rol plugin;
    private final Player player;
    private long cooldownEnd = 0; // Stores the timestamp when the fireball ability will be available again.

    /**
     * Constructor for the NightCreature class.
     *
     * @param plugin The main plugin instance. Used to access configuration.
     * @param player The player using the ability. Represents the player who is a night creature.
     */
    public NightCreature(Rol plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    /**
     * Activates the fireball ability for the night creature, launching a fireball in the player's direction.
     * Includes cooldown management to prevent rapid firing.
     */
    public void activateNightCreatureAbility() {
        FileConfiguration config = plugin.getConfig();
        long cooldownSeconds = config.getLong("nightcreature.fireball.cooldown");

        // Check cooldown
        if (System.currentTimeMillis() < cooldownEnd) {
            long timeLeft = (cooldownEnd - System.currentTimeMillis()) / 1000;
            player.sendMessage("[Rol] Fireball ability is on cooldown. " + timeLeft + " seconds remaining.");
            return; // Exit the method if the ability is on cooldown.
        }

        // Launch fireball
        Fireball fireball = player.getWorld().spawn(player.getLocation().add(0, 1.5, 0), Fireball.class);
        fireball.setShooter(player);
        Vector direction = player.getLocation().getDirection();
        fireball.setVelocity(direction.multiply(1.5));

        // Set cooldown
        cooldownEnd = System.currentTimeMillis() + (cooldownSeconds * 1000);
    }

    /**
     * Applies night creature effects to the player, such as night vision and strength, but only during the night.
     * Also sets the player's maximum health.
     */
    public void applyNightCreatureEffects() {
        FileConfiguration config = plugin.getConfig();

        // Night Vision (only at night)
        boolean nightVisionEnabled = config.getBoolean("nightcreature.nightVision.enabled");
        if (nightVisionEnabled && isNightTime(player)) {
            applyPotionEffect(player, PotionEffectType.NIGHT_VISION, 1, 255); // Night vision for the night creature
        } else {
            removePotionEffect(player, PotionEffectType.NIGHT_VISION); // Remove night vision during the day
        }

        // Strength (only at night)
        boolean strengthEnabled = config.getBoolean("nightcreature.strength.enabled");
        if (strengthEnabled && isNightTime(player)) {
            applyPotionEffect(player, PotionEffectType.STRENGTH, 1, 255); // Strength for the night creature
        } else {
            removePotionEffect(player, PotionEffectType.STRENGTH); // Remove strength during the day
        }

        // Max Health
        double maxHealth = config.getDouble("nightcreature.maxHealth");
        player.setHealthScale(maxHealth);
        player.setHealth(Math.min(player.getHealth(), maxHealth)); // Ensure current health doesn't exceed the new max.
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

    /**
     * Applies a potion effect to the player.
     *
     * @param player The player to apply the effect to.
     * @param type The type of potion effect.
     * @param amplifier The amplifier of the effect (0 for basic, 1 for stronger, etc.).
     * @param duration The duration of the effect in ticks (20 ticks = 1 second). Set to a large value (e.g., 3600, for 3 minutes)
     */
    private void applyPotionEffect(Player player, PotionEffectType type, int amplifier, int duration) {
        player.addPotionEffect(new PotionEffect(type, duration, amplifier, false, false));
    }

    /**
     * Removes a potion effect from the player.
     *
     * @param player The player to remove the effect from.
     * @param type The type of potion effect to remove.
     */
    private void removePotionEffect(Player player, PotionEffectType type) {
        player.removePotionEffect(type);
    }
}
