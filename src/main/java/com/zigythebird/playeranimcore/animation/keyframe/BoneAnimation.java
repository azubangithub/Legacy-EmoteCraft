/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranimcore.animation.keyframe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A class of a deserialized animation for a given bone
 * <p>
 * Responsible for holding the various {@link Keyframe Keyframes} for the bone's animation transformations
 */
public class BoneAnimation {
	private final KeyframeStack rotationKeyFrames;
	private final KeyframeStack positionKeyFrames;
	private final KeyframeStack scaleKeyFrames;
	private final List<Keyframe> bendKeyFrames;

	public BoneAnimation(KeyframeStack rotationKeyFrames,
						KeyframeStack positionKeyFrames,
						KeyframeStack scaleKeyFrames,
						List<Keyframe> bendKeyFrames) {
		this.rotationKeyFrames = rotationKeyFrames;
		this.positionKeyFrames = positionKeyFrames;
		this.scaleKeyFrames = scaleKeyFrames;
		this.bendKeyFrames = bendKeyFrames;
	}

	public BoneAnimation() {
		this(new KeyframeStack(), new KeyframeStack(), new KeyframeStack(), new ArrayList<>());
	}

	public KeyframeStack rotationKeyFrames() { return this.rotationKeyFrames; }
	public KeyframeStack positionKeyFrames() { return this.positionKeyFrames; }
	public KeyframeStack scaleKeyFrames() { return this.scaleKeyFrames; }
	public List<Keyframe> bendKeyFrames() { return this.bendKeyFrames; }

	public KeyframeStack getRotationKeyFrames() { return this.rotationKeyFrames; }
	public KeyframeStack getPositionKeyFrames() { return this.positionKeyFrames; }
	public KeyframeStack getScaleKeyFrames() { return this.scaleKeyFrames; }
	public List<Keyframe> getBendKeyFrames() { return this.bendKeyFrames; }

	public boolean hasKeyframes() {
		return rotationKeyFrames.hasKeyframes() || positionKeyFrames.hasKeyframes() ||
				scaleKeyFrames.hasKeyframes() || !bendKeyFrames.isEmpty();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BoneAnimation)) return false;
		BoneAnimation that = (BoneAnimation) o;
		return Objects.equals(scaleKeyFrames, that.scaleKeyFrames) &&
		       Objects.equals(bendKeyFrames, that.bendKeyFrames) &&
		       Objects.equals(rotationKeyFrames, that.rotationKeyFrames) &&
		       Objects.equals(positionKeyFrames, that.positionKeyFrames);
	}

	@Override
	public int hashCode() {
		return Objects.hash(rotationKeyFrames, positionKeyFrames, scaleKeyFrames, bendKeyFrames);
	}

	@Override
	public String toString() {
		return "BoneAnimation{" +
				"rotationKeyFrames=" + rotationKeyFrames +
				", positionKeyFrames=" + positionKeyFrames +
				", scaleKeyFrames=" + scaleKeyFrames +
				", bendKeyFrames=" + bendKeyFrames +
				'}';
	}
}
