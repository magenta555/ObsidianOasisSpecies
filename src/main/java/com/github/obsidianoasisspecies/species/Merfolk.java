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

public class Merfolk implements Listener {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final int COOLDOWN_TIME = 10 * 20;
    private final ObsidianOasisSpecies plugin;

    public Merfolk(ObsidianOasisSpecies plugin) {    
        this.plugin = plugin;   
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.getInventory().getItemInMainHand().getType().toString().contains("SWORD")) {
                
                World world = player.getWorld();
                long currentTime = world.getTime();
                
                if (cooldowns.containsKey(playerId)) {
                    long lastUseTime = cooldowns.get(playerId);
                    if (currentTime - lastUseTime < COOLDOWN_TIME) {
                        player.sendTitle("", "Cooldown: " + ((COOLDOWN_TIME - (currentTime - lastUseTime)) / 20) + " seconds");
                        return;
                    }
                }

                player.sendTitle("", "You used an ability!");
                cooldowns.put(playerId, currentTime);
            }
        }
    }
}
