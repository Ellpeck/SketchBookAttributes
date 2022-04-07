package de.ellpeck.sketchbookattributes;

import de.ellpeck.sketchbookattributes.entities.TridentLikeRenderer;
import de.ellpeck.sketchbookattributes.items.GildedCrossbowItem;
import de.ellpeck.sketchbookattributes.items.SpecialBowItem;
import de.ellpeck.sketchbookattributes.network.PacketHandler;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public final class Registry {

    public static void setup(FMLCommonSetupEvent event) {
        PacketHandler.setup();
    }

    public static final class Client {

        public static final KeyBinding OPEN_KEYBIND = new KeyBinding("key." + SketchBookAttributes.ID + ".open", 73, "key.categories." + SketchBookAttributes.ID);
        public static final KeyBinding SKILL_KEYBIND = new KeyBinding("key." + SketchBookAttributes.ID + ".skill", 82, "key.categories." + SketchBookAttributes.ID);

        public static void setup(FMLClientSetupEvent event) {
            ClientRegistry.registerKeyBinding(OPEN_KEYBIND);
            ClientRegistry.registerKeyBinding(SKILL_KEYBIND);

            RenderingRegistry.registerEntityRenderingHandler(SketchBookAttributes.ICE_BALL.get(),
                    m -> new SpriteRenderer<>(m, event.getMinecraftSupplier().get().getItemRenderer(), 0.75F, true));
            RenderingRegistry.registerEntityRenderingHandler(SketchBookAttributes.TRIDENT_LIKE.get(),
                    m -> new TridentLikeRenderer(m, event.getMinecraftSupplier().get().getItemRenderer()));

            ItemModelsProperties.register(SketchBookAttributes.GILDED_CROSSBOW.get(), new ResourceLocation(SketchBookAttributes.ID, "pull"), (stack, world, entity) -> entity != null && !GildedCrossbowItem.isCharged(stack) ? (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / (float) GildedCrossbowItem.getGildedChargeDuration(stack) : 0);
            ItemModelsProperties.register(SketchBookAttributes.GILDED_CROSSBOW.get(), new ResourceLocation(SketchBookAttributes.ID, "pulling"), (stack, world, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !GildedCrossbowItem.isCharged(stack) ? 1 : 0);
            ItemModelsProperties.register(SketchBookAttributes.GILDED_CROSSBOW.get(), new ResourceLocation(SketchBookAttributes.ID, "charged"), (stack, world, entity) -> entity != null && CrossbowItem.isCharged(stack) ? 1 : 0);
            ItemModelsProperties.register(SketchBookAttributes.GILDED_CROSSBOW.get(), new ResourceLocation(SketchBookAttributes.ID, "firework"), (stack, world, entity) -> entity != null && CrossbowItem.isCharged(stack) && CrossbowItem.containsChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1 : 0);

            for (RegistryObject<Item> entry : SketchBookAttributes.ITEMS.getEntries()) {
                Item item = entry.get();
                if (item instanceof SpecialBowItem) {
                    float speed = 20 / ((SpecialBowItem) item).drawSpeedMultiplier;
                    ItemModelsProperties.register(item, new ResourceLocation(SketchBookAttributes.ID, "pull"), (stack, world, entity) -> entity != null && entity.getUseItem() == stack ? (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / speed : 0);
                    ItemModelsProperties.register(item, new ResourceLocation(SketchBookAttributes.ID, "pulling"), (stack, world, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0);
                }
            }
        }
    }
}
