package de.ellpeck.sketchbookattributes.data;

import de.ellpeck.sketchbookattributes.SketchBookAttributes;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public class PlayerAttributes implements INBTSerializable<CompoundNBT> {

    public static final int MAX_LEVEL = 100;

    private static final UUID MELEE_DAMAGE_ATTRIBUTE = UUID.fromString("46f4847e-e70c-4cf5-9c6c-6bb5057c5c25");
    private static final UUID MELEE_SPEED_ATTRIBUTE = UUID.fromString("04971c8a-62d5-4f01-b67f-d55a29e4482c");
    private static final UUID MAX_HEALTH_ATTRIBUTE = UUID.fromString("dd9a5f51-97c5-4a48-b26d-8089e52e2096");
    private static final UUID MOVE_SPEED_ATTRIBUTE = UUID.fromString("f32d97d8-274e-47f3-9cd1-80004bfd2e2d");

    public PlayerClass playerClass;
    public int pointsToNextLevel;
    public int level;
    public int strength;
    public int dexterity;
    public int constitution;
    public int intelligence;
    public int agility;
    public float mana;
    public int skillPoints;

    public PlayerAttributes() {
        this.mana = this.getMaxMana();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (this.playerClass != null)
            nbt.putInt("player_class", this.playerClass.ordinal());
        nbt.putInt("points_to_next_level", this.pointsToNextLevel);
        nbt.putInt("level", this.level);
        nbt.putInt("strength", this.strength);
        nbt.putInt("dexterity", this.dexterity);
        nbt.putInt("constitution", this.constitution);
        nbt.putInt("intelligence", this.intelligence);
        nbt.putInt("agility", this.agility);
        nbt.putFloat("mana", this.mana);
        nbt.putInt("skill_points", this.skillPoints);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.playerClass = nbt.contains("player_class") ? PlayerClass.values()[nbt.getInt("player_class")] : null;
        this.pointsToNextLevel = nbt.getInt("points_to_next_level");
        this.level = nbt.getInt("level");
        this.strength = nbt.getInt("strength");
        this.dexterity = nbt.getInt("dexterity");
        this.constitution = nbt.getInt("constitution");
        this.intelligence = nbt.getInt("intelligence");
        this.agility = nbt.getInt("agility");
        this.mana = nbt.getFloat("mana");
        this.skillPoints = nbt.getInt("skill_points");
    }

    // copied from PlayerEntity and adapted to be slightly higher
    public int getXpNeededForNextLevel() {
        if (this.level >= 30) {
            return 150 + (this.level - 30) * 18;
        } else {
            return this.level >= 15 ? 50 + (this.level - 15) * 10 : 25 + this.level * 5;
        }
    }

    public float getHealthRegenPerSecond() {
        return this.constitution * SketchBookAttributes.healthRegenPerLevel.get().floatValue();
    }

    public float getMaxMana() {
        return 20 + this.intelligence * SketchBookAttributes.manaBonusPerLevel.get().floatValue();
    }

    public float getManaRegenPerSecond() {
        return 1 / 1.5F + this.intelligence * SketchBookAttributes.manaRegenPerLevel.get().floatValue();
    }

    public float getMeleeDamageBonus() {
        return this.strength * SketchBookAttributes.meleeDamagePerLevel.get().floatValue();
    }

    public float getRangedDamageBonus() {
        return this.dexterity * SketchBookAttributes.rangedDamagePerLevel.get().floatValue();
    }

    public float getHealthBonus() {
        return this.constitution * SketchBookAttributes.healthBonusPerLevel.get().floatValue();
    }

    public float getMeleeSpeedBonus() {
        return this.agility * SketchBookAttributes.meleeSpeedPerLevel.get().floatValue();
    }

    public float getMovementSpeedBonus() {
        return this.agility * SketchBookAttributes.movementSpeedPerLevel.get().floatValue();
    }

    public void reapplyAttributes(PlayerEntity player) {
        if (player.level.isClientSide)
            return;
        this.reapplyAttribute(player, Attributes.ATTACK_DAMAGE, MELEE_DAMAGE_ATTRIBUTE, this.getMeleeDamageBonus());
        this.reapplyAttribute(player, Attributes.MAX_HEALTH, MAX_HEALTH_ATTRIBUTE, this.getHealthBonus());
        this.reapplyAttribute(player, Attributes.ATTACK_SPEED, MELEE_SPEED_ATTRIBUTE, this.getMeleeSpeedBonus());
        this.reapplyAttribute(player, Attributes.MOVEMENT_SPEED, MOVE_SPEED_ATTRIBUTE, this.getMovementSpeedBonus());
    }

    public boolean gainXp(float amount) {
        if (this.level >= MAX_LEVEL)
            return false;
        this.pointsToNextLevel += amount;
        while (this.pointsToNextLevel >= this.getXpNeededForNextLevel()) {
            this.pointsToNextLevel -= this.getXpNeededForNextLevel();
            if (this.level < MAX_LEVEL) {
                this.level++;
                this.skillPoints++;
            } else {
                this.pointsToNextLevel = 0;
            }
        }
        return true;
    }

    private void reapplyAttribute(PlayerEntity player, Attribute type, UUID id, float value) {
        ModifiableAttributeInstance instance = player.getAttribute(type);
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, type.getRegistryName() + " bonus", value, AttributeModifier.Operation.ADDITION));
    }
}
