package de.ellpeck.sketchbookattributes;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.util.Collections;
import java.util.function.Function;

public class AttributesScreen extends Screen {

    private static final ResourceLocation IMAGE = new ResourceLocation(SketchBookAttributes.ID, "textures/ui/attributes.png");
    private static final int IMAGE_WIDTH = 247;
    private static final int IMAGE_HEIGHT = 157;

    private final AttributeData data;
    private StatInfo[] stats;

    public AttributesScreen(AttributeData data) {
        super(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".screen"));
        this.data = data;
    }

    @Override
    protected void init() {
        int left = (this.width - IMAGE_WIDTH) / 2;
        int top = (this.height - IMAGE_HEIGHT) / 2;

        this.stats = new StatInfo[]{
                new StatInfo(this.data, "strength", d -> d.strength, left + 39, top + 28),
                new StatInfo(this.data, "dexterity", d -> d.dexterity, left + 39, top + 52),
                new StatInfo(this.data, "constitution", d -> d.constitution, left + 39, top + 76),
                new StatInfo(this.data, "intelligence", d -> d.intelligence, left + 39, top + 100),
                new StatInfo(this.data, "agility", d -> d.agility, left + 39, top + 124)
        };

        for (StatInfo stat : this.stats)
            this.addButton(stat.button);
    }

    @Override
    public void render(MatrixStack stack, int x, int y, float pt) {
        int left = (this.width - IMAGE_WIDTH) / 2;
        int top = (this.height - IMAGE_HEIGHT) / 2;

        this.renderBackground(stack);

        this.minecraft.textureManager.bind(IMAGE);
        this.blit(stack, left, top, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        this.font.draw(stack, this.title, left + 122 - this.font.width(this.title) / 2F, top + 8, 4210752);

        for (StatInfo stat : this.stats)
            stat.render(stack, this.font);

        super.render(stack, x, y, pt);

        for (StatInfo stat : this.stats)
            stat.renderTooltip(stack, this.font, x, y);
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

    private static class StatInfo {

        public final Button button;

        private final AttributeData data;
        private final String name;
        private final Function<AttributeData, Integer> getLevel;
        private final int x;
        private final int y;

        public StatInfo(AttributeData data, String name, Function<AttributeData, Integer> getLevel, int x, int y) {
            this.data = data;
            this.name = name;
            this.getLevel = getLevel;
            this.x = x;
            this.y = y;

            this.button = new ExtendedButton(this.x - 19, this.y + 1, 15, 15, new StringTextComponent("+"), b -> {

            });
        }

        public void render(MatrixStack stack, FontRenderer font) {
            String level = this.getLevel.apply(this.data).toString();
            font.draw(stack, new TranslationTextComponent("attribute." + SketchBookAttributes.ID + "." + this.name), this.x + 5, this.y + 5, 4210752);
            font.draw(stack, level, this.x + 95 - font.width(level), this.y + 5, 4210752);
        }

        public void renderTooltip(MatrixStack stack, FontRenderer font, int mouseX, int mouseY) {
            if (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + 99 && mouseY < this.y + 17) {
                ITextComponent text = new TranslationTextComponent("attribute." + SketchBookAttributes.ID + "." + this.name + ".description");
                GuiUtils.drawHoveringText(stack, Collections.singletonList(text), mouseX, mouseY, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, font);
            }
        }
    }
}
