package com.zigythebird.bendable_cuboids;

import com.zigythebird.bendable_cuboids.impl.*;
import net.minecraft.util.EnumFacing;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BendingTest {

    @Test
    public void testLimbBendingMath() {
        // Player arm: 4x12x4, from x=-3, y=-2, z=-2 to x=1, y=10, z=2
        float posX1 = -3, posY1 = -2, posZ1 = -2;
        float posX2 = 1, posY2 = 10, posZ2 = 2;
        float sizeX = 4, sizeY = 12, sizeZ = 4;

        BendableCuboidData data = new BendableCuboidData(40, 16, sizeX, sizeY, sizeZ, 0, 0, 0, false, 64, 64, null);

        Vector3f[] vertices = new Vector3f[8];
        vertices[0] = new Vector3f(posX1, posY1, posZ1);
        vertices[1] = new Vector3f(posX2, posY1, posZ1);
        vertices[2] = new Vector3f(posX2, posY2, posZ1);
        vertices[3] = new Vector3f(posX1, posY2, posZ1);
        vertices[4] = new Vector3f(posX1, posY1, posZ2);
        vertices[5] = new Vector3f(posX2, posY1, posZ2);
        vertices[6] = new Vector3f(posX2, posY2, posZ2);
        vertices[7] = new Vector3f(posX1, posY2, posZ2);

        List<Quad> quads = new ArrayList<>();
        Map<Vector3f, RememberingPos> posMap = new HashMap<>();

        Quad.createAndAddQuads(quads, posMap, new Vector3f[]{vertices[1], vertices[0], vertices[2]}, 44, 20, 48, 32, 64, 64, false);
        Assertions.assertFalse(quads.isEmpty(), "Subdivided quads should be generated");
        Assertions.assertTrue(quads.size() > 1, "Arm should be divided into multiple segments along Y axis");

        // Set up bending plane along UP direction (elbow at y=4)
        EnumFacing direction = EnumFacing.UP;
        Plane basePlane = new Plane(BendUtil.step(direction), vertices[6]);
        Plane otherPlane = new Plane(BendUtil.step(direction), vertices[0]);
        float bendHeight = 12.0f;
        float bendValue = (float) Math.toRadians(45.0); // 45 degree bend

        BendApplier applier = BendUtil.getBend(0, 4.0f, 0, basePlane, otherPlane, true, false, bendHeight, bendValue);
        Assertions.assertNotNull(applier);
        Assertions.assertNotNull(applier.matrix4f());

        // Test shoulder (top vertex, y = -2) -> should remain unbent / stationary
        RememberingPos topVertex = new RememberingPos(0, -2, 0);
        applier.applyTo(topVertex);
        Assertions.assertEquals(-2.0f, topVertex.getPos().y, 0.05f, "Top of arm should not move during bend");

        // Test wrist (bottom vertex, y = 10) -> should be displaced by bend
        RememberingPos bottomVertex = new RememberingPos(0, 10, 0);
        applier.applyTo(bottomVertex);
        Assertions.assertNotEquals(10.0f, bottomVertex.getPos().y, "Hand/wrist should be rotated by bend");
        Assertions.assertNotEquals(0.0f, bottomVertex.getPos().z, "Hand/wrist should move along Z when bent");
    }
}
