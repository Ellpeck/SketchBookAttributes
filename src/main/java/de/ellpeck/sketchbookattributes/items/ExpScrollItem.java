package de.ellpeck.sketchbookattributes.items;

import de.ellpeck.sketchbookattributes.data.AttributeData;
import de.ellpeck.sketchbookattributes.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class ExpScrollItem extends Item {

    public ExpScrollItem() {
        super(new Properties().tab(ItemGroup.TAB_MISC));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!world.isClientSide) {
            AttributeData data = AttributeData.get(world);
            data.getAttributes(player).skillPoints++;
            PacketHandler.sendToAll(data.getPacket());
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 1);
        }
        held.shrink(1);
        return ActionResult.success(held);
    }
}
