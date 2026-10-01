package com.zigythebird.bendable_cuboids.mixin;

import com.zigythebird.bendable_cuboids.api.BendableCube;
import com.zigythebird.bendable_cuboids.api.BendableModelPart;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModelRenderer.class)
public abstract class ModelRendererMixin implements BendableModelPart {

    @Shadow
    public List<ModelBox> cubeList;

    @Unique
    private float bc$scale = 0.0625F;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(float scale, CallbackInfo ci) {
        this.bc$scale = scale;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;callList(I)V"))
    private void redirectCallList(int list) {
        if (this.bc$hasBentBoxes()) {
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            for (int i = 0; i < this.cubeList.size(); ++i) {
                this.cubeList.get(i).render(buffer, this.bc$scale);
            }
        } else {
            GlStateManager.callList(list);
        }
    }

    @Unique
    private boolean bc$hasBentBoxes() {
        if (this.cubeList == null || this.cubeList.isEmpty()) return false;
        for (int i = 0; i < this.cubeList.size(); ++i) {
            ModelBox box = this.cubeList.get(i);
            if (box instanceof BendableCube) {
                if (((BendableCube) box).getBend() != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public BendableCube bc$getCuboid(int index) {
        if (this.cubeList != null && index >= 0 && index < this.cubeList.size()) {
            ModelBox box = this.cubeList.get(index);
            if (box instanceof BendableCube) {
                return (BendableCube) box;
            }
        }
        return null;
    }
}
