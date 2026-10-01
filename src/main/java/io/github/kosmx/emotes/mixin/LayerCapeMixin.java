package io.github.kosmx.emotes.mixin;

import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerCape.class)
public abstract class LayerCapeMixin {

    @Shadow
    @Final
    private RenderPlayer playerRenderer;

    /**
     * When an emote is active, cancel vanilla cape rendering and do our own
     * that follows the body transform and applies the "cape" bone animation.
     */
    @Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
    private void emotecraft$renderAnimatedCape(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        if (player instanceof IAnimatedPlayer) {
            PlayerAnimManager manager = ((IAnimatedPlayer) player).playerAnimLib$getAnimManager();
            if (manager != null && manager.isActive()) {
                ci.cancel();

                // Still need to check vanilla conditions
                if (!player.hasPlayerInfo() || player.isInvisible()
                        || !player.isWearing(EnumPlayerModelParts.CAPE)
                        || player.getLocationCape() == null) {
                    return;
                }

                ItemStack chestStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                if (chestStack.getItem() == Items.ELYTRA) {
                    return;
                }

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.playerRenderer.bindTexture(player.getLocationCape());
                GlStateManager.pushMatrix();

                ModelPlayer model = this.playerRenderer.getMainModel();

                // 1. Torso transform (rotationPoint + rotations)
                model.bipedBody.postRender(0.0625F);

                // 2. Translate to cape attachment point at back of torso
                GlStateManager.translate(0.0F, 0.0F, 0.125F);

                // 3. Rotate 180 on Y so cape faces backwards
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

                // 4. Custom cape bone animation matching modern playeranim
                PlayerAnimBone capeBone = new PlayerAnimBone("cape");
                capeBone.setToInitialPose();
                capeBone = manager.get3DTransform(capeBone);

                capeBone.positionX *= -1;
                capeBone.positionZ *= -1;
                capeBone.rotX *= -1;
                capeBone.rotZ *= -1;

                net.minecraft.client.model.ModelRenderer cape = ((io.github.kosmx.emotes.mc.IModelPlayerCape) model).emotecraft$getCape();
                if (cape != null) {
                    com.zigythebird.playeranim.util.RenderUtil.translatePartToBone(cape, capeBone, 0.0F, 0.0F, 0.0F);

                    PlayerAnimBone torsoBone = new PlayerAnimBone("torso");
                    torsoBone.setToInitialPose();
                    torsoBone = manager.get3DTransform(torsoBone);
                    float capeBend = capeBone.getBend() + torsoBone.getBend();

                    if (capeBend != 0.0F) {
                        com.zigythebird.bendable_cuboids.impl.compatibility.PlayerBendHelper.bend(cape, capeBend);
                    } else {
                        com.zigythebird.bendable_cuboids.impl.compatibility.PlayerBendHelper.resetBend(cape);
                    }
                }

                model.renderCape(0.0625F);

                if (cape != null) {
                    cape.rotationPointX = 0.0F;
                    cape.rotationPointY = 0.0F;
                    cape.rotationPointZ = 0.0F;
                    cape.rotateAngleX = 0.0F;
                    cape.rotateAngleY = 0.0F;
                    cape.rotateAngleZ = 0.0F;
                    com.zigythebird.bendable_cuboids.impl.compatibility.PlayerBendHelper.resetBend(cape);
                }

                GlStateManager.popMatrix();
            }
        }
    }
}
