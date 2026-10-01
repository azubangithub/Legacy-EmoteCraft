package com.zigythebird.playeranimcore.math;

import java.util.Objects;

public class Vec3f {
    private final float x;
    private final float y;
    private final float z;

    public static final Vec3f ZERO = new Vec3f(0f, 0f, 0f);
    public static final Vec3f ONE = new Vec3f(1f, 1f, 1f);

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float x() { return this.x; }
    public float y() { return this.y; }
    public float z() { return this.z; }

    public float getX() { return this.x; }
    public float getY() { return this.y; }
    public float getZ() { return this.z; }

    /**
     * Scale the vector
     *
     * @param scalar scalar
     * @return scaled vector
     */
    public Vec3f mul(float scalar) {
        return new Vec3f(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    /**
     * Add two vectors
     *
     * @param other other vector
     * @return sum vector
     */
    public Vec3f add(Vec3f other) {
        return new Vec3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vec3f)) return false;
        Vec3f vec = (Vec3f) o;
        return Float.compare(vec.x, x) == 0 && Float.compare(vec.y, y) == 0 && Float.compare(vec.z, z) == 0;
    }

    @Override
    public String toString() {
        return "Vec3f[" + this.x + "; " + this.y + "; " + this.z + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
