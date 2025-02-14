package com.github.rol;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class Vampire extends Abilities {
    private final Rol plugin; // Reference to the main plugin class
    private final Player player; // Reference to the player associated with this ability
    private final Color vampireRed = Color.fromRGB(139, 0, 0); // Color for vampire particle effects
    private static long cooldownSeconds; // Cooldown duration for teleport ability
    private static double teleportDistance; // Distance to teleport
    private static double particleDensity; // Density of particles spawned during teleport
    private static int particleCount; // Number of particles to spawn

    // Constructor that initializes the Vampire ability for a player
    public Vampire(Rol plugin, Player player) {
        this.plugin = plugin; // Initialize plugin reference
        this.player = player; // Initialize player reference
        loadConfig(); // Load configuration settings for the ability
    }

    // Method to load configuration settings from the plugin's config file
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig(); // Get the configuration file
        cooldownSeconds = config.getLong("vampire.teleport.cooldown"); // Load cooldown duration for teleport
        teleportDistance = config.getDouble("vampire.teleport.distance"); // Load teleport distance from config
        particleDensity = config.getDouble("vampire.teleport.particleDensity"); // Load particle density from config
        particleCount = config.getInt("vampire.teleport.particleCount"); // Load number of particles from config
    }

    // Method to activate the Vampire's teleport ability
    public void activateVampireAbility() {
        UUID playerId = player.getUniqueId(); // Get the player's unique ID

        if (isOnCooldown(playerId)) { // Check if the player is on cooldown
            long remainingCooldown = getRemainingCooldown(playerId); // Get remaining cooldown time
            player.sendMessage("[Rol] Vampire Teleport Cooldown: " + remainingCooldown); // Inform player of cooldown
            return; // Exit method if on cooldown
        }

        Location originalLocation = player.getLocation(); // Store original location of the player
        Vector direction = originalLocation.getDirection(); // Get the direction in which the player is looking

        // Calculate the new teleport location based on direction and distance
        Location teleportLocation = originalLocation.clone().add(direction.multiply(teleportDistance));
        
        Block targetBlock = teleportLocation.getBlock(); // Get block at teleport location
        Block aboveTargetBlock = teleportLocation.clone().add(0, 1, 0).getBlock(); // Get block above the target block

        if (!targetBlock.isPassable() || !aboveTargetBlock.isPassable()) { 
            return; // Exit if target or above block is not passable (cannot teleport)
        }

        player.teleport(teleportLocation); // Teleport the player to the new location

        new BukkitRunnable() {
            @Override
            public void run() {
                player.setRotation(player.getLocation().getYaw() + 180, player.getLocation().getPitch()); 
                // Rotate player to face opposite direction after teleporting
            }
        }.runTaskLater(plugin, 1L); // Schedule rotation adjustment for later execution

        spawnRedstoneDustParticles(originalLocation, teleportLocation); // Spawn particles during teleportation
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f); 
        // Play sound effect for vampire teleportation
        
        startCooldown(playerId, cooldownSeconds); // Start cooldown for this ability for the player
    }

    // Method to spawn redstone dust particles between two locations during teleportation
    private void spawnRedstoneDustParticles(Location startLocation, Location endLocation) {
        Vector direction = endLocation.toVector().subtract(startLocation.toVector()).normalize(); 
        double distance = startLocation.distance(endLocation); 

        new BukkitRunnable() {
            double travelled = 0; // Distance travelled by particles
            
            @Override
            public void run() {
                if (travelled > distance) { 
                    cancel(); // Stop running if all particles have been spawned
                    return;
                }
                
                Location particleLocation = startLocation.clone().add(direction.clone().multiply(travelled)); 
                DustOptions dustOptions = new DustOptions(vampireRed, 1.0f); 
                player.getWorld().spawnParticle(Particle.DUST, particleLocation, particleCount, 0, 0, 0, dustOptions); 
                travelled += particleDensity; // Increment distance travelled by particle density value
            }
        }.runTaskTimer(plugin, 0, 1); // Schedule particle spawning task with a delay of 1 tick between each spawn
    }

    // Method to apply effects specific to Vampires based on time of day
    public void applyVampireEffects() {
        boolean isNight = isNightTime(player); // Check if it is currently night time

        applyPotionEffect(PotionEffectType.NIGHT_VISION, "vampire.nightVision", isNight); 
        applyPotionEffect(PotionEffectType.REGENERATION, "vampire.regeneration", isNight); 
        applyPotionEffect(PotionEffectType.STRENGTH, "vampire.strength", isNight); 

        double maxHealth = plugin.getConfig().getDouble("vampire.maxHealth"); 
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
