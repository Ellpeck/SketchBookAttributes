package de.ellpeck.sketchbookattributes.network;

import de.ellpeck.sketchbookattributes.data.AttributeData;
import de.ellpeck.sketchbookattributes.data.PlayerAttributes;
import de.ellpeck.sketchbookattributes.data.PlayerClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClassButtonPacket {

    private final PlayerClass playerClass;

    public ClassButtonPacket(PlayerClass playerClass) {
        this.playerClass = playerClass;
    }

    public static ClassButtonPacket fromBytes(PacketBuffer buf) {
        return new ClassButtonPacket(PlayerClass.values()[buf.readInt()]);
    }

    public static void toBytes(ClassButtonPacket packet, PacketBuffer buf) {
        buf.writeInt(packet.playerClass.ordinal());
    }

    @SuppressWarnings("Convert2Lambda")
    public static void onMessage(ClassButtonPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(new Runnable() {
            @Override
            public void run() {
                PlayerEntity player = ctx.get().getSender();
                AttributeData data = AttributeData.get(player.level);
                PlayerAttributes attributes = data.getAttributes(player);
                if (attributes.playerClass != null)
                    return;
                attributes.playerClass = message.playerClass;
                attributes.reapplyAttributes(player);
                PacketHandler.sendTo(player, data.getPacket());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
