package de.ellpeck.sketchbookattributes.data;

import de.ellpeck.sketchbookattributes.SketchBookAttributes;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerAttributes implements INBTSerializable<CompoundNBT> {

    public static final int MAX_LEVEL = 100;

    private static final UUID MELEE_DAMAGE_ATTRIBUTE = UUID.fromString("46f4847e-e70c-4cf5-9c6c-6bb5057c5c25");
    private static final UUID MELEE_SPEED_ATTRIBUTE = UUID.fromString("04971c8a-62d5-4f01-b67f-d55a29e4482c");
    private static final UUID MAX_HEALTH_ATTRIBUTE = UUID.fromString("dd9a5f51-97c5-4a48-b26d-8089e52e2096");
    private static final UUID MOVE_SPEED_ATTRIBUTE = UUID.fromString("f32d97d8-274e-47f3-9cd1-80004bfd2e2d");
    private static final UUID ARMOR_ATTRIBUTE = UUID.fromString("b693d319-e042-473f-a401-e36ac9af894f");

    public PlayerClass playerClass;
    public int pointsToNextLevel;
    public int level;
    public float mana;
    public int skillPoints;
    public int addedStrength;
    public int addedDexterity;
    public int addedConstitution;
    public int addedIntelligence;
    public int addedAgility;

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
        nbt.putInt("strength", this.addedStrength);
        nbt.putInt("dexterity", this.addedDexterity);
        nbt.putInt("constitution", this.addedConstitution);
        nbt.putInt("intelligence", this.addedIntelligence);
        nbt.putInt("agility", this.addedAgility);
        nbt.putFloat("mana", this.mana);
        nbt.putInt("skill_points", this.skillPoints);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.playerClass = nbt.contains("player_class") ? PlayerClass.values()[nbt.getInt("player_class")] : null;
        this.pointsToNextLevel = nbt.getInt("points_to_next_level");
        this.level = nbt.getInt("level");
        this.addedStrength = nbt.getInt("strength");
        this.addedDexterity = nbt.getInt("dexterity");
        this.addedConstitution = nbt.getInt("constitution");
        this.addedIntelligence = nbt.getInt("intelligence");
        this.addedAgility = nbt.getInt("agility");
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

    public int getStrength() {
        return this.addedStrength + (this.playerClass != null ? this.playerClass.strengthBonus : 0);
    }

    public int getDexterity() {
        return this.addedDexterity + (this.playerClass != null ? this.playerClass.dexterityBonus : 0);
    }

    public int getConstitution() {
        return this.addedConstitution + (this.playerClass != null ? this.playerClass.constitutionBonus : 0);
    }

    public int getIntelligence() {
        return this.addedIntelligence + (this.playerClass != null ? this.playerClass.intelligenceBonus : 0);
    }

    public int getAgility() {
        return this.addedAgility + (this.playerClass != null ? this.playerClass.agilityBonus : 0);
    }

    public float getHealthRegenPerSecond() {
        return this.getConstitution() * SketchBookAttributes.healthRegenPerLevel.get().floatValue();
    }

    public float getMaxMana() {
        return 20 + this.getIntelligence() * SketchBookAttributes.manaBonusPerLevel.get().floatValue();
    }

    public float getManaRegenPerSecond() {
        return 1 / 1.5F + this.getIntelligence() * SketchBookAttributes.manaRegenPerLevel.get().floatValue();
    }

    public float getMeleeDamageBonus() {
        return this.getStrength() * SketchBookAttributes.meleeDamagePerLevel.get().floatValue();
    }

    public float getRangedDamageBonus() {
        return this.getDexterity() * SketchBookAttributes.rangedDamagePerLevel.get().floatValue();
    }

    public float getHealthBonus() {
        return this.getConstitution() * SketchBookAttributes.healthBonusPerLevel.get().floatValue();
    }

    public float getMeleeSpeedBonus() {
        return this.getAgility() * SketchBookAttributes.meleeSpeedPerLevel.get().floatValue();
    }

    public float getMovementSpeedBonus() {
        return this.getAgility() * SketchBookAttributes.movementSpeedPerLevel.get().floatValue();
    }

    public void reapplyAttributes(PlayerEntity player) {
        if (player.level.isClientSide)
            return;
        this.reapplyAttribute(player, Attributes.ATTACK_DAMAGE, MELEE_DAMAGE_ATTRIBUTE, this.getMeleeDamageBonus());
        this.reapplyAttribute(player, Attributes.MAX_HEALTH, MAX_HEALTH_ATTRIBUTE, this.getHealthBonus());
        this.reapplyAttribute(player, Attributes.ATTACK_SPEED, MELEE_SPEED_ATTRIBUTE, this.getMeleeSpeedBonus());
        this.reapplyAttribute(player, Attributes.MOVEMENT_SPEED, MOVE_SPEED_ATTRIBUTE, this.getMovementSpeedBonus());
        this.reapplyAttribute(player, Attributes.ARMOR, ARMOR_ATTRIBUTE, this.playerClass == PlayerClass.FIGHTER ? 8 : 0);
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

    public boolean canUseItem(ItemStack stack) {
        for (String config : SketchBookAttributes.attributeItemRequirements.get()) {
            Matcher matcher = SketchBookAttributes.ITEM_REQUIREMENT_REGEX.matcher(config);
            if (!matcher.matches() || !Pattern.matches(matcher.group(1), stack.getItem().getRegistryName().toString()))
                continue;
            int amount = Integer.parseInt(matcher.group(3));
            switch (matcher.group(2)) {
                case "strength":
                    return this.getStrength() >= amount;
                case "dexterity":
                    return this.getDexterity() >= amount;
                case "constitution":
                    return this.getConstitution() >= amount;
                case "intelligence":
                    return this.getIntelligence() >= amount;
                case "agility":
                    return this.getAgility() >= amount;
            }
        }
        return true;
    }

    private void reapplyAttribute(PlayerEntity player, Attribute type, UUID id, float value) {
        ModifiableAttributeInstance instance = player.getAttribute(type);
        instance.removeModifier(id);
        if (value != 0)
            instance.addTransientModifier(new AttributeModifier(id, type.getRegistryName() + " bonus", value, AttributeModifier.Operation.ADDITION));
    }

    public static boolean canUseHeldItem(PlayerEntity player, boolean displayMessage) {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty())
            return true;
        PlayerAttributes attributes = AttributeData.get(player.level).getAttributes(player);
        if (attributes.canUseItem(stack))
            return true;
        if (displayMessage)
            player.displayClientMessage(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".requirements_not_met"), true);
        return false;
    }
}
