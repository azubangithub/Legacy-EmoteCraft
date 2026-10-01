/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranimcore.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.enums.AnimationStage;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import com.zigythebird.playeranimcore.math.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A compiled animation instance for use by the {@link AnimationController}
 * <p>
 * Modifications or extensions of a compiled Animation are not supported, and therefore an instance of <code>Animation</code> is considered final and immutable
 */
public class Animation implements Supplier<UUID> {
    private final ExtraAnimationData data;
    private final float length;
    private final LoopType loopType;
    private final Map<String, BoneAnimation> boneAnimations;
    private final Keyframes keyFrames;
    private final Map<String, Vec3f> bones;
    private final Map<String, String> parents;

    public Animation(ExtraAnimationData data, float length, LoopType loopType, Map<String, BoneAnimation> boneAnimations, Keyframes keyFrames, Map<String, Vec3f> bones, Map<String, String> parents) {
        this.data = data;
        this.length = length;
        this.loopType = loopType;
        this.boneAnimations = boneAnimations;
        this.keyFrames = keyFrames;
        this.bones = bones;
        this.parents = parents;
    }

    public ExtraAnimationData data() { return this.data; }
    public float length() { return this.length; }
    public LoopType loopType() { return this.loopType; }
    public Map<String, BoneAnimation> boneAnimations() { return this.boneAnimations; }
    public Keyframes keyFrames() { return this.keyFrames; }
    public Map<String, Vec3f> bones() { return this.bones; }
    public Map<String, String> parents() { return this.parents; }

    public ExtraAnimationData getData() { return this.data; }
    public float getLength() { return this.length; }
    public LoopType getLoopType() { return this.loopType; }
    public Map<String, BoneAnimation> getBoneAnimations() { return this.boneAnimations; }
    public Keyframes getKeyFrames() { return this.keyFrames; }
    public Map<String, Vec3f> getBones() { return this.bones; }
    public Map<String, String> getParents() { return this.parents; }

    public static class Keyframes {
        private final SoundKeyframeData[] sounds;
        private final ParticleKeyframeData[] particles;
        private final CustomInstructionKeyframeData[] customInstructions;

        public Keyframes(SoundKeyframeData[] sounds, ParticleKeyframeData[] particles, CustomInstructionKeyframeData[] customInstructions) {
            this.sounds = sounds;
            this.particles = particles;
            this.customInstructions = customInstructions;
        }

        public SoundKeyframeData[] sounds() { return this.sounds; }
        public ParticleKeyframeData[] particles() { return this.particles; }
        public CustomInstructionKeyframeData[] customInstructions() { return this.customInstructions; }

        public SoundKeyframeData[] getSounds() { return this.sounds; }
        public ParticleKeyframeData[] getParticles() { return this.particles; }
        public CustomInstructionKeyframeData[] getCustomInstructions() { return this.customInstructions; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Keyframes)) return false;
            Keyframes keyframes = (Keyframes) o;
            return Arrays.equals(sounds, keyframes.sounds) &&
                   Arrays.equals(particles, keyframes.particles) &&
                   Arrays.equals(customInstructions, keyframes.customInstructions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(Arrays.hashCode(sounds), Arrays.hashCode(particles), Arrays.hashCode(customInstructions));
        }

        @Override
        public String toString() {
            return "Keyframes{" +
                    "sounds=" + Arrays.toString(sounds) +
                    ", particles=" + Arrays.toString(particles) +
                    ", customInstructions=" + Arrays.toString(customInstructions) +
                    '}';
        }
    }

    static Animation generateWaitAnimation(float length) {
        return new Animation(new ExtraAnimationData(ExtraAnimationData.NAME_KEY, AnimationStage.WAIT.name()), length, LoopType.PLAY_ONCE,
                Collections.emptyMap(), UniversalAnimLoader.NO_KEYFRAMES, new HashMap<>(), new HashMap<>());
    }

    public boolean isPlayingAt(float tick) {
        return loopType.shouldPlayAgain(this) || tick < length() && tick > 0;
    }

    @Nullable
    public BoneAnimation getBone(String id) {
        return this.boneAnimations.get(id);
    }

    public Optional<BoneAnimation> getBoneOptional(String id) {
        return Optional.ofNullable(getBone(id));
    }

    /**
     * Loop type functional interface to define post-play handling for a given animation
     * <p>
     * Custom loop types are supported by extending this class and providing the extended class instance as the loop type for the animation
     */
    @FunctionalInterface
    public interface LoopType {
        Map<String, LoopType> LOOP_TYPES = new ConcurrentHashMap<>(4);

        LoopType DEFAULT = new LoopType() {
            @Override
            public boolean shouldPlayAgain(Animation currentAnimation) {
                return currentAnimation.loopType().shouldPlayAgain(currentAnimation);
            }

            @Override
            public float restartFromTick(Animation currentAnimation) {
                return currentAnimation.loopType().restartFromTick(currentAnimation);
            }
        };
        LoopType PLAY_ONCE = register("play_once", register("false", (currentAnimation) -> false));
        LoopType HOLD_ON_LAST_FRAME = register("hold_on_last_frame", (currentAnimation) -> true);
        LoopType LOOP = register("loop", register("true", (currentAnimation) -> true));

        /**
         * Override in a custom instance to dynamically decide whether an animation should repeat or stop
         *
         * @param currentAnimation The current animation that just played
         * @return Whether the animation should play again, or stop
         */
        boolean shouldPlayAgain(Animation currentAnimation);

        /**
         * Override in a custom instance to dynamically decide where an animation should start after looping.
         *
         * @param currentAnimation The current animation that just played
         * @return The tick the animation starts from after looping.
         */
        default float restartFromTick(Animation currentAnimation) {
            return 0;
        }

        static LoopType returnToTickLoop(float tick) {
            return new LoopType() {
                @Override
                public boolean shouldPlayAgain(Animation currentAnimation) {
                    return true;
                }

                @Override
                public float restartFromTick(Animation currentAnimation) {
                    return tick;
                }
            };
        }

        /**
         * Retrieve a LoopType instance based on a {@link JsonElement}
         * <p>
         * Returns either {@link LoopType#PLAY_ONCE} or {@link LoopType#LOOP} based on a boolean or string element type,
         * or any other registered loop type with a matching type string
         *
         * @param json The <code>loop</code> {@link JsonElement} to attempt to parse
         * @return A usable LoopType instance
         */
        static LoopType fromJson(JsonElement json) {
            if (json == null || !json.isJsonPrimitive())
                return PLAY_ONCE;

            JsonPrimitive primitive = json.getAsJsonPrimitive();

            if (primitive.isBoolean())
                return primitive.getAsBoolean() ? LOOP : PLAY_ONCE;

            if (primitive.isString())
                return fromString(primitive.getAsString());

            return PLAY_ONCE;
        }

        static LoopType fromString(String name) {
            return LOOP_TYPES.getOrDefault(name, PLAY_ONCE);
        }

        /**
         * Register a LoopType with Player Animation Library for handling loop functionality of animations
         * <p>
         * <b><u>MUST be called during mod construct</u></b>
         * <p>
         *
         * @param name The name of the loop type
         * @param loopType The loop type to register
         * @return The registered {@code LoopType}
         */
        static LoopType register(String name, LoopType loopType) {
            LOOP_TYPES.put(name, loopType);

            return loopType;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Animation)) return false;
        Animation animation = (Animation) o;
        return Float.compare(length, animation.length) == 0 && Objects.equals(keyFrames, animation.keyFrames) && Objects.equals(bones, animation.bones) && Objects.equals(parents, animation.parents) && Objects.equals(boneAnimations, animation.boneAnimations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(length, boneAnimations, keyFrames, bones, parents);
    }

    private UUID generateUuid() {
        return generateUuid(
                Float.floatToIntBits(length),
                boneAnimations.hashCode(),
                keyFrames.hashCode(),
                bones.hashCode(),
                parents.hashCode()
        );
    }

    private static UUID generateUuid(int... hashes) {
        long mostSigBits = 17L;
        long leastSigBits = 31L;
        for (int hash : hashes) {
            mostSigBits = 31L * mostSigBits + hash;
            leastSigBits = 37L * leastSigBits + hash;
        }
        return new UUID(mostSigBits, leastSigBits);
    }

    public UUID uuid() {
        if (!data().has(ExtraAnimationData.UUID_KEY)) {
            data().put(ExtraAnimationData.UUID_KEY, generateUuid());
        } else if (data().getRaw(ExtraAnimationData.UUID_KEY) instanceof String) {
            String str = (String) data().getRaw(ExtraAnimationData.UUID_KEY);
            data().put(ExtraAnimationData.UUID_KEY, UUID.fromString(str));
        }
        return data().<UUID>get(ExtraAnimationData.UUID_KEY).get();
    }

    @Override
    public UUID get() {
        return uuid();
    }
}
