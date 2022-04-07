package de.ellpeck.sketchbookattributes.items;

import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;
import net.minecraft.item.crafting.Ingredient;

public class SpecialSwordItem extends SwordItem {

    public SpecialSwordItem(int damage, float speed, int durability) {
        super(StubTier.INSTANCE, damage, speed, new Properties().tab(ItemGroup.TAB_COMBAT).durability(durability));
    }

    private static class StubTier implements IItemTier {

        public static final StubTier INSTANCE = new StubTier();

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
}
