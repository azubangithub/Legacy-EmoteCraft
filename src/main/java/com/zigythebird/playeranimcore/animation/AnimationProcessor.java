package com.zigythebird.playeranimcore.animation;

import com.zigythebird.playeranimcore.animation.layered.AnimationStack;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.enums.AnimationStage;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.LinkedList;
import java.util.Queue;

@ApiStatus.Internal
public abstract class AnimationProcessor {
	/**
	 * Each AnimationProcessor must be bound to a player
	 */
	public AnimationProcessor() {}

	/**
	 * This method is called once per render frame for each player being rendered
	 * <p>
	 * It is an internal method for automated animation parsing.
	 */
	public abstract void handleAnimations(float partialTick, boolean fullTick);

	/**
	 * Build an animation queue for the given {@link RawAnimation}
	 *
	 * @param rawAnimation The raw animation to be compiled
	 * @return A queue of animations and loop types to play
	 */
	public Queue<QueuedAnimation> buildAnimationQueue(RawAnimation rawAnimation) {
		LinkedList<QueuedAnimation> animations = new LinkedList<>();
		for (RawAnimation.Stage stage : rawAnimation.getAnimationStages()) {
			Animation animation;
			if (stage.stage() == AnimationStage.WAIT) { // This is intentional. Do not change this or T̶s̶l̶a̶t̶ I will be unhappy!!!
				animation = Animation.generateWaitAnimation(stage.additionalTicks());
			} else {
				animation = stage.animation();
			}

			if (animation != null) animations.add(new QueuedAnimation(animation, stage.loopType()));
		}
		return animations;
	}

	/**
	 * Tick and apply transformations to the model based on the current state of the {@link AnimationController}
	 *
	 * @param playerAnimManager		The PlayerAnimManager instance being used for this animation processor
	 * @param state                 An {@link AnimationData} instance applied to this render frame
	 */
	public void tickAnimation(AnimationStack playerAnimManager, AnimationData state) {
		playerAnimManager.getLayers().removeIf(pair -> pair.right() == null || pair.right().canRemove());
		for (Pair<Integer, IAnimation> pair : playerAnimManager.getLayers()) {
			IAnimation animation = pair.right();

			if (animation.isActive())
				animation.setupAnim(state.copy());
		}
	}

	/**
	 * {@link Animation} and {@link Animation.LoopType} override pair,
	 * used to define a playable animation stage for a player
	 */
	public static class QueuedAnimation {
		private final Animation animation;
		private final Animation.LoopType loopType;

		public QueuedAnimation(Animation animation, Animation.LoopType loopType) {
			this.animation = animation;
			this.loopType = loopType;
		}

		public Animation animation() { return this.animation; }
		public Animation.LoopType loopType() { return this.loopType; }

		public Animation getAnimation() { return this.animation; }
		public Animation.LoopType getLoopType() { return this.loopType; }

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof QueuedAnimation)) return false;
			QueuedAnimation that = (QueuedAnimation) o;
			return java.util.Objects.equals(animation, that.animation) && loopType == that.loopType;
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(animation, loopType);
		}
	}
}
