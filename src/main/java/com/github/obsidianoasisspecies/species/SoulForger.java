package com.github.obsidianoasisspecies.species;

import com.github.obsidianoasisspecies.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

public class SoulForger implements Listener {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final int COOLDOWN_TIME = 10 * 20;
    private final ObsidianOasisSpecies plugin;

    public SoulForger(ObsidianOasisSpecies plugin) {    
        this.plugin = plugin;   
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Species species = plugin.getPlayerSpecies(player);
        if (species != Species.SOULFORGER) {
            return;
        }

        String color = Species.SOULFORGER.getChatColor();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            List<String> lore = player.getInventory().getItemInMainHand().getItemMeta().getLore();

            if (lore != null) {
                if (lore.contains(color + "Owner: " + player.getName())) {
                    World world = player.getWorld();
                    long currentTime = world.getTime();
                    
                    if (cooldowns.containsKey(playerId)) {
                        long lastUseTime = cooldowns.get(playerId);
                        if (currentTime - lastUseTime < COOLDOWN_TIME) {
                            player.sendTitle("", "§dCooldown: " + ((COOLDOWN_TIME - (currentTime - lastUseTime)) / 20) + " seconds");
                            return;
                        }
                    }

                    player.sendTitle("", color + "Ability Activated!");
                    cooldowns.put(playerId, currentTime);
                }
            }
        }
    }
}
