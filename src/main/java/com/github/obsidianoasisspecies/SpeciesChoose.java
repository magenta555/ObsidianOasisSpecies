package com.github.obsidianoasisspecies;

import com.github.obsidianoasisspecies.species.Species;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SpeciesChoose implements Listener {

    private final Inventory inventory;   
    private final ObsidianOasisSpecies plugin;   
    private final Map<Integer, Species> slotSpeciesMap = new HashMap<>();
    private Species chosenSpecies;

    @SuppressWarnings("deprecation")
    public SpeciesChoose(ObsidianOasisSpecies plugin) {    
        this.plugin = plugin;    
        inventory = Bukkit.createInventory(null, 9, "§d§lChoose Your Species!");    
        initializeInventory();    
    }

    private void initializeInventory() {       
        Map<Integer, Species> speciesSlots = new HashMap<>();      
        speciesSlots.put(0, Species.HUMAN);      
        speciesSlots.put(1, Species.WEREWOLF);      
        speciesSlots.put(2, Species.VAMPIRE);      
        speciesSlots.put(3, Species.SOULFORGER);      
        speciesSlots.put(4, Species.MERFOLK);

        for (int i = 0; i < inventory.getSize(); i++) {         
            ItemStack item;         
            if (speciesSlots.containsKey(i)) {             
                Species species = speciesSlots.get(i);             
                item = createSpeciesItem(species);             
                slotSpeciesMap.put(i, species);         
            } else {             
                item = createStainedGlassPane();         
            }         
            inventory.setItem(i, item);      
        }  
    }

    @SuppressWarnings("deprecation")
    private ItemStack createStainedGlassPane() {       
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);       
        ItemMeta meta = item.getItemMeta();       
        meta.setDisplayName(" ");       
        item.setItemMeta(meta);       
        return item;   
    }

    @SuppressWarnings("deprecation")
    private ItemStack createSpeciesItem(Species species) {       
        Material material;       
        switch (species) {           
            case HUMAN: material = Material.GRAY_STAINED_GLASS_PANE; break;           
            case WEREWOLF: material = Material.BROWN_STAINED_GLASS_PANE; break;           
            case VAMPIRE: material = Material.RED_STAINED_GLASS_PANE; break;           
            case SOULFORGER: material = Material.CYAN_STAINED_GLASS_PANE; break;           
            case MERFOLK: material = Material.BLUE_STAINED_GLASS_PANE; break;           
            default: material = Material.BARRIER; break;       
        }  

        ItemStack item = new ItemStack(material, 1);       
        ItemMeta meta = item.getItemMeta();       
        meta.setDisplayName(species.getName());       
        meta.setLore(Arrays.asList("§d§lClick to permanently choose this species!"));       
        item.setItemMeta(meta);       
        return item;   
    }

    public Inventory getInventory() {   
        return inventory;   
    }

    @EventHandler   
    public void onInventoryClick(InventoryClickEvent event) {    
        if (event.getView().getTitle().contains("Choose")) {    
            Player player = (Player) event.getWhoClicked();    
            ItemStack clickedItem = event.getCurrentItem();    
            Inventory clickedInventory = event.getClickedInventory();    

            event.setCancelled(true);    

            int slot = event.getSlot();    
            if (slotSpeciesMap.containsKey(slot)) {    
                chosenSpecies = slotSpeciesMap.get(slot);    
                openConfirmationInventory(player);    
            }   
        } else if (event.getView().getTitle().contains("Confirm")) {
            handleConfirmation(event);
        }    
    }

    private void openConfirmationInventory(Player player) {
        Inventory confirmationInventory = Bukkit.createInventory(null, 9, "§d§lConfirm " + chosenSpecies.getName() + "?");
        
        ItemStack confirmItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName("§2§lConfirm!");
        confirmMeta.setLore(Arrays.asList("§d§lClick to confirm your choice: " + chosenSpecies.getName()));
        confirmItem.setItemMeta(confirmMeta);
        
        ItemStack cancelItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName("§4§lCancel!");
        cancelMeta.setLore(Arrays.asList("§4§lClick to cancel!"));
        cancelItem.setItemMeta(cancelMeta);

        confirmationInventory.setItem(0, confirmItem);
        confirmationInventory.setItem(8, cancelItem);

        player.openInventory(confirmationInventory);
    }

    private void handleConfirmation(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem != null && clickedItem.hasItemMeta()) {
            if (clickedItem.getType() == Material.EMERALD_BLOCK) {
                plugin.setPlayerSpecies(player, chosenSpecies);
                player.sendMessage("§d§lYou have chosen to be a " + chosenSpecies.getName() + "!");
                player.closeInventory();
            } else if (clickedItem.getType() == Material.REDSTONE_BLOCK) {
                player.openInventory(getInventory());
            }
        }
    }
}
