package io.github.kosmx.emotes.compat.lfgm;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.physics.BreastPhysics;
import com.zigythebird.bendable_cuboids.api.BendableCube;
import com.zigythebird.bendable_cuboids.api.BendableModelPart;
import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Calculates breast physics forces and impulses from player model animations during emote playback.
 * Does not replace or disable player locomotion or other physics sources; adds model motion deltas to LFGM's physics.
 */
public class LFGMPhysicsHandler {

    private static final Map<BreastPhysics, BreastTracker> TRACKERS = new WeakHashMap<>();

    private static class BreastTracker {
        boolean hasPrev = false;
        UUID lastEmoteId = null;

        // Previous bone poses (translations in blocks, rotations in radians)
        float prevBodyX, prevBodyY, prevBodyZ;
        float prevBodyRotX, prevBodyRotY, prevBodyRotZ;

        float prevTorsoX, prevTorsoY, prevTorsoZ;
        float prevTorsoRotX, prevTorsoRotY, prevTorsoRotZ;
        float prevTorsoBend;

        // Previous velocities for acceleration
        float prevVelY, prevVelX, prevVelRot;

        void reset() {
            hasPrev = false;
            lastEmoteId = null;
            prevVelY = 0;
            prevVelX = 0;
            prevVelRot = 0;
        }

        void init(PlayerAnimBone body, PlayerAnimBone torso, UUID emoteId) {
            hasPrev = true;
            lastEmoteId = emoteId;

            prevBodyX = body.getPosX() / 16.0f;
            prevBodyY = body.getPosY() / 16.0f;
            prevBodyZ = body.getPosZ() / 16.0f;
            prevBodyRotX = body.getRotX();
            prevBodyRotY = body.getRotY();
            prevBodyRotZ = body.getRotZ();

            prevTorsoX = torso.getPosX() / 16.0f;
            prevTorsoY = torso.getPosY() / 16.0f;
            prevTorsoZ = torso.getPosZ() / 16.0f;
            prevTorsoRotX = torso.getRotX();
            prevTorsoRotY = torso.getRotY();
            prevTorsoRotZ = torso.getRotZ();
            prevTorsoBend = torso.getBend();

            prevVelY = 0;
            prevVelX = 0;
            prevVelRot = 0;
        }

        void updatePose(PlayerAnimBone body, PlayerAnimBone torso, UUID emoteId) {
            hasPrev = true;
            lastEmoteId = emoteId;

            prevBodyX = body.getPosX() / 16.0f;
            prevBodyY = body.getPosY() / 16.0f;
            prevBodyZ = body.getPosZ() / 16.0f;
            prevBodyRotX = body.getRotX();
            prevBodyRotY = body.getRotY();
            prevBodyRotZ = body.getRotZ();

            prevTorsoX = torso.getPosX() / 16.0f;
            prevTorsoY = torso.getPosY() / 16.0f;
            prevTorsoZ = torso.getPosZ() / 16.0f;
            prevTorsoRotX = torso.getRotX();
            prevTorsoRotY = torso.getRotY();
            prevTorsoRotZ = torso.getRotZ();
            prevTorsoBend = torso.getBend();
        }
    }

    public static LFGMPhysicsResult computeEmotePhysics(
            BreastPhysics breastPhysics,
            EntityConfig entityConfig,
            EntityLivingBase entity,
            IGenderArmor armor) {

        if (entity == null || !entity.world.isRemote || !(entity instanceof EntityPlayer)) {
            return null;
        }

        if (!(entity instanceof IPlayerEntity) || !(entity instanceof IAnimatedPlayer)) {
            BreastTracker tracker = TRACKERS.get(breastPhysics);
            if (tracker != null) tracker.reset();
            return null;
        }

        IPlayerEntity playerEntity = (IPlayerEntity) entity;
        if (!playerEntity.isPlayingEmote()) {
            BreastTracker tracker = TRACKERS.get(breastPhysics);
            if (tracker != null) tracker.reset();
            return null;
        }

        IAnimatedPlayer animatedPlayer = (IAnimatedPlayer) entity;
        PlayerAnimManager manager = animatedPlayer.playerAnimLib$getAnimManager();
        if (manager == null || !manager.isActive()) {
            BreastTracker tracker = TRACKERS.get(breastPhysics);
            if (tracker != null) tracker.reset();
            return null;
        }

        EmotePlayer emotePlayer = playerEntity.emotecraft$getEmote();
        Animation emoteData = emotePlayer != null ? emotePlayer.getData() : null;
        UUID emoteId = emoteData != null ? emoteData.uuid() : null;

        BreastTracker tracker = TRACKERS.computeIfAbsent(breastPhysics, k -> new BreastTracker());

        // Sample current bone transforms (body root and torso)
        PlayerAnimBone bodyBone = manager.get3DTransform(new PlayerAnimBone("body"));
        PlayerAnimBone torsoBone = manager.get3DTransform(new PlayerAnimBone("torso"));

        if (!tracker.hasPrev || (emoteId != null && !emoteId.equals(tracker.lastEmoteId))) {
            // New emote started: initialize baseline without sudden jerk
            tracker.init(bodyBone, torsoBone, emoteId);
            return null;
        }

        boolean isLeft = (entityConfig.getLeftBreastPhysics() == breastPhysics);
        boolean isUniboob = entityConfig.getBreasts().isUniboob();

        // Breast anchor position relative to torso waist pivot in blocks (1 block = 16 model pixels)
        float xBreast = isUniboob ? 0.0f : (isLeft ? -2.0f : 2.0f) / 16.0f;
        float yBreast = 4.0f / 16.0f; // upper chest height above pivot
        float zBreast = 2.0f / 16.0f; // forward from torso pivot axis

        // Linear translations in blocks
        float curBodyX = bodyBone.getPosX() / 16.0f;
        float curBodyY = bodyBone.getPosY() / 16.0f;
        float curBodyZ = bodyBone.getPosZ() / 16.0f;

        float curTorsoX = torsoBone.getPosX() / 16.0f;
        float curTorsoY = torsoBone.getPosY() / 16.0f;
        float curTorsoZ = torsoBone.getPosZ() / 16.0f;

        float dLinearX = (curTorsoX - tracker.prevTorsoX) + (curBodyX - tracker.prevBodyX);
        float dLinearY = (curTorsoY - tracker.prevTorsoY) + (curBodyY - tracker.prevBodyY);
        float dLinearZ = (curTorsoZ - tracker.prevTorsoZ) + (curBodyZ - tracker.prevBodyZ);

        // Rotations in radians
        float dRotX = (torsoBone.getRotX() - tracker.prevTorsoRotX) + (bodyBone.getRotX() - tracker.prevBodyRotX);
        float dRotY = (torsoBone.getRotY() - tracker.prevTorsoRotY) + (bodyBone.getRotY() - tracker.prevBodyRotY);
        float dRotZ = (torsoBone.getRotZ() - tracker.prevTorsoRotZ) + (bodyBone.getRotZ() - tracker.prevBodyRotZ);
        float dBend = torsoBone.getBend() - tracker.prevTorsoBend;

        float totalPitchDelta = dRotX + dBend;
        float totalYawDelta = dRotY;
        float totalRollDelta = dRotZ;

        // Effective motion of the breast anchor in 3D:
        // Y displacement: linear Y + roll contribution (opposite for left/right) - pitch contribution
        float effectiveMotionY = dLinearY + (totalRollDelta * xBreast - totalPitchDelta * zBreast);

        // X displacement: linear X + yaw contribution - roll contribution
        float effectiveMotionX = dLinearX + (totalYawDelta * zBreast - totalRollDelta * yBreast);

        // Acceleration (rate of change of velocity)
        float accelY = effectiveMotionY - tracker.prevVelY;
        float accelX = effectiveMotionX - tracker.prevVelX;
        float accelRot = totalYawDelta - tracker.prevVelRot;

        tracker.prevVelY = effectiveMotionY;
        tracker.prevVelX = effectiveMotionX;
        tracker.prevVelRot = totalYawDelta;

        tracker.updatePose(bodyBone, torsoBone, emoteId);

        // Respect LFGM settings (gender, bust size, bounce multiplier, armor resistance)
        float targetBreastSize = entityConfig.getBustSize();
        if (!entityConfig.getGender().canHaveBreasts() || targetBreastSize <= 0.01f) {
            return null;
        }

        if (!entityConfig.getArmorPhysicsOverride() && armor.coversBreasts()) {
            float tightness = MathHelper.clamp(armor.tightness(), 0, 1);
            targetBreastSize *= 1.0f - 0.15f * tightness;
        }

        float breastWeight = entityConfig.getBustSize() * 1.25f;
        float bounceIntensity = (targetBreastSize * 3.0f) * Math.round((entityConfig.getBounceMultiplier() * 3.0f) * 100.0f) / 100.0f;
        if (!entityConfig.getArmorPhysicsOverride() && armor.coversBreasts()) {
            float resistance = MathHelper.clamp(armor.physicsResistance(), 0, 1);
            bounceIntensity *= 1.0f - resistance;
        }

        if (bounceIntensity <= 0.001f) {
            return null;
        }

        // Motion forces combining velocity (inertia) and acceleration (kick)
        float motionForceY = effectiveMotionY * 1.2f + accelY * 0.8f;
        float deltaTargetY = motionForceY * bounceIntensity;

        float motionForceX = effectiveMotionX * 1.2f + accelX * 0.8f;
        float deltaTargetX = -motionForceX * bounceIntensity * 0.6f;

        // Yaw sway (convert radians to degrees to match LFGM's degree-based yaw offset logic)
        float yawDeltaDeg = (float) Math.toDegrees(totalYawDelta);
        float rotAccelDeg = (float) Math.toDegrees(accelRot);
        float rotForce = yawDeltaDeg * 1.0f + rotAccelDeg * 0.5f;
        float deltaTargetRot = -(rotForce / 15.0f) * bounceIntensity * 1.5f;

        // Secondary reaction of yaw twist to lateral bounce X
        deltaTargetX += -(rotForce / 15.0f) * bounceIntensity * 0.1f;

        // Leaning sag from gravity when bent forward
        float currentPitch = torsoBone.getRotX() + torsoBone.getBend() + bodyBone.getRotX();
        if (currentPitch > 0.05f) {
            float sag = (float) Math.sin(Math.min(currentPitch, Math.PI / 2.0)) * breastWeight * 0.35f;
            deltaTargetY += sag;
        }

        // Subtly vary left vs right for organic, non-uniform motion if not uniboob
        if (!isUniboob) {
            float asymmetry = isLeft ? 1.05f : 0.95f;
            deltaTargetY *= asymmetry;
        }

        // Instantaneous impulse for snappier reaction on sudden changes
        float impulseY = accelY * bounceIntensity * 0.4f;
        float impulseX = -accelX * bounceIntensity * 0.25f;
        float impulseRot = -(rotAccelDeg / 15.0f) * bounceIntensity * 0.3f;

        // Clamp to prevent any extreme keyframe spikes from destabilizing the spring
        deltaTargetY = MathHelper.clamp(deltaTargetY, -2.5f, 2.5f);
        deltaTargetX = MathHelper.clamp(deltaTargetX, -2.0f, 2.0f);
        deltaTargetRot = MathHelper.clamp(deltaTargetRot, -25.0f, 25.0f);

        impulseY = MathHelper.clamp(impulseY, -0.8f, 0.8f);
        impulseX = MathHelper.clamp(impulseX, -0.6f, 0.6f);
        impulseRot = MathHelper.clamp(impulseRot, -8.0f, 8.0f);

        return new LFGMPhysicsResult(deltaTargetY, deltaTargetX, deltaTargetRot, impulseY, impulseX, impulseRot);
    }

    /**
     * Applies the upper-torso bend transformation to OpenGL so that breasts follow the bent upper torso
     * rather than remaining rigidly attached to the lower unbent waist.
     */
    public static void applyTorsoBendTransform(ModelRenderer bipedBody, EntityLivingBase entity, float scale) {
        float bend = 0.0f;
        float bendY = 6.0f * scale;

        if (bipedBody instanceof BendableModelPart) {
            BendableCube cube = ((BendableModelPart) bipedBody).bc$getCuboid(0);
            if (cube != null) {
                bend = cube.getBend();
                if (cube.getBendY() > 0) {
                    bendY = cube.getBendY() * scale;
                }
            }
        }

        if (Math.abs(bend) < 0.0001f && entity instanceof IAnimatedPlayer) {
            PlayerAnimManager manager = ((IAnimatedPlayer) entity).playerAnimLib$getAnimManager();
            if (manager != null && manager.isActive()) {
                PlayerAnimBone torso = manager.get3DTransform(new PlayerAnimBone("torso"));
                if (torso != null) {
                    bend = torso.getBend();
                }
            }
        }

        if (Math.abs(bend) < 0.0001f) {
            return;
        }

        // Bend pivot is at y = 6.0 model units (halfway down the 12-unit torso)
        GlStateManager.translate(0.0f, bendY, 0.0f);
        GlStateManager.rotate(bend * (180.0f / (float) Math.PI), 1.0f, 0.0f, 0.0f);
        GlStateManager.translate(0.0f, -bendY, 0.0f);
    }
}
