package de.ellpeck.sketchbookattributes;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class AttributeData implements ICapabilitySerializable<CompoundNBT> {

    public static final int MAX_LEVEL = 100;

    private static final UUID MELEE_DAMAGE_ATTRIBUTE = UUID.fromString("46f4847e-e70c-4cf5-9c6c-6bb5057c5c25");
    private static final UUID MELEE_SPEED_ATTRIBUTE = UUID.fromString("04971c8a-62d5-4f01-b67f-d55a29e4482c");
    private static final UUID MAX_HEALTH_ATTRIBUTE = UUID.fromString("dd9a5f51-97c5-4a48-b26d-8089e52e2096");
    private static final UUID MOVE_SPEED_ATTRIBUTE = UUID.fromString("f32d97d8-274e-47f3-9cd1-80004bfd2e2d");

    public int pointsToNextLevel;
    public int level;
    public int strength;
    public int dexterity;
    public int constitution;
    public int intelligence;
    public int agility;
    public int mana = 20;
    public int skillPoints;

    private final PlayerEntity player;
    private final LazyOptional<AttributeData> lazyThis;

    public AttributeData(PlayerEntity player) {
        this.player = player;
        this.lazyThis = LazyOptional.of(() -> this);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("points_to_next_level", this.pointsToNextLevel);
        nbt.putInt("level", this.level);
        nbt.putInt("strength", this.strength);
        nbt.putInt("dexterity", this.dexterity);
        nbt.putInt("constitution", this.constitution);
        nbt.putInt("intelligence", this.intelligence);
        nbt.putInt("agility", this.agility);
        nbt.putInt("mana", this.mana);
        nbt.putInt("skill_points", this.skillPoints);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.pointsToNextLevel = nbt.getInt("points_to_next_level");
        this.level = nbt.getInt("level");
        this.strength = nbt.getInt("strength");
        this.dexterity = nbt.getInt("dexterity");
        this.constitution = nbt.getInt("constitution");
        this.intelligence = nbt.getInt("intelligence");
        this.agility = nbt.getInt("agility");
        this.mana = nbt.getInt("mana");
        this.skillPoints = nbt.getInt("skill_points");

        this.reapplyAttributes();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == SketchBookAttributes.ATTRIBUTE_DATA_CAPABILITY ? this.lazyThis.cast() : LazyOptional.empty();
    }

    public PacketHandler.SyncAttributes getPacket() {
        return new PacketHandler.SyncAttributes(this.player.getUUID(), this.serializeNBT());
    }

    // copied from PlayerEntity and adapted to be slightly higher
    public int getXpNeededForNextLevel() {
        if (this.level >= 30) {
            return 150 + (this.level - 30) * 18;
        } else {
            return this.level >= 15 ? 50 + (this.level - 15) * 10 : 25 + this.level * 5;
        }
    }

    // TODO use this and mana regen in tick event
    public float getHealthRegenPerSecond() {
        return this.constitution * SketchBookAttributes.healthRegenPerLevel.get();
    }

    public float getManaRegenPerSecond() {
        return 1 / 1.5F + this.intelligence * SketchBookAttributes.manaRegenPerLevel.get();
    }

    public float getMeleeDamageBonus() {
        return this.strength * SketchBookAttributes.meleeDamagePerLevel.get();
    }

    // TODO apply this to arrows when they spawn, bleh
    public float getRangedDamageBonus() {
        return this.dexterity * SketchBookAttributes.rangedDamagePerLevel.get();
    }

    public float getHealthBonus() {
        return this.constitution * SketchBookAttributes.healthBonusPerLevel.get();
    }

    public float getMeleeSpeedBonus() {
        return this.agility * SketchBookAttributes.meleeSpeedPerLevel.get();
    }

    // TODO apply this to the bow drawing speed using ArrowLooseEvent
    public float getRangedSpeedBonus() {
        return this.agility * SketchBookAttributes.rangedSpeedPerLevel.get();
    }

    public float getMovementSpeedBonus() {
        return this.agility * SketchBookAttributes.movementSpeedPerLevel.get();
    }

    public void reapplyAttributes() {
        if (this.player.level.isClientSide)
            return;
        this.reapplyAttribute(Attributes.ATTACK_DAMAGE, MELEE_DAMAGE_ATTRIBUTE, this.getMeleeDamageBonus());
        this.reapplyAttribute(Attributes.MAX_HEALTH, MAX_HEALTH_ATTRIBUTE, this.getHealthBonus());
        this.reapplyAttribute(Attributes.ATTACK_SPEED, MELEE_SPEED_ATTRIBUTE, this.getMeleeSpeedBonus());
        this.reapplyAttribute(Attributes.MOVEMENT_SPEED, MOVE_SPEED_ATTRIBUTE, this.getMovementSpeedBonus());
    }

    public void gainXp(float amount) {
        if (this.level >= MAX_LEVEL)
            return;
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
    }

    private void reapplyAttribute(Attribute type, UUID id, float value) {
        ModifiableAttributeInstance instance = this.player.getAttribute(type);
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, type.getRegistryName() + " bonus", value, Operation.ADDITION));
    }

    public static AttributeData get(PlayerEntity player) {
        return player.getCapability(SketchBookAttributes.ATTRIBUTE_DATA_CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Player " + player + " does not have the attribute data capability"));
    }
}
