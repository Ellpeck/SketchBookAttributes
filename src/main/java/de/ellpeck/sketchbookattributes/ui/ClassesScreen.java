package de.ellpeck.sketchbookattributes.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.ellpeck.sketchbookattributes.SketchBookAttributes;
import de.ellpeck.sketchbookattributes.data.PlayerAttributes;
import de.ellpeck.sketchbookattributes.data.PlayerClass;
import de.ellpeck.sketchbookattributes.network.ClassButtonPacket;
import de.ellpeck.sketchbookattributes.network.PacketHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.util.Collections;
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
        for (int i = 0; i < this.classes.length; i++) {
            this.classes[i] = new ClassInfo(this.data, PlayerClass.values()[i], left + 41, top + 34 + 31 * i);
            this.addButton(this.classes[i].button);
        }
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

    private class ClassInfo {

        public final Button button;

        private final PlayerAttributes data;
        private final PlayerClass playerClass;
        private final int x;
        private final int y;

        public ClassInfo(PlayerAttributes data, PlayerClass playerClass, int x, int y) {
            this.data = data;
            this.playerClass = playerClass;
            this.x = x;
            this.y = y;

            this.button = new ExtendedButton(this.x + 142 - 50, this.y + 4, 50, 20, new TranslationTextComponent("info." + SketchBookAttributes.ID + ".choose"), b -> {
                PacketHandler.sendToServer(new ClassButtonPacket(this.playerClass));
                // also set the class on the client so that the screen stays closed
                this.data.playerClass = this.playerClass;
                ClassesScreen.this.minecraft.setScreen(null);
            });
        }

        public void render(MatrixStack stack) {
            ITextComponent text = new TranslationTextComponent("class." + SketchBookAttributes.ID + "." + this.playerClass.name().toLowerCase(Locale.ROOT));
            ClassesScreen.this.font.draw(stack, text, this.x + 5, this.y + 10, 4210752);
        }

        public void renderTooltip(MatrixStack stack, int mouseX, int mouseY) {
            if (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + 142 && mouseY < this.y + 28) {
                ITextComponent text = new TranslationTextComponent("class." + SketchBookAttributes.ID + "." + this.playerClass.name().toLowerCase(Locale.ROOT) + ".description");
                GuiUtils.drawHoveringText(stack, Collections.singletonList(text), mouseX, mouseY, Integer.MAX_VALUE, Integer.MAX_VALUE, 200, ClassesScreen.this.font);
            }
        }
    }
}
