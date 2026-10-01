/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranimcore.animation.keyframe;

import java.util.Objects;

/**
 * A named pair object that stores a {@link Keyframe} and a float representing a temporally placed {@code Keyframe}
 */
public class KeyframeLocation<T extends Keyframe> {
    private final T keyframe;
    private final float startTick;
    private final float tick;

    public KeyframeLocation(T keyframe, float startTick, float tick) {
        this.keyframe = keyframe;
        this.startTick = startTick;
        this.tick = tick;
    }

    public T keyframe() { return this.keyframe; }
    public float startTick() { return this.startTick; }
    public float tick() { return this.tick; }

    public T getKeyframe() { return this.keyframe; }
    public float getStartTick() { return this.startTick; }
    public float getTick() { return this.tick; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyframeLocation)) return false;
        KeyframeLocation<?> that = (KeyframeLocation<?>) o;
        return Float.compare(that.startTick, startTick) == 0 &&
               Float.compare(that.tick, tick) == 0 &&
               Objects.equals(keyframe, that.keyframe);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyframe, startTick, tick);
    }

    @Override
    public String toString() {
        return "KeyframeLocation{" +
                "keyframe=" + keyframe +
                ", startTick=" + startTick +
                ", tick=" + tick +
                '}';
    }
}
