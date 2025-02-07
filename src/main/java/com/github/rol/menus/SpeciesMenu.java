// SpeciesMenu.java
package com.github.rol.menus;

import com.github.rol.Rol;
import com.github.rol.managers.SpeciesManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

/**
 * Creates and manages the species selection inventory menu.
 */
public class SpeciesMenu {

    private final Inventory inventory;

    /**
     * Constructor for the SpeciesMenu class.
     *
     * @param plugin         The main plugin instance.
     * @param speciesManager The species manager instance.
     */
    @SuppressWarnings("deprecation")
    public SpeciesMenu(Rol plugin, SpeciesManager speciesManager) {
        // Create inventory
        inventory = Bukkit.createInventory(null, 9, "Choose Your Species");

        // Initialize menu items
        initializeMenuItems();
    }

    /**
     * Initializes the menu items with species options.
     */
    @SuppressWarnings("deprecation")
    private void initializeMenuItems() {
        // Human
        ItemStack humanItem = new ItemStack(Material.PLAYER_HEAD, 1);
        ItemMeta humanMeta = humanItem.getItemMeta();
        if (humanMeta != null) {
            humanMeta.setDisplayName("Human");
            humanMeta.setLore(Collections.singletonList("The default species."));
            humanItem.setItemMeta(humanMeta);
        }
        inventory.setItem(2, humanItem);

        // Vampire
        ItemStack vampireItem = new ItemStack(Material.RED_STAINED_GLASS, 1);
        ItemMeta vampireMeta = vampireItem.getItemMeta();
        if (vampireMeta != null) {
            vampireMeta.setDisplayName("Vampire");
            vampireMeta.setLore(Collections.singletonList("A creature of the night."));
            vampireItem.setItemMeta(vampireMeta);
        }
        inventory.setItem(4, vampireItem);

        // Night Creature
        ItemStack nightCreatureItem = new ItemStack(Material.ENDER_EYE, 1);
        ItemMeta nightCreatureMeta = nightCreatureItem.getItemMeta();
        if (nightCreatureMeta != null) {
            nightCreatureMeta.setDisplayName("Night Creature");
            nightCreatureMeta.setLore(Collections.singletonList("A mysterious being of darkness."));
            nightCreatureItem.setItemMeta(nightCreatureMeta);
        }
        inventory.setItem(6, nightCreatureItem);
    }

    /**
     * Opens the inventory for the specified player.
     *
     * @param player The player to open the inventory for.
     */
    public void openInventory(final Player player) {
        player.openInventory(inventory);
    }
}
