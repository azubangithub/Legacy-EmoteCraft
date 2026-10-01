package com.zigythebird.bendable_cuboids.mixin;

import com.zigythebird.bendable_cuboids.api.BendableCube;
import com.zigythebird.bendable_cuboids.impl.*;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Function;

@Mixin(ModelBox.class)
public abstract class ModelBoxMixin implements BendableCube {

    @Shadow
    @Final
    public float posX1;
    @Shadow
    @Final
    public float posY1;
    @Shadow
    @Final
    public float posZ1;
    @Shadow
    @Final
    public float posX2;
    @Shadow
    @Final
    public float posY2;
    @Shadow
    @Final
    public float posZ2;

    @Unique
    private BendableCuboidData bc$data;
    @Unique
    private final Vector3f[] bc$vertices = new Vector3f[8];
    @Unique
    @Nullable
    private Quad[] sides;
    @Unique
    @Nullable
    private RememberingPos[] positions;

    @Unique
    protected float bc$fixX;
    @Unique
    protected float bc$fixY;
    @Unique
    protected float bc$fixZ;
    @Unique
    protected Plane bc$basePlane;
    @Unique
    protected Plane bc$otherPlane;
    @Unique
    protected float bc$fullSize;

    @Unique
    protected EnumFacing bc$direction;
    @Unique
    protected int bc$pivot;
    @Unique
    protected float bc$bend = 0;

    @Inject(method = "<init>(Lnet/minecraft/client/model/ModelRenderer;IIFFFIIIFZ)V", at = @At("RETURN"))
    private void onInit(ModelRenderer renderer, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror, CallbackInfo ci) {
        this.bc$data = new BendableCuboidData(texU, texV, dx, dy, dz, delta, delta, delta, mirror, renderer.textureWidth, renderer.textureHeight, null);
    }

    @Override
    public void rebuild(@NotNull EnumFacing direction, int point) {
        if (this.sides == null || this.positions == null) bc$build();

        if (this.bc$direction == direction && this.bc$pivot == point) return;
        this.bc$direction = Objects.requireNonNull(direction);
        this.bc$pivot = point;

        direction = EnumFacing.UP;

        Vector3f pivot = new Vector3f(0, 0, 0);
        if (point >= 0) {
            float size = BendUtil.step(direction).mul(bc$data.sizeX(), bc$data.sizeY(), bc$data.sizeZ()).length();
            if (point <= size) {
                pivot = BendUtil.step(direction).mul(size - (point * 2));
                bc$vertices[6] = bc$vertices[6].sub(pivot);
            }
        }
        this.bc$basePlane = new Plane(BendUtil.step(direction), bc$vertices[6]);
        this.bc$otherPlane = new Plane(BendUtil.step(direction), bc$vertices[0]);

        this.bc$fullSize = -BendUtil.step(direction).dot(bc$vertices[0]) + BendUtil.step(direction).dot(bc$vertices[6]);
        this.bc$fixX = (bc$data.sizeX() + posX1 + posX1 - pivot.x()) / 2;
        this.bc$fixY = (bc$data.sizeY() + posY1 + posY1 - pivot.y()) / 2;
        this.bc$fixZ = (bc$data.sizeZ() + posZ1 + posZ1 - pivot.z()) / 2;
    }

    @Unique
    private void bc$build() {
        List<Quad> planes = new ArrayList<>();
        Map<Vector3f, RememberingPos> posMap = new HashMap<>();
        float pminX = posX1 - bc$data.extraX();
        float pminY = posY1 - bc$data.extraY();
        float pminZ = posZ1 - bc$data.extraZ();
        float pmaxX = posX2 + bc$data.extraX();
        float pmaxY = posY2 + bc$data.extraY();
        float pmaxZ = posZ2 + bc$data.extraZ();
        if (bc$data.mirror()) {
            float tmp = pminX;
            pminX = pmaxX;
            pmaxX = tmp;
        }

        this.bc$vertices[0] = new Vector3f(pminX, pminY, pminZ); //west south down
        this.bc$vertices[1] = new Vector3f(pmaxX, pminY, pminZ); //east south down
        this.bc$vertices[2] = new Vector3f(pmaxX, pmaxY, pminZ); //east south up
        this.bc$vertices[3] = new Vector3f(pminX, pmaxY, pminZ); //west south up
        this.bc$vertices[4] = new Vector3f(pminX, pminY, pmaxZ); //west north down
        this.bc$vertices[5] = new Vector3f(pmaxX, pminY, pmaxZ); //east north down
        this.bc$vertices[6] = new Vector3f(pmaxX, pmaxY, pmaxZ); //east north up
        this.bc$vertices[7] = new Vector3f(pminX, pmaxY, pmaxZ); //west north up

        float j = bc$data.u();
        float k = bc$data.u() + bc$data.sizeZ();
        float l = bc$data.u() + bc$data.sizeZ() + bc$data.sizeX();
        float m = bc$data.u() + bc$data.sizeZ() + bc$data.sizeX() + bc$data.sizeX();
        float n = bc$data.u() + bc$data.sizeZ() + bc$data.sizeX() + bc$data.sizeZ();
        float o = bc$data.u() + bc$data.sizeZ() + bc$data.sizeX() + bc$data.sizeZ() + bc$data.sizeX();
        float p = bc$data.v();
        float q = bc$data.v() + bc$data.sizeZ();
        float r = bc$data.v() + bc$data.sizeZ() + bc$data.sizeY();
        float textureWidth = bc$data.textureWidth();
        float textureHeight = bc$data.textureHeight();
        boolean mirror = bc$data.mirror();

        if (bc$data.visibleFaces().contains(EnumFacing.DOWN)) Quad.createAndAddQuads(planes, posMap, new Vector3f[]{bc$vertices[5], bc$vertices[4], bc$vertices[1]}, k, p, l, q, textureWidth, textureHeight, mirror);
        if (bc$data.visibleFaces().contains(EnumFacing.UP)) Quad.createAndAddQuads(planes, posMap, new Vector3f[]{bc$vertices[2], bc$vertices[3], bc$vertices[6]}, l, q, m, p, textureWidth, textureHeight, mirror);
        if (bc$data.visibleFaces().contains(EnumFacing.WEST)) Quad.createAndAddQuads(planes, posMap, new Vector3f[]{bc$vertices[0], bc$vertices[4], bc$vertices[3]}, j, q, k, r, textureWidth, textureHeight, mirror);
        if (bc$data.visibleFaces().contains(EnumFacing.NORTH)) Quad.createAndAddQuads(planes, posMap, new Vector3f[]{bc$vertices[1], bc$vertices[0], bc$vertices[2]}, k, q, l, r, textureWidth, textureHeight, mirror);
        if (bc$data.visibleFaces().contains(EnumFacing.EAST)) Quad.createAndAddQuads(planes, posMap, new Vector3f[]{bc$vertices[5], bc$vertices[1], bc$vertices[6]}, l, q, n, r, textureWidth, textureHeight, mirror);
        if (bc$data.visibleFaces().contains(EnumFacing.SOUTH)) Quad.createAndAddQuads(planes, posMap, new Vector3f[]{bc$vertices[4], bc$vertices[5], bc$vertices[7]}, n, q, o, r, textureWidth, textureHeight, mirror);

        this.sides = planes.toArray(new Quad[0]);
        this.positions = posMap.values().toArray(new RememberingPos[0]);
        bc$iteratePositions(Function.identity());
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(BufferBuilder renderer, float scale, CallbackInfo ci) {
        if (this.bc$bend != 0 && this.sides != null) {
            renderer.begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
            for (Quad quad : this.sides) {
                quad.render(renderer, scale);
            }
            Tessellator.getInstance().draw();
            ci.cancel();
        }
    }

    @Override
    public Matrix4f applyBend(float bendValue) {
        if (Math.abs(bendValue) < 0.0001f) {
            this.bc$bend = 0;
            if (this.positions != null) {
                for (RememberingPos pos : this.positions) {
                    pos.setPos(null);
                }
            }
            return new Matrix4f();
        }

        this.bc$bend = bendValue;
        BendApplier bendApplier = BendUtil.getBend(this, bendValue);
        bc$iteratePositions(bendApplier.consumer());
        return bendApplier.matrix4f();
    }

    @Unique
    private void bc$iteratePositions(Function<Vector3f, Vector3f> function) {
        if (this.positions != null) {
            for (RememberingPos pos : this.positions) {
                pos.setPos(function.apply(pos.getOriginalPos()));
            }
        }
    }

    @Override
    public float getBend() {
        return this.bc$bend;
    }

    @Override
    public EnumFacing getBendDirection() {
        return this.bc$direction;
    }

    @Override
    public int getBendPivot() {
        return this.bc$pivot;
    }

    @Override
    public float getBendX() {
        return this.bc$fixX;
    }

    @Override
    public float getBendY() {
        return this.bc$fixY;
    }

    @Override
    public float getBendZ() {
        return this.bc$fixZ;
    }

    @Override
    public Plane getBasePlane() {
        return this.bc$basePlane;
    }

    @Override
    public Plane getOtherPlane() {
        return this.bc$otherPlane;
    }

    @Override
    public float bendHeight() {
        return this.bc$fullSize;
    }
}
