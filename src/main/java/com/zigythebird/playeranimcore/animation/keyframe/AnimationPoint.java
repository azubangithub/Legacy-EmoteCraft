/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranimcore.animation.keyframe;

import com.zigythebird.playeranimcore.easing.EasingType;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.parser.ast.Expression;

import java.util.List;
import java.util.Objects;

/**
 * Animation state class that holds the state of an animation at a given point
 */
public class AnimationPoint {
	private final EasingType easingType;
	@Nullable
	private final List<List<Expression>> easingArgs;
	private final float currentTick;
	private final float transitionLength;
	private final float animationStartValue;
	private final float animationEndValue;

	public AnimationPoint(EasingType easingType, @Nullable List<List<Expression>> easingArgs, float currentTick, float transitionLength, float animationStartValue, float animationEndValue) {
		this.easingType = easingType;
		this.easingArgs = easingArgs;
		this.currentTick = currentTick;
		this.transitionLength = transitionLength;
		this.animationStartValue = animationStartValue;
		this.animationEndValue = animationEndValue;
	}

	public AnimationPoint(Keyframe keyframe, float currentTick, float transitionLength, float animationStartValue, float animationEndValue) {
		this(keyframe == null ? EasingType.LINEAR : keyframe.easingType(), keyframe == null ? null : keyframe.easingArgs(), currentTick, transitionLength, animationStartValue, animationEndValue);
	}

	public EasingType easingType() { return this.easingType; }
	@Nullable
	public List<List<Expression>> easingArgs() { return this.easingArgs; }
	public float currentTick() { return this.currentTick; }
	public float transitionLength() { return this.transitionLength; }
	public float animationStartValue() { return this.animationStartValue; }
	public float animationEndValue() { return this.animationEndValue; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AnimationPoint)) return false;
		AnimationPoint that = (AnimationPoint) o;
		return Float.compare(that.currentTick, currentTick) == 0 &&
		       Float.compare(that.transitionLength, transitionLength) == 0 &&
		       Float.compare(that.animationStartValue, animationStartValue) == 0 &&
		       Float.compare(that.animationEndValue, animationEndValue) == 0 &&
		       Objects.equals(easingType, that.easingType) &&
		       Objects.equals(easingArgs, that.easingArgs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(easingType, easingArgs, currentTick, transitionLength, animationStartValue, animationEndValue);
	}

	@Override
	public String toString() {
		return "Tick: " + this.currentTick +
				" | Transition Length: " + this.transitionLength +
				" | Start Value: " + this.animationStartValue +
				" | End Value: " + this.animationEndValue;
	}
}
