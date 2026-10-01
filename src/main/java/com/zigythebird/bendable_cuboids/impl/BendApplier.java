package com.zigythebird.bendable_cuboids.impl;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.Function;

public class BendApplier {
    private final Matrix4f matrix4f;
    private final Function<Vector3f, Vector3f> consumer;

    public BendApplier(Matrix4f matrix4f, Function<Vector3f, Vector3f> consumer) {
        this.matrix4f = matrix4f;
        this.consumer = consumer;
    }

    public Matrix4f matrix4f() {
        return matrix4f;
    }

    public Function<Vector3f, Vector3f> consumer() {
        return consumer;
    }

    public void applyTo(RememberingPos pos) {
        pos.setPos(consumer.apply(pos.getOriginalPos()));
    }
}
