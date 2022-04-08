package de.ellpeck.sketchbookattributes.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class BarbarianAxeItem extends AxeItem {

    public BarbarianAxeItem() {
        super(SpecialItemTier.INSTANCE, 8, -3.1F, new Properties().tab(ItemGroup.TAB_COMBAT).durability(1700));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        player.addEffect(new EffectInstance(Effects.REGENERATION, 5 * 20, 1));
        player.getCooldowns().addCooldown(this, 30 * 20);
        return ActionResult.success(player.getItemInHand(hand));
    }
}
