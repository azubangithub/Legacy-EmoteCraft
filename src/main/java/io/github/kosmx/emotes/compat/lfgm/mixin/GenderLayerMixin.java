package io.github.kosmx.emotes.compat.lfgm.mixin;

import com.wildfire.client.render.GenderLayer;
import io.github.kosmx.emotes.compat.lfgm.LFGMPhysicsHandler;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GenderLayer.class, remap = false)
public abstract class GenderLayerMixin {

    @Shadow @Final private RenderLivingBase<?> renderer;

    @Inject(
        method = "renderBreastWithTransforms",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/ModelRenderer;postRender(F)V",
            shift = At.Shift.AFTER
        ),
        remap = false,
        require = 0
    )
    private void emotecraft$applyTorsoBendDev(
            EntityLivingBase entity, ItemStack armorStack, float scale,
            ResourceLocation entityTexture, boolean bounceEnabled, float physPositionX, float physPositionY, float bounceRotation,
            float breastSize, float breastOffsetX, float breastOffsetY, float breastOffsetZ, float zOff, float outwardAngle,
            boolean uniboob, boolean isChestplateOccupied, boolean breathingAnimation, boolean left, boolean hasJacketLayer,
            CallbackInfo ci) {
        if (this.renderer != null && this.renderer.getMainModel() instanceof ModelBiped) {
            ModelBiped model = (ModelBiped) this.renderer.getMainModel();
            LFGMPhysicsHandler.applyTorsoBendTransform(model.bipedBody, entity, scale);
        }
    }

    @Inject(
        method = "renderBreastWithTransforms",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/ModelRenderer;func_78794_c(F)V",
            shift = At.Shift.AFTER
        ),
        remap = false,
        require = 0
    )
    private void emotecraft$applyTorsoBendProd(
            EntityLivingBase entity, ItemStack armorStack, float scale,
            ResourceLocation entityTexture, boolean bounceEnabled, float physPositionX, float physPositionY, float bounceRotation,
            float breastSize, float breastOffsetX, float breastOffsetY, float breastOffsetZ, float zOff, float outwardAngle,
            boolean uniboob, boolean isChestplateOccupied, boolean breathingAnimation, boolean left, boolean hasJacketLayer,
            CallbackInfo ci) {
        if (this.renderer != null && this.renderer.getMainModel() instanceof ModelBiped) {
            ModelBiped model = (ModelBiped) this.renderer.getMainModel();
            LFGMPhysicsHandler.applyTorsoBendTransform(model.bipedBody, entity, scale);
        }
    }
}
