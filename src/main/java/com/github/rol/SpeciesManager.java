package com.github.rol;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class SpeciesManager {
    private final Rol plugin; // Reference to the main plugin class
    private final Map<UUID, String> playerSpecies = new HashMap<>(); // Map to store player species by UUID
    private File speciesFile; // File object for the species configuration file
    private FileConfiguration speciesConfig; // Configuration object for the species file
    private final List<String> validSpecies = Arrays.asList("HUMAN", "VAMPIRE", "NIGHTCREATURE"); // List of valid species

    // Constructor that initializes the SpeciesManager
    public SpeciesManager(Rol plugin) {
        this.plugin = plugin;
        createSpeciesFile(); // Create the species.yml file if it doesn't exist
        reloadSpeciesConfig(); // Load the configuration from the file
        loadSpeciesData(); // Load existing species data into memory
    }

    // Method to create the species.yml file if it does not exist
    private void createSpeciesFile() {
        speciesFile = new File(plugin.getDataFolder(), "species.yml"); // Define the path for the species file
        if (!speciesFile.exists()) { // Check if the file already exists
            if (speciesFile.getParentFile().mkdirs()) { // Create parent directories if needed
                plugin.getLogger().info("Creating species.yml file."); // Log creation message
            }
            try {
                if (speciesFile.createNewFile()) { // Attempt to create the new file
                    plugin.getLogger().info("Successfully created species.yml"); // Log success message
                } else {
                    plugin.getLogger().warning("Failed to create species.yml. File may already exist."); // Log warning if file exists
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create species.yml!"); // Log severe error message on exception
                e.printStackTrace(); // Print stack trace for debugging
            }
        }
    }

    // Method to reload the species configuration from the file
    public void reloadSpeciesConfig() {
        speciesConfig = YamlConfiguration.loadConfiguration(speciesFile); // Load YAML configuration from file
        try {
            scanSpeciesConfig(); // Validate the configuration format
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("Invalid config format in species.yml: " + e.getMessage()); // Log error for invalid format
        }
    }

    // Method to save the current configuration back to the file
    public void saveSpeciesConfig() {
        try {
            speciesConfig.save(speciesFile); // Save configuration to file
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save species.yml!"); // Log error on failure to save
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    // Getter method for the species configuration object
    public FileConfiguration getSpeciesConfig() {
        return speciesConfig; 
    }

    // Method to load existing species data from the configuration file into memory
    public void loadSpeciesData() {
        try {
            speciesConfig.load(speciesFile); // Load data from the configuration file
            ConfigurationSection speciesSection = speciesConfig.getConfigurationSection("species"); // Get "species" section

            if (speciesSection != null) { 
                for (String uuidString : speciesSection.getKeys(false)) { 
                    try {
                        UUID uuid = UUID.fromString(uuidString); // Convert string UUID to UUID object
                        String species = speciesSection.getString(uuidString); // Get associated species string

                        if (species != null) { 
                            playerSpecies.put(uuid, species.toUpperCase()); // Store in map with uppercase name
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID found in species data: " + uuidString); 
                    }
                }
                plugin.getLogger().info("Loaded " + playerSpecies.size() + " species entries from species.yml."); 
            } else {
                plugin.getLogger().info("No existing species data found in species.yml."); 
            }
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("Could not load species.yml!"); 
            e.printStackTrace(); 
        }
    }

    // Method to save current player species data back to the configuration file
    public void saveSpeciesData() {
        ConfigurationSection speciesSection = getSpeciesConfig().createSection("species"); 
        playerSpecies.forEach((uuid, species) -> speciesSection.set(uuid.toString(), species)); 
        saveSpeciesConfig(); 
    }

    // Method to set a player's species and notify them of the change
    public void setPlayerSpecies(Player player, String speciesName) {
        playerSpecies.put(player.getUniqueId(), speciesName.toUpperCase()); 
        player.sendMessage("[Rol] You are now a " + speciesName + "!"); 
        saveSpeciesData(); 
    }

    // Method to retrieve a player's current species based on their UUID
    public String getPlayerSpecies(Player player) {
        return playerSpecies.get(player.getUniqueId()); 
    }

    // Method to clear a player's current species and remove associated effects
    public void clearPlayerSpecies(Player player) {
        String species = playerSpecies.remove(player.getUniqueId()); 

        if (species != null) { 
            removeSpeciesEffects(player, species); 
            saveSpeciesData(); 
        }
    }

    // Method to check if a given species name is valid
    public boolean isValidSpecies(String speciesName) {
        return validSpecies.contains(speciesName.toUpperCase()); 
    }

    // Method to apply effects based on a player's current species type
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

    // Method to remove effects associated with a player's current species type
    public void removeSpeciesEffects(Player player, String speciesName) {
        switch (speciesName) { 
            case "VAMPIRE":
                removePotionEffect(player, PotionEffectType.NIGHT_VISION); 
                removePotionEffect(player, PotionEffectType.REGENERATION); 
                removePotionEffect(player, PotionEffectType.STRENGTH); 
                player.setHealthScale(20); 
                player.setHealth(Math.min(player.getHealth(), 20)); 
                break;
            case "NIGHTCREATURE":
                removePotionEffect(player, PotionEffectType.NIGHT_VISION); 
                removePotionEffect(player, PotionEffectType.STRENGTH); 
                player.setHealthScale(20); 
                player.setHealth(Math.min(player.getHealth(), 20)); 
                break;
        }
    }

    // Helper method to remove a specific potion effect from a player
    private void removePotionEffect(Player player, PotionEffectType effectType) {
        player.removePotionEffect(effectType); 
    }

    // Method to scan and validate the format of the configuration file for errors like tabs instead of spaces
    public void scanSpeciesConfig() throws IllegalArgumentException {
        Scanner scan = null; 

        try {
            scan = new Scanner(speciesFile); 
            int row = 0; 

            while (scan.hasNextLine()) { 
                row++; 
                String line = scan.nextLine(); 

                if (line.contains("\t")) {  // Check for tab characters in YAML files which are invalid.
                    String error = ("Tab found in species.yml on line # " + row + "! Replace with spaces.");
                    throw new IllegalArgumentException(error);
                }
            }
            getSpeciesConfig().load(speciesFile);  // Reload config after validation.
            
        } catch (FileNotFoundException e) {
            plugin.getLogger().severe("species.yml not found during validation!");  
            e.printStackTrace();  
            
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("Error loading species.yml during validation!");  
            e.printStackTrace();  
            
        } finally {
            if (scan != null) {  
                scan.close();  // Ensure scanner is closed after use.
            }
        }
    }

    // Method to get a list of all valid species names available in this manager.
    public List<String> getAllSpecies() {
        return new ArrayList<>(validSpecies);  // Return a copy of validSpecies list.
    }
}
