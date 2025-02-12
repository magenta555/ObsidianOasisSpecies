package com.github.rol;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Abilities {

    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    protected abstract FileConfiguration getConfig();

    protected abstract Player getPlayer();

    protected void startCooldown(UUID playerId, long cooldownSeconds) {
        cooldowns.put(playerId, System.currentTimeMillis() + (cooldownSeconds * 1000));
    }

    protected boolean isOnCooldown(UUID playerId) {
        return cooldowns.containsKey(playerId) && cooldowns.get(playerId) > System.currentTimeMillis();
    }

    protected long getRemainingCooldown(UUID playerId) {
        if (cooldowns.containsKey(playerId)) {
            long endTime = cooldowns.get(playerId);
            long timeLeft = (endTime - System.currentTimeMillis()) / 1000;
            return Math.max(0, timeLeft);
        }
        return 0;
    }

     protected void applyPotionEffect(PotionEffectType effectType, String configPath, boolean isNight) {
        FileConfiguration config = getConfig();
        boolean enabled = config.getBoolean(configPath + ".enabled", true);
        int amplifier = config.getInt(configPath + ".amplifier");

        if (enabled && isNight) {
            PotionEffect nightEffect = new PotionEffect(effectType, 11 * 20, amplifier, false, false, true);
            getPlayer().addPotionEffect(nightEffect);
        } else {
            getPlayer().removePotionEffect(effectType);
        }
    }

    protected boolean isNightTime(Player player) {
        long time = player.getWorld().getTime();
        return time > 12300 && time < 23850;
    }

     protected void applyMaxHealth(double maxHealth) {
        Player player = getPlayer();
        player.setHealthScale(maxHealth);
        player.setHealth(Math.min(player.getHealth(), maxHealth));
    }
}
