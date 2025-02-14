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
    private final Rol plugin;
    private final SpeciesManager speciesManager;

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

    public SpeciesCommand(Rol plugin, SpeciesManager speciesManager) {
        this.plugin = plugin;
        this.speciesManager = speciesManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (command.getName().equalsIgnoreCase("species")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(onlyPlayersMessage);
                return true;
            }
            return handleSpeciesCommand((Player) sender);
        }

        if (command.getName().equalsIgnoreCase("setspecies")) {
            return handleSetSpeciesCommand(sender, args);
        }

        if (command.getName().equalsIgnoreCase("clearspecies")) {
            return handleClearSpeciesCommand(sender, args);
        }

        if (command.getName().equalsIgnoreCase("listspecies")) {
            return handleListSpeciesCommand(sender, args);
        }

        return false;
    }

    private boolean handleSpeciesCommand(Player player) {
        if (!player.hasPermission("rol.species")) {
            player.sendMessage(noSpeciesPermMessage);
            return true;
        }
        try {
            speciesManager.isValidSpecies(speciesManager.getPlayerSpecies(player));
            player.sendMessage(onlySetSpeciesOnceMessage);
            return true;
        } catch (Exception e) {
            new SpeciesMenu(plugin, speciesManager).openInventory(player);
            return true;
        }
    }

    private boolean handleSetSpeciesCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rol.setspecies")) {
            sender.sendMessage(noSetSpeciesPermMessage);
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(usageSetSpeciesMessage);
            return true;
        }

        Optional.ofNullable(plugin.getServer().getPlayerExact(args[1])).ifPresentOrElse(target -> {
            String speciesName = args[0].toUpperCase();

            if (speciesManager.isValidSpecies(speciesName)) {
                speciesManager.setPlayerSpecies(target, speciesName);
                sender.sendMessage(String.format(speciesSetMessage, target.getName(), speciesName));
                if (target instanceof Player) {
                    target.sendMessage(String.format(yourSpeciesSetMessage, speciesName));
                }
            } else {
                sender.sendMessage(invalidSpeciesMessage);
            }
        }, () -> sender.sendMessage(playerNotFoundMessage));

        return true;
    }

    private boolean handleClearSpeciesCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rol.clearspecies")) {
            sender.sendMessage(noClearSpeciesPermMessage);
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(usageClearSpeciesMessage);
            return true;
        }

        Optional.ofNullable(plugin.getServer().getPlayerExact(args[0])).ifPresentOrElse(target -> {
            speciesManager.clearPlayerSpecies(target);
            sender.sendMessage(String.format(speciesClearedMessage, target.getName()));
            if (target instanceof Player) {
                target.sendMessage("[Rol] Your species has been cleared by " + sender.getName() + ". You can now choose a new species.");
            }
        }, () -> sender.sendMessage(playerNotFoundMessage));

        return true;
    }

    private boolean handleListSpeciesCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rol.listspecies")) {
            sender.sendMessage(noListSpeciesPermMessage);
            return true;
        }

        if (args.length == 0) {
            if (!sender.hasPermission("rol.listspecies.all")) {
                sender.sendMessage("You do not have permission to list the species of all players.");
                return true;
            }
            StringBuilder sb = new StringBuilder("Current Players' Species:\n");
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                String species = speciesManager.getPlayerSpecies(onlinePlayer);
                sb.append(onlinePlayer.getName()).append(": ");
                if (species != null) {
                    sb.append(species);
                } else {
                    sb.append("None");
                }
                sb.append("\n");
            }
            sender.sendMessage(sb.toString());
            return true;

        } else if (args.length == 1) {
            Optional.ofNullable(plugin.getServer().getPlayerExact(args[0])).ifPresentOrElse(target -> {
                String species = speciesManager.getPlayerSpecies(target);
                if (species != null) {
                    sender.sendMessage(String.format(currentSpeciesMessage, species));
                } else {
                    sender.sendMessage(String.format("%s has no species set.", target.getName()));
                }
            }, () -> sender.sendMessage(playerNotFoundMessage));
            return true;
        } else {
            sender.sendMessage("Usage: /listspecies [player]");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("setspecies")) {
            if (!sender.hasPermission("rol.setspecies")) {
                return Collections.emptyList();
            }

            if (args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], speciesManager.getAllSpecies(), new ArrayList<>());
            } else if (args.length == 2) {
                List<String> playerNames = new ArrayList<>();
                plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> playerNames.add(onlinePlayer.getName()));
                return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
            }
        } else if (command.getName().equalsIgnoreCase("clearspecies")) {
            if (!sender.hasPermission("rol.clearspecies")) {
                return Collections.emptyList();
            }

            if (args.length == 1) {
                List<String> playerNames = new ArrayList<>();
                plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> playerNames.add(onlinePlayer.getName()));
                return StringUtil.copyPartialMatches(args[0], playerNames, new ArrayList<>());
            }
        } else if (command.getName().equalsIgnoreCase("listspecies")) {
            if (!sender.hasPermission("rol.listspecies")) {
                return Collections.emptyList();
            }

            if (args.length == 1) {
                List<String> playerNames = new ArrayList<>();
                plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> playerNames.add(onlinePlayer.getName()));
                return StringUtil.copyPartialMatches(args[0], playerNames, new ArrayList<>());
            }
        }

        return Collections.emptyList();
    }
}