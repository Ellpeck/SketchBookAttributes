package de.ellpeck.sketchbookattributes.items;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.SwordItem;

public class SpecialSwordItem extends SwordItem {

    public SpecialSwordItem(int damage, float speed, int durability) {
        super(SpecialItemTier.INSTANCE, damage, speed, new Properties().tab(ItemGroup.TAB_COMBAT).durability(durability));
    }

}
