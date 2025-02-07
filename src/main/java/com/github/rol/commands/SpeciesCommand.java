// SpeciesCommand.java
package com.github.rol.commands;

import com.github.rol.Rol;
import com.github.rol.menus.SpeciesMenu;
import com.github.rol.managers.SpeciesManager;
import org.bukkit.ChatColor;
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

/**
 * Handles the /rol species and /rol setspecies commands.
 */
public class SpeciesCommand implements CommandExecutor, TabCompleter {

    private final Rol plugin;
    private final SpeciesManager speciesManager;

    /**
     * Constructor for the SpeciesCommand class.
     *
     * @param plugin         The main plugin instance.
     * @param speciesManager The species manager instance.
     */
    public SpeciesCommand(Rol plugin, SpeciesManager speciesManager) {
        this.plugin = plugin;
        this.speciesManager = speciesManager;
    }

    /**
     * Executes the given command, returning its success.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                // Open species menu
                SpeciesMenu speciesMenu = new SpeciesMenu(plugin, speciesManager);
                speciesMenu.openInventory(player);
                return true;
            } else if (args[0].equalsIgnoreCase("species")) {
                // Open species menu
                SpeciesMenu speciesMenu = new SpeciesMenu(plugin, speciesManager);
                speciesMenu.openInventory(player);
                return true;
            } else if (args[0].equalsIgnoreCase("setspecies")) {
                if (player.hasPermission("rol.setspecies")) {
                    if (args.length == 3) {
                        Player target = plugin.getServer().getPlayer(args[2]);
                        if (target != null) {
                            String speciesName = args[1].toUpperCase();
                            if (speciesManager.isValidSpecies(speciesName)) {
                                speciesManager.setPlayerSpecies(target, speciesName);
                                player.sendMessage(ChatColor.LIGHT_PURPLE + "[Rol] Set " + target.getName() + "'s species to " + speciesName + ".");
                                target.sendMessage(ChatColor.LIGHT_PURPLE + "[Rol] Your species has been set to " + speciesName + ".");
                            } else {
                                player.sendMessage(ChatColor.LIGHT_PURPLE + "[Rol] Invalid species type!");
                            }
                        } else {
                            player.sendMessage(ChatColor.LIGHT_PURPLE + "[Rol] Player not found!");
                        }
                    } else {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "[Rol] Usage: /rol setspecies <species> <player>");
                    }
                } else {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "[Rol] You do not have permission to use this command.");
                }
                return true;
            } else {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "[Rol] Usage: /rol species");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "[Rol] This command can only be used by players.");
            return true;
        }
    }

    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside of a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param alias   The alias used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed and command label
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor.
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("species");
            completions.add("setspecies");
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("setspecies")) {
            List<String> completions = new ArrayList<>();
            completions.add("vampire");
            completions.add("human");
            completions.add("nightcreature");
            return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("setspecies")) {
            List<String> completions = new ArrayList<>();
            plugin.getServer().getOnlinePlayers().forEach(player -> completions.add(player.getName()));
            return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
