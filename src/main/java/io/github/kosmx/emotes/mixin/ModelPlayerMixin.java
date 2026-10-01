package io.github.kosmx.emotes.mixin;

import com.zigythebird.bendable_cuboids.impl.compatibility.PlayerBendHelper;
import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.accessors.IMutableModel;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import io.github.kosmx.emotes.mc.IModelPlayerCape;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPlayer.class)
public abstract class ModelPlayerMixin extends ModelBiped implements IMutableModel, IModelPlayerCape {

    @Shadow public ModelRenderer bipedLeftArmwear;
    @Shadow public ModelRenderer bipedRightArmwear;
    @Shadow public ModelRenderer bipedLeftLegwear;
    @Shadow public ModelRenderer bipedRightLegwear;
    @Shadow public ModelRenderer bipedBodyWear;
    @Shadow public ModelRenderer bipedCape;
    @Shadow private boolean smallArms;

    @Unique
    private PlayerAnimManager pal$animation;

    @Unique
    private final PlayerAnimBone pal$head = new PlayerAnimBone("head");
    @Unique
    private final PlayerAnimBone pal$torso = new PlayerAnimBone("torso");
    @Unique
    private final PlayerAnimBone pal$rightArm = new PlayerAnimBone("right_arm");
    @Unique
    private final PlayerAnimBone pal$leftArm = new PlayerAnimBone("left_arm");
    @Unique
    private final PlayerAnimBone pal$rightLeg = new PlayerAnimBone("right_leg");
    @Unique
    private final PlayerAnimBone pal$leftLeg = new PlayerAnimBone("left_leg");

    @Inject(method = "<init>", at = @At("RETURN"))
    private void emotecraft$initBends(float modelSize, boolean smallArmsIn, CallbackInfo ci) {
        PlayerBendHelper.initBend(this.bipedBody, EnumFacing.DOWN);
        PlayerBendHelper.initBend(this.bipedRightArm, EnumFacing.UP);
        PlayerBendHelper.initBend(this.bipedLeftArm, EnumFacing.UP);
        PlayerBendHelper.initBend(this.bipedRightLeg, EnumFacing.UP);
        PlayerBendHelper.initBend(this.bipedLeftLeg, EnumFacing.UP);

        PlayerBendHelper.initBend(this.bipedBodyWear, EnumFacing.DOWN);
        PlayerBendHelper.initBend(this.bipedRightArmwear, EnumFacing.UP);
        PlayerBendHelper.initBend(this.bipedLeftArmwear, EnumFacing.UP);
        PlayerBendHelper.initBend(this.bipedLeftLegwear, EnumFacing.UP);
        PlayerBendHelper.initBend(this.bipedCape, EnumFacing.UP, 6);
    }

    @Inject(method = "setRotationAngles", at = @At("HEAD"))
    private void emotecraft$resetInitialPose(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn, CallbackInfo ci) {
        float armX = 5.0F;
        float armY = this.smallArms ? 2.5F : 2.0F;

        this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedHead.rotateAngleX = 0.0F;
        this.bipedHead.rotateAngleY = 0.0F;
        this.bipedHead.rotateAngleZ = 0.0F;

        this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedBody.rotateAngleX = 0.0F;
        this.bipedBody.rotateAngleY = 0.0F;
        this.bipedBody.rotateAngleZ = 0.0F;

        this.bipedRightArm.setRotationPoint(-armX, armY, 0.0F);
        this.bipedRightArm.rotateAngleX = 0.0F;
        this.bipedRightArm.rotateAngleY = 0.0F;
        this.bipedRightArm.rotateAngleZ = 0.0F;

        this.bipedLeftArm.setRotationPoint(armX, armY, 0.0F);
        this.bipedLeftArm.rotateAngleX = 0.0F;
        this.bipedLeftArm.rotateAngleY = 0.0F;
        this.bipedLeftArm.rotateAngleZ = 0.0F;

        this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.bipedRightLeg.rotateAngleX = 0.0F;
        this.bipedRightLeg.rotateAngleY = 0.0F;
        this.bipedRightLeg.rotateAngleZ = 0.0F;

        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedLeftLeg.rotateAngleX = 0.0F;
        this.bipedLeftLeg.rotateAngleY = 0.0F;
        this.bipedLeftLeg.rotateAngleZ = 0.0F;

        copyModelAngles(this.bipedHead, this.bipedHeadwear);
        copyModelAngles(this.bipedBody, this.bipedBodyWear);
        copyModelAngles(this.bipedRightArm, this.bipedRightArmwear);
        copyModelAngles(this.bipedLeftArm, this.bipedLeftArmwear);
        copyModelAngles(this.bipedRightLeg, this.bipedRightLegwear);
        copyModelAngles(this.bipedLeftLeg, this.bipedLeftLegwear);
    }

    @Inject(method = "setRotationAngles", at = @At("RETURN"))
    private void emotecraft$setupPlayerAnimation(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn, CallbackInfo ci) {
        if (entityIn instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer) entityIn;
            if (player instanceof IAnimatedPlayer) {
                PlayerAnimManager manager = ((IAnimatedPlayer) player).playerAnimLib$getAnimManager();
                if (manager != null && manager.isActive()) {
                    this.pal$animation = manager;
                    AnimationProcessor processor = ((IAnimatedPlayer) player).playerAnimLib$getAnimProcessor();
                    processor.handleAnimations(manager.getTickDelta(), false);

                    float armX = 5.0F;
                    float armY = this.smallArms ? 2.5F : 2.0F;

                    RenderUtil.copyVanillaPart(this.bipedHead, pal$head, 0.0F, 0.0F, 0.0F);
                    RenderUtil.copyVanillaPart(this.bipedBody, pal$torso, 0.0F, 0.0F, 0.0F);
                    RenderUtil.copyVanillaPart(this.bipedRightArm, pal$rightArm, -armX, armY, 0.0F);
                    RenderUtil.copyVanillaPart(this.bipedLeftArm, pal$leftArm, armX, armY, 0.0F);
                    RenderUtil.copyVanillaPart(this.bipedRightLeg, pal$rightLeg, -1.9F, 12.0F, 0.0F);
                    RenderUtil.copyVanillaPart(this.bipedLeftLeg, pal$leftLeg, 1.9F, 12.0F, 0.0F);

                    manager.updatePart(this.bipedHead, pal$head, 0.0F, 0.0F, 0.0F);
                    manager.updatePart(this.bipedRightArm, pal$rightArm, -armX, armY, 0.0F);
                    manager.updatePart(this.bipedLeftArm, pal$leftArm, armX, armY, 0.0F);
                    manager.updatePart(this.bipedRightLeg, pal$rightLeg, -1.9F, 12.0F, 0.0F);
                    manager.updatePart(this.bipedLeftLeg, pal$leftLeg, 1.9F, 12.0F, 0.0F);
                    manager.updatePart(this.bipedBody, pal$torso, 0.0F, 0.0F, 0.0F);

                    copyModelAngles(this.bipedHead, this.bipedHeadwear);
                    copyModelAngles(this.bipedBody, this.bipedBodyWear);
                    copyModelAngles(this.bipedRightArm, this.bipedRightArmwear);
                    copyModelAngles(this.bipedLeftArm, this.bipedLeftArmwear);
                    copyModelAngles(this.bipedRightLeg, this.bipedRightLegwear);
                    copyModelAngles(this.bipedLeftLeg, this.bipedLeftLegwear);

                    PlayerBendHelper.bend(this.bipedBody, pal$torso.getBend());
                    PlayerBendHelper.bend(this.bipedRightArm, pal$rightArm.getBend());
                    PlayerBendHelper.bend(this.bipedLeftArm, pal$leftArm.getBend());
                    PlayerBendHelper.bend(this.bipedRightLeg, pal$rightLeg.getBend());
                    PlayerBendHelper.bend(this.bipedLeftLeg, pal$leftLeg.getBend());

                    PlayerBendHelper.bend(this.bipedBodyWear, pal$torso.getBend());
                    PlayerBendHelper.bend(this.bipedRightArmwear, pal$rightArm.getBend());
                    PlayerBendHelper.bend(this.bipedLeftArmwear, pal$leftArm.getBend());
                    PlayerBendHelper.bend(this.bipedRightLegwear, pal$rightLeg.getBend());
                    PlayerBendHelper.bend(this.bipedLeftLegwear, pal$leftLeg.getBend());
                    return;
                }
            }
        }

        this.pal$animation = null;
        this.pal$head.setToInitialPose();
        this.pal$torso.setToInitialPose();
        this.pal$rightArm.setToInitialPose();
        this.pal$leftArm.setToInitialPose();
        this.pal$rightLeg.setToInitialPose();
        this.pal$leftLeg.setToInitialPose();

        PlayerBendHelper.resetBend(this.bipedBody);
        PlayerBendHelper.resetBend(this.bipedRightArm);
        PlayerBendHelper.resetBend(this.bipedLeftArm);
        PlayerBendHelper.resetBend(this.bipedRightLeg);
        PlayerBendHelper.resetBend(this.bipedLeftLeg);

        PlayerBendHelper.resetBend(this.bipedBodyWear);
        PlayerBendHelper.resetBend(this.bipedRightArmwear);
        PlayerBendHelper.resetBend(this.bipedLeftArmwear);
        PlayerBendHelper.resetBend(this.bipedRightLegwear);
        PlayerBendHelper.resetBend(this.bipedLeftLegwear);
        PlayerBendHelper.resetBend(this.bipedCape);

        copyModelAngles(this.bipedHead, this.bipedHeadwear);
        copyModelAngles(this.bipedBody, this.bipedBodyWear);
        copyModelAngles(this.bipedRightArm, this.bipedRightArmwear);
        copyModelAngles(this.bipedLeftArm, this.bipedLeftArmwear);
        copyModelAngles(this.bipedRightLeg, this.bipedRightLegwear);
        copyModelAngles(this.bipedLeftLeg, this.bipedLeftLegwear);
    }

    @Override
    public void playerAnimLib$setAnimation(PlayerAnimManager animation) {
        this.pal$animation = animation;
    }

    @Override
    public PlayerAnimManager playerAnimLib$getAnimation() {
        return this.pal$animation;
    }

    @Override
    public ModelRenderer emotecraft$getCape() {
        return this.bipedCape;
    }
}
