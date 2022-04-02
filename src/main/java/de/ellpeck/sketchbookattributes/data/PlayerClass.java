package de.ellpeck.sketchbookattributes.data;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.function.BiFunction;

public enum PlayerClass {

    FIGHTER(15, 0, 10, 0, 0, (p, a) -> false),
    RANGER(0, 20, 0, 0, 10, (p, a) -> {
        if (a.mana < 20)
            return false;
        ItemStack held = p.getMainHandItem();
        if (!(held.getItem() instanceof BowItem))
            return false;
        for (int i = 0; i < 5; i++) {
            ArrowEntity arrow = new ArrowEntity(p.level, p);
            arrow.pickup = AbstractArrowEntity.PickupStatus.DISALLOWED;
            arrow.shootFromRotation(p, p.xRot, p.yRot, 0, 3, 5);
            arrow.setCritArrow(p.getRandom().nextFloat() <= 0.05F);

            int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, held);
            if (j > 0)
                arrow.setBaseDamage(arrow.getBaseDamage() + (double) j * 0.5 + 0.5);
            int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, held);
            if (k > 0)
                arrow.setKnockback(k);
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, held) > 0)
                arrow.setSecondsOnFire(100);

            p.level.addFreshEntity(arrow);
        }
        if (!p.isCreative())
            a.mana -= 20;
        return true;
    }),
    SPELLCASTER(0, 5, 0, 15, 5, (p, a) -> {
        if (a.mana < 15)
            return false;
        ItemStack fruit = new ItemStack(Items.CHORUS_FRUIT);
        fruit.finishUsingItem(p.level, p);
        if (!p.isCreative())
            a.mana -= 15;
        return true;
    });

    public final int strengthBonus;
    public final int dexterityBonus;
    public final int constitutionBonus;
    public final int intelligenceBonus;
    public final int agilityBonus;
    public final BiFunction<PlayerEntity, PlayerAttributes, Boolean> executeSkill;

    PlayerClass(int strengthBonus, int dexterityBonus, int constitutionBonus, int intelligenceBonus, int agilityBonus, BiFunction<PlayerEntity, PlayerAttributes, Boolean> executeSkill) {
        this.strengthBonus = strengthBonus;
        this.dexterityBonus = dexterityBonus;
        this.constitutionBonus = constitutionBonus;
        this.intelligenceBonus = intelligenceBonus;
        this.agilityBonus = agilityBonus;
        this.executeSkill = executeSkill;
    }
}
