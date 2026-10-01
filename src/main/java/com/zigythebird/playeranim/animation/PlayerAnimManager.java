package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.animation.layered.AnimationStack;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelRenderer;

public class PlayerAnimManager extends AnimationStack {
    private final AbstractClientPlayer player;

    private float lastUpdateTime;
    private boolean isFirstTick = true;
    private float tickDelta;

    public PlayerAnimManager(AbstractClientPlayer player) {
        this.player = player;
    }

    public float getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public void updatedAt(float updateTime) {
        this.lastUpdateTime = updateTime;
    }

    public boolean isFirstTick() {
        return this.isFirstTick;
    }

    protected void finishFirstTick() {
        this.isFirstTick = false;
    }

    public float getTickDelta() {
        return this.tickDelta;
    }

    public void setTickDelta(float tickDelta) {
        this.tickDelta = tickDelta;
    }

    public AbstractClientPlayer getPlayer() {
        return player;
    }

    public void updatePart(ModelRenderer part, PlayerAnimBone bone, float defaultX, float defaultY, float defaultZ) {
        bone = this.get3DTransform(bone);
        RenderUtil.translatePartToBone(part, bone, defaultX, defaultY, defaultZ);
    }
}
