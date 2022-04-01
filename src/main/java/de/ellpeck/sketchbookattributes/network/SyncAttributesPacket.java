package de.ellpeck.sketchbookattributes.network;

import de.ellpeck.sketchbookattributes.data.AttributeData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncAttributesPacket {

    private final CompoundNBT data;

    public SyncAttributesPacket(CompoundNBT data) {
        this.data = data;
    }

    public static SyncAttributesPacket fromBytes(PacketBuffer buf) {
        return new SyncAttributesPacket(buf.readNbt());
    }

    public static void toBytes(SyncAttributesPacket packet, PacketBuffer buf) {
        buf.writeNbt(packet.data);
    }

    @SuppressWarnings("Convert2Lambda")
    public static void onMessage(SyncAttributesPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(new Runnable() {
            @Override
            public void run() {
                World level = Minecraft.getInstance().level;
                if (level == null)
                    return;
                AttributeData data = AttributeData.get(level);
                data.load(message.data);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
