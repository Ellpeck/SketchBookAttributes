package de.ellpeck.sketchbookattributes.items;

import de.ellpeck.sketchbookattributes.SketchBookAttributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public class HasteBootsItem extends ArmorItem {

    public HasteBootsItem() {
        super(new Material(), EquipmentSlotType.FEET, new Properties().tab(ItemGroup.TAB_COMBAT));
    }

    private static class Material implements IArmorMaterial {

        @Override
        public int getDurabilityForSlot(EquipmentSlotType slot) {
            return 450;
        }

        @Override
        public int getDefenseForSlot(EquipmentSlotType slot) {
            return ArmorMaterial.DIAMOND.getDefenseForSlot(slot);
        }

        @Override
        public int getEnchantmentValue() {
            return ArmorMaterial.DIAMOND.getEnchantmentValue();
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_GENERIC;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return SketchBookAttributes.ID + ":haste_boots";
        }

        @Override
        public float getToughness() {
            return ArmorMaterial.DIAMOND.getToughness();
        }

        @Override
        public float getKnockbackResistance() {
            return ArmorMaterial.DIAMOND.getKnockbackResistance();
        }
    }
}
