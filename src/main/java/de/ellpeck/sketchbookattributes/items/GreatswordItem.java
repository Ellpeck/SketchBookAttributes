package de.ellpeck.sketchbookattributes.items;

import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SwordItem;

public class GreatswordItem extends SwordItem {

    public GreatswordItem(IItemTier tier, int damage, float speed, int durability) {
        super(tier, damage, speed, new Properties().tab(ItemGroup.TAB_COMBAT).durability(durability));
    }
}
