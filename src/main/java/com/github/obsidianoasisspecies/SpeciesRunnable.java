package com.github.obsidianoasisspecies;

import com.github.obsidianoasisspecies.species.Species;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;
import org.bukkit.scoreboard.*;
import io.papermc.paper.world.MoonPhase;

public class SpeciesRunnable extends BukkitRunnable {
    private final ObsidianOasisSpecies plugin;
    
    public SpeciesRunnable(ObsidianOasisSpecies plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void run() {

        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Species species = plugin.getPlayerSpecies(player);
            player.setScoreboard(scoreboard);

            if (species != null) {
                Team team = scoreboard.registerNewTeam(player.getName() + species.getName())
                team.setPrefix(species.getName());
                applyConditionalEffects(player, species);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void applyConditionalEffects(Player player, Species species) {
        World world = player.getWorld();
        long time = world.getTime();
        boolean isDay = time > 23000 || time < 13000;
        MoonPhase moonPhase = world.getMoonPhase();
        boolean isVampireOrNightCreature = species == Species.VAMPIRE || species == Species.NIGHTCREATURE;
        boolean isWerewolf = species == Species.WEREWOLF;

        if ((isVampireOrNightCreature || isWerewolf) && isDay || (isWerewolf && moonPhase != MoonPhase.FULL_MOON)) {
            player.setMaxHealth(species.getMaxHearts() / 2);
        } else {
            player.setMaxHealth(species.getMaxHearts());
        }

        if (isDay && isVampireOrNightCreature && isUnderSunlight(player)) {
            player.setFireTicks(20);
        } else {
            applyPotionEffects(player, species);
        }

    }

    private void applyPotionEffects(Player player, Species species) {
        for (PotionEffectType effect : species.getPotionEffects()) {
            player.addPotionEffect(new PotionEffect(effect, 12 * 20, 1));
        }
    }

    private boolean isUnderSunlight(Player player) {
        Location location = player.getLocation();
        World world = player.getWorld();

        for (int y = location.getBlockY() + 1; y <= world.getMaxHeight(); y++) {
            Block block = world.getBlockAt(location.getBlockX(), y, location.getBlockZ());
            if (block.getType().isSolid()) {
                return false; 
            }
        }

        return true;
    }
}