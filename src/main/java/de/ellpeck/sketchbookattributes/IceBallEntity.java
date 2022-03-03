package de.ellpeck.sketchbookattributes;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class IceBallEntity extends AbstractFireballEntity {

    public IceBallEntity(EntityType<IceBallEntity> p_i50166_1_, World p_i50166_2_) {
        super(p_i50166_1_, p_i50166_2_);
    }

    public IceBallEntity(EntityType<IceBallEntity> p_i50168_1_, LivingEntity p_i50168_2_, double p_i50168_3_, double p_i50168_5_, double p_i50168_7_, World p_i50168_9_) {
        super(p_i50168_1_, p_i50168_2_, p_i50168_3_, p_i50168_5_, p_i50168_7_, p_i50168_9_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack getItem() {
        ItemStack itemstack = this.getItemRaw();
        return itemstack.isEmpty() ? new ItemStack(Items.SNOWBALL) : itemstack;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource p_70097_1_, float p_70097_2_) {
        return false;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected IParticleData getTrailParticle() {
        return ParticleTypes.ITEM_SNOWBALL;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult ray) {
        super.onHitBlock(ray);
        if (!this.level.isClientSide) {
            BlockPos blockpos = ray.getBlockPos().relative(ray.getDirection());
            if (this.level.isEmptyBlock(blockpos))
                this.level.setBlockAndUpdate(blockpos, Blocks.SNOW.defaultBlockState());
        }
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult ray) {
        super.onHitEntity(ray);
        if (!this.level.isClientSide) {
            Entity entity = ray.getEntity();
            entity.hurt(DamageSource.thrown(this, this.getOwner()), 5);
            if (entity instanceof LivingEntity)
                ((LivingEntity) entity).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 3 * 20, 0));
        }
    }

    @Override
    protected void onHit(RayTraceResult p_70227_1_) {
        super.onHit(p_70227_1_);
        if (!this.level.isClientSide) {
            this.remove();
        }

    }

}
