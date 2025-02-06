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

public class Vampire implements Listener {

    private final Rol plugin;

    public Vampire(Rol plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Action action = event.getAction();

        // Check if the player is a vampire, using the ability item, and right-clicking
        if (isVampire(player) && isAbilityItem(item) && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
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

        return itemName.contains(configuredItemName);
    }

    private void teleportPlayer(Player player) {
        Location currentLocation = player.getLocation();
        Location newLocation = currentLocation.add(currentLocation.getDirection().multiply(10));
        player.teleport(newLocation);
    }
}