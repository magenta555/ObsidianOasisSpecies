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
import io.papermc.paper.world.MoonPhase;

public class SpeciesRunnable extends BukkitRunnable {
    private final ObsidianOasisSpecies plugin;

    public SpeciesRunnable(ObsidianOasisSpecies plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Species species = plugin.getPlayerSpecies(player);

            if (species != null) {
                applyConditionalEffects(player, species);
            }
        }
    }

    private void applyConditionalEffects(Player player, Species species) {
        World world = player.getWorld();
        long time = world.getTime();
        boolean isDay = time > 23000 || time < 13000;
        MoonPhase moonPhase = world.getMoonPhase();
        
        // Set max health based on species and conditions
        if ((species == Species.VAMPIRE || species == Species.NIGHTCREATURE || species == Species.WEREWOLF) && isDay) {
            player.setMaxHealth(species.getMaxHearts() / 2); // Half health during the day
        } else if (species == Species.WEREWOLF && moonPhase != MoonPhase.FULL_MOON) {
            player.setMaxHealth(species.getMaxHearts() / 2);
        } else {
            player.setMaxHealth(species.getMaxHearts()); // Normal max health for other species
        }

        // Apply Vampire and Night Creature effects only at night
        if ((species == Species.VAMPIRE || species == Species.NIGHTCREATURE)) {
            if (isDay) {
                if (isUnderSunlight(player)) {
                    player.setFireTicks(20);
                }
            } else {
                for (PotionEffectType effect : species.getPotionEffects()) {
                    player.addPotionEffect(new PotionEffect(effect, 12 * 20, 1));
                }
            }
        }

        // Apply Werewolf effects only during a full moon
        if (species == Species.WEREWOLF && !isDay && moonPhase == MoonPhase.FULL_MOON) {
            for (PotionEffectType effect : species.getPotionEffects()) {
                player.addPotionEffect(new PotionEffect(effect, 12 * 20, 1));
            }
        }
        // Apply other species' effects regardless of time
        if (species != Species.VAMPIRE && species != Species.NIGHTCREATURE && species != Species.WEREWOLF){
            for (PotionEffectType effect : species.getPotionEffects()) {
                player.addPotionEffect(new PotionEffect(effect, 12 * 20, 1));
            }
        }
    }

    private boolean isUnderSunlight(Player player) {
        Location location = player.getLocation();
        World world = player.getWorld();
        int highestBlockY = world.getHighestBlockYAt(location);

        for (int y = location.getBlockY() + 1; y <= world.getMaxHeight(); y++) {
            Block block = world.getBlockAt(location.getBlockX(), y, location.getBlockZ());
            if (block.getType().isSolid()) {
                return false; // If any solid block is found above, they are not in direct sunlight.
            }
        }

        return true; // If no solid blocks are found above, they are exposed to sunlight.
    }
}