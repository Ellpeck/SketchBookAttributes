package de.ellpeck.sketchbookattributes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AttributeData implements ICapabilitySerializable<CompoundNBT> {

    public static final int MAX_LEVEL = 100;

    public int pointsToNextLevel;
    public int level;
    public int strength;
    public int dexterity;
    public int constitution;
    public int intelligence;
    public int agility;
    public int mana = 20;

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
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == SketchBookAttributes.ATTRIBUTE_DATA_CAPABILITY ? this.lazyThis.cast() : LazyOptional.empty();
    }

    public PacketHandler.SyncAttributes getPacket() {
        return new PacketHandler.SyncAttributes(this.player.getUUID(), this.serializeNBT());
    }

    public static AttributeData get(PlayerEntity player) {
        return player.getCapability(SketchBookAttributes.ATTRIBUTE_DATA_CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Player " + player + " does not have the attribute data capability"));
    }
}
