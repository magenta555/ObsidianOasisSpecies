package com.github.rol;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;

public class ConfirmationMenu {
    private final Inventory inventory; // Inventory for confirming species selection
    private final String speciesName; // The name of the species to confirm

    @SuppressWarnings("deprecation")
    public ConfirmationMenu(String speciesName) {
        this.speciesName = speciesName; // Initialize speciesName

        inventory = Bukkit.createInventory(null, 9, "Confirm " + speciesName + " Selection"); // Create a 9-slot inventory with a title

        // Create confirmation item
        ItemStack confirmItem = new ItemStack(Material.GREEN_WOOL); // Use green wool for confirmation
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName("Confirm Selection");
            confirmMeta.setLore(Arrays.asList("Are you sure you want to select " + speciesName + "?", "This choice is permanent."));
            confirmItem.setItemMeta(confirmMeta);
        }
        inventory.setItem(3, confirmItem); // Place confirm item in slot 3

        // Create cancel item
        ItemStack cancelItem = new ItemStack(Material.RED_WOOL); // Use red wool for cancellation
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName("Cancel Selection");
            cancelMeta.setLore(Collections.singletonList("Click here to cancel."));
            cancelItem.setItemMeta(cancelMeta);
        }
        inventory.setItem(5, cancelItem); // Place cancel item in slot 5
    }

    public Inventory getInventory() {
        return inventory;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void openInventory(Player player) {
        player.openInventory(inventory); // Open confirmation menu for player
    }
}
