package de.ellpeck.sketchbookattributes;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class PacketHandler {

    private static final String VERSION = "1.0";
    private static SimpleChannel network;

    public static void setup() {
        network = NetworkRegistry.newSimpleChannel(new ResourceLocation(SketchBookAttributes.ID, "network"), () -> VERSION, VERSION::equals, VERSION::equals);
        network.registerMessage(0, SyncAttributes.class, SyncAttributes::toBytes, SyncAttributes::fromBytes, SyncAttributes::onMessage);
        network.registerMessage(1, AttributeButton.class, AttributeButton::toBytes, AttributeButton::fromBytes, AttributeButton::onMessage);
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

    public static class SyncAttributes {

        private final CompoundNBT data;

        public SyncAttributes(CompoundNBT data) {
            this.data = data;
        }

        public static SyncAttributes fromBytes(PacketBuffer buf) {
            return new SyncAttributes(buf.readNbt());
        }

        public static void toBytes(SyncAttributes packet, PacketBuffer buf) {
            buf.writeNbt(packet.data);
        }

        @SuppressWarnings("Convert2Lambda")
        public static void onMessage(SyncAttributes message, Supplier<NetworkEvent.Context> ctx) {
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

    public static class AttributeButton {

        private final String type;

        public AttributeButton(String type) {
            this.type = type;
        }

        public static AttributeButton fromBytes(PacketBuffer buf) {
            return new AttributeButton(buf.readUtf());
        }

        public static void toBytes(AttributeButton packet, PacketBuffer buf) {
            buf.writeUtf(packet.type);
        }

        @SuppressWarnings("Convert2Lambda")
        public static void onMessage(AttributeButton message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(new Runnable() {
                @Override
                public void run() {
                    PlayerEntity player = ctx.get().getSender();
                    AttributeData data = AttributeData.get(player.level);
                    AttributeData.PlayerAttributes attributes = data.getAttributes(player);
                    if (attributes.skillPoints <= 0)
                        return;
                    attributes.skillPoints--;
                    switch (message.type) {
                        case "strength":
                            attributes.strength++;
                            break;
                        case "dexterity":
                            attributes.dexterity++;
                            break;
                        case "constitution":
                            attributes.constitution++;
                            break;
                        case "intelligence":
                            attributes.intelligence++;
                            break;
                        case "agility":
                            attributes.agility++;
                            break;
                    }
                    attributes.reapplyAttributes(player);
                    PacketHandler.sendTo(player, data.getPacket());
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}