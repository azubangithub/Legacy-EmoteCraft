package io.github.kosmx.emotes.mixin;

import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderLivingBase.class)
public abstract class RenderLivingBaseMixin<T extends EntityLivingBase> {

    @Unique
    private boolean pal$pushed = false;

    @Inject(method = "renderModel", at = @At("HEAD"))
    private void emotecraft$preRenderModel(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo ci) {
        if (!this.pal$pushed && entitylivingbaseIn instanceof IAnimatedPlayer) {
            PlayerAnimManager manager = ((IAnimatedPlayer) entitylivingbaseIn).playerAnimLib$getAnimManager();
            if (manager != null && manager.isActive()) {
                GlStateManager.pushMatrix();
                this.pal$pushed = true;

                PlayerAnimBone body = manager.get3DTransform(new PlayerAnimBone("body"));

                GlStateManager.translate(0.0F, 1.501F, 0.0F);
                GlStateManager.scale(-1.0F, -1.0F, 1.0F);

                GlStateManager.scale(body.getScaleX(), body.getScaleY(), body.getScaleZ());
                GlStateManager.translate(body.getPosX() / 16.0F, body.getPosY() / 16.0F + 0.75F, body.getPosZ() / 16.0F);

                RenderUtil.rotateAroundBone(body);

                GlStateManager.translate(0.0F, -0.75F, 0.0F);
                GlStateManager.scale(-1.0F, -1.0F, 1.0F);
                GlStateManager.translate(0.0F, -1.501F, 0.0F);
                return;
            }
        }
    }

    @Inject(method = "renderLayers", at = @At("RETURN"))
    private void emotecraft$postRenderLayers(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn, CallbackInfo ci) {
        if (this.pal$pushed) {
            GlStateManager.popMatrix();
            this.pal$pushed = false;
        }
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    private void emotecraft$postDoRender(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (this.pal$pushed) {
            GlStateManager.popMatrix();
            this.pal$pushed = false;
        }
    }
}
