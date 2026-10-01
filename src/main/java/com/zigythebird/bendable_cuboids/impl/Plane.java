package com.zigythebird.bendable_cuboids.impl;

import org.joml.Vector3f;

/**
 * A plane in 3D space.
 * Forms a normal vector and a position/distance for calculation.
 */
public class Plane {
    public final Vector3f normal;
    private final float normDistance;

    public Plane(Vector3f normal, Vector3f position) {
        this.normal = new Vector3f(normal);
        this.normal.normalize();
        this.normDistance = -this.normal.dot(position);
    }

    public Plane(Vector3f normal, float normDistance) {
        this.normal = new Vector3f(normal);
        this.normDistance = normDistance;
    }

    public Plane scaled(float scalar) {
        return new Plane(new Vector3f(normal), normDistance * scalar);
    }

    /**
     * Returns the SIGNED distance between pos and this plane
     */
    public float distanceTo(Vector3f pos) {
        return normal.dot(pos) + normDistance;
    }

    /**
     * Returns the SIGNED distance between this plane and otherPlane. 0 if not parallel.
     */
    public float distanceTo(Plane otherPlane) {
        Vector3f tmp = new Vector3f(this.normal);
        tmp.cross(otherPlane.normal);
        if (tmp.dot(tmp) < 0.01f) {
            return this.normDistance + this.normal.dot(otherPlane.normal) * otherPlane.normDistance;
        } else {
            return 0;
        }
    }
}
