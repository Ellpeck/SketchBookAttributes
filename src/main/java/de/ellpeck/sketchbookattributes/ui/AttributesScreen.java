package de.ellpeck.sketchbookattributes.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.ellpeck.sketchbookattributes.network.AttributeButtonPacket;
import de.ellpeck.sketchbookattributes.network.PacketHandler;
import de.ellpeck.sketchbookattributes.Registry;
import de.ellpeck.sketchbookattributes.SketchBookAttributes;
import de.ellpeck.sketchbookattributes.data.PlayerAttributes;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.function.Function;

public class AttributesScreen extends Screen {

    private static final ResourceLocation IMAGE = new ResourceLocation(SketchBookAttributes.ID, "textures/ui/attributes.png");
    private static final int IMAGE_WIDTH = 272;
    private static final int IMAGE_HEIGHT = 177;

    private final PlayerAttributes data;
    private AttributeInfo[] attributes;

    public AttributesScreen(PlayerAttributes data) {
        super(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".screen"));
        this.data = data;
    }

    @Override
    protected void init() {
        int left = (this.width - IMAGE_WIDTH) / 2;
        int top = (this.height - IMAGE_HEIGHT) / 2;

        this.attributes = new AttributeInfo[]{
                new AttributeInfo(this.data, "strength", PlayerAttributes::getStrength, left + 39, top + 33),
                new AttributeInfo(this.data, "dexterity", PlayerAttributes::getDexterity, left + 39, top + 60),
                new AttributeInfo(this.data, "constitution", PlayerAttributes::getConstitution, left + 39, top + 87),
                new AttributeInfo(this.data, "intelligence", PlayerAttributes::getIntelligence, left + 39, top + 114),
                new AttributeInfo(this.data, "agility", PlayerAttributes::getAgility, left + 39, top + 141)
        };

        for (AttributeInfo attribute : this.attributes)
            this.addButton(attribute.button);
    }

    @Override
    public void render(MatrixStack stack, int x, int y, float pt) {
        int left = (this.width - IMAGE_WIDTH) / 2;
        int top = (this.height - IMAGE_HEIGHT) / 2;

        this.renderBackground(stack);

        this.minecraft.textureManager.bind(IMAGE);
        blit(stack, left, top, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, 512, 256);
        blit(stack, left + 25, top + 166, 0, 177, 220 * this.data.pointsToNextLevel / this.data.getXpNeededForNextLevel(), 6, 512, 256);
        this.font.draw(stack, this.title, left + IMAGE_WIDTH / 2F - this.font.width(this.title) / 2F, top + 8, 4210752);

        // draw level with shadow
        ITextComponent level = new TranslationTextComponent("info." + SketchBookAttributes.ID + ".level", this.data.level);
        float levelX = left + IMAGE_WIDTH / 2F - this.font.width(level) / 2F;
        float levelY = top + 165;
        this.font.draw(stack, level, levelX - 1, levelY, 0);
        this.font.draw(stack, level, levelX + 1, levelY, 0);
        this.font.draw(stack, level, levelX, levelY - 1, 0);
        this.font.draw(stack, level, levelX, levelY + 1, 0);
        this.font.draw(stack, level, levelX, levelY, 8453920);

        for (AttributeInfo attribute : this.attributes)
            attribute.render(stack);

        int statX = left + 150;
        int statY = top + 44;
        int statOffset = 12;
        this.renderStat(stack, "health", this.minecraft.player.getMaxHealth(), statX, statY);
        this.renderStat(stack, "health_regen", this.data.getHealthRegenPerSecond(), statX, statY + statOffset);
        this.renderStat(stack, "mana", this.data.getMaxMana(), statX, statY + statOffset * 2);
        this.renderStat(stack, "mana_regen", this.data.getManaRegenPerSecond(), statX, statY + statOffset * 3);
        this.renderStat(stack, "melee_bonus", this.data.getMeleeDamageBonus(), statX, statY + statOffset * 4);
        this.renderStat(stack, "ranged_bonus", this.data.getRangedDamageBonus(), statX, statY + statOffset * 5);
        this.renderStat(stack, "melee_speed", this.data.getMeleeSpeedBonus(), statX, statY + statOffset * 6);
        this.renderStat(stack, "movement_speed", this.data.getMovementSpeedBonus(), statX, statY + statOffset * 7);
        this.renderStat(stack, "skill_points", this.data.skillPoints, statX, statY + statOffset * 8);

        super.render(stack, x, y, pt);

        for (AttributeInfo attribute : this.attributes)
            attribute.renderTooltip(stack, x, y);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        InputMappings.Input input = InputMappings.getKey(i, j);
        if (Registry.Client.OPEN_KEYBIND.isActiveAndMatches(input)) {
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderStat(MatrixStack stack, String name, float value, int x, int y) {
        String valueString = new DecimalFormat("0.##").format(value);
        this.font.draw(stack, new TranslationTextComponent("stat." + SketchBookAttributes.ID + "." + name), x, y, 4210752);
        this.font.draw(stack, valueString, x + 102 - this.font.width(valueString), y, 4210752);
    }

    private class AttributeInfo {

        public final Button button;

        private final PlayerAttributes data;
        private final String name;
        private final Function<PlayerAttributes, Integer> getLevel;
        private final int x;
        private final int y;

        public AttributeInfo(PlayerAttributes data, String name, Function<PlayerAttributes, Integer> getLevel, int x, int y) {
            this.data = data;
            this.name = name;
            this.getLevel = getLevel;
            this.x = x;
            this.y = y;

            this.button = new ExtendedButton(this.x - 19, this.y + 1, 15, 15, new StringTextComponent("+"),
                    b -> PacketHandler.sendToServer(new AttributeButtonPacket(this.name)));
            this.button.active = this.data.skillPoints > 0;
        }

        public void render(MatrixStack stack) {
            String level = this.getLevel.apply(this.data).toString();
            ITextComponent text = new TranslationTextComponent("attribute." + SketchBookAttributes.ID + "." + this.name);
            AttributesScreen.this.font.draw(stack, text, this.x + 5, this.y + 5, 4210752);
            AttributesScreen.this.font.draw(stack, level, this.x + 80 - AttributesScreen.this.font.width(level), this.y + 5, 4210752);

            this.button.active = this.data.skillPoints > 0;
        }

        public void renderTooltip(MatrixStack stack, int mouseX, int mouseY) {
            if (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + 83 && mouseY < this.y + 17) {
                ITextComponent text = new TranslationTextComponent("attribute." + SketchBookAttributes.ID + "." + this.name + ".description");
                GuiUtils.drawHoveringText(stack, Collections.singletonList(text), mouseX, mouseY, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, AttributesScreen.this.font);
            }
        }
    }
}
