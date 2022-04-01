package de.ellpeck.sketchbookattributes;

import de.ellpeck.sketchbookattributes.data.AttributeData;
import de.ellpeck.sketchbookattributes.data.PlayerAttributes;
import de.ellpeck.sketchbookattributes.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Locale;

public class StaffItem extends Item {

    private static final int RANGE = 40;

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
                Mode nextMode = this.allowedModes[(ArrayUtils.indexOf(this.allowedModes, mode) + 1) % this.allowedModes.length];
                this.setMode(stack, nextMode);
            }
        } else {
            AttributeData data = AttributeData.get(level);
            PlayerAttributes attributes = data.getAttributes(player);
            if (!player.isCreative() && attributes.mana < mode.requiredMana)
                return ActionResult.fail(stack);

            if (!level.isClientSide) {
                Vector3d view = player.getViewVector(1);
                switch (mode) {
                    case FIRE_BALL:
                        SmallFireballEntity fireball = new SmallFireballEntity(level, player, view.x, view.y, view.z);
                        fireball.setPos(player.getX(), player.getEyeY(), player.getZ());
                        level.addFreshEntity(fireball);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundCategory.PLAYERS, 1, 1);
                        break;
                    case ICE_BALL:
                        IceBallEntity iceBall = new IceBallEntity(SketchBookAttributes.ICE_BALL.get(), player, view.x, view.y, view.z, level);
                        iceBall.setPos(player.getX(), player.getEyeY(), player.getZ());
                        level.addFreshEntity(iceBall);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOAT_PADDLE_WATER, SoundCategory.PLAYERS, 1, 1);
                        break;
                    case JUMP:
                        applyTargetEffect(player, player, new EffectInstance(Effects.JUMP, 5 * 20, 1));
                        break;
                    case HEAL:
                        applyTargetEffect(player, player, new EffectInstance(Effects.HEAL, 0, 1));
                        break;
                    case SPEED:
                        if (!applyTargetEffect(player, new EffectInstance(Effects.MOVEMENT_SPEED, 3 * 20, 1)))
                            return ActionResult.fail(stack);
                        break;
                    case STRENGTH:
                        if (!applyTargetEffect(player, new EffectInstance(Effects.DAMAGE_BOOST, 5 * 20)))
                            return ActionResult.fail(stack);
                        break;
                    case METEORS:
                        RayTraceResult ray = Utility.pickEntity(player, RANGE);
                        if (ray == null)
                            ray = player.pick(RANGE, 1, false);
                        if (ray.getType() == RayTraceResult.Type.MISS)
                            return ActionResult.fail(stack);
                        Vector3d target = ray.getLocation();
                        for (int i = 0; i < 15; i++) {
                            double x = target.x + MathHelper.nextDouble(random, -10, 10);
                            double y = target.y + MathHelper.nextDouble(random, 15, 20);
                            double z = target.z + MathHelper.nextDouble(random, -10, 10);
                            double goalX = target.x + MathHelper.nextDouble(random, -2, 2);
                            double goalZ = target.z + MathHelper.nextDouble(random, -2, 2);
                            Vector3d motion = new Vector3d(goalX - x, target.y - y, goalZ - z).normalize();
                            SmallFireballEntity meteor = new SmallFireballEntity(level, x, y, z, motion.x, motion.y, motion.z);
                            meteor.getPersistentData().putBoolean(SketchBookAttributes.ID + ":meteor", true);
                            level.addFreshEntity(meteor);
                        }
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundCategory.PLAYERS, 1, 1);
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

    private static boolean applyTargetEffect(PlayerEntity player, EffectInstance effect) {
        EntityRayTraceResult result = Utility.pickEntity(player, RANGE);
        if (result != null) {
            Entity entity = result.getEntity();
            if (entity instanceof LivingEntity) {
                applyTargetEffect(player, (LivingEntity) entity, effect);
                return true;
            }
        }
        return false;
    }

    private static void applyTargetEffect(PlayerEntity player, LivingEntity target, EffectInstance effect) {
        if (effect.getEffect().isInstantenous()) {
            effect.getEffect().applyInstantenousEffect(player, player, target, effect.getAmplifier(), 1);
        } else {
            target.addEffect(effect);
        }

        int color = effect.getEffect().getColor();
        AxisAlignedBB bounds = target.getBoundingBox().move(-target.getX(), -target.getY(), -target.getZ());
        ((ServerWorld) player.level).sendParticles(
                new RedstoneParticleData((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 2),
                target.getX(), target.getY(), target.getZ(), 50,
                bounds.getXsize() / 2, bounds.getYsize() / 2, bounds.getZsize() / 2, 0);
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
