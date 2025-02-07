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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Vampire {

    private final Rol plugin;
    private final Player player;
    private final Color vampireRed = Color.fromRGB(139, 0, 0);

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
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
            long timeLeft = getRemainingCooldown(playerId);
            player.sendMessage("[Rol] Ability is on cooldown. " + timeLeft + " seconds remaining.");
            return;
        }

        Location originalLocation = player.getLocation();
        Vector direction = originalLocation.getDirection();
        Location teleportLocation = originalLocation.clone().add(direction.multiply(teleportDistance));

        Block targetBlock = teleportLocation.getBlock();
        Block aboveTargetBlock = teleportLocation.clone().add(0, 1, 0).getBlock();

        if (!targetBlock.isPassable() || !aboveTargetBlock.isPassable()) {
            player.sendMessage("[Rol] Teleport destination is not safe.");
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

        startCooldown(playerId);
    }

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
        FileConfiguration config = plugin.getConfig();

        boolean isNight = isNightTime(player);

        applyPotionEffect(PotionEffectType.NIGHT_VISION, "vampire.nightVision", isNight);

        applyPotionEffect(PotionEffectType.REGENERATION, "vampire.regeneration", isNight);

        applyPotionEffect(PotionEffectType.STRENGTH, "vampire.strength", isNight);

        applyMaxHealth(config.getDouble("vampire.maxHealth"));
    }

    private void applyPotionEffect(PotionEffectType effectType, String configPath, boolean isNight) {
        FileConfiguration config = plugin.getConfig();
        boolean enabled = config.getBoolean(configPath + ".enabled", true);
        int amplifier = config.getInt(configPath + ".amplifier");

        if (enabled && isNight) {
            PotionEffect nightEffect = new PotionEffect(effectType, 20, amplifier, false, false, true);
            player.addPotionEffect(nightEffect);
        } else {
            player.removePotionEffect(effectType);
        }
    }

    public void applyMaxHealth(double maxHealth) {
        double healthScale = maxHealth;

        player.setHealthScale(healthScale);

        player.setHealth(Math.min(player.getHealth(), healthScale));
    }

    private boolean isNightTime(Player player) {
        long time = player.getWorld().getTime();
        return time > 12300 && time < 23850;
    }
}
