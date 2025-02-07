package com.github.rol.abilities;

import com.github.rol.Rol;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NightCreature {

    private final Rol plugin;
    private final Player player;

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static long cooldownSeconds;

    public NightCreature(Rol plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        cooldownSeconds = config.getLong("nightcreature.fireball.cooldown", 20);
    }

    public void activateNightCreatureAbility() {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId)) {
            long timeLeft = getRemainingCooldown(playerId);
            player.sendMessage("[Rol] Fireball ability is on cooldown. " + timeLeft + " seconds remaining.");
            return;
        }

        SmallFireball fireball = player.getWorld().spawn(player.getLocation().add(0, 1.5, 0), SmallFireball.class);
        fireball.setShooter(player);
        Vector direction = player.getLocation().getDirection();
        fireball.setVelocity(direction.multiply(1.5));

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

    public void applyNightCreatureEffects() {
        FileConfiguration config = plugin.getConfig();

        boolean isNight = isNightTime(player);

        applyPotionEffect(PotionEffectType.NIGHT_VISION, "nightcreature.nightVision", isNight);

        applyPotionEffect(PotionEffectType.STRENGTH, "nightcreature.strength", isNight);

        double maxHealth = config.getDouble("nightcreature.maxHealth");
        player.setHealthScale(maxHealth);
        player.setHealth(Math.min(player.getHealth(), maxHealth));
    }

    private void applyPotionEffect(PotionEffectType effectType, String configPath, boolean isNight) {
        FileConfiguration config = plugin.getConfig();
        boolean enabled = config.getBoolean(configPath + ".enabled", true);
        int amplifier = config.getInt(configPath + ".amplifier");

        if (enabled && isNight) {
            PotionEffect nightEffect = new PotionEffect(effectType, 200, amplifier, false, false, true);
            player.addPotionEffect(nightEffect);
        } else {
            player.removePotionEffect(effectType);
        }
    }

    private boolean isNightTime(Player player) {
        long time = player.getWorld().getTime();
        return time > 12300 && time < 23850;
    }

}
