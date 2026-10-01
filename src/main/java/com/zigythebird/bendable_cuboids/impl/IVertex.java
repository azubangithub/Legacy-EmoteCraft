package com.zigythebird.bendable_cuboids.impl;

import org.joml.Vector3f;

/**
 * Interface for custom vertices
 */
public interface IVertex {

    Vector3f getPos();

    float getU();

    float getV();

    IVertex remap(float u, float v);
}
