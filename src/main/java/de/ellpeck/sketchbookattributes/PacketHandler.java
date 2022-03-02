package de.ellpeck.sketchbookattributes;

import de.ellpeck.sketchbookattributes.SketchBookAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.UUID;
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

        private final UUID player;
        private final CompoundNBT data;

        public SyncAttributes(UUID player, CompoundNBT data) {
            this.player = player;
            this.data = data;
        }

        public static SyncAttributes fromBytes(PacketBuffer buf) {
            return new SyncAttributes(buf.readUUID(), buf.readNbt());
        }

        public static void toBytes(SyncAttributes packet, PacketBuffer buf) {
            buf.writeUUID(packet.player);
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
                    PlayerEntity player = level.getPlayerByUUID(message.player);
                    if (player == null)
                        return;
                    AttributeData data = AttributeData.get(player);
                    data.deserializeNBT(message.data);
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
                    AttributeData data = AttributeData.get(player);
                    if (data.skillPoints <= 0)
                        return;
                    data.skillPoints--;
                    switch (message.type) {
                        case "strength":
                            data.strength++;
                            break;
                        case "dexterity":
                            data.dexterity++;
                            break;
                        case "constitution":
                            data.constitution++;
                            break;
                        case "intelligence":
                            data.intelligence++;
                            break;
                        case "agility":
                            data.agility++;
                            break;
                    }
                    data.reapplyAttributes();
                    PacketHandler.sendTo(player, data.getPacket());
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}