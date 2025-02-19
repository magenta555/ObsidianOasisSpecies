package com.github.obsidianoasisspecies;

import com.github.obsidianoasisspecies.species.Species;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpeciesCommand implements CommandExecutor, TabCompleter {

    private final ObsidianOasisSpecies plugin;

    public SpeciesCommand(ObsidianOasisSpecies plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§d§lUsage: /species choose/list/clear/set");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "choose":
                return chooseSpecies(sender);
            case "list":
                return listSpecies(sender);
            case "clear":
                return clearSpecies(sender, args);
            case "set":
                return setSpecies(sender, args);
            default:
                sender.sendMessage("§d§lInvalid subcommand. Usage: /species choose/list/clear/set");
                return true;
        }
    }

    private boolean chooseSpecies(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§d§lThis command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getPlayerSpecies(player) != null) {
            player.sendMessage("§d§lYou have already chosen your species.");
            return true;
        }

        SpeciesInventory speciesInventory = new SpeciesInventory(plugin);
        player.openInventory(speciesInventory.getInventory());

        return true;
    }

    private boolean listSpecies(CommandSender sender) {
        if (!sender.hasPermission("obsidianoasisspecies.list")) {
            sender.sendMessage("§d§lYou do not have permission to use this command.");
            return true;
        }

        java.util.Map<java.util.UUID, Species> playerSpecies = plugin.getAllPlayerSpecies();

        if (playerSpecies.isEmpty()) {
            sender.sendMessage("§d§lNo players have chosen a species yet.");
            return true;
        }

        sender.sendMessage("§d§l-----Player Species-----");
        for (java.util.Map.Entry<java.util.UUID, Species> entry : playerSpecies.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null) {
                sender.sendMessage("§d§l" + player.getName() + ": " + entry.getValue().getName());
            }
        }
        return true;
    }

    private boolean clearSpecies(CommandSender sender, String[] args) {
        if (!sender.hasPermission("obsidianoasisspecies.clear")) {
            sender.sendMessage("§d§lYou do not have permission to use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("§d§lUsage: /species clear <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage("§d§lPlayer " + args[1] + " not found.");
            return true;
        }

        plugin.clearPlayerSpecies(target);
        sender.sendMessage("§d§lSpecies cleared for player " + target.getName());

        return true;
    }

    private boolean setSpecies(CommandSender sender, String[] args) {
       if (!sender.hasPermission("obsidianoasisspecies.set")) {
           sender.sendMessage("§d§lYou do not have permission to use this command.");
           return true;
       }

       if (args.length != 3) {
           sender.sendMessage("§d§lUsage: /species set <player> <species>");
           return true;
       }

       Player target = Bukkit.getPlayer(args[1]);
       if (target == null) {
           sender.sendMessage("§d§lPlayer " + args[1] + " not found.");
           return true;
       }

       String speciesName = args[2].toUpperCase();
       Species species = null;

       try {
           species = Species.valueOf(speciesName);
       } catch (IllegalArgumentException e) {
           sender.sendMessage("§d§lInvalid species: " + args[2]);
           return true;
       }

       plugin.setPlayerSpecies(target, species);
       sender.sendMessage("§d§lSet species of " + target.getName() + " to " + species.getName());

       return true;
   }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("choose", "list", "clear", "set").stream()
                    .filter(subcommand -> subcommand.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "clear":
                case "set":
                    List<String> playerNames = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        playerNames.add(player.getName());
                    }
                    return playerNames.stream()
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                default:
                    return new ArrayList<>();
            }
        }

       if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return Arrays.stream(Species.values())
                    .map(Species::name)
                    .map(String::toLowerCase)
                    .filter(name -> name.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}