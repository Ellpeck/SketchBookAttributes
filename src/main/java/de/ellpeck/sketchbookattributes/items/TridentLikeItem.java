package de.ellpeck.sketchbookattributes.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import de.ellpeck.sketchbookattributes.entities.TridentLikeEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.stats.Stats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class TridentLikeItem extends TridentItem {

    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public TridentLikeItem(int durability, double attackDamage, double attackSpeed) {
        super(new Properties().tab(ItemGroup.TAB_COMBAT).durability(durability));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType slot) {
        return slot == EquipmentSlotType.MAINHAND ? this.defaultModifiers : ImmutableMultimap.of();
    }

    // copied from base to change the entity being spawned
    @Override
    public void releaseUsing(ItemStack p_77615_1_, World p_77615_2_, LivingEntity p_77615_3_, int p_77615_4_) {
        if (p_77615_3_ instanceof PlayerEntity) {
            PlayerEntity playerentity = (PlayerEntity) p_77615_3_;
            int i = this.getUseDuration(p_77615_1_) - p_77615_4_;
            if (i >= 10) {
                int j = EnchantmentHelper.getRiptide(p_77615_1_);
                if (j <= 0 || playerentity.isInWaterOrRain()) {
                    if (!p_77615_2_.isClientSide) {
                        p_77615_1_.hurtAndBreak(1, playerentity, (p_220047_1_) -> p_220047_1_.broadcastBreakEvent(p_77615_3_.getUsedItemHand()));
                        if (j == 0) {
                            // change: instance of TridentLikeEntity
                            TridentEntity tridententity = new TridentLikeEntity(p_77615_2_, playerentity, p_77615_1_);
                            tridententity.shootFromRotation(playerentity, playerentity.xRot, playerentity.yRot, 0.0F, 2.5F + (float) j * 0.5F, 1.0F);
                            if (playerentity.abilities.instabuild) {
                                tridententity.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                            }

                            p_77615_2_.addFreshEntity(tridententity);
                            p_77615_2_.playSound(null, tridententity, SoundEvents.TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                            if (!playerentity.abilities.instabuild) {
                                playerentity.inventory.removeItem(p_77615_1_);
                            }
                        }
                    }

                    playerentity.awardStat(Stats.ITEM_USED.get(this));
                    if (j > 0) {
                        float f7 = playerentity.yRot;
                        float f = playerentity.xRot;
                        float f1 = -MathHelper.sin(f7 * ((float) Math.PI / 180F)) * MathHelper.cos(f * ((float) Math.PI / 180F));
                        float f2 = -MathHelper.sin(f * ((float) Math.PI / 180F));
                        float f3 = MathHelper.cos(f7 * ((float) Math.PI / 180F)) * MathHelper.cos(f * ((float) Math.PI / 180F));
                        float f4 = MathHelper.sqrt(f1 * f1 + f2 * f2 + f3 * f3);
                        float f5 = 3.0F * ((1.0F + (float) j) / 4.0F);
                        f1 = f1 * (f5 / f4);
                        f2 = f2 * (f5 / f4);
                        f3 = f3 * (f5 / f4);
                        playerentity.push(f1, f2, f3);
                        playerentity.startAutoSpinAttack(20);
                        if (playerentity.isOnGround()) {
                            playerentity.move(MoverType.SELF, new Vector3d(0.0D, 1.1999999F, 0.0D));
                        }

                        SoundEvent soundevent;
                        if (j >= 3) {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_3;
                        } else if (j == 2) {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_2;
                        } else {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_1;
                        }

                        p_77615_2_.playSound(null, playerentity, soundevent, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    }

                }
            }
        }
    }
}
