package com.zigythebird.playeranim.util;

import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;

public final class RenderUtil {
    public static void copyVanillaPart(ModelRenderer part, PlayerAnimBone bone, float defaultX, float defaultY, float defaultZ) {
        bone.setToInitialPose();
        bone.setPosX(part.rotationPointX - defaultX);
        bone.setPosY(-(part.rotationPointY - defaultY));
        bone.setPosZ(part.rotationPointZ - defaultZ);

        bone.setRotX(part.rotateAngleX);
        bone.setRotY(part.rotateAngleY);
        bone.setRotZ(part.rotateAngleZ);

        bone.setBend(0);
    }

    public static void translatePartToBone(ModelRenderer part, PlayerAnimBone bone, float defaultX, float defaultY, float defaultZ) {
        part.rotationPointX = bone.getPosX() + defaultX;
        part.rotationPointY = -bone.getPosY() + defaultY;
        part.rotationPointZ = bone.getPosZ() + defaultZ;

        part.rotateAngleX = bone.getRotX();
        part.rotateAngleY = bone.getRotY();
        part.rotateAngleZ = bone.getRotZ();
    }

    public static void rotateAroundBone(PlayerAnimBone bone) {
        if (bone.getRotZ() != 0.0F) {
            GlStateManager.rotate(bone.getRotZ() * (180.0F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
        }
        if (bone.getRotY() != 0.0F) {
            GlStateManager.rotate(bone.getRotY() * (180.0F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
        }
        if (bone.getRotX() != 0.0F) {
            GlStateManager.rotate(bone.getRotX() * (180.0F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
        }
    }
}
