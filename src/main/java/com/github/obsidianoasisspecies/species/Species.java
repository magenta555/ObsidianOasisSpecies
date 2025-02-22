package com.github.obsidianoasisspecies.species;

import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public enum Species {
    HUMAN("§7Human", "§7", 9, Arrays.asList(PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffectType.SPEED, PotionEffectType.JUMP_BOOST)),
    MERFOLK("§9Merfolk", "§9", 12, Arrays.asList(PotionEffectType.WATER_BREATHING, PotionEffectType.CONDUIT_POWER, PotionEffectType.DOLPHINS_GRACE)),
    NIGHTCREATURE("§bNight Creature", "§b", 8, Arrays.asList(PotionEffectType.NIGHT_VISION, PotionEffectType.SPEED, PotionEffectType.JUMP_BOOST)),
    SOULFORGER("§8Soul Forger", "§8", 11, Arrays.asList(PotionEffectType.FIRE_RESISTANCE, PotionEffectType.REGENERATION, PotionEffectType.HASTE)),
    VAMPIRE("§4Vampire", "§4", 13, Arrays.asList(PotionEffectType.NIGHT_VISION, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.STRENGTH)),
    WEREWOLF("§6Werewolf", "§6", 18, Arrays.asList(PotionEffectType.STRENGTH, PotionEffectType.REGENERATION, PotionEffectType.NIGHT_VISION));

    private final String name;
    private final String chatColor;
    private final int maxHearts;
    private final List<PotionEffectType> potionEffects;

    Species(String name, String chatColor, int maxHearts, List<PotionEffectType> potionEffects) {
        this.name = name;
        this.chatColor = chatColor;
        this.maxHearts = maxHearts;
        this.potionEffects = potionEffects;
    }

    public String getName() {
        return name;
    }

    public String getChatColor() {
        return chatColor;
    }

    public int getMaxHearts() {
        return maxHearts * 2;
    }

    public List<PotionEffectType> getPotionEffects() {
        return potionEffects;
    }
}
