package com.github.rol;

import com.github.rol.*;
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

    private final Rol plugin;
    private final Player player;
    private final Color vampireRed = Color.fromRGB(139, 0, 0);

    private static long cooldownSeconds;
    private static double teleportDistance;
    private static double particleDensity;
    private static int particleCount;

    public Vampire(Rol plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        cooldownSeconds = config.getLong("vampire.teleport.cooldown");
        teleportDistance = config.getDouble("vampire.teleport.distance");
        particleDensity = config.getDouble("vampire.teleport.particleDensity");
        particleCount = config.getInt("vampire.teleport.particleCount");
    }

    public void activateVampireAbility() {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId)) {
            long remainingCooldown = getRemainingCooldown(playerId);
            player.sendMessage("[Rol] Vampire Teleport Cooldown: " + remainingCooldown);
            return;
        }

        Location originalLocation = player.getLocation();
        Vector direction = originalLocation.getDirection();
        Location teleportLocation = originalLocation.clone().add(direction.multiply(teleportDistance));

        Block targetBlock = teleportLocation.getBlock();
        Block aboveTargetBlock = teleportLocation.clone().add(0, 1, 0).getBlock();

        if (!targetBlock.isPassable() || !aboveTargetBlock.isPassable()) {
            return;
        }

        player.teleport(teleportLocation);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.setRotation(player.getLocation().getYaw() + 180, player.getLocation().getPitch());
            }
        }.runTaskLater(plugin, 1L);

        spawnRedstoneDustParticles(originalLocation, teleportLocation);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);

        startCooldown(playerId, cooldownSeconds);
    }

    private void spawnRedstoneDustParticles(Location startLocation, Location endLocation) {
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
                DustOptions dustOptions = new DustOptions(vampireRed, 1.0f);

                player.getWorld().spawnParticle(Particle.DUST, particleLocation, particleCount, 0, 0, 0, dustOptions);
                travelled += particleDensity;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    public void applyVampireEffects() {
        boolean isNight = isNightTime(player);

        applyPotionEffect(PotionEffectType.NIGHT_VISION, "vampire.nightVision", isNight);

        applyPotionEffect(PotionEffectType.REGENERATION, "vampire.regeneration", isNight);

        applyPotionEffect(PotionEffectType.STRENGTH, "vampire.strength", isNight);

        double maxHealth = plugin.getConfig().getDouble("vampire.maxHealth");
        applyMaxHealth(maxHealth);
    }

    @Override
    protected FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    @Override
    protected Player getPlayer() {
        return player;
    }
}