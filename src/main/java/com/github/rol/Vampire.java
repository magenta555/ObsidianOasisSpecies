package com.github.rol;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.bukkit.Color;
import org.bukkit.Particle.DustOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Vampire implements Listener {

    private final Rol plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final int TELEPORT_COOLDOWN = 30; // Seconds
    private final double PARTICLE_DISTANCE = 0.5; // Distance between particles

    public Vampire(Rol plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Action action = event.getAction();

        if (isVampire(player) && isAbilityItem(item) && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            if (isOnCooldown(player)) {
                player.sendMessage("§cYou must wait " + getRemainingCooldown(player) + " seconds before using this ability again.");
                return;
            }
            useVampireAbility(player);
        }
    }

    private boolean isVampire(Player player) {
        String role = plugin.getPlayerRole(player);
        return role != null && role.equals("vampire"); //Prevent null pointer
    }

    private boolean isAbilityItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        FileConfiguration config = plugin.getConfig();
        String configuredItemName = config.getString("rol.vampire.abilityitem").toUpperCase();
        String itemName = item.getType().name().toUpperCase();

        return itemName.contains(configuredItemName);
    }

    private void useVampireAbility(Player player) {
        // 1. Particle Trail
        startParticleTrail(player);

        // 2. Teleport and Rotate (delayed by 1 second)
        new BukkitRunnable() {
            @Override
            public void run() {
                stopParticleTrail(player);
                teleportAndRotatePlayer(player);
                // 3. Damage Immunity (after teleport)
                giveTemporaryDamageImmunity(player, 20); // 20 ticks = 1 second
                startCooldown(player);
            }
        }.runTaskLater(plugin, 20); // Delay 1 second

    }

    private void teleportAndRotatePlayer(Player player) {
        Location currentLocation = player.getLocation();
        Location newLocation = currentLocation.add(currentLocation.getDirection().multiply(5));
        newLocation.setYaw(currentLocation.getYaw() + 180); // Rotate 180 degrees

        player.teleport(newLocation);
    }

    private void giveTemporaryDamageImmunity(Player player, int durationTicks) {
        //Set metadata to indicate immunity
        player.setMetadata("immune", new FixedMetadataValue(plugin, true));

        new BukkitRunnable() {
            @Override
            public void run() {
                //Remove metadata after the duration
                player.removeMetadata("immune", plugin);
            }
        }.runTaskLater(plugin, durationTicks);
    }

    //Damage listener to handle immunity
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (player.hasMetadata("immune")) {
                event.setCancelled(true); // Cancel the damage
            }
        }
    }

    private void startCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (TELEPORT_COOLDOWN * 1000));
    }

    private boolean isOnCooldown(Player player) {
        return cooldowns.containsKey(player.getUniqueId()) && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    private long getRemainingCooldown(Player player) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            long endTime = cooldowns.get(player.getUniqueId());
            long timeLeft = (endTime - System.currentTimeMillis()) / 1000;
            return Math.max(0, timeLeft);
        }
        return 0;
    }


    private void startParticleTrail(Player player) {
        player.setMetadata("particleTrail", new FixedMetadataValue(plugin, new BukkitRunnable() {
            Location lastLocation = player.getLocation();

            @Override
            public void run() {
                if (!player.isOnline() || !player.hasMetadata("particleTrail")) {
                    this.cancel();
                    return;
                }

                Location currentLocation = player.getLocation();
                double distance = lastLocation.distance(currentLocation);

                if (distance > 0) {
                    Vector direction = currentLocation.toVector().subtract(lastLocation.toVector()).normalize();
                    double currentDistance = 0;

                    while (currentDistance < distance) {
                        Location particleLocation = lastLocation.clone().add(direction.clone().multiply(currentDistance));
                        DustOptions dustOptions = new DustOptions(Color.RED, 1); //creates a red dust option
                        player.getWorld().spawnParticle(Particle.SPELL_MOB, particleLocation, 1, 0, 0, 0, 0, dustOptions); // Red spiral particle
                        currentDistance += PARTICLE_DISTANCE;
                    }
                }

                lastLocation = currentLocation.clone();
            }
        }.runTaskTimer(plugin, 0, 2))); // Run every 2 ticks
    }

    private void stopParticleTrail(Player player) {
        if (player.hasMetadata("particleTrail")) {
            BukkitRunnable task = (BukkitRunnable) player.getMetadata("particleTrail").get(0).value();
            task.cancel();
            player.removeMetadata("particleTrail", plugin);
        }
    }
}
