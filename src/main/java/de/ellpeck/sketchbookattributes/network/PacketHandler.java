package de.ellpeck.sketchbookattributes.network;

import de.ellpeck.sketchbookattributes.SketchBookAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {

    private static final String VERSION = "1.0";
    private static SimpleChannel network;

    public static void setup() {
        network = NetworkRegistry.newSimpleChannel(new ResourceLocation(SketchBookAttributes.ID, "network"), () -> VERSION, VERSION::equals, VERSION::equals);
        network.registerMessage(0, SyncAttributesPacket.class, SyncAttributesPacket::toBytes, SyncAttributesPacket::fromBytes, SyncAttributesPacket::onMessage);
        network.registerMessage(1, AttributeButtonPacket.class, AttributeButtonPacket::toBytes, AttributeButtonPacket::fromBytes, AttributeButtonPacket::onMessage);
        network.registerMessage(2, ClassButtonPacket.class, ClassButtonPacket::toBytes, ClassButtonPacket::fromBytes, ClassButtonPacket::onMessage);
        network.registerMessage(3, SkillActivatedPacket.class, SkillActivatedPacket::toBytes, SkillActivatedPacket::fromBytes, SkillActivatedPacket::onMessage);
    }

    public static void sendToAll(Object message) {
        network.send(PacketDistributor.ALL.noArg(), message);
    }

    public static void sendTo(PlayerEntity player, Object message) {
        network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), message);
    }

    public static void sendToServer(Object message) {
        network.send(PacketDistributor.SERVER.noArg(), message);
    }

}