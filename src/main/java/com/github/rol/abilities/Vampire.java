// VampireAbility.java
package com.github.rol.abilities;

import com.github.rol.Rol;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Implements the vampire's teleport ability.
 */
public class Vampire {

    private final Rol plugin;
    private final Player player;
    private long cooldownEnd = 0;

    /**
     * Constructor for the VampireAbility class.
     *
     * @param plugin The main plugin instance.
     * @param player The player using the ability.
     */
    public Vampire(Rol plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    /**
     * Activates the teleport ability for the vampire.
     */
    public void activateVampireAbility() {
        FileConfiguration config = plugin.getConfig();
        long cooldownSeconds = config.getLong("vampire.teleport.cooldown", 20);

        // Check cooldown
        if (System.currentTimeMillis() < cooldownEnd) {
            long timeLeft = (cooldownEnd - System.currentTimeMillis()) / 1000;
            player.sendMessage(org.bukkit.ChatColor.LIGHT_PURPLE + "[Rol] Teleport ability is on cooldown. " + timeLeft + " seconds remaining.");
            return;
        }

        // Calculate teleport location
        double teleportDistance = config.getDouble("vampire.teleport.distance", 5.0);
        Location originalLocation = player.getLocation();
        Vector direction = originalLocation.getDirection();
        Location teleportLocation = originalLocation.add(direction.multiply(teleportDistance));

        // Safety check: Ensure the destination has air
        Block targetBlock = teleportLocation.getBlock();
        Block aboveTargetBlock = teleportLocation.clone().add(0, 1, 0).getBlock(); // Check block above
        if (!targetBlock.isPassable() || !aboveTargetBlock.isPassable()) {
            player.sendMessage(org.bukkit.ChatColor.LIGHT_PURPLE + "[Rol] Teleport destination is not safe.");
            return;
        }

        // Teleport the player
        player.teleport(teleportLocation);
        player.setRotation(player.getLocation().getYaw() + 180, player.getLocation().getPitch());

        // Spawn particles
        spawnRedstoneDustParticles(originalLocation);

        // Play sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);

        // Set cooldown
        cooldownEnd = System.currentTimeMillis() + (cooldownSeconds * 1000);
    }

    /**
     * Spawns redstone dust particles along the teleport path.
     *
     * @param startLocation The location where the teleport starts.
     */
    private void spawnRedstoneDustParticles(Location startLocation) {
        FileConfiguration config = plugin.getConfig();
        double particleDensity = config.getDouble("vampire.teleport.particleDensity", 0.5);

        // Calculate the vector between start and end location
        Location endLocation = player.getLocation();
        Vector direction = endLocation.toVector().subtract(startLocation.toVector());
        double distance = direction.length();
        direction.normalize();

        // Spawn particles along the path
        new BukkitRunnable() {
            double travelled = 0;

            @Override
            public void run() {
                if (travelled > distance) {
                    cancel();
                    return;
                }

                Location particleLocation = startLocation.clone().add(direction.clone().multiply(travelled));
                player.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 5, 0, 0, 0, 1);
                travelled += particleDensity;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    public void applyVampireEffects() {
        FileConfiguration config = plugin.getConfig();

        // Night Vision (only at night)
        boolean nightVisionEnabled = config.getBoolean("vampire.nightVision.enabled", true);
        if (nightVisionEnabled && isNightTime(player)) {
            // Apply the night vision effect here
            // You can use PotionEffect or custom methods
        }

        // Regeneration (only at night)
        boolean regenerationEnabled = config.getBoolean("vampire.regeneration.enabled", true);
        if (regenerationEnabled && isNightTime(player)) {
            // Apply the regeneration effect here
            // You can use PotionEffect or custom methods
        }

        // Strength (only at night)
        boolean strengthEnabled = config.getBoolean("vampire.strength.enabled", true);
        if (strengthEnabled && isNightTime(player)) {
            // Apply the strength effect here
            // You can use PotionEffect or custom methods
        }

        // Max Health
        double maxHealth = config.getDouble("vampire.maxHealth", 40); // Default to 40 hearts
        player.setHealthScale(maxHealth);
        player.setHealth(Math.min(player.getHealth(), maxHealth)); // Ensure current health doesn't exceed the max
    }

    private boolean isNightTime(Player player) {
        long time = player.getWorld().getTime();
        return time > 12300 && time < 23850;
    }
}
