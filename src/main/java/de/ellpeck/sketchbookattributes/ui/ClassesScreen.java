package de.ellpeck.sketchbookattributes.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.ellpeck.sketchbookattributes.SketchBookAttributes;
import de.ellpeck.sketchbookattributes.data.PlayerAttributes;
import de.ellpeck.sketchbookattributes.data.PlayerClass;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Locale;

public class ClassesScreen extends Screen {

    private static final ResourceLocation IMAGE = new ResourceLocation(SketchBookAttributes.ID, "textures/ui/classes.png");
    private static final int IMAGE_WIDTH = 195;
    private static final int IMAGE_HEIGHT = 136;

    private final PlayerAttributes data;
    private ClassInfo[] classes;

    public ClassesScreen(PlayerAttributes data) {
        super(new TranslationTextComponent("info." + SketchBookAttributes.ID + ".classes"));
        this.data = data;
    }

    @Override
    protected void init() {
        int left = (this.width - IMAGE_WIDTH) / 2;
        int top = (this.height - IMAGE_HEIGHT) / 2;

        this.classes = new ClassInfo[PlayerClass.values().length];
        for (int i = 0; i < this.classes.length; i++)
            this.classes[i] = new ClassInfo(this.data, PlayerClass.values()[i], left + 41, top + 34 + 31 * i);
    }

    @Override
    public void render(MatrixStack stack, int x, int y, float pt) {
        int left = (this.width - IMAGE_WIDTH) / 2;
        int top = (this.height - IMAGE_HEIGHT) / 2;

        this.renderBackground(stack);
        this.minecraft.textureManager.bind(IMAGE);
        this.blit(stack, left, top, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        this.font.draw(stack, this.title, left + IMAGE_WIDTH / 2F - this.font.width(this.title) / 2F, top + 8, 4210752);

        for (ClassInfo cls : this.classes)
            cls.render(stack);

        super.render(stack, x, y, pt);

        for (ClassInfo cls : this.classes)
            cls.renderTooltip(stack, x, y);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // TODO make this false but right now it's a good way to refresh the ui while editing
        return true;
    }

    private class ClassInfo {

        private final PlayerAttributes data;
        private final PlayerClass cls;
        private final int x;
        private final int y;

        public ClassInfo(PlayerAttributes data, PlayerClass cls, int x, int y) {
            this.data = data;
            this.cls = cls;
            this.x = x;
            this.y = y;
        }

        public void render(MatrixStack stack) {
            ITextComponent text = new TranslationTextComponent("class." + SketchBookAttributes.ID + "." + this.cls.name().toLowerCase(Locale.ROOT));
            ClassesScreen.this.font.draw(stack, text, this.x + 5, this.y + 10, 4210752);
        }

        public void renderTooltip(MatrixStack stack, int mouseX, int mouseY) {
        }
    }
}
