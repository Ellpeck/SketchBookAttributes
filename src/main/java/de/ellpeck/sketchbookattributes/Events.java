package de.ellpeck.sketchbookattributes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

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
        if (data.gainXp(amount)) {
            // send packet to everyone for the nametag display
            PacketHandler.sendToAll(data.getPacket());
        }
    }

    @SubscribeEvent
    public static void serverStarting(FMLServerStartingEvent event) {
        event.getServer().getCommands().getDispatcher().register(Commands.literal(SketchBookAttributes.ID).requires(s -> s.hasPermission(2))
                .then(Commands.literal("level").then(Commands.argument("level", IntegerArgumentType.integer(0, AttributeData.MAX_LEVEL)).executes(c -> {
                    CommandSource source = c.getSource();
                    AttributeData data = AttributeData.get(source.getPlayerOrException());
                    data.level = IntegerArgumentType.getInteger(c, "level");
                    data.pointsToNextLevel = 0;
                    PacketHandler.sendToAll(data.getPacket());
                    source.sendSuccess(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".level_set", source.getDisplayName(), data.level), true);
                    return 0;
                })))
                .then(Commands.literal("points").then(Commands.argument("points", IntegerArgumentType.integer(0)).executes(c -> {
                    CommandSource source = c.getSource();
                    AttributeData data = AttributeData.get(source.getPlayerOrException());
                    data.skillPoints = IntegerArgumentType.getInteger(c, "points");
                    PacketHandler.sendToAll(data.getPacket());
                    source.sendSuccess(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".points_set", source.getDisplayName(), data.skillPoints), true);
                    return 0;
                }))));
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (event.player.level.isClientSide)
            return;
        AttributeData data = AttributeData.get(event.player);
        if (event.player.tickCount % 20 == 0) {
            data.mana = Math.min(data.maxMana, data.mana + data.getManaRegenPerSecond());
            event.player.heal(data.getHealthRegenPerSecond());
            System.out.println(event.player.getHealth());
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
            if (mc.screen == null && Registry.Client.OPEN_KEYBIND.consumeClick()) {
                AttributeData data = AttributeData.get(mc.player);
                mc.setScreen(new AttributesScreen(data));
            }
        }
    }
}
