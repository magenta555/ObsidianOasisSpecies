package com.github.rol;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.Block;
import org.bukkit.Location;

public class SpeciesListener implements Listener {
    private final Rol plugin; // Reference to the main plugin class
    private final SpeciesManager speciesManager; // Reference to the SpeciesManager for managing species data

    // Constructor that initializes the SpeciesListener
    public SpeciesListener(Rol plugin, SpeciesManager speciesManager) {
        this.plugin = plugin;
        this.speciesManager = speciesManager;

        // Schedule a repeating task to check sunlight exposure for all online players
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    checkSunlight(player); // Check if the player is exposed to sunlight
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)
    }

    // Event handler for when a player joins the game
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer(); // Get the player who joined
        String species = speciesManager.getPlayerSpecies(player); // Get the player's species

        if (species != null) {
            speciesManager.applySpeciesEffects(player, species); // Apply effects based on species
        }
    }

    @SuppressWarnings("deprecation") // Suppress warnings for deprecated methods
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return; // Ensure the clicker is a player

        Player player = (Player) event.getWhoClicked(); // Cast the clicked entity to Player
        String inventoryName = event.getView().getTitle(); // Get the title of the inventory

        if (inventoryName.equals("Choose Your Species")) { // Check if it's the species selection inventory
            event.setCancelled(true); // Cancel the event to prevent default behavior

            ItemStack clickedItem = event.getCurrentItem(); // Get the item that was clicked
            if (clickedItem == null || !clickedItem.hasItemMeta()) return; // Ensure item is valid

            String speciesName = null;

            if (clickedItem.getItemMeta().getDisplayName().contains("Human")) {
                speciesName = "HUMAN";
            } else if (clickedItem.getItemMeta().getDisplayName().contains("Vampire")) {
                speciesName = "VAMPIRE";
            } else if (clickedItem.getItemMeta().getDisplayName().contains("Soul Forger")) {
                speciesName = "SOULFORGER";
            }

            if (speciesName != null) {
                ConfirmationMenu confirmationMenu = new ConfirmationMenu(speciesName);
                player.openInventory(confirmationMenu.getInventory()); // Open confirmation menu for selected species
                player.sendMessage("SpeciesName " + speciesName);
            }
        } else if (inventoryName.startsWith("Confirm ")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;
            if (clickedItem.getItemMeta().getDisplayName().equals("Confirm Selection")) {
                String speciesName = inventoryName.substring(8, inventoryName.length() - 9);
                speciesManager.setPlayerSpecies(player, speciesName);
                player.closeInventory();
                player.sendMessage("[Rol] Your species has been set to " + speciesName + "!");
            } else if (clickedItem.getItemMeta().getDisplayName().equals("Cancel Selection")) {
                player.closeInventory();
                player.sendMessage("[Rol] Species selection canceled.");
            }
        }
    }

    // Event handler for when a player interacts with an object or area
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer(); // Get the player who interacted
        String species = speciesManager.getPlayerSpecies(player); // Get player's current species

        if (species != null) {
            if (species.equalsIgnoreCase("VAMPIRE")) {
                // Check for right-click action with a sword in hand for vampires
                if (event.getAction() == Action.RIGHT_CLICK_AIR &&
                        player.getInventory().getItemInMainHand().getType().toString().contains("SWORD")) {
                    Vampire vampire = new Vampire(plugin, player);
                    vampire.activateVampireAbility(); // Activate vampire-specific ability
                }
            } else if (species.equalsIgnoreCase("NIGHTCREATURE")) {
                // Check for right-click action with a blaze rod in hand for night creatures
                if (event.getAction() == Action.RIGHT_CLICK_AIR &&
                        player.getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD) {
                    NightCreature nightCreature = new NightCreature(plugin, player);
                    nightCreature.activateNightCreatureAbility(); // Activate night creature-specific ability
                }
            }
        }
    }

    // Method to check if a player is exposed to sunlight and apply fire damage accordingly
    private void checkSunlight(Player player) {
        String species = speciesManager.getPlayerSpecies(player);

        if (species != null && (species.equalsIgnoreCase("VAMPIRE") || 
            species.equalsIgnoreCase("NIGHTCREATURE"))) {
            if (isDaytime(player.getWorld().getTime()) && !isUnderSunlight(player)) {
                player.setFireTicks(40); // Set fire ticks to damage the player for being in sunlight
            }
        }
    }

    // Helper method to determine if it is daytime in the world based on time value
    private boolean isDaytime(long time) {
        return time > 0 && time < 12300; // Daytime is from 0 to 12300 ticks in Minecraft's day-night cycle
    }

    // Helper method to check if a player is under direct sunlight or blocked by solid blocks above them
    private boolean isUnderSunlight(Player player) {
        Location location = player.getLocation();
        World world = player.getWorld();
        int highestBlockY = world.getHighestBlockYAt(location);

        if (highestBlockY > location.getY()) {
            return true; // If there is a block higher than the player's Y coordinate, they are under cover.
        }

        for (int y = location.getBlockY() + 1; y <= world.getMaxHeight(); y++) {
            Block block = world.getBlockAt(location.getBlockX(), y, location.getBlockZ());
            if (block.getType().isSolid()) {
                return true; // If any solid block is found above, they are not in direct sunlight.
            }
        }

        return false; // If no solid blocks are found above, they are exposed to sunlight.
    }
}
