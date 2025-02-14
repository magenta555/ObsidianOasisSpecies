package com.github.rol;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

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
        ItemStack humanItem = new ItemStack(Material.WHITE_STAINED_GLASS, 1); // Create item stack for Human
        ItemMeta humanMeta = humanItem.getItemMeta(); // Get item meta for Human item
        if (humanMeta != null) { // Check if item meta is not null
            humanMeta.setDisplayName("Human"); // Set display name for the item
            humanMeta.setLore(Collections.singletonList("The default species.")); // Set lore for additional information
            humanItem.setItemMeta(humanMeta); // Apply the meta to the item stack
        }
        inventory.setItem(0, humanItem); // Place Human item in the first slot of the inventory

        // Create and configure the "Vampire" item
        ItemStack vampireItem = new ItemStack(Material.RED_STAINED_GLASS, 1); // Create item stack for Vampire
        ItemMeta vampireMeta = vampireItem.getItemMeta(); // Get item meta for Vampire item
        if (vampireMeta != null) { // Check if item meta is not null
            vampireMeta.setDisplayName("Vampire"); // Set display name for the item
            vampireMeta.setLore(Collections.singletonList("A creature of the night.")); // Set lore for additional information
            vampireItem.setItemMeta(vampireMeta); // Apply the meta to the item stack
        }
        inventory.setItem(1, vampireItem); // Place Vampire item in the second slot of the inventory

        // Create and configure the "Night Creature" item
        ItemStack nightCreatureItem = new ItemStack(Material.BLACK_STAINED_GLASS, 1); // Create item stack for Night Creature
        ItemMeta nightCreatureMeta = nightCreatureItem.getItemMeta(); // Get item meta for Night Creature item
        if (nightCreatureMeta != null) { // Check if item meta is not null
            nightCreatureMeta.setDisplayName("Night Creature"); // Set display name for the item
            nightCreatureMeta.setLore(Collections.singletonList("A mysterious being of darkness.")); // Set lore for additional information
            nightCreatureItem.setItemMeta(nightCreatureMeta); // Apply the meta to the item stack
        }
        inventory.setItem(2, nightCreatureItem); // Place Night Creature item in the third slot of the inventory
    }

    // Method to open the species selection inventory for a player
    public void openInventory(final Player player) {
        player.openInventory(inventory); // Open the inventory for the specified player
    }
}
