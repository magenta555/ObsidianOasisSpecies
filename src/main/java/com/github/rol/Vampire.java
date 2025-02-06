package com.github.rol;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.FileConfiguration;

public class Vampire implements Listener {

    private final Rol plugin;

    public Vampire(Rol plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the player is a vampire and using the ability item
        if (isVampire(player) && isAbilityItem(item)) {
            teleportPlayer(player);
        }
    }

    private boolean isVampire(Player player) {
        String role = plugin.getPlayerRole(player);
        return role.equals("vampire");
    }

    private boolean isAbilityItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        FileConfiguration config = plugin.getConfig();
        String configuredItemName = config.getString("rol.vampire.abilityitem", "SWORD").toUpperCase();
        String itemName = item.getType().name().toUpperCase();

        return itemName.contains("SWORD") || itemName.equals(configuredItemName);
    }

    private void teleportPlayer(Player player) {
        Location currentLocation = player.getLocation();
        Location newLocation = currentLocation.add(currentLocation.getDirection().multiply(10));
        player.teleport(newLocation);
        player.sendMessage("§d[Rol] You have been teleported 10 blocks forward!");
    }
}
