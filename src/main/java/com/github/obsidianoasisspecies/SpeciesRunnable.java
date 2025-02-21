package com.github.obsidianoasisspecies;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SpeciesRunnable extends BukkitRunnable {
    private final ObsidianOasisSpecies plugin;

    public SpeciesRunnable(ObsidianOasisSpecies plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.applySpeciesAttributes(player);
        }
    }
}