// Rol.java
package com.github.rol;

import com.github.rol.commands.SpeciesCommand;
import com.github.rol.listeners.SpeciesListener;
import com.github.rol.managers.SpeciesManager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

/**
 * The main class for the Rol plugin.
 */
public final class Rol extends JavaPlugin {

    private SpeciesManager speciesManager;

    /**
     * Called when the plugin is enabled.
     */
@Override
public void onEnable() {
    // Plugin startup logic
    getLogger().info("Plugin has been enabled!");

    // Initialize the species manager
    speciesManager = new SpeciesManager(this);
    speciesManager.loadSpeciesData();

    // Register command executor
    SpeciesCommand speciesCommand = new SpeciesCommand(this, speciesManager);
    Objects.requireNonNull(getCommand("rol")).setExecutor(speciesCommand);
    Objects.requireNonNull(getCommand("rol")).setTabCompleter(speciesCommand);

    // Register event listener
    getServer().getPluginManager().registerEvents(new SpeciesListener(this, speciesManager), this);

    // Save default config if it doesn't exist
    saveDefaultConfig();

    // Load the tick interval from the config
    int tickInterval = getConfig().getInt("effect-tick-interval");

    // Apply species effects to all online players every X ticks
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

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Plugin has been disabled!");

        // Save species data
        speciesManager.saveSpeciesData();
    }

    /**
     * Gets the species manager.
     *
     * @return The species manager.
     */
    public SpeciesManager getSpeciesManager() {
        return speciesManager;
    }
}
