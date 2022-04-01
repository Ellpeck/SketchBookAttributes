package de.ellpeck.sketchbookattributes;

import de.ellpeck.sketchbookattributes.network.PacketHandler;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class Registry {

    public static void setup(FMLCommonSetupEvent event) {
        PacketHandler.setup();
    }

    public static class Client {

        public static final KeyBinding OPEN_KEYBIND = new KeyBinding("key." + SketchBookAttributes.ID + ".open", 73, "key.categories.misc");

        public static void setup(FMLClientSetupEvent event) {
            ClientRegistry.registerKeyBinding(OPEN_KEYBIND);
            RenderingRegistry.registerEntityRenderingHandler(SketchBookAttributes.ICE_BALL.get(),
                    m -> new SpriteRenderer<>(m, event.getMinecraftSupplier().get().getItemRenderer(), 0.75F, true));
        }
    }
}
