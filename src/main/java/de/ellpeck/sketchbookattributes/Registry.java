package de.ellpeck.sketchbookattributes;

import net.java.games.input.Keyboard;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import sun.security.krb5.internal.ktab.KeyTabInputStream;

import javax.annotation.Nullable;

public class Registry {

    public static void setup(FMLCommonSetupEvent event) {
        PacketHandler.setup();

        // register the capability with this super bloated system that is being removed in 1.17+ anyway
        CapabilityManager.INSTANCE.register(AttributeData.class, new Capability.IStorage<AttributeData>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<AttributeData> capability, AttributeData instance, Direction side) {
                return new CompoundNBT();
            }

            @Override
            public void readNBT(Capability<AttributeData> capability, AttributeData instance, Direction side, INBT nbt) {

            }
        }, () -> null);
    }

    public static class Client {

        public static final KeyBinding OPEN_KEYBIND = new KeyBinding("key." + SketchBookAttributes.ID + ".open", 73, "key.categories.gameplay");

        public static void setup(FMLClientSetupEvent event) {
            ClientRegistry.registerKeyBinding(OPEN_KEYBIND);
        }
    }
}
