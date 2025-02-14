// Declare a package for the class
package com.github.rol;

// Import necessary classes from the Bukkit API
import org.bukkit.configuration.ConfigurationSection; // For handling configuration sections
import org.bukkit.configuration.InvalidConfigurationException; // Exception for invalid configurations
import org.bukkit.configuration.file.FileConfiguration; // For file configuration handling
import org.bukkit.configuration.file.YamlConfiguration; // For YAML file configuration handling
import org.bukkit.entity.Player; // For player entity handling
import org.bukkit.potion.PotionEffectType; // For potion effect types
import java.io.File; // For file handling
import java.io.FileNotFoundException; // Exception for file not found scenarios
import java.io.IOException; // Exception for input/output operations
import java.util.*; // Import all classes from the java.util package

// Define the SpeciesManager class
public class SpeciesManager {
    // Declare a final instance of Rol plugin
    private final Rol plugin;
    // Declare a map to associate player UUIDs with species names
    private final Map<UUID, String> playerSpecies = new HashMap<>();
    // Declare a file to hold species data
    private File speciesFile;
    // Declare a configuration object for species data
    private FileConfiguration speciesConfig;
    // List of valid species names
    private final List<String> validSpecies = Arrays.asList("HUMAN", "VAMPIRE", "NIGHTCREATURE");

    // Constructor for SpeciesManager, takes a Rol plugin instance as parameter
    public SpeciesManager(Rol plugin) {
        this.plugin = plugin; // Assign the passed plugin instance to the class variable
        createSpeciesFile(); // Create the species file if it does not exist
        reloadSpeciesConfig(); // Reload the species configuration from the file
        loadSpeciesData(); // Load existing species data from the configuration
    }

    // Method to create the species file if it does not exist
    private void createSpeciesFile() {
        // Initialize speciesFile with a new File object pointing to species.yml in the plugin's data folder
        speciesFile = new File(plugin.getDataFolder(), "species.yml");
        // Check if the species file does not exist
        if (!speciesFile.exists()) {
            // Attempt to create parent directories for the species file if they do not exist
            if (speciesFile.getParentFile().mkdirs()) {
                plugin.getLogger().info("Creating species.yml file."); // Log creation message
            }
            try {
                // Try to create a new species.yml file
                if (speciesFile.createNewFile()) {
                    plugin.getLogger().info("Successfully created species.yml"); // Log success message
                } else {
                    plugin.getLogger().warning("Failed to create species.yml.  File may already exist."); // Log warning message if file exists
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create species.yml!"); // Log severe error message on failure to create file
                e.printStackTrace(); // Print stack trace for debugging purposes
            }
        }
    }

    // Method to reload the species configuration from the file
    public void reloadSpeciesConfig() {
        speciesConfig = YamlConfiguration.loadConfiguration(speciesFile); // Load YAML configuration from species file
        try {
            scanSpeciesConfig(); // Validate the configuration format by scanning it
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("Invalid config format in species.yml: " + e.getMessage()); // Log error message for invalid format
        }
    }

    // Method to save the current species configuration back to the file
    public void saveSpeciesConfig() {
        try {
            speciesConfig.save(speciesFile); // Save the configuration to the species file
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save species.yml!"); // Log severe error message on failure to save file
            e.printStackTrace(); // Print stack trace for debugging purposes
        }
    }

    // Method to get the current species configuration object
    public FileConfiguration getSpeciesConfig() {
        return speciesConfig; // Return the current configuration object
    }

    // Method to load existing species data from the configuration file into memory
    public void loadSpeciesData() {
        try {
            speciesConfig.load(speciesFile); // Load configuration data from the species file into memory
            
            ConfigurationSection speciesSection = speciesConfig.getConfigurationSection("species"); // Get 'species' section from config
            
            if (speciesSection != null) { // Check if 'species' section exists in config 
                for (String uuidString : speciesSection.getKeys(false)) { // Iterate over each UUID string in 'species' section keys 
                    try {
                        UUID uuid = UUID.fromString(uuidString); // Convert UUID string to UUID object 
                        String species = speciesSection.getString(uuidString); // Get associated species string 
                        if (species != null) { 
                            playerSpecies.put(uuid, species.toUpperCase());  // Store UUID and uppercase species name in playerSpecies map 
                        }
                    } catch (IllegalArgumentException e) { 
                        plugin.getLogger().warning("Invalid UUID found in species data: " + uuidString);  // Log warning for invalid UUID format 
                    }
                }
                plugin.getLogger().info("Loaded " + playerSpecies.size() + " species entries from species.yml.");  // Log number of loaded entries 
            } else { 
                plugin.getLogger().info("No existing species data found in species.yml.");  // Log info if no data found 
            }
        } catch (IOException | InvalidConfigurationException e) { 
            plugin.getLogger().severe("Could not load species.yml!");  // Log severe error message on failure to load config 
            e.printStackTrace();  // Print stack trace for debugging purposes 
        }
    }

    // Method to save current playerSpecies data back into the configuration file 
    public void saveSpeciesData() { 
        ConfigurationSection speciesSection = getSpeciesConfig().createSection("species");  // Create 'species' section in config 
        
        playerSpecies.forEach((uuid, species) -> speciesSection.set(uuid.toString(), species));  // Store each player's UUID and their corresponding species in config 
        
        saveSpeciesConfig();  // Save changes made to config back into the file 
    }

    // Method to set a player's species and notify them 
    public void setPlayerSpecies(Player player, String speciesName) { 
        playerSpecies.put(player.getUniqueId(), speciesName.toUpperCase());  // Store player's UUID and uppercase version of their new species name 
        
        player.sendMessage("[Rol] You are now a " + speciesName + "!");  // Send message to player confirming their new status 
        
        saveSpeciesData();  // Save updated playerSpecies data into config 
    }

    // Method to retrieve a player's current assigned species 
    public String getPlayerSpecies(Player player) { 
        return playerSpecies.get(player.getUniqueId());  // Return player's current assigned species based on their UUID 
    }

    // Method to clear a player's assigned species and remove effects 
    public void clearPlayerSpecies(Player player) { 
        String species = playerSpecies.remove(player.getUniqueId());  // Remove player's UUID entry from playerSpecies map and retrieve their associated specie 
        
        if (species != null) {  // Check if a valid specie was found and removed 
            removeSpeciesEffects(player, species);  // Remove any effects associated with that specie from player 
            
            saveSpeciesData();  // Save updated playerSpecies data into config after clearing effects 
        }
    }

    // Method to check if a given specie name is valid 
    public boolean isValidSpecies(String speciesName) { 
        return validSpecies.contains(speciesName.toUpperCase());  // Return true if validSpecies list contains uppercase version of specie name, otherwise false  
    }

    // Method to apply effects based on player's specie type  
    public void applySpeciesEffects(Player player, String speciesName) {  
        switch (speciesName) {  // Switch statement based on specie name  
            case "VAMPIRE":  // Case for vampire specie  
                Vampire vampire = new Vampire(plugin, player);  // Create new Vampire object with current plugin and player reference  
                vampire.applyVampireEffects();  // Apply vampire-specific effects  
                break;  // Exit switch statement  
                
            case "NIGHTCREATURE":  // Case for night creature specie  
                NightCreature nightCreature = new NightCreature(plugin, player);  // Create new NightCreature object with current plugin and player reference  
                nightCreature.applyNightCreatureEffects();  // Apply night creature-specific effects  
                break;  // Exit switch statement  
        }  
    }  

    // Method to remove effects based on player's specie type  
    public void removeSpeciesEffects(Player player, String speciesName) {  
        switch (speciesName) {  // Switch statement based on specie name         case "VAMPIRE":             removePotionEffect(player, PotionEffectType.NIGHT_VISION);             removePotionEffect(player, PotionEffectType.REGENERATION);             removePotionEffect(player, PotionEffectType.STRENGTH);             player.setHealthScale(20);             player.setHealth(Math.min(player.getHealth(), 20));             break;          case "NIGHTCREATURE":             removePotionEffect(player, PotionEffectType.NIGHT_VISION);             removePotionEffect(player, PotionEffectType.STRENGTH);             player.setHealthScale(20);             player.setHealth(Math.min(player.getHealth(), 20));             break;          }      }  

      // Method to remove a specific potion effect from a player  
      private void removePotionEffect(Player player, PotionEffectType effectType) {  
          player.removePotionEffect(effectType);  // Remove specified potion effect type from player  
      }  

      // Method to scan and validate the structure of the YAML configuration file  
      public void scanSpeciesConfig() throws IllegalArgumentException {  
          Scanner scan = null;   // Initialize scanner variable for reading lines of config file  
          try {  
              scan = new Scanner(speciesFile);   // Create scanner object for reading from specified file  
              int row = 0;   // Initialize row counter for tracking line numbers  
              while (scan.hasNextLine()) {   // Loop through each line until no more lines are left   
                  row++;   // Increment row counter   
                  String line = scan.nextLine();   // Read next line from scanner   
                  if (line.contains("\t")) {   // Check if line contains tab characters   
                      String error = ("Tab found in species.yml on line # " + row + "!  Replace with spaces.");   // Create error message indicating tab presence   
                      throw new IllegalArgumentException(error);   // Throw exception indicating invalid format   
                  }   
              }   
              getSpeciesConfig().load(speciesFile);   // Load YAML configuration after validation checks   
          } catch (FileNotFoundException e) {   // Catch block for handling missing files   
              plugin.getLogger().severe("species.yml not found during validation!");   // Log severe error message indicating missing config   
              e.printStackTrace();   // Print stack trace for debugging purposes   
          } catch (IOException | InvalidConfigurationException e) {   // Catch block for handling I/O or invalid config exceptions   
              plugin.getLogger().severe("Error loading species.yml during validation!");  　// Log severe error message indicating loading issues   
              e.printStackTrace();  　// Print stack trace for debugging purposes   
          } finally {   
              if (scan != null) {  　// Check if scanner was initialized successfully    
                  scan.close();  　// Close scanner resource after use    
              }    
 　       }    
     }    

     /* Method returns list of all valid specie names */    
     public List<String> getAllSpecies() {    
         return new ArrayList<>(validSpecies);     /* Return a new ArrayList containing all valid specie names */    
     }    
}
