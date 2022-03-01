package de.ellpeck.sketchbookattributes;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Events {

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof PlayerEntity)
            event.addCapability(new ResourceLocation(SketchBookAttributes.ID, "attributes"), new AttributeData((PlayerEntity) entity));
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        if (!player.level.isClientSide) {
            AttributeData data = AttributeData.get(player);
            for (PlayerEntity other : player.level.players()) {
                // send the new player's data to people on the server
                PacketHandler.sendTo(other, data.getPacket());

                // send the data of people on the server to the new player
                if (other != player) {
                    AttributeData otherData = AttributeData.get(other);
                    PacketHandler.sendTo(player, otherData.getPacket());
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerXpChange(PlayerXpEvent.XpChange event) {
        PlayerEntity player = event.getPlayer();
        int amount = event.getAmount();
        if (player.level.isClientSide || amount <= 0)
            return;
        AttributeData data = AttributeData.get(player);
        if (data.level < AttributeData.MAX_LEVEL) {
            data.pointsToNextLevel += amount;
            while (data.pointsToNextLevel >= data.getXpNeededForNextLevel()) {
                data.pointsToNextLevel -= data.getXpNeededForNextLevel();
                data.level++;
                // send levelup packet to everyone for the nametag display
                PacketHandler.sendToAll(data.getPacket());
            }
        }
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class Client {

        @SubscribeEvent
        public static void renderNameplate(RenderNameplateEvent event) {
            Entity entity = event.getEntity();
            ITextComponent content = event.getContent();
            if (entity instanceof PlayerEntity && content instanceof IFormattableTextComponent) {
                AttributeData data = AttributeData.get((PlayerEntity) entity);
                ((IFormattableTextComponent) content)
                        .append(" ")
                        .append(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".level", data.level).withStyle(TextFormatting.GOLD));
            }
        }

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.START)
                return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null && Registry.Client.OPEN_KEYBIND.consumeClick())
                mc.setScreen(new AttributesScreen());
        }
    }
}
