package com.github.rol;

import com.github.rol.*;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class Rol extends JavaPlugin {

    private SpeciesManager speciesManager;

    @Override
    public void onEnable() {
        getLogger().info("Plugin has been enabled!");

        speciesManager = new SpeciesManager(this);
        speciesManager.loadSpeciesData();

        // Register the /species and /setspecies commands
        SpeciesCommand speciesCommand = new SpeciesCommand(this, speciesManager);
        getCommand("species").setExecutor(speciesCommand);
        getCommand("setspecies").setExecutor(speciesCommand);
        getCommand("clearspecies").setExecutor(speciesCommand);


        getCommand("species").setTabCompleter(speciesCommand);
        getCommand("setspecies").setTabCompleter(speciesCommand);
        getCommand("clearspecies").setTabCompleter(speciesCommand);


        getServer().getPluginManager().registerEvents(new SpeciesListener(this, speciesManager), this);

        saveDefaultConfig();

        int tickInterval = getConfig().getInt("effect-tick-interval");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    String species = speciesManager.getPlayerSpecies(player);
                    if (species != null) {
                        speciesManager.applySpeciesEffects(player, species);
                    }
                }
            }
        }.runTaskTimer(this, 0L, tickInterval);
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin has been disabled!");

        speciesManager.saveSpeciesData();
    }

    public SpeciesManager getSpeciesManager() {
        return speciesManager;
    }
}
