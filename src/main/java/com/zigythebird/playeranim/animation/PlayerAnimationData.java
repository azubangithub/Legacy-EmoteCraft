package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranimcore.animation.AnimationData;
import net.minecraft.client.entity.AbstractClientPlayer;

public class PlayerAnimationData extends AnimationData {
    private final AbstractClientPlayer player;

    public PlayerAnimationData(AbstractClientPlayer player, float velocity, float partialTick) {
        super(velocity, partialTick);
        this.player = player;
    }

    public AbstractClientPlayer getPlayer() {
        return this.player;
    }

    public PlayerAnimManager getPlayerAnimManager() {
        if (getPlayer() instanceof IAnimatedPlayer) {
            return ((IAnimatedPlayer) getPlayer()).playerAnimLib$getAnimManager();
        }
        return null;
    }

    @Override
    public PlayerAnimationData copy() {
        return new PlayerAnimationData(getPlayer(), getVelocity(), getPartialTick());
    }
}
