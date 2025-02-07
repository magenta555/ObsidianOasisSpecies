// VampireAbility.java
package com.github.rol.abilities;

import com.github.rol.Rol;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Implements the vampire's teleport and passive abilities.
 */
public class Vampire {

    private final Rol plugin;
    private final Player player;
    private long cooldownEnd = 0;
    private final Color vampireRed = Color.fromRGB(139, 0, 0); // Dark red for vampire feel

    /**
     * Constructor for the Vampire class.
     *
     * @param plugin The main plugin instance.
     * @param player The player using the ability.
     */
    public Vampire(Rol plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    /**
     * Activates the vampire's teleport ability.
     */
    public void activateVampireAbility() {
        FileConfiguration config = plugin.getConfig();
        long cooldownSeconds = config.getLong("vampire.teleport.cooldown", 20);

        // Check cooldown
        if (System.currentTimeMillis() < cooldownEnd) {
            long timeLeft = (cooldownEnd - System.currentTimeMillis()) / 1000;
            player.sendMessage("[Rol] Teleport ability is on cooldown. " + timeLeft + " seconds remaining.");
            return;
        }

        // Calculate teleport location
        double teleportDistance = config.getDouble("vampire.teleport.distance", 5.0);
        Location originalLocation = player.getLocation();
        Vector direction = originalLocation.getDirection();
        Location teleportLocation = originalLocation.clone().add(direction.multiply(teleportDistance));

        // Safety check: Ensure the destination has air
        Block targetBlock = teleportLocation.getBlock();
        Block aboveTargetBlock = teleportLocation.clone().add(0, 1, 0).getBlock(); // Check block above

        if (!targetBlock.isPassable() || !aboveTargetBlock.isPassable()) {
            player.sendMessage("[Rol] Teleport destination is not safe.");
            return;
        }

        // Teleport the player
        player.teleport(teleportLocation);

        // Optional: Rotate the player *after* teleport
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setRotation(player.getLocation().getYaw() + 180, player.getLocation().getPitch());
            }
        }.runTaskLater(plugin, 1L); // Slight delay to ensure correct location

        // Spawn particles at both locations
        spawnRedstoneDustParticles(originalLocation, teleportLocation);

        // Play sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);

        // Set cooldown
        cooldownEnd = System.currentTimeMillis() + (cooldownSeconds * 1000);
    }

    /**
     * Spawns redstone dust particles between two locations.
     *
     * @param startLocation The starting location.
     * @param endLocation   The ending location.
     */
    private void spawnRedstoneDustParticles(Location startLocation, Location endLocation) {
        FileConfiguration config = plugin.getConfig();
        double particleDensity = config.getDouble("vampire.teleport.particleDensity", 0.5);
        int particleCount = config.getInt("vampire.teleport.particleCount", 10); //Configurable particle count

        Vector direction = endLocation.toVector().subtract(startLocation.toVector()).normalize();
        double distance = startLocation.distance(endLocation);

        new BukkitRunnable() {
            double travelled = 0;

            @Override
            public void run() {
                if (travelled > distance) {
                    cancel();
                    return;
                }

                Location particleLocation = startLocation.clone().add(direction.clone().multiply(travelled));
                DustOptions dustOptions = new DustOptions(vampireRed, 1.0f); // Use pre-defined color

                player.getWorld().spawnParticle(Particle.DUST, particleLocation, particleCount, 0, 0, 0, dustOptions); // Use REDSTONE with DustOptions
                travelled += particleDensity;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Applies vampire-specific potion effects based on the time of day.
     */
    public void applyVampireEffects() {
        FileConfiguration config = plugin.getConfig();

        boolean isNight = isNightTime(player);

        // Night Vision
        applyPotionEffect(PotionEffectType.NIGHT_VISION, "vampire.nightVision", isNight);

        // Regeneration
        applyPotionEffect(PotionEffectType.REGENERATION, "vampire.regeneration", isNight);

        // Strength
        applyPotionEffect(PotionEffectType.STRENGTH, "vampire.strength", isNight);

        // Max Health (Health boost is better)
         applyMaxHealth(config.getDouble("vampire.maxHealth", 40));
    }

    private void applyPotionEffect(PotionEffectType effectType, String configPath, boolean isNight) {
        FileConfiguration config = plugin.getConfig();
        boolean enabled = config.getBoolean(configPath + ".enabled");
        int amplifier = config.getInt(configPath + ".amplifier"); //default amplifier

        if (enabled && isNight) {
            PotionEffect nightEffect = new PotionEffect(effectType, Integer.MAX_VALUE, amplifier, false, false, true);
            player.addPotionEffect(nightEffect);
        } else {
            //remove effect if it exists
            player.removePotionEffect(effectType);
        }
    }


    /**
     * Applies max health and ensures player's health is within bounds
     */
    public void applyMaxHealth(double maxHealth) {
        double healthScale = maxHealth;

        //Set the players health scale
        player.setHealthScale(healthScale);

        //Set the players health, and ensure the players health is not above the max health scale
         player.setHealth(Math.min(player.getHealth(), healthScale));
    }

    /**
     * Checks if it's night time in the player's world.
     *
     * @param player The player.
     * @return True if it's night time, false otherwise.
     */
    private boolean isNightTime(Player player) {
        long time = player.getWorld().getTime();
        return time > 12300 && time < 23850;
    }
}
