package com.github.obsidianoasisspecies.species;

import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

/**
 * Enum representing different species in the Obsidian Oasis plugin.
 */
public enum Species {
    HUMAN("§7Human", "§7", 9, Arrays.asList(
        PotionEffectType.HERO_OF_THE_VILLAGE, 
        PotionEffectType.LUCK, 
        PotionEffectType.SPEED)),
    MERFOLK("§9Merfolk", "§9", 12, Arrays.asList(
        PotionEffectType.WATER_BREATHING, 
        PotionEffectType.CONDUIT_POWER, 
        PotionEffectType.DOLPHINS_GRACE)),
    NIGHTCREATURE("§bNight Creature", "§b", 8, Arrays.asList(
        PotionEffectType.INVISIBILITY, 
        PotionEffectType.INFESTED, 
        PotionEffectType.JUMP_BOOST)),
    SOULFORGER("§8Soul Forger", "§8", 11, Arrays.asList(
        PotionEffectType.STRENGTH, 
        PotionEffectType.SLOWNESS, 
        PotionEffectType.HASTE)),
    VAMPIRE("§4Vampire", "§4", 13, Arrays.asList(
        PotionEffectType.NIGHT_VISION, 
        PotionEffectType.REGENERATION, 
        PotionEffectType.SLOW_FALLING)),
    WEREWOLF("§6Werewolf", "§6", 20, Arrays.asList(
        PotionEffectType.GLOWING, 
        PotionEffectType.WIND_CHARGED, 
        PotionEffectType.SATURATION));

    private final String name;
    private final String chatColor;
    private final int maxHearts;
    private final List<PotionEffectType> potionEffects;

    /**
     * Constructor for the Species enum.
     *
     * @param name          The name of the species.
     * @param chatColor     The chat color associated with the species.
     * @param maxHearts     The maximum number of hearts for the species.
     * @param potionEffects The potion effects associated with the species.
     */
    Species(String name, String chatColor, int maxHearts, List<PotionEffectType> potionEffects) {
        this.name = name;
        this.chatColor = chatColor;
        this.maxHearts = maxHearts;
        this.potionEffects = potionEffects;
    }

    /**
     * Gets the name of the species.
     *
     * @return The name of the species.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the chat color associated with the species.
     *
     * @return The chat color.
     */
    public String getChatColor() {
        return chatColor;
    }

    /**
     * Gets the maximum number of hearts for the species.
     * 
     * Note: Minecraft uses half-hearts, so we multiply by 2.
     *
     * @return The maximum number of hearts.
     */
    public int getMaxHearts() {
        return maxHearts * 2;
    }

    /**
     * Gets the potion effects associated with the species.
     *
     * @return The list of potion effects.
     */
    public List<PotionEffectType> getPotionEffects() {
        return potionEffects;
    }
}
