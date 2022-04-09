package de.ellpeck.sketchbookattributes.mixin;

import de.ellpeck.sketchbookattributes.data.PlayerAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorItem.class)
public class ArmorItemMixin extends Item {

    public ArmorItemMixin(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Inject(at = @At("HEAD"), method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", cancellable = true)
    public void use(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult<ItemStack>> callback) {
        ItemStack stack = player.getItemInHand(hand);
        if (!PlayerAttributes.canUseItem(player, stack, true))
            callback.setReturnValue(ActionResult.fail(stack));
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlotType armorType, Entity entity) {
        return super.canEquip(stack, armorType, entity) && (!(entity instanceof PlayerEntity) || PlayerAttributes.canUseItem((PlayerEntity) entity, stack, false));
    }
}
