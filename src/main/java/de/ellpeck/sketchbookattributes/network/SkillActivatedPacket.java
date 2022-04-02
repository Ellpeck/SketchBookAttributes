package de.ellpeck.sketchbookattributes.network;

import de.ellpeck.sketchbookattributes.data.AttributeData;
import de.ellpeck.sketchbookattributes.data.PlayerAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SkillActivatedPacket {

    public static SkillActivatedPacket fromBytes(PacketBuffer buf) {
        return new SkillActivatedPacket();
    }

    public static void toBytes(SkillActivatedPacket packet, PacketBuffer buf) {
        // noop
    }

    @SuppressWarnings("Convert2Lambda")
    public static void onMessage(SkillActivatedPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(new Runnable() {
            @Override
            public void run() {
                PlayerEntity player = ctx.get().getSender();
                AttributeData data = AttributeData.get(player.level);
                PlayerAttributes attributes = data.getAttributes(player);
                if (attributes.playerClass == null)
                    return;
                if (attributes.playerClass.executeSkill.apply(player, attributes))
                    PacketHandler.sendTo(player, data.getPacket());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
