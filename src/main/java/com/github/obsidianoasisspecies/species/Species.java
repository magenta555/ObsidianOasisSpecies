package com.github.obsidianoasisspecies.species;

import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public enum Species {
    HUMAN("§7Human", "§7", 20, Arrays.asList(PotionEffectType.REGENERATION, PotionEffectType.SPEED, PotionEffectType.JUMP_BOOST)),
    WEREWOLF("§6Werewolf", "§6", 24, Arrays.asList(PotionEffectType.STRENGTH, PotionEffectType.REGENERATION, PotionEffectType.NIGHT_VISION)),
    VAMPIRE("§4Vampire", "§4", 16, Arrays.asList(PotionEffectType.INVISIBILITY, PotionEffectType.SPEED, PotionEffectType.STRENGTH)),
    NIGHTCREATURE("§bNight Creature", "§b", 18, Arrays.asList(PotionEffectType.NIGHT_VISION, PotionEffectType.SPEED, PotionEffectType.JUMP_BOOST)),
    SOULFORGER("§8Soul Forger", "§8", 22, Arrays.asList(PotionEffectType.FIRE_RESISTANCE, PotionEffectType.REGENERATION, PotionEffectType.HASTE)),
    MERFOLK("§9Merfolk", "§9", 18, Arrays.asList(PotionEffectType.WATER_BREATHING, PotionEffectType.SPEED, PotionEffectType.JUMP_BOOST));

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
        return maxHearts;
    }

    public List<PotionEffectType> getPotionEffects() {
        return potionEffects;
    }
}
