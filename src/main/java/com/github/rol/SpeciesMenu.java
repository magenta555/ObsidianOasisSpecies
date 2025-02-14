package com.github.rol;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpeciesMenu {
    private final Inventory inventory; // Inventory for the species selection menu

    @SuppressWarnings("deprecation") // Suppress warnings for deprecated methods
    public SpeciesMenu(Rol plugin, SpeciesManager speciesManager) {
        inventory = Bukkit.createInventory(null, 9, "Choose Your Species"); // Create a 9-slot inventory with a title
        initializeMenuItems(); // Call method to initialize the menu items
    }

    @SuppressWarnings("deprecation") // Suppress warnings for deprecated methods
    private void initializeMenuItems() {
        // Create and configure the "Human" item
        ItemStack humanItem = new ItemStack(Material.WHITE_STAINED_GLASS, 1);
        ItemMeta humanMeta = humanItem.getItemMeta();
        if (humanMeta != null) {
            humanMeta.setDisplayName("Human");
            humanMeta.setLore(Arrays.asList("The default species.", "Click to select!"));
            humanItem.setItemMeta(humanMeta);
        }
        inventory.setItem(0, humanItem);

        // Create and configure the "Vampire" item
        ItemStack vampireItem = new ItemStack(Material.RED_STAINED_GLASS, 1);
        ItemMeta vampireMeta = vampireItem.getItemMeta();
        if (vampireMeta != null) {
            vampireMeta.setDisplayName("§4Vampire");
            vampireMeta.setLore(Arrays.asList("§4A creature of the night.", "Click to select!"));
            vampireItem.setItemMeta(vampireMeta);
        }
        inventory.setItem(1, vampireItem);

        // Create and configure the "Night Creature" item
        ItemStack nightCreatureItem = new ItemStack(Material.BLACK_STAINED_GLASS, 1);
        ItemMeta nightCreatureMeta = nightCreatureItem.getItemMeta();
        if (nightCreatureMeta != null) {
            nightCreatureMeta.setDisplayName("Night Creature");
            nightCreatureMeta.setLore(Arrays.asList("A mysterious being of darkness.", "Click to select!"));
            nightCreatureItem.setItemMeta(nightCreatureMeta);
        }
        inventory.setItem(2, nightCreatureItem);
    }

    // Method to open the species selection inventory for a player
    public void openInventory(final Player player) {
        player.openInventory(inventory); // Open the inventory for the specified player
    }
}
