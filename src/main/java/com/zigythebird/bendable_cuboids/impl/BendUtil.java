package com.zigythebird.bendable_cuboids.impl;

import com.zigythebird.bendable_cuboids.api.BendableCube;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class BendUtil {
    private static final Vector3f Z_AXIS = new Vector3f(0, 0, 1);

    public static Vector3f step(EnumFacing facing) {
        if (facing == null) return new Vector3f(0, 0, 0);
        net.minecraft.util.math.Vec3i vec = facing.getDirectionVec();
        return new Vector3f(vec.getX(), vec.getY(), vec.getZ());
    }

    public static BendApplier getBend(BendableCube cuboid) {
        return getBend(cuboid, cuboid.getBend());
    }

    public static BendApplier getBend(BendableCube cuboid, float bendValue) {
        return getBend(cuboid.getBendX(), cuboid.getBendY(), cuboid.getBendZ(),
                cuboid.getBasePlane(), cuboid.getOtherPlane(), cuboid.isBendInverted(), false, cuboid.bendHeight(), bendValue);
    }

    /**
     * Applies the transformation to every position
     * @param bendValue bend value in radians
     */
    public static BendApplier getBend(float bendX, float bendY, float bendZ, Plane basePlane, Plane otherPlane,
                                      boolean isBendInverted, boolean mirrorBend, float bendHeight, float bendValue) {
        if (mirrorBend) bendValue *= -1;
        final float finalBend = bendValue;
        Matrix4f transformMatrix = applyBendToMatrix(new Matrix4f(), bendX, bendY, bendZ, bendValue);

        float halfSize = bendHeight / 2;

        return new BendApplier(transformMatrix, pos -> {
            float distFromBase = Math.abs(basePlane.distanceTo(pos));
            float distFromOther = Math.abs(otherPlane.distanceTo(pos));
            float s = (float) Math.tan(finalBend / 2) * pos.z;
            if (mirrorBend || !isBendInverted) {
                float temp = distFromBase;
                distFromBase = distFromOther;
                distFromOther = temp;
            }
            float v = halfSize - ((isBendInverted ? s < 0 : s >= 0) ? Math.min(Math.abs(s) / 2, 1) : Math.abs(s));
            if (distFromBase < distFromOther) {
                if (distFromBase + distFromOther <= bendHeight && distFromBase > v)
                    pos.y = bendY + s;
                Vector4f reposVector = new Vector4f(pos, 1f);
                reposVector.mul(transformMatrix);
                pos = new Vector3f(reposVector.x, reposVector.y, reposVector.z);
            } else if (distFromBase + distFromOther <= bendHeight && distFromOther > v) {
                pos.y = bendY - s;
            }
            return pos;
        });
    }

    public static BendApplier getBendLegacy(BendableCube cuboid, float bendValue) {
        return getBendLegacy(cuboid.getBendDirection(), cuboid.getBendX(), cuboid.getBendY(), cuboid.getBendZ(),
                cuboid.getBasePlane(), cuboid.getOtherPlane(), cuboid.isBendInverted(), false, cuboid.bendHeight(), bendValue);
    }

    /**
     * Legacy stretchy bend algorithm
     */
    public static BendApplier getBendLegacy(EnumFacing bendDirection, float bendX, float bendY, float bendZ, Plane basePlane, Plane otherPlane,
                                            boolean isBendInverted, boolean mirrorBend, float bendHeight, float bendValue) {
        if (mirrorBend) bendValue *= -1;
        final float finalBend = bendValue;
        Matrix4f transformMatrix = applyBendToMatrix(new Matrix4f(), bendX, bendY, bendZ, bendValue);

        Vector3f directionUnit = step(bendDirection);
        directionUnit.cross(Z_AXIS);
        Plane bendPlane = new Plane(directionUnit, new Vector3f(bendX, bendY, bendZ));
        float halfSize = bendHeight / 2;

        return new BendApplier(transformMatrix, pos -> {
            float distFromBend = isBendInverted ? -bendPlane.distanceTo(pos) : bendPlane.distanceTo(pos);
            float distFromBase = basePlane.distanceTo(pos);
            float distFromOther = otherPlane.distanceTo(pos);
            Vector3f x = step(bendDirection);
            if (mirrorBend) {
                float temp = distFromBase;
                distFromBase = distFromOther;
                distFromOther = temp;
                distFromBend *= -1;
            }
            double s = Math.tan(finalBend / 2) * distFromBend;
            boolean isInBendArea = Math.abs(distFromBase) + Math.abs(distFromOther) <= Math.abs(bendHeight);
            if (Math.abs(distFromBase) < Math.abs(distFromOther)) {
                if (isInBendArea) {
                    x.mul((float) (-distFromBase / halfSize * s));
                    pos.add(x);
                }
                Vector4f reposVector = new Vector4f(pos, 1f);
                reposVector.mul(transformMatrix);
                pos = new Vector3f(reposVector.x, reposVector.y, reposVector.z);
            } else if (isInBendArea) {
                x.mul((float) (-distFromOther / halfSize * s));
                pos.add(x);
            }
            return pos;
        });
    }

    public static Matrix4f applyBendToMatrix(Matrix4f transformMatrix, float bendX, float bendY, float bendZ, float bendValue) {
        transformMatrix.translate(bendX, bendY, bendZ);
        transformMatrix.rotateX(bendValue);
        transformMatrix.translate(-bendX, -bendY, -bendZ);
        return transformMatrix;
    }

    public static void applyBendToGl(float bendX, float bendY, float bendZ, float bendValue) {
        GlStateManager.translate(bendX, bendY, bendZ);
        GlStateManager.rotate(bendValue * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-bendX, -bendY, -bendZ);
    }
}
