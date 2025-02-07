// SpeciesManager.java
package com.github.rol.managers;

import com.github.rol.Rol;
import com.github.rol.abilities.NightCreature;
import com.github.rol.abilities.Vampire;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player species data, saving, and loading.
 */
public class SpeciesManager {

    private final Rol plugin;
    private final Map<UUID, String> playerSpecies = new HashMap<>();

    /**
     * Constructor for the SpeciesManager class.
     *
     * @param plugin The main plugin instance.
     */
    public SpeciesManager(Rol plugin) {
        this.plugin = plugin;
    }

    /**
     * Sets the species for a player.
     *
     * @param player      The player to set the species for.
     * @param speciesName The name of the species.
     */
    @SuppressWarnings("deprecation")
    public void setPlayerSpecies(Player player, String speciesName) {
        playerSpecies.put(player.getUniqueId(), speciesName.toUpperCase());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "[Rol] You are now a " + speciesName + "!");
        applySpeciesEffects(player, speciesName.toUpperCase());
    }

    /**
     * Gets the species of a player.
     *
     * @param player The player to get the species of.
     * @return The species of the player, or null if the player has no species set.
     */
    public String getPlayerSpecies(Player player) {
        return playerSpecies.get(player.getUniqueId());
    }

    /**
     * Checks if a species is valid.
     *
     * @param speciesName The name of the species to check.
     * @return True if the species is valid, false otherwise.
     */
    public boolean isValidSpecies(String speciesName) {
        return speciesName.equalsIgnoreCase("HUMAN") ||
                speciesName.equalsIgnoreCase("VAMPIRE") ||
                speciesName.equalsIgnoreCase("NIGHTCREATURE");
    }

    /**
     * Applies species-specific effects to the player.
     *
     * @param player      The player to apply the effects to.
     * @param speciesName The name of the species.
     */
    public void applySpeciesEffects(Player player, String speciesName) {
        // Remove existing effects
        // This is optional, if you want to clear old species effects
        //removeAllSpeciesEffects(player);

        // Apply new effects based on the species
        switch (speciesName) {
            case "VAMPIRE":
                Vampire vampire = new Vampire(plugin, player);
                vampire.applyVampireEffects();
                break;
            case "NIGHTCREATURE":
                NightCreature nightCreature = new NightCreature(plugin, player);
                nightCreature.applyNightCreatureEffects();
                break;
            // HUMAN does not have effects.
        }
    }

    /**
     * Loads species data from the config file.
     */
    public void loadSpeciesData() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection speciesSection = config.getConfigurationSection("species");

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
            plugin.getLogger().info("[Rol] Loaded " + playerSpecies.size() + " species entries from config.");
        } else {
            plugin.getLogger().info("[Rol] No existing species data found.");
        }
    }

    /**
     * Saves species data to the config file.
     */
    public void saveSpeciesData() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection speciesSection = config.createSection("species");

        playerSpecies.forEach((uuid, species) -> speciesSection.set(uuid.toString(), species));

        plugin.saveConfig();
        plugin.getLogger().info("[Rol] Saved " + playerSpecies.size() + " species entries to config.");
    }
}
