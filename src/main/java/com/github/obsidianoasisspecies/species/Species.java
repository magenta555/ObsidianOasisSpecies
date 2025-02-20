package com.github.obsidianoasisspecies.species;
public enum Species {
    HUMAN("§7Human"),
    WEREWOLF("§6Werewolf"),
    VAMPIRE("§4Vampire"),
    NIGHTCREATURE("§bNight Creature"),
    SOULFORGER("§8Soul Forger"),
    MERFOLK("§9Merfolk");
    private final String name;
    Species(String name) {
          this.name = name;
   }
   public String getName() {     
          return name; 
   }
}