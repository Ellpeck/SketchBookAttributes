package de.ellpeck.sketchbookattributes.network;

import de.ellpeck.sketchbookattributes.data.AttributeData;
import de.ellpeck.sketchbookattributes.data.PlayerAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AttributeButtonPacket {

    private final String type;

    public AttributeButtonPacket(String type) {
        this.type = type;
    }

    public static AttributeButtonPacket fromBytes(PacketBuffer buf) {
        return new AttributeButtonPacket(buf.readUtf());
    }

    public static void toBytes(AttributeButtonPacket packet, PacketBuffer buf) {
        buf.writeUtf(packet.type);
    }

    @SuppressWarnings("Convert2Lambda")
    public static void onMessage(AttributeButtonPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(new Runnable() {
            @Override
            public void run() {
                PlayerEntity player = ctx.get().getSender();
                AttributeData data = AttributeData.get(player.level);
                PlayerAttributes attributes = data.getAttributes(player);
                if (attributes.skillPoints <= 0)
                    return;
                attributes.skillPoints--;
                switch (message.type) {
                    case "strength":
                        attributes.addedStrength++;
                        break;
                    case "dexterity":
                        attributes.addedDexterity++;
                        break;
                    case "constitution":
                        attributes.addedConstitution++;
                        break;
                    case "intelligence":
                        attributes.addedIntelligence++;
                        break;
                    case "agility":
                        attributes.addedAgility++;
                        break;
                }
                attributes.reapplyAttributes(player);
                PacketHandler.sendTo(player, data.getPacket());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
