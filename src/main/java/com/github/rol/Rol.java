package com.github.rol;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.Set;

public class Rol extends JavaPlugin {

    @Override
    public void onEnable() {
        saveResource("config.yml", false);
        getServer().getPluginManager().registerEvents(new Vampire(this), this);
        this.getCommand("rol").setExecutor(this);
        this.getCommand("r").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (a.length == 0) {
            s.sendMessage("§d[Rol] Rol, version 0, created by magenta555");
            s.sendMessage("§d[Rol] Roleplaying plugin for Minecraft servers!");
            return true;
        }

        if (a[0].equals("reload")) {
            reloadConfig();
            s.sendMessage("§d[Rol] Reloaded Rol config.yml");
            return true;
        }

        if (a[0].equals("species")) {
            if (a.length == 3 && a[1].equals("vampire")) {
                Player target = Bukkit.getPlayer(a[2]);
                if (target != null) {
                    assignRole(target, "vampire");
                    s.sendMessage("§d[Rol] " + target.getName() + " is now a vampire!");
                } else {
                    s.sendMessage("§c[Rol] Player " + a[2] + " not found!");
                }
                return true;
            } else {
                s.sendMessage("§c[Rol] Usage: /rol species vampire [player]");
                return true;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
        List<String> tabComplete = new ArrayList<String>();

        if (a.length == 1) {
            tabComplete.add("help");
            tabComplete.add("reload");
            tabComplete.add("species");
        }

        if (a.length == 2 && a[0].equals("species")) {
            tabComplete.add("vampire");
        }

        if (a.length == 3 && a[0].equals("species") && a[1].equals("vampire")) {
            // Add online player names for tab completion
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                tabComplete.add(onlinePlayer.getName());
            }
        }

        return tabComplete;
    }

    // Method to assign a role to a player, storing it in the config
    public void assignRole(Player player, String role) {
        FileConfiguration config = getConfig();
        config.set("players." + player.getName(), role);
        saveConfig();
    }

    // Method to get a player's role, retrieving it from the config
    public String getPlayerRole(Player player) {
        FileConfiguration config = getConfig();
        return config.getString("players." + player.getName(), "human"); // Default to "human" if no role is assigned
    }
    
    // Method to load player roles from the config on plugin enable
    @Override
    public void onLoad() {
        FileConfiguration config = getConfig();
        if (config.getConfigurationSection("players") != null) {
            Set<String> playerNames = config.getConfigurationSection("players").getKeys(false);
            for (String playerName : playerNames) {
                String role = config.getString("players." + playerName);
                getLogger().info("Loaded role " + role + " for player " + playerName + " from config.");
            }
        } else {
            getLogger().info("No player roles found in config.");
        }
    }
}