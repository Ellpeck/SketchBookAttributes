package de.ellpeck.sketchbookattributes.data;

public enum PlayerClass {

    FIGHTER(15, 0, 10, 0, 0),
    RANGER(0, 20, 0, 0, 10),
    SPELLCASTER(0, 5, 0, 15, 5);

    public final int strengthBonus;
    public final int dexterityBonus;
    public final int constitutionBonus;
    public final int intelligenceBonus;
    public final int agilityBonus;

    PlayerClass(int strengthBonus, int dexterityBonus, int constitutionBonus, int intelligenceBonus, int agilityBonus) {
        this.strengthBonus = strengthBonus;
        this.dexterityBonus = dexterityBonus;
        this.constitutionBonus = constitutionBonus;
        this.intelligenceBonus = intelligenceBonus;
        this.agilityBonus = agilityBonus;
    }
}
