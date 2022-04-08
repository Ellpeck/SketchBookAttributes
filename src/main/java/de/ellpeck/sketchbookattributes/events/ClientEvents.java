package de.ellpeck.sketchbookattributes.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.ellpeck.sketchbookattributes.Registry;
import de.ellpeck.sketchbookattributes.SketchBookAttributes;
import de.ellpeck.sketchbookattributes.data.AttributeData;
import de.ellpeck.sketchbookattributes.data.PlayerAttributes;
import de.ellpeck.sketchbookattributes.network.PacketHandler;
import de.ellpeck.sketchbookattributes.network.SkillActivatedPacket;
import de.ellpeck.sketchbookattributes.ui.AttributesScreen;
import de.ellpeck.sketchbookattributes.ui.ClassesScreen;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(Dist.CLIENT)
public final class ClientEvents {

    private static final ResourceLocation MANA = new ResourceLocation(SketchBookAttributes.ID, "textures/ui/mana.png");

    @SubscribeEvent
    public static void renderNameplate(RenderNameplateEvent event) {
        Entity entity = event.getEntity();
        ITextComponent content = event.getContent();
        if (entity instanceof PlayerEntity && content instanceof IFormattableTextComponent) {
            PlayerAttributes attributes = AttributeData.get(entity.level).getAttributes((PlayerEntity) entity);
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
        if (mc.screen == null) {
            PlayerAttributes data = AttributeData.get(mc.player.level).getAttributes(mc.player);
            if (data.playerClass == null) {
                mc.setScreen(new ClassesScreen(data));
            } else if (Registry.Client.OPEN_KEYBIND.consumeClick()) {
                mc.setScreen(new AttributesScreen(data));
            } else if (Registry.Client.SKILL_KEYBIND.consumeClick()) {
                PacketHandler.sendToServer(new SkillActivatedPacket());
            }
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
        PlayerAttributes attributes = data.getAttributes(mc.player);
        MatrixStack stack = event.getMatrixStack();
        MainWindow res = event.getWindow();

        // display mana bar
        if (!mc.player.isSpectator()) {
            stack.pushPose();
            mc.textureManager.bind(MANA);
            int x = res.getGuiScaledWidth() / 2 + 10;
            int y = res.getGuiScaledHeight() - (mc.player.isCreative() ? 29 : 45);
            AbstractGui.blit(stack, x, y, 0, 0, 81, 5, 256, 256);
            AbstractGui.blit(stack, x, y, 0, 5, (int) (81 * (attributes.mana / attributes.getMaxMana())), 5, 256, 256);
            stack.popPose();
        }

        // display class skill
        if (attributes.playerClass != null && !(mc.screen instanceof ChatScreen)) {
            stack.pushPose();
            String className = "class." + SketchBookAttributes.ID + "." + attributes.playerClass.name().toLowerCase(Locale.ROOT);
            int y = res.getGuiScaledHeight() - 2;

            ITextComponent desc = new TranslationTextComponent(className + ".skill.description", Registry.Client.SKILL_KEYBIND.getKey().getDisplayName());
            List<IReorderingProcessor> split = mc.font.split(desc, res.getGuiScaledWidth() / 4);
            for (int i = split.size() - 1; i >= 0; i--) {
                y -= mc.font.lineHeight;
                mc.font.drawShadow(stack, split.get(i), 2, y, 0xffffff);
            }

            y -= mc.font.lineHeight;
            mc.font.drawShadow(stack, new TranslationTextComponent(className + ".skill"), 2, y - 3, 0xffffff);
            stack.popPose();
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        for (String config : SketchBookAttributes.attributeItemRequirements.get()) {
            Matcher matcher = SketchBookAttributes.ITEM_REQUIREMENT_REGEX.matcher(config);
            if (!matcher.matches() || !Pattern.matches(matcher.group(1), stack.getItem().getRegistryName().toString()))
                continue;
            TranslationTextComponent ability = new TranslationTextComponent("attribute." + SketchBookAttributes.ID + "." + matcher.group(2));
            event.getToolTip().add(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".requirements", matcher.group(3), ability).withStyle(TextFormatting.DARK_PURPLE));
        }
    }
}
