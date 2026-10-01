package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;

public interface IAnimatedPlayer {
    PlayerAnimManager playerAnimLib$getAnimManager();
    AnimationProcessor playerAnimLib$getAnimProcessor();
}
