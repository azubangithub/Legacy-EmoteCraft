package com.zigythebird.bendable_cuboids.impl.compatibility;

import com.zigythebird.bendable_cuboids.api.BendableCube;
import com.zigythebird.bendable_cuboids.api.BendableModelPart;
import com.zigythebird.bendable_cuboids.impl.BendUtil;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.EnumFacing;

public class PlayerBendHelper {
    public static void resetBend(ModelRenderer modelRenderer) {
        PlayerBendHelper.bend(modelRenderer, 0);
    }

    public static void bend(ModelRenderer modelRenderer, float rotation) {
        if (modelRenderer instanceof BendableModelPart) {
            BendableCube cube = ((BendableModelPart) modelRenderer).bc$getCuboid(0);
            if (cube != null) {
                cube.applyBend(rotation);
            }
        }
    }

    public static void initBend(ModelRenderer modelRenderer, EnumFacing direction) {
        if (modelRenderer instanceof BendableModelPart) {
            BendableCube cube = ((BendableModelPart) modelRenderer).bc$getCuboid(0);
            if (cube != null) cube.rebuild(direction);
        }
    }

    public static void initBend(ModelRenderer modelRenderer, EnumFacing direction, int pivot) {
        if (modelRenderer instanceof BendableModelPart) {
            BendableCube cube = ((BendableModelPart) modelRenderer).bc$getCuboid(0);
            if (cube != null) cube.rebuild(direction, pivot);
        }
    }

    public static void applyTorsoBendToGl(float bend) {
        BendUtil.applyBendToGl(0, 6.0F, 0, bend);
    }
}
