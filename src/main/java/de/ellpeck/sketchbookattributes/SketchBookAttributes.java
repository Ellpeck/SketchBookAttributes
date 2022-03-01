package de.ellpeck.sketchbookattributes;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;

@Mod(SketchBookAttributes.ID)
public class SketchBookAttributes {

    public static final String ID = "sketchbookattributes";

    @CapabilityInject(AttributeData.class)
    public static final Capability<AttributeData> ATTRIBUTE_DATA_CAPABILITY = null;

    public SketchBookAttributes() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent event) {
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
}