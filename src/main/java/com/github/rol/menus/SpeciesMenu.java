// SpeciesMenu.java
package com.github.rol.menus;

import com.github.rol.Rol;
import com.github.rol.managers.SpeciesManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    private final Rol plugin;
    private final SpeciesManager speciesManager;
    private final Inventory inventory;

    /**
     * Constructor for the SpeciesMenu class.
     *
     * @param plugin         The main plugin instance.
     * @param speciesManager The species manager instance.
     */
    public SpeciesMenu(Rol plugin, SpeciesManager speciesManager) {
        this.plugin = plugin;
        this.speciesManager = speciesManager;

        // Create inventory
        inventory = Bukkit.createInventory(null, 9, ChatColor.DARK_PURPLE + "Choose Your Species");

        // Initialize menu items
        initializeMenuItems();
    }

    /**
     * Initializes the menu items with species options.
     */
    private void initializeMenuItems() {
        // Human
        ItemStack humanItem = new ItemStack(Material.PLAYER_HEAD, 1);
        ItemMeta humanMeta = humanItem.getItemMeta();
        if (humanMeta != null) {
            humanMeta.setDisplayName(ChatColor.WHITE + "Human");
            humanMeta.setLore(Collections.singletonList(ChatColor.GRAY + "The default species."));
            humanItem.setItemMeta(humanMeta);
        }
        inventory.setItem(2, humanItem);

        // Vampire
        ItemStack vampireItem = new ItemStack(Material.RED_STAINED_GLASS, 1);
        ItemMeta vampireMeta = vampireItem.getItemMeta();
        if (vampireMeta != null) {
            vampireMeta.setDisplayName(ChatColor.RED + "Vampire");
            vampireMeta.setLore(Collections.singletonList(ChatColor.GRAY + "A creature of the night."));
            vampireItem.setItemMeta(vampireMeta);
        }
        inventory.setItem(4, vampireItem);

        // Night Creature
        ItemStack nightCreatureItem = new ItemStack(Material.ENDER_EYE, 1);
        ItemMeta nightCreatureMeta = nightCreatureItem.getItemMeta();
        if (nightCreatureMeta != null) {
            nightCreatureMeta.setDisplayName(ChatColor.DARK_GRAY + "Night Creature");
            nightCreatureMeta.setLore(Collections.singletonList(ChatColor.GRAY + "A mysterious being of darkness."));
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
