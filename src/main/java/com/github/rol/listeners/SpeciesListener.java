package com.github.rol.listeners;

import com.github.rol.Rol;
import com.github.rol.abilities.NightCreature;
import com.github.rol.abilities.Vampire;
import com.github.rol.managers.SpeciesManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.Block;
import org.bukkit.Location;

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

        // Start the sunlight check task
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    checkSunlight(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Check every 20 ticks (1 second)
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
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                        player.getInventory().getItemInMainHand().getType().toString().contains("SWORD")) {
                    Vampire vampire = new Vampire(plugin, player);
                    vampire.activateVampireAbility();
                }
            } else if (species.equalsIgnoreCase("NIGHTCREATURE")) {
                // Night Creature Ability Activation
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK &&
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
       //This section is removed because the sunlight check is now handled in a separate task to account for players who may not be taking damage but should still burn
    }

    /**
     * Checks if the player is in sunlight and applies the burning effect if necessary.
     *
     * @param player The player to check.
     */
    private void checkSunlight(Player player) {
        String species = speciesManager.getPlayerSpecies(player);

        if (species != null && (species.equalsIgnoreCase("VAMPIRE") || species.equalsIgnoreCase("NIGHTCREATURE"))) {
            if (isDaytime(player.getWorld().getTime()) && !isUnderSunlight(player)) {
                player.setFireTicks(100); // Set fire ticks
                player.sendMessage("[Rol] The sun burns your skin!");
            }
        }
    }

    /**
     * Checks if it is daytime based on the world time.
     *
     * @param time The world time.
     * @return True if it is daytime, false otherwise.
     */
    private boolean isDaytime(long time) {
        return time > 0 && time < 12300;
    }

    /**
     * Checks if the player is under direct sunlight (not shaded).
     *
     * @param player The player to check.
     * @return True if the player is under direct sunlight, false otherwise.
     */
    private boolean isUnderSunlight(Player player) {
        Location location = player.getLocation();
        World world = player.getWorld();

        // Check the highest block at the player's location
        int highestBlockY = world.getHighestBlockYAt(location);

        // If the highest block is above the player, they are shaded
        if (highestBlockY > location.getY()) {
            return true; //Shaded by a block above
        }

         // Check if there are any opaque blocks directly above the player
         for (int y = location.getBlockY() + 1; y <= world.getMaxHeight(); y++) {
            Block block = world.getBlockAt(location.getBlockX(), y, location.getBlockZ());
            if (block.getType().isSolid()) {
                return true; // There is a solid block above, so they are shaded
            }
        }
         
        return false; //Not shaded, exposed to sunlight
    }
}
