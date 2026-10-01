package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.bones.AdvancedPlayerAnimBone;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class PlayerAnimationController extends AnimationController {
    private static final Map<String, Vec3f> BONE_POSITIONS = new HashMap<>();
    static {
        BONE_POSITIONS.put("right_arm", new Vec3f(5, 22, 0));
        BONE_POSITIONS.put("left_arm", new Vec3f(-5, 22, 0));
        BONE_POSITIONS.put("left_leg", new Vec3f(-2f, 12, 0f));
        BONE_POSITIONS.put("right_leg", new Vec3f(2f, 12, 0f));
        BONE_POSITIONS.put("torso", new Vec3f(0, 24, 0));
        BONE_POSITIONS.put("head", new Vec3f(0, 24, 0));
        BONE_POSITIONS.put("body", new Vec3f(0, 12, 0));
        BONE_POSITIONS.put("cape", new Vec3f(0, 24, 2));
        BONE_POSITIONS.put("elytra", new Vec3f(0, 24, 2));
    }

    protected List<AdvancedPlayerAnimBone> top_bones;
    protected final AbstractClientPlayer player;

    public PlayerAnimationController(AbstractClientPlayer player, AnimationStateHandler animationHandler) {
        super(animationHandler);
        this.player = player;
    }

    public AbstractClientPlayer getPlayer() {
        return this.player;
    }

    @Override
    public void registerBones() {
        this.top_bones = new ArrayList<>();

        this.registerPlayerAnimBone("body");
        this.registerTopPlayerAnimBone("right_arm");
        this.registerTopPlayerAnimBone("left_arm");
        this.registerPlayerAnimBone("right_leg");
        this.registerPlayerAnimBone("left_leg");
        this.registerTopPlayerAnimBone("head");
        this.registerPlayerAnimBone("torso");
        this.registerPlayerAnimBone("right_item");
        this.registerPlayerAnimBone("left_item");
        this.registerTopPlayerAnimBone("cape");
        this.registerPlayerAnimBone("elytra");
    }

    public void registerTopPlayerAnimBone(String name) {
        this.top_bones.add(this.registerPlayerAnimBone(name));
    }

    @Override
    protected Queue<AnimationProcessor.QueuedAnimation> getQueuedAnimations(RawAnimation rawAnimation) {
        if (player == null || !(player instanceof IAnimatedPlayer)) return null;
        return ((IAnimatedPlayer) this.player).playerAnimLib$getAnimProcessor().buildAnimationQueue(rawAnimation);
    }

    @Override
    protected void applyCustomPivotPoints() {
        if (bones.containsKey("torso")) {
            float bend = bones.get("torso").getBend();
            float absBend = Math.abs(bend);
            if (absBend > 0.001F && (this.currentAnimation != null && this.currentAnimation.animation().data().getNullable(ExtraAnimationData.APPLY_BEND_TO_OTHER_BONES_KEY) == Boolean.TRUE)) {
                float h = -(1.0F - MathHelper.cos(absBend));
                float i = 1.0F - MathHelper.sin(absBend);
                int sign = (int) Math.signum(bend);
                for (AdvancedPlayerAnimBone bone : top_bones) {
                    float offset = getBonePosition(bone.getName()).y() - 18.0F;
                    this.activeBones.put(bone.getName(), bone);
                    bone.rotX += bend;
                    bone.positionZ += (offset * i - offset) * sign;
                    bone.positionY += offset * h;
                    bone.rotXEnabled = true;
                    bone.positionYEnabled = true;
                    bone.positionZEnabled = true;
                }
            }
        }
        super.applyCustomPivotPoints();
    }

    @Override
    public Vec3f getBonePosition(String name) {
        if (BONE_POSITIONS.containsKey(name)) return BONE_POSITIONS.get(name);
        if (pivotBones.containsKey(name)) return pivotBones.get(name).getPivot();
        return Vec3f.ZERO;
    }
}
