package de.ellpeck.sketchbookattributes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
    public static void entityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (!entity.level.isClientSide && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;

            // send the new player's data to people on the server
            AttributeData data = AttributeData.get(player);
            PacketHandler.sendToAll(data.getPacket());

            // send the data of people on the server to the new player
            for (PlayerEntity other : entity.level.players()) {
                if (other == player)
                    continue;
                AttributeData otherData = AttributeData.get(other);
                PacketHandler.sendTo(player, otherData.getPacket());
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

    }
}
