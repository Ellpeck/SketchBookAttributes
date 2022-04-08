package de.ellpeck.sketchbookattributes.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class BrawlerGauntletItem extends SpecialSwordItem {

    public BrawlerGauntletItem() {
        super(5, -2.1F, 700);
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (hand != Hand.OFF_HAND)
            return super.use(world, player, hand);
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return ActionResult.consume(stack);
    }
}
