/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranimcore.animation.keyframe;

import com.zigythebird.playeranimcore.easing.EasingType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import team.unnamed.mocha.parser.ast.Expression;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Animation keyframe data
 */
public class Keyframe {
	private final float length;
	private final List<Expression> startValue;
	private final List<Expression> endValue;
	private final EasingType easingType;
	private final List<List<Expression>> easingArgs;

	public Keyframe(float length, List<Expression> startValue, List<Expression> endValue, EasingType easingType, List<List<Expression>> easingArgs) {
		this.length = length;
		this.startValue = startValue;
		this.endValue = endValue;
		this.easingType = easingType;
		this.easingArgs = easingArgs;
	}

	public Keyframe(float length, List<Expression> startValue, List<Expression> endValue) {
		this(length, startValue, endValue, EasingType.LINEAR);
	}

	public Keyframe(float length, List<Expression> startValue, List<Expression> endValue, EasingType easingType) {
		this(length, startValue, endValue, easingType, new ObjectArrayList<>(0));
	}

	public Keyframe(float length) {
		this(length, Collections.emptyList(), Collections.emptyList());
	}

	public float length() { return this.length; }
	public List<Expression> startValue() { return this.startValue; }
	public List<Expression> endValue() { return this.endValue; }
	public EasingType easingType() { return this.easingType; }
	public List<List<Expression>> easingArgs() { return this.easingArgs; }

	public float getLength() { return this.length; }
	public List<Expression> getStartValue() { return this.startValue; }
	public List<Expression> getEndValue() { return this.endValue; }
	public EasingType getEasingType() { return this.easingType; }
	public List<List<Expression>> getEasingArgs() { return this.easingArgs; }

	public static float getLastKeyframeTime(List<Keyframe> list) {
		return (float) list.stream().mapToDouble(Keyframe::length).sum();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.length, this.startValue, this.endValue, this.easingType.id, this.easingArgs);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Keyframe)) return false;
		Keyframe keyframe = (Keyframe) o;
		return Float.compare(length, keyframe.length) == 0 &&
		       easingType.id == keyframe.easingType.id &&
		       Objects.equals(endValue, keyframe.endValue) &&
		       Objects.equals(startValue, keyframe.startValue) &&
		       Objects.equals(easingArgs, keyframe.easingArgs);
	}

	@Override
	public String toString() {
		return "Keyframe{" +
				"length=" + length +
				", startValue=" + startValue +
				", endValue=" + endValue +
				", easingType=" + easingType +
				", easingArgs=" + easingArgs +
				'}';
	}
}
