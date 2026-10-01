package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.animation.layered.AnimationStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;

public class PlayerAnimationProcessor extends AnimationProcessor {
    private final AbstractClientPlayer player;

    public PlayerAnimationProcessor(AbstractClientPlayer player) {
        super();
        this.player = player;
    }

    @Override
    public void tickAnimation(AnimationStack stack, AnimationData state) {
        super.tickAnimation(stack, state);

        if (stack instanceof PlayerAnimManager) {
            ((PlayerAnimManager) stack).finishFirstTick();
        }
    }

    @Override
    public void handleAnimations(float partialTick, boolean fullTick) {
        if (!(player instanceof IAnimatedPlayer)) return;
        PlayerAnimManager animatableManager = ((IAnimatedPlayer) player).playerAnimLib$getAnimManager();
        if (animatableManager == null) return;

        float velocity = (float) ((Math.abs(player.motionX) + Math.abs(player.motionZ)) / 2.0);
        int currentTick = player.ticksExisted;

        float currentFrameTime = currentTick + partialTick;

        AnimationData animationData = new PlayerAnimationData(player, velocity, partialTick);

        if (fullTick) {
            animatableManager.tick(animationData.copy());
        }

        if (!animatableManager.isFirstTick() && currentFrameTime == animatableManager.getLastUpdateTime()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && !mc.isGamePaused()) {
            animatableManager.updatedAt(currentFrameTime);
        }

        this.tickAnimation(animatableManager, animationData);
    }

    public AbstractClientPlayer getPlayer() {
        return this.player;
    }
}
