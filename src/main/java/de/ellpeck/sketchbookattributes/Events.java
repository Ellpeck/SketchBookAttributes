package de.ellpeck.sketchbookattributes;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
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
                })))
                .then(Commands.literal("reset").executes(c -> {
                    CommandSource source = c.getSource();
                    PlayerEntity player = source.getPlayerOrException();
                    AttributeData data = AttributeData.get(player.level);
                    AttributeData.PlayerAttributes attributes = data.getAttributes(player);
                    // deserializing with empty data is basically a reset :)
                    attributes.deserializeNBT(new CompoundNBT());
                    attributes.reapplyAttributes(player);
                    PacketHandler.sendToAll(data.getPacket());
                    source.sendSuccess(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".reset", source.getDisplayName()), true);
                    return 0;
                })));
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

    @SubscribeEvent
    public static void livingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (source != null && source.isProjectile()) {
            Entity shooter = source.getEntity();
            if (shooter instanceof PlayerEntity) {
                AttributeData.PlayerAttributes attributes = AttributeData.get(shooter.level).getAttributes((PlayerEntity) shooter);
                event.setAmount(event.getAmount() + attributes.getRangedDamageBonus());
            }
        }
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class Client {

        private static final ResourceLocation MANA = new ResourceLocation(SketchBookAttributes.ID, "textures/ui/mana.png");

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

        @SubscribeEvent
        public static void onOverlayRender(RenderGameOverlayEvent.Post event) {
            if (event.getType() != RenderGameOverlayEvent.ElementType.ALL)
                return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null)
                return;
            AttributeData data = AttributeData.get(mc.player.level);
            AttributeData.PlayerAttributes attributes = data.getAttributes(mc.player);
            MatrixStack stack = event.getMatrixStack();
            MainWindow res = event.getWindow();

            // display mana bar
            if (!mc.player.isSpectator()) {
                stack.pushPose();
                mc.textureManager.bind(MANA);
                int x = res.getGuiScaledWidth() / 2 + 10;
                int y = res.getGuiScaledHeight() - (mc.player.isCreative() ? 29 : 45);
                AbstractGui.blit(stack, x, y, 0, 0, 81, 5, 256, 256);
                AbstractGui.blit(stack, x, y, 0, 5, (int) (81 * (attributes.mana / attributes.maxMana)), 5, 256, 256);
                stack.popPose();
            }
        }
    }
}