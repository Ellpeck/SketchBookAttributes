package de.ellpeck.sketchbookattributes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Locale;

public class StaffItem extends Item {

    private final Mode[] allowedModes;

    public StaffItem(Mode... allowedModes) {
        super(new Properties().tab(ItemGroup.TAB_COMBAT).stacksTo(1));
        this.allowedModes = allowedModes;
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Mode mode = this.getMode(stack);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                Mode nextMode = this.allowedModes[(mode.ordinal() + 1) % this.allowedModes.length];
                this.setMode(stack, nextMode);
            }
        } else {
            AttributeData data = AttributeData.get(level);
            AttributeData.PlayerAttributes attributes = data.getAttributes(player);
            if (attributes.mana < mode.requiredMana)
                return ActionResult.fail(stack);

            if (!level.isClientSide) {
                switch (mode) {
                    case FIRE_BALL:
                        Vector3d view = player.getViewVector(1);
                        SmallFireballEntity fireball = new SmallFireballEntity(level, player, view.x, view.y, view.z);
                        fireball.setPos(player.getX(), player.getEyeY(), player.getZ());
                        level.addFreshEntity(fireball);
                        level.levelEvent(null, 1018, player.blockPosition(), 0);
                        break;
                    case ICE_BALL:
                        break;
                    case JUMP:
                        applyTargetEffect(player, new EffectInstance(Effects.JUMP, 5 * 20, 1));
                        break;
                    case HEAL:
                        applyTargetEffect(player, new EffectInstance(Effects.HEAL, 0, 1));
                        break;
                    case SPEED:
                        applyTargetEffect(player, new EffectInstance(Effects.MOVEMENT_SPEED, 3 * 20, 1));
                        break;
                    case STRENGTH:
                        applyTargetEffect(player, new EffectInstance(Effects.MOVEMENT_SPEED, 5 * 20));
                        break;
                    case METEORS:
                        break;
                }
                if (!player.isCreative())
                    attributes.mana -= mode.requiredMana;
                PacketHandler.sendTo(player, data.getPacket());
            }
        }
        return ActionResult.success(stack);
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        IFormattableTextComponent name = ((IFormattableTextComponent) super.getName(stack));
        return name.append(" (").append(this.getModeDescription(this.getMode(stack))).append(")");
    }

    private Mode getMode(ItemStack stack) {
        if (stack.hasTag()) {
            Mode ret = Mode.valueOf(stack.getTag().getString("mode"));
            if (ret != null)
                return ret;
        }
        return this.allowedModes[0];
    }

    private void setMode(ItemStack stack, Mode mode) {
        stack.getOrCreateTag().putString("mode", mode.toString());
    }

    private TranslationTextComponent getModeDescription(Mode mode) {
        return new TranslationTextComponent("mode." + SketchBookAttributes.ID + "." + mode.toString().toLowerCase(Locale.ROOT));
    }

    private static void applyTargetEffect(PlayerEntity player, EffectInstance effect) {
        // see GameRenderer.pick for reference
        int range = 40;
        Vector3d eyePos = player.getEyePosition(1);
        Vector3d view = player.getViewVector(1).scale(range);
        AxisAlignedBB area = player.getBoundingBox().expandTowards(view).inflate(1, 1, 1);
        EntityRayTraceResult result = ProjectileHelper.getEntityHitResult(player, eyePos, eyePos.add(view), area, Entity::isAlive, range * range);
        if (result != null) {
            Entity entity = result.getEntity();
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).addEffect(effect);

                int color = effect.getEffect().getColor();
                AxisAlignedBB bounds = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
                ((ServerWorld) player.level).sendParticles(
                        new RedstoneParticleData((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 2),
                        entity.getX(), entity.getY(), entity.getZ(), 50,
                        bounds.getXsize() / 2, bounds.getYsize() / 2, bounds.getZsize() / 2, 0);
            }
        }
    }

    public enum Mode {
        FIRE_BALL(10),
        ICE_BALL(15),
        JUMP(6),
        HEAL(25),
        SPEED(25),
        STRENGTH(30),
        METEORS(50);

        public final int requiredMana;

        Mode(int requiredMana) {
            this.requiredMana = requiredMana;
        }
    }
}
