package com.github.rol;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SpeciesCommand implements CommandExecutor, TabCompleter {
    private final Rol plugin; // Reference to the main plugin class
    private final SpeciesManager speciesManager; // Reference to the SpeciesManager for managing species data

    // Messages used for player feedback
    private final String noSpeciesPermMessage = "You do not have permission to use the /species command.";
    private final String noSetSpeciesPermMessage = "You do not have permission to use the /setspecies command.";
    private final String noClearSpeciesPermMessage = "You do not have permission to use the /clearspecies command.";
    private final String noListSpeciesPermMessage = "You do not have permission to use the /listspecies command.";
    private final String invalidSpeciesMessage = "Invalid species type!";
    private final String playerNotFoundMessage = "Player not found!";
    private final String speciesSetMessage = "Set %s's species to %s.";
    private final String speciesClearedMessage = "Cleared %s's species.";
    private final String yourSpeciesSetMessage = "Your species has been set to %s.";
    private final String usageSetSpeciesMessage = "Usage: /setspecies <species> <player>";
    private final String usageClearSpeciesMessage = "Usage: /clearspecies <player>";
    private final String onlyPlayersMessage = "This command can only be used by players.";
    private final String onlySetSpeciesOnceMessage = "You can only set your species once! Please contact an admin for help!";
    private final String currentSpeciesMessage = "You are currently a %s.";
    private final String noSpeciesSelectedMessage = "You have not selected a species yet.";

    // Constructor that initializes the SpeciesCommand
    public SpeciesCommand(Rol plugin, SpeciesManager speciesManager) {
        this.plugin = plugin; // Initialize the plugin reference
        this.speciesManager = speciesManager; // Initialize the species manager reference
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Check if the command sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(onlyPlayersMessage); // Send message if not a player
            return true;
        }

        Player player = (Player) sender; // Cast sender to Player

        // Handle /species command
        if (command.getName().equalsIgnoreCase("species")) {
            return handleSpeciesCommand(player);
        }

        // Handle /setspecies command
        if (command.getName().equalsIgnoreCase("setspecies")) {
            return handleSetSpeciesCommand(player, args);
        }

        // Handle /clearspecies command
        if (command.getName().equalsIgnoreCase("clearspecies")) {
            return handleClearSpeciesCommand(player, args);
        }

        // Handle /listspecies command
        if (command.getName().equalsIgnoreCase("listspecies")) {
            return handleListSpeciesCommand(player);
        }

        return false; // Return false if no valid command was found
    }

    private boolean handleSpeciesCommand(Player player) {
        if (!player.hasPermission("rol.species")) { // Check for permission
            player.sendMessage(noSpeciesPermMessage); // Send no permission message
            return true;
        }
        try {
            // Check if player has already set their species
            speciesManager.isValidSpecies(speciesManager.getPlayerSpecies(player));
            player.sendMessage(onlySetSpeciesOnceMessage); // Inform player they can only set once
            return true;
        } catch (Exception e) {
            new SpeciesMenu(plugin, speciesManager).openInventory(player); // Open species selection menu
            return true;
        }
    }

    // Method to handle the /setspecies command logic
    private boolean handleSetSpeciesCommand(Player player, String[] args) {
        if (!player.hasPermission("rol.setspecies")) { // Check for permission
            player.sendMessage(noSetSpeciesPermMessage);
            return true;
        }

        if (args.length != 2) { // Check argument count
            player.sendMessage(usageSetSpeciesMessage);
            return true;
        }

        Optional.ofNullable(plugin.getServer().getPlayerExact(args[1])).ifPresentOrElse(target -> {
            String speciesName = args[0].toUpperCase(); // Convert input species name to uppercase

            if (speciesManager.isValidSpecies(speciesName)) { // Validate the species name
                speciesManager.setPlayerSpecies(target, speciesName); // Set target player's species
                player.sendMessage(String.format(speciesSetMessage, target.getName(), speciesName)); // Notify executor of success
                target.sendMessage(String.format(yourSpeciesSetMessage, speciesName)); // Notify target of their new species
            } else {
                player.sendMessage(invalidSpeciesMessage); // Notify executor of invalid species type
            }
        }, () -> player.sendMessage(playerNotFoundMessage)); // Notify executor if target player is not found

        return true;
    }

    // Method to handle the /clearspecies command logic
    private boolean handleClearSpeciesCommand(Player player, String[] args) {
        if (!player.hasPermission("rol.clearspecies")) { // Check for permission
            player.sendMessage(noClearSpeciesPermMessage);
            return true;
        }

        if (args.length != 1) { // Check argument count
            player.sendMessage(usageClearSpeciesMessage);
            return true;
        }

        Optional.ofNullable(plugin.getServer().getPlayerExact(args[0])).ifPresentOrElse(target -> {
            speciesManager.clearPlayerSpecies(target); // Clear the target player's species
            player.sendMessage(String.format(speciesClearedMessage, target.getName())); // Notify executor of success
            target.sendMessage("[Rol] Your species has been cleared by " + player.getName() + ". You can now choose a new species."); // Notify target of their cleared status
        }, () -> player.sendMessage(playerNotFoundMessage));

        return true;
    }

    // Method to handle the /listspecies command logic
    private boolean handleListSpeciesCommand(Player player) {
        if (!player.hasPermission("rol.listspecies")) { // Check for permission
            player.sendMessage(noListSpeciesPermMessage); // Send no permission message
            return true;
        }

        String species = speciesManager.getPlayerSpecies(player); // Get the player's species

        if (species != null) {
            player.sendMessage(String.format(currentSpeciesMessage, species)); // Inform the player of their species
        } else {
            player.sendMessage(noSpeciesSelectedMessage); // Inform the player if no species is set
        }

        return true; // Command executed successfully
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("setspecies")) {
            if (!sender.hasPermission("rol.setspecies")) {
                return Collections.emptyList();  // Return empty list if no permission
            }

            if (args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], speciesManager.getAllSpecies(), new ArrayList<>());
                // Suggest valid species names based on partial input
            } else if (args.length == 2) {
                List<String> playerNames = new ArrayList<>();
                plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> playerNames.add(onlinePlayer.getName()));
                return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
                // Suggest online players based on partial input
            }
        } else if (command.getName().equalsIgnoreCase("clearspecies")) {
            if (!sender.hasPermission("rol.clearspecies")) {
                return Collections.emptyList();  // Return empty list if no permission
            }

            if (args.length == 1) {
                List<String> playerNames = new ArrayList<>();
                plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> playerNames.add(onlinePlayer.getName()));
                return StringUtil.copyPartialMatches(args[0], playerNames, new ArrayList<>());
                // Suggest online players based on partial input for clearing their species
            }
        }

        return Collections.emptyList();  // Return empty list for any other cases
    }
}
