package io.github.kosmx.emotes.mixin;

import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerHeldItem.class)
public abstract class LayerHeldItemMixin {

    @Unique
    private final PlayerAnimBone bendableCuboids$rightArm = new PlayerAnimBone("right_arm");
    @Unique
    private final PlayerAnimBone bendableCuboids$leftArm = new PlayerAnimBone("left_arm");

    @Inject(method = "renderHeldItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/LayerHeldItem;translateToHand(Lnet/minecraft/util/EnumHandSide;)V", shift = At.Shift.AFTER))
    private void emotecraft$renderArmItemBend(EntityLivingBase entity, ItemStack stack, ItemCameraTransforms.TransformType transformType, EnumHandSide handSide, CallbackInfo ci) {
        if (entity instanceof IAnimatedPlayer) {
            PlayerAnimManager manager = ((IAnimatedPlayer) entity).playerAnimLib$getAnimManager();
            if (manager != null && manager.isActive()) {
                PlayerAnimBone bone = handSide == EnumHandSide.LEFT ? bendableCuboids$leftArm : bendableCuboids$rightArm;
                bone.setBend(0);
                manager.get3DTransform(bone);
                float bend = bone.getBend();
                if (bend != 0.0F) {
                    float offset = 0.25F;
                    GlStateManager.translate(0.0F, offset, 0.0F);
                    GlStateManager.rotate(bend * (180.0F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                    GlStateManager.translate(0.0F, -offset, 0.0F);
                }
            }
        }
    }
}
