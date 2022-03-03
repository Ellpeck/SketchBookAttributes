package de.ellpeck.sketchbookattributes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

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
                        break;
                    case ICE_BALL:
                        break;
                    case JUMP:
                        break;
                    case HEAL:
                        break;
                    case SPEED:
                        break;
                }
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
