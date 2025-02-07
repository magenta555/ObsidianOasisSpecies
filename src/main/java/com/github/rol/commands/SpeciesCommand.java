package com.github.rol.commands;

import com.github.rol.Rol;
import com.github.rol.menus.SpeciesMenu;
import com.github.rol.managers.SpeciesManager;
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

public class SpeciesCommand implements CommandExecutor, TabCompleter {

    private final Rol plugin;
    private final SpeciesManager speciesManager;

    public SpeciesCommand(Rol plugin, SpeciesManager speciesManager) {
        this.plugin = plugin;
        this.speciesManager = speciesManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                SpeciesMenu speciesMenu = new SpeciesMenu(plugin, speciesManager);
                speciesMenu.openInventory(player);
                return true;
            } else if (args[0].equalsIgnoreCase("species")) {
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
                                player.sendMessage("Set " + target.getName() + "'s species to " + speciesName + ".");
                                target.sendMessage("Your species has been set to " + speciesName + ".");
                            } else {
                                player.sendMessage("Invalid species type!");
                            }
                        } else {
                            player.sendMessage("Player not found!");
                        }
                    } else {
                        player.sendMessage("Usage: /rol setspecies <species> <player>");
                    }
                } else {
                    player.sendMessage("You do not have permission to use this command.");
                }
                return true;
            } else {
                player.sendMessage("Usage: /rol species");
                return true;
            }
        } else {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
    }

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
