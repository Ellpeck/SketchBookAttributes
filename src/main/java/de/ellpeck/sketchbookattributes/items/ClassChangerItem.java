package de.ellpeck.sketchbookattributes.items;

import de.ellpeck.sketchbookattributes.data.AttributeData;
import de.ellpeck.sketchbookattributes.data.PlayerAttributes;
import de.ellpeck.sketchbookattributes.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ClassChangerItem extends Item {

    public ClassChangerItem() {
        super(new Properties().tab(ItemGroup.TAB_MISC).stacksTo(1));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!world.isClientSide) {
            AttributeData data = AttributeData.get(world);
            PlayerAttributes attributes = data.getAttributes(player);
            attributes.playerClass = null;
            attributes.skillPoints += attributes.addedStrength;
            attributes.addedStrength = 0;
            attributes.skillPoints += attributes.addedDexterity;
            attributes.addedDexterity = 0;
            attributes.skillPoints += attributes.addedConstitution;
            attributes.addedConstitution = 0;
            attributes.skillPoints += attributes.addedIntelligence;
            attributes.addedIntelligence = 0;
            attributes.skillPoints += attributes.addedAgility;
            attributes.addedAgility = 0;
            attributes.reapplyAttributes(player);
            PacketHandler.sendToAll(data.getPacket());
        }
        held.shrink(1);
        return ActionResult.success(held);
    }
}
