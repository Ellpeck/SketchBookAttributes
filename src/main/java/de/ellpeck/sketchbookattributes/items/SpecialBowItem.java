package de.ellpeck.sketchbookattributes.items;

import de.ellpeck.sketchbookattributes.SketchBookAttributes;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemGroup;

public class SpecialBowItem extends BowItem {

    public final float drawSpeedMultiplier;
    private final float damageMultiplier;

    public SpecialBowItem(int durability, float drawSpeedMultiplier, float damageMultiplier) {
        super(new Properties().tab(ItemGroup.TAB_COMBAT).durability(durability));
        this.drawSpeedMultiplier = drawSpeedMultiplier;
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    public AbstractArrowEntity customArrow(AbstractArrowEntity arrow) {
        arrow.setBaseDamage(arrow.getBaseDamage() * this.damageMultiplier);
        arrow.getPersistentData().putString(SketchBookAttributes.ID + ":bow", this.getRegistryName().toString());
        return arrow;
    }
}
