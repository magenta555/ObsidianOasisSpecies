// SpeciesListener.java
package com.github.rol.listeners;

import com.github.rol.Rol;
import com.github.rol.abilities.NightCreature;
import com.github.rol.abilities.Vampire;
import com.github.rol.managers.SpeciesManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles event listening for species-specific behaviors.
 */
public class SpeciesListener implements Listener {

    private final Rol plugin;
    private final SpeciesManager speciesManager;

    /**
     * Constructor for the SpeciesListener class.
     *
     * @param plugin         The main plugin instance.
     * @param speciesManager The species manager instance.
     */
    public SpeciesListener(Rol plugin, SpeciesManager speciesManager) {
        this.plugin = plugin;
        this.speciesManager = speciesManager;
    }

    /**
     * Called when a player joins the server.
     *
     * @param event The PlayerJoinEvent.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String species = speciesManager.getPlayerSpecies(player);

        // Apply species effects if the player has a species
        if (species != null) {
            speciesManager.applySpeciesEffects(player, species);
        }
    }

    /**
     * Called when a player clicks in an inventory.
     *
     * @param event The InventoryClickEvent.
     */
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String inventoryName = event.getView().getTitle();

        if (inventoryName.equals("Choose Your Species")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            String speciesName = null;
            if (clickedItem.getType() == Material.PLAYER_HEAD && clickedItem.getItemMeta().getDisplayName().contains("Human")) {
                speciesName = "HUMAN";
            } else if (clickedItem.getType() == Material.RED_STAINED_GLASS && clickedItem.getItemMeta().getDisplayName().contains("Vampire")) {
                speciesName = "VAMPIRE";
            } else if (clickedItem.getType() == Material.ENDER_EYE && clickedItem.getItemMeta().getDisplayName().contains("Night Creature")) {
                speciesName = "NIGHTCREATURE";
            }

            if (speciesName != null) {
                speciesManager.setPlayerSpecies(player, speciesName);
                player.closeInventory();
            }
        }
    }

    /**
     * Called when a player interacts with the world.
     *
     * @param event The PlayerInteractEvent.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String species = speciesManager.getPlayerSpecies(player);

        if (species != null) {
            if (species.equalsIgnoreCase("VAMPIRE")) {
                // Vampire Ability Activation
                if (event.getAction().toString().contains("RIGHT_CLICK") &&
                        player.getInventory().getItemInMainHand().getType().toString().contains("SWORD")) {
                    Vampire vampire = new Vampire(plugin, player);
                    vampire.activateVampireAbility();
                }
            } else if (species.equalsIgnoreCase("NIGHTCREATURE")) {
                // Night Creature Ability Activation
                if (event.getAction().toString().contains("RIGHT_CLICK") &&
                        player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                    NightCreature nightCreature = new NightCreature(plugin, player);
                    nightCreature.activateNightCreatureAbility();
                }
            }
        }
    }

    /**
     * Called when an entity is damaged.
     *
     * @param event The EntityDamageEvent.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        String species = speciesManager.getPlayerSpecies(player);

        if (species != null && (species.equalsIgnoreCase("VAMPIRE") || species.equalsIgnoreCase("NIGHTCREATURE"))) {
            // Burn in sunlight like a zombie
            if (player.getWorld().getTime() > 0 && player.getWorld().getTime() < 12300) {
                player.setFireTicks(100); // Set fire ticks
                player.sendMessage("[Rol] The sun burns your skin!");
            }
        }
    }
}
