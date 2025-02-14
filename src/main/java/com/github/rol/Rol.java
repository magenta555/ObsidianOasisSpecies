// Defines the package for the class
package com.github.rol;

// Imports the Player class from Bukkit API
import org.bukkit.entity.Player;
// Imports the JavaPlugin class from Bukkit API
import org.bukkit.plugin.java.JavaPlugin;
// Imports the BukkitRunnable class from Bukkit API
import org.bukkit.scheduler.BukkitRunnable;

// Defines the main plugin class, Rol, extending JavaPlugin
public final class Rol extends JavaPlugin {
    // Declares a private SpeciesManager field
    private SpeciesManager speciesManager;

    // Called when the plugin is enabled
    @Override
    public void onEnable() {
        // Logs a message to the console
        getLogger().info("Plugin has been enabled!");

        // Initializes the SpeciesManager
        speciesManager = new SpeciesManager(this);
        // Loads species data from file
        speciesManager.loadSpeciesData();

        // Creates a new SpeciesCommand instance
        SpeciesCommand speciesCommand = new SpeciesCommand(this, speciesManager);

        // Sets the executors for various commands
        getCommand("species").setExecutor(speciesCommand);
        getCommand("setspecies").setExecutor(speciesCommand);
        getCommand("clearspecies").setExecutor(speciesCommand);
        getCommand("listspecies").setExecutor(speciesCommand);


        // Sets the tab completers for various commands
        getCommand("species").setTabCompleter(speciesCommand);
        getCommand("setspecies").setTabCompleter(speciesCommand);
        getCommand("clearspecies").setTabCompleter(speciesCommand);
        getCommand("listspecies").setTabCompleter(speciesCommand);


        // Registers the SpeciesListener
        getServer().getPluginManager().registerEvents(new SpeciesListener(this, speciesManager), this);

        // Saves the default configuration file
        saveDefaultConfig();

        // Creates a BukkitRunnable to apply species effects to players
        new BukkitRunnable() {
            // Overrides the run method
            @Override
            public void run() {
                // Iterates through all online players
                for (Player player : getServer().getOnlinePlayers()) {
                    // Gets the player's species
                    String species = speciesManager.getPlayerSpecies(player);
                    // If the player has a species
                    if (species != null) {
                        // Applies the species effects to the player
                        speciesManager.applySpeciesEffects(player, species);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20); // Runs the task every 20 ticks (1 second)
    }

    // Called when the plugin is disabled
    @Override
    public void onDisable() {
        // Logs a message to the console
        getLogger().info("Plugin has been disabled!");

        // Saves species data to file
        speciesManager.saveSpeciesData();
    }

    // Getter for the SpeciesManager
    public SpeciesManager getSpeciesManager() {
        return speciesManager;
    }
}
