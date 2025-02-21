package com.github.obsidianoasisspecies;

import com.github.obsidianoasisspecies.species.Species;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Chat implements Listener {
    private final ObsidianOasisSpecies plugin;

    public Chat(ObsidianOasisSpecies plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Species species = plugin.getPlayerSpecies(player);

        if (species != null) {
            String chatColor = species.getChatColor();
            String message = event.getMessage();

            // Format the chat message with the species' chat color
            event.setFormat(chatColor + player.getName() + ": " + ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
