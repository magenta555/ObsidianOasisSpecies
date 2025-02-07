// NightCreature.java
package com.github.rol.abilities;

import com.github.rol.Rol;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.SmallFireball;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import java.util.UUID;

public class NightCreature extends Abilities {

    private final Rol plugin;
    private final Player player;
    private static double fireballSpeed;
    private static double fireballYield;
    private static boolean fireballIncendiary;
    private static long cooldownSeconds;

    public NightCreature(Rol plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        cooldownSeconds = config.getLong("nightcreature.fireball.cooldown");
        fireballSpeed = config.getDouble("nightcreature.fireball.speed");
        fireballYield = config.getDouble("nightcreature.fireball.yield");
        fireballIncendiary = config.getBoolean("nightcreature.fireball.incendiary");
    }

    public void activateNightCreatureAbility() {
        UUID playerId = player.getUniqueId();

        if (isOnCooldown(playerId)) {
            long remainingCooldown = getRemainingCooldown(playerId);
            player.sendMessage("[Rol] Night Creature Fireball Cooldown: " + remainingCooldown);
            return;
        }

        SmallFireball fireball = player.getWorld().spawn(player.getLocation().add(0, 1.5, 0), SmallFireball.class);
        fireball.setShooter(player);
        Vector direction = player.getLocation().getDirection();
        fireball.setVelocity(direction.multiply(fireballSpeed));
        fireball.setYield((float) fireballYield);
        fireball.setIsIncendiary(fireballIncendiary);

        startCooldown(playerId, cooldownSeconds);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
    }

    public void applyNightCreatureEffects() {
        boolean isNight = isNightTime(player);

        applyPotionEffect(PotionEffectType.NIGHT_VISION, "nightcreature.nightVision", isNight);

        applyPotionEffect(PotionEffectType.STRENGTH, "nightcreature.strength", isNight);

        double maxHealth = plugin.getConfig().getDouble("nightcreature.maxHealth");
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
