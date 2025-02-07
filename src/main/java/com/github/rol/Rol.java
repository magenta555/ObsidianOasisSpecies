package com.github.rol;

import com.github.rol.commands.SpeciesCommand;
import com.github.rol.listeners.SpeciesListener;
import com.github.rol.managers.SpeciesManager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public final class Rol extends JavaPlugin {

    private SpeciesManager speciesManager;

@Override
public void onEnable() {
    getLogger().info("Plugin has been enabled!");

    speciesManager = new SpeciesManager(this);
    speciesManager.loadSpeciesData();

    SpeciesCommand speciesCommand = new SpeciesCommand(this, speciesManager);
    Objects.requireNonNull(getCommand("rol")).setExecutor(speciesCommand);
    Objects.requireNonNull(getCommand("rol")).setTabCompleter(speciesCommand);

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
