package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.PlayerAnimManager;

public interface IMutableModel {
    void playerAnimLib$setAnimation(PlayerAnimManager animation);
    PlayerAnimManager playerAnimLib$getAnimation();
}
