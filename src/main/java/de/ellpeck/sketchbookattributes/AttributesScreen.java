package de.ellpeck.sketchbookattributes;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class AttributesScreen extends Screen {

    private static final ResourceLocation IMAGE = new ResourceLocation(SketchBookAttributes.ID, "textures/ui/attributes.png");
    private static final int IMAGE_WIDTH = 195;
    private static final int IMAGE_HEIGHT = 136;

    public AttributesScreen() {
        super(new TranslationTextComponent(SketchBookAttributes.ID + ".attributes_screen"));
    }

    @Override
    public void render(MatrixStack stack, int x, int y, float pt) {
        int left = (this.width - IMAGE_WIDTH) / 2;
        int top = (this.height - IMAGE_HEIGHT) / 2;

        this.renderBackground(stack);
        this.minecraft.textureManager.bind(IMAGE);
        this.blit(stack, left, top, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        super.render(stack, x, y, pt);
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

}
