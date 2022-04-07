package de.ellpeck.sketchbookattributes.items;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

import java.util.List;

// absolute trainwreck of a class but ok
public class GildedCrossbowItem extends CrossbowItem {

    public boolean startSoundPlayed = false;
    public boolean midLoadSoundPlayed = false;

    public GildedCrossbowItem() {
        super(new Properties().tab(ItemGroup.TAB_COMBAT).durability(800));
    }

    // copy of super with performShooting changed to ours
    @Override
    public ActionResult<ItemStack> use(World p_77659_1_, PlayerEntity p_77659_2_, Hand p_77659_3_) {
        ItemStack itemstack = p_77659_2_.getItemInHand(p_77659_3_);
        if (isCharged(itemstack)) {
            performGildedShooting(p_77659_1_, p_77659_2_, p_77659_3_, itemstack, getShootingPower(itemstack), 1.0F);
            setCharged(itemstack, false);
            return ActionResult.consume(itemstack);
        } else if (!p_77659_2_.getProjectile(itemstack).isEmpty()) {
            if (!isCharged(itemstack)) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
                p_77659_2_.startUsingItem(p_77659_3_);
            }

            return ActionResult.consume(itemstack);
        } else {
            return ActionResult.fail(itemstack);
        }
    }

    // copy of super with getChargeDuration changed to ours
    @Override
    public void onUseTick(World p_219972_1_, LivingEntity p_219972_2_, ItemStack p_219972_3_, int p_219972_4_) {
        if (!p_219972_1_.isClientSide) {
            int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, p_219972_3_);
            SoundEvent soundevent = this.getStartSound(i);
            SoundEvent soundevent1 = i == 0 ? SoundEvents.CROSSBOW_LOADING_MIDDLE : null;
            float f = (float) (p_219972_3_.getUseDuration() - p_219972_4_) / (float) getGildedChargeDuration(p_219972_3_);
            if (f < 0.2F) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
            }

            if (f >= 0.2F && !this.startSoundPlayed) {
                this.startSoundPlayed = true;
                p_219972_1_.playSound(null, p_219972_2_.getX(), p_219972_2_.getY(), p_219972_2_.getZ(), soundevent, SoundCategory.PLAYERS, 0.5F, 1.0F);
            }

            if (f >= 0.5F && soundevent1 != null && !this.midLoadSoundPlayed) {
                this.midLoadSoundPlayed = true;
                p_219972_1_.playSound(null, p_219972_2_.getX(), p_219972_2_.getY(), p_219972_2_.getZ(), soundevent1, SoundCategory.PLAYERS, 0.5F, 1.0F);
            }
        }

    }

    // copy of super with getPowerForTime changed to ours
    @Override
    public void releaseUsing(ItemStack p_77615_1_, World p_77615_2_, LivingEntity p_77615_3_, int p_77615_4_) {
        int i = this.getUseDuration(p_77615_1_) - p_77615_4_;
        float f = getGildedPowerForTime(i, p_77615_1_);
        if (f >= 1.0F && !isCharged(p_77615_1_) && tryLoadProjectiles(p_77615_3_, p_77615_1_)) {
            setCharged(p_77615_1_, true);
            SoundCategory soundcategory = p_77615_3_ instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE;
            p_77615_2_.playSound(null, p_77615_3_.getX(), p_77615_3_.getY(), p_77615_3_.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundcategory, 1.0F, 1.0F / (random.nextFloat() * 0.5F + 1.0F) + 0.2F);
        }

    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        // crossbow is very specific and only works with the crossbow item, because why wouldn't it
        return UseAction.BOW;
    }

    public static int getGildedChargeDuration(ItemStack stack) {
        return (int) (getChargeDuration(stack) / 1.2F);
    }

    private static float getGildedPowerForTime(int i, ItemStack stack) {
        float f = (float) i / (float) getGildedChargeDuration(stack);
        return Math.min(f, 1);
    }

    // copy of super with shootProjectile changed to ours
    public static void performGildedShooting(World p_220014_0_, LivingEntity p_220014_1_, Hand p_220014_2_, ItemStack p_220014_3_, float p_220014_4_, float p_220014_5_) {
        List<ItemStack> list = getChargedProjectiles(p_220014_3_);
        float[] afloat = getShotPitches(p_220014_1_.getRandom());

        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemstack = list.get(i);
            boolean flag = p_220014_1_ instanceof PlayerEntity && ((PlayerEntity) p_220014_1_).abilities.instabuild;
            if (!itemstack.isEmpty()) {
                if (i == 0) {
                    shootGildedProjectile(p_220014_0_, p_220014_1_, p_220014_2_, p_220014_3_, itemstack, afloat[i], flag, p_220014_4_, p_220014_5_, 0.0F);
                } else if (i == 1) {
                    shootGildedProjectile(p_220014_0_, p_220014_1_, p_220014_2_, p_220014_3_, itemstack, afloat[i], flag, p_220014_4_, p_220014_5_, -10.0F);
                } else if (i == 2) {
                    shootGildedProjectile(p_220014_0_, p_220014_1_, p_220014_2_, p_220014_3_, itemstack, afloat[i], flag, p_220014_4_, p_220014_5_, 10.0F);
                }
            }
        }

        onCrossbowShot(p_220014_0_, p_220014_1_, p_220014_3_);
    }

    // copy of super with getArrow changed to ours
    private static void shootGildedProjectile(World p_220016_0_, LivingEntity p_220016_1_, Hand p_220016_2_, ItemStack p_220016_3_, ItemStack p_220016_4_, float p_220016_5_, boolean p_220016_6_, float p_220016_7_, float p_220016_8_, float p_220016_9_) {
        if (!p_220016_0_.isClientSide) {
            boolean flag = p_220016_4_.getItem() == Items.FIREWORK_ROCKET;
            ProjectileEntity projectileentity;
            if (flag) {
                projectileentity = new FireworkRocketEntity(p_220016_0_, p_220016_4_, p_220016_1_, p_220016_1_.getX(), p_220016_1_.getEyeY() - (double) 0.15F, p_220016_1_.getZ(), true);
            } else {
                projectileentity = getGildedArrow(p_220016_0_, p_220016_1_, p_220016_3_, p_220016_4_);
                if (p_220016_6_ || p_220016_9_ != 0.0F) {
                    ((AbstractArrowEntity) projectileentity).pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                }
            }

            if (p_220016_1_ instanceof ICrossbowUser) {
                ICrossbowUser icrossbowuser = (ICrossbowUser) p_220016_1_;
                icrossbowuser.shootCrossbowProjectile(icrossbowuser.getTarget(), p_220016_3_, projectileentity, p_220016_9_);
            } else {
                Vector3d vector3d1 = p_220016_1_.getUpVector(1.0F);
                Quaternion quaternion = new Quaternion(new Vector3f(vector3d1), p_220016_9_, true);
                Vector3d vector3d = p_220016_1_.getViewVector(1.0F);
                Vector3f vector3f = new Vector3f(vector3d);
                vector3f.transform(quaternion);
                projectileentity.shoot((double) vector3f.x(), (double) vector3f.y(), (double) vector3f.z(), p_220016_7_, p_220016_8_);
            }

            p_220016_3_.hurtAndBreak(flag ? 3 : 1, p_220016_1_, (p_220017_1_) -> {
                p_220017_1_.broadcastBreakEvent(p_220016_2_);
            });
            p_220016_0_.addFreshEntity(projectileentity);
            p_220016_0_.playSound((PlayerEntity) null, p_220016_1_.getX(), p_220016_1_.getY(), p_220016_1_.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0F, p_220016_5_);
        }
    }

    // copy of super with damage increased
    private static AbstractArrowEntity getGildedArrow(World p_220024_0_, LivingEntity p_220024_1_, ItemStack p_220024_2_, ItemStack p_220024_3_) {
        ArrowItem arrowitem = (ArrowItem) (p_220024_3_.getItem() instanceof ArrowItem ? p_220024_3_.getItem() : Items.ARROW);
        AbstractArrowEntity abstractarrowentity = arrowitem.createArrow(p_220024_0_, p_220024_3_, p_220024_1_);
        if (p_220024_1_ instanceof PlayerEntity) {
            abstractarrowentity.setCritArrow(true);
        }

        abstractarrowentity.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        abstractarrowentity.setShotFromCrossbow(true);
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, p_220024_2_);
        if (i > 0) {
            abstractarrowentity.setPierceLevel((byte) i);
        }

        // change: increased damage by 50%
        abstractarrowentity.setBaseDamage(abstractarrowentity.getBaseDamage() * 1.5);

        return abstractarrowentity;
    }

}
