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

public class SpeciesMenu {

    private final Inventory inventory;

    @SuppressWarnings("deprecation")
    public SpeciesMenu(Rol plugin, SpeciesManager speciesManager) {
        inventory = Bukkit.createInventory(null, 9, "Choose Your Species");

        initializeMenuItems();
    }

    @SuppressWarnings("deprecation")
    private void initializeMenuItems() {
        ItemStack humanItem = new ItemStack(Material.WHITE_STAINED_GLASS, 1);
        ItemMeta humanMeta = humanItem.getItemMeta();
        if (humanMeta != null) {
            humanMeta.setDisplayName("Human");
            humanMeta.setLore(Collections.singletonList("The default species."));
            humanItem.setItemMeta(humanMeta);
        }
        inventory.setItem(0, humanItem);

        ItemStack vampireItem = new ItemStack(Material.RED_STAINED_GLASS, 1);
        ItemMeta vampireMeta = vampireItem.getItemMeta();
        if (vampireMeta != null) {
            vampireMeta.setDisplayName("Vampire");
            vampireMeta.setLore(Collections.singletonList("A creature of the night."));
            vampireItem.setItemMeta(vampireMeta);
        }
        inventory.setItem(1, vampireItem);

        ItemStack nightCreatureItem = new ItemStack(Material.BLACK_STAINED_GLASS, 1);
        ItemMeta nightCreatureMeta = nightCreatureItem.getItemMeta();
        if (nightCreatureMeta != null) {
            nightCreatureMeta.setDisplayName("Night Creature");
            nightCreatureMeta.setLore(Collections.singletonList("A mysterious being of darkness."));
            nightCreatureItem.setItemMeta(nightCreatureMeta);
        }
        inventory.setItem(2, nightCreatureItem);
    }

    public void openInventory(final Player player) {
        player.openInventory(inventory);
    }
}
