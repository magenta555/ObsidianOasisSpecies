// NightCreatureAbility.java
package com.github.rol.abilities;

import com.github.rol.Rol;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Implements the night creature's fireball ability.
 */
public class NightCreature {

    private final Rol plugin;
    private final Player player;
    private long cooldownEnd = 0;

    /**
     * Constructor for the NightCreatureAbility class.
     *
     * @param plugin The main plugin instance.
     * @param player The player using the ability.
     */
    public NightCreature(Rol plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    /**
     * Activates the fireball ability for the night creature.
     */
    @SuppressWarnings("deprecation")
    public void activateNightCreatureAbility() {
        FileConfiguration config = plugin.getConfig();
        long cooldownSeconds = config.getLong("nightcreature.fireball.cooldown", 20);

        // Check cooldown
        if (System.currentTimeMillis() < cooldownEnd) {
            long timeLeft = (cooldownEnd - System.currentTimeMillis()) / 1000;
            player.sendMessage(ChatColor.LIGHT_PURPLE + "[Rol] Fireball ability is on cooldown. " + timeLeft + " seconds remaining.");
            return;
        }

        // Launch fireball
        Fireball fireball = player.getWorld().spawn(player.getLocation().add(0, 1.5, 0), Fireball.class);
        fireball.setShooter(player);
        Vector direction = player.getLocation().getDirection();
        fireball.setVelocity(direction.multiply(1.5));

        // Set cooldown
        cooldownEnd = System.currentTimeMillis() + (cooldownSeconds * 1000);
    }

    public void applyNightCreatureEffects() {
        FileConfiguration config = plugin.getConfig();

        // Night Vision (only at night)
        boolean nightVisionEnabled = config.getBoolean("nightcreature.nightVision.enabled", true);
        if (nightVisionEnabled && isNightTime(player)) {
            // Apply the night vision effect here
            // You can use PotionEffect or custom methods
        }

        // Strength (only at night)
        boolean strengthEnabled = config.getBoolean("nightcreature.strength.enabled", true);
        if (strengthEnabled && isNightTime(player)) {
            // Apply the strength effect here
            // You can use PotionEffect or custom methods
        }

        // Max Health
        double maxHealth = config.getDouble("nightcreature.maxHealth", 30); // Default to 30 hearts
        player.setHealthScale(maxHealth);
        player.setHealth(Math.min(player.getHealth(), maxHealth)); // Ensure current health doesn't exceed the max
    }

    private boolean isNightTime(Player player) {
        long time = player.getWorld().getTime();
        return time > 12300 && time < 23850;
    }
}
