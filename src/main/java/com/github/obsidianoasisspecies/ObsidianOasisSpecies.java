package com.github.obsidianoasisspecies;

import com.github.obsidianoasisspecies.species.Species;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ObsidianOasisSpecies extends JavaPlugin {
    private HashMap<UUID, Species> playerSpecies;
    private File speciesFile;
    private FileConfiguration speciesConfig;

    @Override
    public void onEnable() {
        playerSpecies = new HashMap<>();
        speciesFile = new File(getDataFolder(), "species.yml");

        if (!speciesFile.exists()) {
            if (!speciesFile.getParentFile().exists()) {
                speciesFile.getParentFile().mkdirs();
            }
            saveDefaultSpeciesConfig();
        }

        speciesConfig = YamlConfiguration.loadConfiguration(speciesFile);
        loadSpeciesData();
        getCommand("species").setExecutor(new SpeciesCommand(this));
        getCommand("species").setTabCompleter(this);
        getServer().getPluginManager().registerEvents(new SpeciesInventory(this), this);
    }

    @Override
    public void onDisable() {
        saveSpeciesData();
    }

    public Species getPlayerSpecies(Player player) {
        return playerSpecies.get(player.getUniqueId());
    }

    public void setPlayerSpecies(Player player, Species species) {
        playerSpecies.put(player.getUniqueId(), species);
        saveSpeciesData();
    }

    public void clearPlayerSpecies(Player player) {
        playerSpecies.remove(player.getUniqueId());
        saveSpeciesData();
    }

    public HashMap<UUID, Species> getAllPlayerSpecies() {
        return playerSpecies;
    }

    private void loadSpeciesData() {
        if (speciesConfig.getConfigurationSection("species") != null) {
            for (String uuidString : speciesConfig.getConfigurationSection("species").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    String speciesName = speciesConfig.getString("species." + uuidString);
                    Species species = Species.valueOf(speciesName);
                    playerSpecies.put(uuid, species);

                } catch (IllegalArgumentException e) {
                    getLogger().warning("Could not load species for UUID: " + uuidString + ". Invalid species name.");
                } catch (Exception e) {
                    getLogger().warning("Could not load species for UUID: " + uuidString + ". Error: " + e.getMessage());
                }
            }

            getLogger().info("Loaded species data from species.yml");
            
        } else {
            getLogger().info("No species data found in species.yml, starting with empty data.");
        }
    }

    private void saveSpeciesData() {
        speciesConfig.set("species", null);

        for (Map.Entry<UUID, Species> entry : playerSpecies.entrySet()) {
            UUID uuid = entry.getKey();
            Species species = entry.getValue();
            speciesConfig.set("species." + uuid.toString(), species.name());
        }

        try {
            speciesConfig.save(speciesFile);
            getLogger().info("Saved species data to species.yml");

        } catch (IOException e) {
            getLogger().severe("Could not save species data to species.yml: " + e.getMessage());
        }
    }

    private void saveDefaultSpeciesConfig() {
        if (!speciesFile.exists()) {
            this.saveResource("species.yml", false);
        }
    }
}