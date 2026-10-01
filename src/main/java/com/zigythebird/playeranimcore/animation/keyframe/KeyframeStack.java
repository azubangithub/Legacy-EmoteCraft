/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranimcore.animation.keyframe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Objects;

/**
 * Stores a triplet of {@link Keyframe Keyframes} in an ordered stack
 */
public class KeyframeStack {
	private final List<Keyframe> xKeyframes;
	private final List<Keyframe> yKeyframes;
	private final List<Keyframe> zKeyframes;

	public KeyframeStack(List<Keyframe> xKeyframes, List<Keyframe> yKeyframes, List<Keyframe> zKeyframes) {
		this.xKeyframes = xKeyframes;
		this.yKeyframes = yKeyframes;
		this.zKeyframes = zKeyframes;
	}

	public KeyframeStack() {
		this(new ObjectArrayList<>(), new ObjectArrayList<>(), new ObjectArrayList<>());
	}

	public List<Keyframe> xKeyframes() { return this.xKeyframes; }
	public List<Keyframe> yKeyframes() { return this.yKeyframes; }
	public List<Keyframe> zKeyframes() { return this.zKeyframes; }

	public List<Keyframe> getXKeyframes() { return this.xKeyframes; }
	public List<Keyframe> getYKeyframes() { return this.yKeyframes; }
	public List<Keyframe> getZKeyframes() { return this.zKeyframes; }

	public static KeyframeStack from(KeyframeStack otherStack) {
		return new KeyframeStack(otherStack.xKeyframes, otherStack.yKeyframes, otherStack.zKeyframes);
	}

	public float getLastKeyframeTime() {
		return Math.max(getLastXAxisKeyframeTime(), Math.max(getLastYAxisKeyframeTime(), getLastZAxisKeyframeTime()));
	}

	public float getLastXAxisKeyframeTime() {
		return Keyframe.getLastKeyframeTime(xKeyframes);
	}

	public float getLastYAxisKeyframeTime() {
		return Keyframe.getLastKeyframeTime(yKeyframes);
	}

	public float getLastZAxisKeyframeTime() {
		return Keyframe.getLastKeyframeTime(zKeyframes);
	}

	public boolean hasKeyframes() {
		return !xKeyframes.isEmpty() || !yKeyframes.isEmpty() || !zKeyframes.isEmpty();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof KeyframeStack)) return false;
		KeyframeStack that = (KeyframeStack) o;
		return Objects.equals(xKeyframes, that.xKeyframes) && Objects.equals(yKeyframes, that.yKeyframes) && Objects.equals(zKeyframes, that.zKeyframes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(xKeyframes, yKeyframes, zKeyframes);
	}

	@Override
	public String toString() {
		return "KeyframeStack{" +
				"xKeyframes=" + xKeyframes +
				", yKeyframes=" + yKeyframes +
				", zKeyframes=" + zKeyframes +
				'}';
	}
}
