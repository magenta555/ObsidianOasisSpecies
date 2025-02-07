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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.Block;
import org.bukkit.Location;

public class SpeciesListener implements Listener {

    private final Rol plugin;
    private final SpeciesManager speciesManager;

    public SpeciesListener(Rol plugin, SpeciesManager speciesManager) {
        this.plugin = plugin;
        this.speciesManager = speciesManager;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    checkSunlight(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String species = speciesManager.getPlayerSpecies(player);

        if (species != null) {
            speciesManager.applySpeciesEffects(player, species);
        }
    }

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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String species = speciesManager.getPlayerSpecies(player);

        if (species != null) {
            if (species.equalsIgnoreCase("VAMPIRE")) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                        player.getInventory().getItemInMainHand().getType().toString().contains("SWORD")) {
                    Vampire vampire = new Vampire(plugin, player);
                    vampire.activateVampireAbility();
                }
            } else if (species.equalsIgnoreCase("NIGHTCREATURE")) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                        player.getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD) {
                    NightCreature nightCreature = new NightCreature(plugin, player);
                    nightCreature.activateNightCreatureAbility();
                }
            }
        }
    }

    private void checkSunlight(Player player) {
        String species = speciesManager.getPlayerSpecies(player);

        if (species != null && (species.equalsIgnoreCase("VAMPIRE") || species.equalsIgnoreCase("NIGHTCREATURE"))) {
            if (isDaytime(player.getWorld().getTime()) && !isUnderSunlight(player)) {
                player.setFireTicks(100);
                player.sendMessage("[Rol] The sun burns your skin!");
            }
        }
    }

    private boolean isDaytime(long time) {
        return time > 0 && time < 12300;
    }

    private boolean isUnderSunlight(Player player) {
        Location location = player.getLocation();
        World world = player.getWorld();

        int highestBlockY = world.getHighestBlockYAt(location);

        if (highestBlockY > location.getY()) {
            return true;
        }

         for (int y = location.getBlockY() + 1; y <= world.getMaxHeight(); y++) {
            Block block = world.getBlockAt(location.getBlockX(), y, location.getBlockZ());
            if (block.getType().isSolid()) {
                return true;
            }
        }
         
        return false;
    }
}
