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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.bukkit.Color;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound; // Import Sound Enum

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Vampire implements Listener {

    private final Rol plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private int teleportCooldown; // Configurable
    private double particleDistance; // Configurable
    private int particleCount; // Configurable
    private Color particleColor; // Configurable
    private String abilitySound; // Configurable
    private double soundVolume; // Configurable
    private double soundPitch; // Configurable
    private double teleportDistance; // Configurable
    private int damageImmunityDuration; // Configurable

    private final String PARTICLE_TRAIL_METADATA = "particleTrail";

    public Vampire(Rol plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();

        teleportCooldown = config.getInt("rol.vampire.cooldown", 20);
        particleDistance = config.getDouble("rol.vampire.particle.distance", 0.5);
        particleCount = config.getInt("rol.vampire.particle.count", 10);

        // Load Color from config (R, G, B)
        int red = config.getInt("rol.vampire.particle.color.red", 255);
        int green = config.getInt("rol.vampire.particle.color.green", 0);
        int blue = config.getInt("rol.vampire.particle.color.blue", 0);
        particleColor = Color.fromRGB(red, green, blue);

        abilitySound = config.getString("rol.vampire.ability_sound", "ENTITY_BAT_TAKEOFF");
        soundVolume = config.getDouble("rol.vampire.sound_volume", 1.0);
        soundPitch = config.getDouble("rol.vampire.sound_pitch", 1.0);
        teleportDistance = config.getDouble("rol.vampire.teleport_distance", 5.0);
        damageImmunityDuration = config.getInt("rol.vampire.damage_immunity_duration", 20);

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
        // Play Sound from config
        try {
            @SuppressWarnings("deprecation")
            Sound sound = Sound.valueOf(abilitySound);
             player.getWorld().playSound(player.getLocation(), sound, (float) soundVolume, (float) soundPitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound name in config: " + abilitySound);
            // Fallback sound if the configured sound is invalid
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
        }

        // 1. Particle Trail
        startParticleTrail(player);

        // 2. Teleport and Rotate (delayed by 1 second)
        new BukkitRunnable() {
            @Override
            public void run() {
                stopParticleTrail(player);
                teleportAndRotatePlayer(player);
                // 3. Damage Immunity (after teleport)
                giveTemporaryDamageImmunity(player, damageImmunityDuration);
                startCooldown(player);
            }
        }.runTaskLater(plugin, 20); // Delay 1 second

    }

    private void teleportAndRotatePlayer(Player player) {
        Location currentLocation = player.getLocation();
        Location newLocation = currentLocation.add(currentLocation.getDirection().multiply(teleportDistance));
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
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (teleportCooldown * 1000));
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
        BukkitRunnable particleTask = new BukkitRunnable() {
            Location lastLocation = player.getLocation();

            @Override
            public void run() {
                if (!player.isOnline() || !player.hasMetadata(PARTICLE_TRAIL_METADATA)) {
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
                        DustOptions dustOptions = new DustOptions(particleColor, 2); // Larger red dust
                        player.getWorld().spawnParticle(Particle.DUST, particleLocation, particleCount, 0, 0, 0, dustOptions); // Increased count to 10
                        currentDistance += particleDistance;
                    }
                }

                lastLocation = currentLocation.clone();
            }
        };

        player.setMetadata(PARTICLE_TRAIL_METADATA, new FixedMetadataValue(plugin, particleTask));
        particleTask.runTaskTimer(plugin, 0, 1); // Run every tick for more frequent particles
    }


    private void stopParticleTrail(Player player) {
        if (player.hasMetadata(PARTICLE_TRAIL_METADATA)) {
            List<MetadataValue> metadataValues = player.getMetadata(PARTICLE_TRAIL_METADATA);
            if (!metadataValues.isEmpty()) {
                MetadataValue metadataValue = metadataValues.get(0);
                if (metadataValue instanceof FixedMetadataValue) {
                    FixedMetadataValue fixedMetadataValue = (FixedMetadataValue) metadataValue;
                    Object value = fixedMetadataValue.value();
                    if (value instanceof BukkitRunnable) {
                        BukkitRunnable task = (BukkitRunnable) value;
                        task.cancel();
                        player.removeMetadata(PARTICLE_TRAIL_METADATA, plugin);
                    } else {
                        plugin.getLogger().warning("Metadata value is not a BukkitRunnable for player: " + player.getName());
                    }
                } else {
                    plugin.getLogger().warning("Metadata value is not a FixedMetadataValue for player: " + player.getName());
                }
            } else {
                plugin.getLogger().warning("Metadata list is empty for player: " + player.getName());
            }
        }
    }
}
