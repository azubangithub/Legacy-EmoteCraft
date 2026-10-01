package io.github.kosmx.emotes.compat.lfgm;

public class LFGMPhysicsResult {
    public final float deltaTargetY;
    public final float deltaTargetX;
    public final float deltaTargetRot;
    public final float impulseY;
    public final float impulseX;
    public final float impulseRot;

    public LFGMPhysicsResult(float deltaTargetY, float deltaTargetX, float deltaTargetRot,
                             float impulseY, float impulseX, float impulseRot) {
        this.deltaTargetY = deltaTargetY;
        this.deltaTargetX = deltaTargetX;
        this.deltaTargetRot = deltaTargetRot;
        this.impulseY = impulseY;
        this.impulseX = impulseX;
        this.impulseRot = impulseRot;
    }
}
