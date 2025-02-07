// SpeciesManager.java
package com.github.rol.managers;

import com.github.rol.Rol;
import com.github.rol.abilities.NightCreature;
import com.github.rol.abilities.Vampire;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class SpeciesManager {

    private final Rol plugin;
    private final Map<UUID, String> playerSpecies = new HashMap<>();
    private File speciesFile;
    private FileConfiguration speciesConfig;

    public SpeciesManager(Rol plugin) {
        this.plugin = plugin;
        createSpeciesFile();
        reloadSpeciesConfig(); // Load, scan for errors, save defaults if needed
        loadSpeciesData();
    }

    private void createSpeciesFile() {
        speciesFile = new File(plugin.getDataFolder(), "species.yml");
        if (!speciesFile.exists()) {
            if (speciesFile.getParentFile().mkdirs()) {
                plugin.getLogger().info("[Rol] Creating species.yml file.");
            }
            try {
                if(speciesFile.createNewFile()){
                    plugin.getLogger().info("[Rol] Successfully created species.yml");
                } else {
                      plugin.getLogger().warning("[Rol] Failed to create species.yml.  File may already exist.");
                }

            } catch (IOException e) {
                plugin.getLogger().severe("[Rol] Could not create species.yml!");
                e.printStackTrace();
            }
        }
    }


    // Custom reload to include error scanning
    public void reloadSpeciesConfig() {
        speciesConfig = YamlConfiguration.loadConfiguration(speciesFile);

        // Load defaults from the bundled resource (if any)
        try {
           scanSpeciesConfig(); // Scan for YAML errors/tabs
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("[Rol] Invalid config format in species.yml: " + e.getMessage());
            // Optionally handle:  backup, reset to defaults, disable the plugin, etc.
        }
    }

    // Custom saveConfig
    public void saveSpeciesConfig() {
        try {
            speciesConfig.save(speciesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[Rol] Could not save species.yml!");
            e.printStackTrace();
        }
    }

    public FileConfiguration getSpeciesConfig() {
        return speciesConfig;
    }

    public void loadSpeciesData() {
        try {
            speciesConfig.load(speciesFile);
            ConfigurationSection speciesSection = speciesConfig.getConfigurationSection("species");

            if (speciesSection != null) {
                for (String uuidString : speciesSection.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidString);
                        String species = speciesSection.getString(uuidString);
                        if (species != null) {
                            playerSpecies.put(uuid, species.toUpperCase());
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("[Rol] Invalid UUID found in species data: " + uuidString);
                    }
                }
                plugin.getLogger().info("[Rol] Loaded " + playerSpecies.size() + " species entries from species.yml.");
            } else {
                plugin.getLogger().info("[Rol] No existing species data found in species.yml.");
            }
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("[Rol] Could not load species.yml!");
            e.printStackTrace();
        }
    }


    public void saveSpeciesData() {
        ConfigurationSection speciesSection = getSpeciesConfig().createSection("species");
        playerSpecies.forEach((uuid, species) -> speciesSection.set(uuid.toString(), species));
        saveSpeciesConfig();
    }

    @SuppressWarnings("deprecation")
    public void setPlayerSpecies(Player player, String speciesName) {
        playerSpecies.put(player.getUniqueId(), speciesName.toUpperCase());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "[Rol] You are now a " + speciesName + "!");
        applySpeciesEffects(player, speciesName.toUpperCase());
        saveSpeciesData(); // Save immediately after setting species
    }

    public String getPlayerSpecies(Player player) {
        return playerSpecies.get(player.getUniqueId());
    }

    public boolean isValidSpecies(String speciesName) {
        return speciesName.equalsIgnoreCase("HUMAN") ||
                speciesName.equalsIgnoreCase("VAMPIRE") ||
                speciesName.equalsIgnoreCase("NIGHTCREATURE");
    }

    public void applySpeciesEffects(Player player, String speciesName) {
        switch (speciesName) {
            case "VAMPIRE":
                Vampire vampire = new Vampire(plugin, player);
                vampire.applyVampireEffects();
                break;
            case "NIGHTCREATURE":
                NightCreature nightCreature = new NightCreature(plugin, player);
                nightCreature.applyNightCreatureEffects();
                break;
        }
    }

    // YAML Format Validation (Tab Check)
    public void scanSpeciesConfig() throws IllegalArgumentException {
        Scanner scan = null;
        try {
            scan = new Scanner(speciesFile);
            int row = 0;
            while (scan.hasNextLine()) {
                row++;
                String line = scan.nextLine();
                if (line.contains("\t")) {
                    String error = ("Tab found in species.yml on line # " + row + "!  Replace with spaces.");
                    throw new IllegalArgumentException(error);
                }
            }
            getSpeciesConfig().load(speciesFile); // Load config *after* validation
        } catch (FileNotFoundException e) {
            plugin.getLogger().severe("[Rol] species.yml not found during validation!");
            e.printStackTrace();
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("[Rol] Error loading species.yml during validation!");
            e.printStackTrace();
        } finally {
            if (scan != null) {
                scan.close();
            }
        }
    }
}
