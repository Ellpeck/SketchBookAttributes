package de.ellpeck.sketchbookattributes.items;

import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemTier;
import net.minecraft.item.crafting.Ingredient;

public class SpecialItemTier implements IItemTier {

    public static final SpecialItemTier INSTANCE = new SpecialItemTier();

    @Override
    public int getUses() {
        return 0;
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public float getAttackDamageBonus() {
        return 0;
    }

    @Override
    public int getLevel() {
        return ItemTier.DIAMOND.getLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return ItemTier.DIAMOND.getEnchantmentValue();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }
}
