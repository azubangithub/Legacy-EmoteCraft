package com.zigythebird.bendable_cuboids.api;

import com.zigythebird.bendable_cuboids.impl.Plane;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public interface BendableCube extends Bendable {
    /**
     * Apply bend on this cuboid
     * Values are in radians
     * @param bendValue bend value (Same as rotX)
     * @return Transformation matrix for transforming children
     */
    Matrix4f applyBend(float bendValue);

    default Matrix4f applyBendDegrees(float bendValue) {
        return applyBend((float) Math.toRadians(bendValue));
    }

    @Nullable
    EnumFacing getBendDirection();
    int getBendPivot();

    float getBendX();
    float getBendY();
    float getBendZ();

    Plane getBasePlane();
    Plane getOtherPlane();

    /**
     * Distance between the two opposite surfaces of the cuboid.
     * @return the size of the cube
     */
    float bendHeight();

    default boolean isBendInverted() {
        EnumFacing direction = getBendDirection();
        if (direction == null) return false;
        return direction.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
    }

    default void rebuild(@NotNull EnumFacing direction) {
        rebuild(direction, -1);
    }

    void rebuild(EnumFacing direction, int pivot);
}
