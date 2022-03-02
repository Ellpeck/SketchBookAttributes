package de.ellpeck.sketchbookattributes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod.EventBusSubscriber
public class Events {

    @SubscribeEvent
    public static void playerJoinedWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (!player.level.isClientSide) {
                AttributeData data = AttributeData.get(player.level);
                AttributeData.PlayerAttributes attributes = data.getAttributes(player);
                attributes.reapplyAttributes(player);
                PacketHandler.sendTo(player, data.getPacket());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerXpChange(PlayerXpEvent.XpChange event) {
        PlayerEntity player = event.getPlayer();
        int amount = event.getAmount();
        if (player.level.isClientSide || amount <= 0)
            return;
        AttributeData data = AttributeData.get(player.level);
        AttributeData.PlayerAttributes attributes = data.getAttributes(player);
        if (attributes.gainXp(amount))
            PacketHandler.sendToAll(data.getPacket());
    }

    @SubscribeEvent
    public static void serverStarting(FMLServerStartingEvent event) {
        event.getServer().getCommands().getDispatcher().register(Commands.literal(SketchBookAttributes.ID).requires(s -> s.hasPermission(2))
                .then(Commands.literal("level").then(Commands.argument("level", IntegerArgumentType.integer(0, AttributeData.PlayerAttributes.MAX_LEVEL)).executes(c -> {
                    CommandSource source = c.getSource();
                    PlayerEntity player = source.getPlayerOrException();
                    AttributeData data = AttributeData.get(player.level);
                    AttributeData.PlayerAttributes attributes = data.getAttributes(player);
                    attributes.level = IntegerArgumentType.getInteger(c, "level");
                    attributes.pointsToNextLevel = 0;
                    PacketHandler.sendToAll(data.getPacket());
                    source.sendSuccess(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".level_set", source.getDisplayName(), attributes.level), true);
                    return 0;
                })))
                .then(Commands.literal("points").then(Commands.argument("points", IntegerArgumentType.integer(0)).executes(c -> {
                    CommandSource source = c.getSource();
                    PlayerEntity player = source.getPlayerOrException();
                    AttributeData data = AttributeData.get(player.level);
                    AttributeData.PlayerAttributes attributes = data.getAttributes(player);
                    attributes.skillPoints = IntegerArgumentType.getInteger(c, "points");
                    PacketHandler.sendToAll(data.getPacket());
                    source.sendSuccess(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".points_set", source.getDisplayName(), attributes.skillPoints), true);
                    return 0;
                }))));
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (event.player.level.isClientSide)
            return;
        AttributeData.PlayerAttributes attributes = AttributeData.get(event.player.level).getAttributes(event.player);
        if (event.player.tickCount % 20 == 0) {
            attributes.mana = Math.min(attributes.maxMana, attributes.mana + attributes.getManaRegenPerSecond());
            event.player.heal(attributes.getHealthRegenPerSecond());
        }
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class Client {

        @SubscribeEvent
        public static void renderNameplate(RenderNameplateEvent event) {
            Entity entity = event.getEntity();
            ITextComponent content = event.getContent();
            if (entity instanceof PlayerEntity && content instanceof IFormattableTextComponent) {
                AttributeData.PlayerAttributes attributes = AttributeData.get(entity.level).getAttributes((PlayerEntity) entity);
                ((IFormattableTextComponent) content)
                        .append(" ")
                        .append(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".level", attributes.level).withStyle(TextFormatting.GOLD));
            }
        }

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.START)
                return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null && Registry.Client.OPEN_KEYBIND.consumeClick()) {
                AttributeData.PlayerAttributes data = AttributeData.get(mc.player.level).getAttributes(mc.player);
                mc.setScreen(new AttributesScreen(data));
            }
        }
    }
}
