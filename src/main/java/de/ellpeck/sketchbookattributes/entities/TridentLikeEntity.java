package de.ellpeck.sketchbookattributes.entities;

import de.ellpeck.sketchbookattributes.SketchBookAttributes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class TridentLikeEntity extends TridentEntity implements IEntityAdditionalSpawnData {

    private ItemStack displayItem;

    public TridentLikeEntity(EntityType<? extends TridentEntity> p_i50148_1_, World p_i50148_2_) {
        super(p_i50148_1_, p_i50148_2_);
    }

    public TridentLikeEntity(World p_i48790_1_, LivingEntity p_i48790_2_, ItemStack p_i48790_3_) {
        super(p_i48790_1_, p_i48790_2_, p_i48790_3_);
    }

    public TridentLikeEntity(World p_i48791_1_, double p_i48791_2_, double p_i48791_4_, double p_i48791_6_) {
        super(p_i48791_1_, p_i48791_2_, p_i48791_4_, p_i48791_6_);
    }

    @Override
    public EntityType<?> getType() {
        return SketchBookAttributes.TRIDENT_LIKE.get();
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public ItemStack getDisplayItem() {
        return this.displayItem == null ? this.getPickupItem() : this.displayItem;
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeItem(this.getDisplayItem());
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.displayItem = buffer.readItem();
    }
}
