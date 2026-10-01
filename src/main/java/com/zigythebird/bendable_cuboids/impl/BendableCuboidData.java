package com.zigythebird.bendable_cuboids.impl;

import net.minecraft.util.EnumFacing;

import java.util.EnumSet;
import java.util.Set;

public class BendableCuboidData {
    private final int u;
    private final int v;
    private final float sizeX;
    private final float sizeY;
    private final float sizeZ;
    private final float extraX;
    private final float extraY;
    private final float extraZ;
    private final boolean mirror;
    private final float textureWidth;
    private final float textureHeight;
    private final Set<EnumFacing> visibleFaces;

    public BendableCuboidData(
            int u, int v,
            float sizeX, float sizeY, float sizeZ,
            float extraX, float extraY, float extraZ,
            boolean mirror,
            float textureWidth, float textureHeight,
            Set<EnumFacing> visibleFaces
    ) {
        this.u = u;
        this.v = v;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.extraX = extraX;
        this.extraY = extraY;
        this.extraZ = extraZ;
        this.mirror = mirror;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.visibleFaces = visibleFaces != null ? visibleFaces : EnumSet.allOf(EnumFacing.class);
    }

    public int u() { return u; }
    public int v() { return v; }
    public float sizeX() { return sizeX; }
    public float sizeY() { return sizeY; }
    public float sizeZ() { return sizeZ; }
    public float extraX() { return extraX; }
    public float extraY() { return extraY; }
    public float extraZ() { return extraZ; }
    public boolean mirror() { return mirror; }
    public float textureWidth() { return textureWidth; }
    public float textureHeight() { return textureHeight; }
    public Set<EnumFacing> visibleFaces() { return visibleFaces; }
}
