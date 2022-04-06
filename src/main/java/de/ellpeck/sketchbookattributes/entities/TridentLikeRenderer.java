package de.ellpeck.sketchbookattributes.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class TridentLikeRenderer extends EntityRenderer<TridentLikeEntity> {

    private final ItemRenderer itemRenderer;

    public TridentLikeRenderer(EntityRendererManager p_i46179_1_, ItemRenderer itemRenderer) {
        super(p_i46179_1_);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(TridentLikeEntity entity, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, IRenderTypeBuffer p_225623_5_, int p_225623_6_) {
        if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25D)) {
            p_225623_4_.pushPose();
            p_225623_4_.scale(1.5F, 1.5F, 1.5F);
            p_225623_4_.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(p_225623_3_, entity.yRotO, entity.yRot) - 90.0F));
            p_225623_4_.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(p_225623_3_, entity.xRotO, entity.xRot) - 45.0F));
            this.itemRenderer.renderStatic(entity.tridentItem, ItemCameraTransforms.TransformType.GROUND, p_225623_6_, OverlayTexture.NO_OVERLAY, p_225623_4_, p_225623_5_);
            p_225623_4_.popPose();
            super.render(entity, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(TridentLikeEntity p_110775_1_) {
        return AtlasTexture.LOCATION_BLOCKS;
    }
}
