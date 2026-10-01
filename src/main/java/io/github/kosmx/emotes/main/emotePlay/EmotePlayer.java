package io.github.kosmx.emotes.main.emotePlay;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.animation.keyframe.event.CustomKeyFrameEvents;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.KeyFrameData;
import com.zigythebird.playeranimcore.enums.PlayState;
import io.github.kosmx.emotes.PlatformTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.text.ITextComponent;
import net.raphimc.noteblocklib.model.Song;

public class EmotePlayer extends PlayerAnimationController {
    private MinecraftNbsPlayer song;
    public boolean perspective = false;

    public EmotePlayer(AbstractClientPlayer player) {
        super(player, (controller, state, animSetter) -> PlayState.STOP);
    }

    @Override
    protected void setupNewAnimation() {
        super.setupNewAnimation();

        Animation emote = getData();

        if (this.song != null) this.song.stop();
        if (emote != null && emote.data().has("song")) {
            Song nbsSong = (Song) emote.data().getRaw("song");
            if (nbsSong != null) {
                this.song = new MinecraftNbsPlayer(getPlayer(), nbsSong);
            } else {
                this.song = null;
            }
        } else {
            this.song = null;
        }
    }

    @Override
    public void stop() {
        super.stop();
        stopTriggeredAnimation();
        this.animationQueue.clear();
        if (this.perspective && PlatformTools.getPerspective() == PlatformTools.getConfig().getCameraType()) {
            Minecraft.getMinecraft().gameSettings.thirdPersonView = 0;
            this.perspective = false;
        }
        if (this.song != null) this.song.stop();
    }

    public static boolean isRunningEmote(EmotePlayer emote) {
        return emote != null && emote.isActive();
    }

    public Animation getData() {
        AnimationProcessor.QueuedAnimation animation = getCurrentAnimation();
        if (animation == null) return null;
        return animation.animation();
    }

    @Override
    protected <T extends KeyFrameData> void handleCustomKeyframe(T[] keyframes, CustomKeyFrameEvents.CustomKeyFrameHandler<T> main, CustomKeyFrameEvents.CustomKeyFrameHandler<T> event, float animationTick, AnimationData animationData) {
        if (this.song != null && !this.song.isFirstSongPlayed() && isActive() && !this.song.isRunning()) {
            ITextComponent nowPlaying = this.song.getNowPlaying();
            if (nowPlaying != null && Minecraft.getMinecraft().ingameGUI != null) {
                Minecraft.getMinecraft().ingameGUI.setOverlayMessage(nowPlaying.getFormattedText(), false);
            }
            this.song.start();
        }

        super.handleCustomKeyframe(keyframes, main, event, animationTick, animationData);
    }
}
