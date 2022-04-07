package de.ellpeck.sketchbookattributes.mixin;

import de.ellpeck.sketchbookattributes.items.GreatswordItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class DisableShieldMixin extends LivingEntity {

    protected DisableShieldMixin(EntityType<? extends MobEntity> p_i48576_1_, World p_i48576_2_) {
        super(p_i48576_1_, p_i48576_2_);
    }

    @Inject(at = @At("HEAD"), method = "maybeDisableShield")
    private void maybeDisableShield(PlayerEntity p_233655_1_, ItemStack p_233655_2_, ItemStack p_233655_3_, CallbackInfo callback) {
        // this is just a copy of the original, but using GreatswordItem instead of AxeItem
        if (!p_233655_2_.isEmpty() && !p_233655_3_.isEmpty() && p_233655_2_.getItem() instanceof GreatswordItem && p_233655_3_.getItem() == Items.SHIELD) {
            float f = 0.25F + (float) EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
            if (this.random.nextFloat() < f) {
                p_233655_1_.getCooldowns().addCooldown(Items.SHIELD, 100);
                this.level.broadcastEntityEvent(p_233655_1_, (byte) 30);
            }
        }
    }
}
