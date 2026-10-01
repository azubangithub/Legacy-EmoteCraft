package io.github.kosmx.emotes.main.mixinFunctions;

import com.zigythebird.playeranim.util.ClientUtil;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;

import java.util.UUID;

public interface IPlayerEntity {
    default void initEmotePerspective() {
        if (isMainPlayer() && PlatformTools.getConfig().enablePerspective.get() && PlatformTools.getPerspective() == 0) {
            emotecraft$getEmote().perspective = true;
            PlatformTools.setPerspective(PlatformTools.getConfig().getCameraType());
        }
    }

    default void emotecraft$playEmote(Animation emote, float tick, boolean isForced) {
        throw new UnsupportedOperationException();
    }

    default EmotePlayer emotecraft$getEmote() {
        throw new UnsupportedOperationException();
    }

    default boolean isPlayingEmote() {
        return EmotePlayer.isRunningEmote(this.emotecraft$getEmote());
    }

    default boolean isMainPlayer() {
        return ClientUtil.getClientPlayer() == this;
    }

    default void stopEmote() {
        emotecraft$getEmote().stop();
    }

    default void stopEmote(UUID emoteID) {
        Animation animation = emotecraft$getEmote().getData();
        if (animation != null && animation.uuid().equals(emoteID)) {
            stopEmote();
        }
    }

    default boolean emotecraft$isForcedEmote() {
        throw new UnsupportedOperationException();
    }

    default void emotecraft$playerEntersInvalidPose() {
        if (!isPlayingEmote() || emotecraft$isForcedEmote()) {
            return;
        }

        if (PlatformTools.getConfig().checkPose.get()) {
            ClientEmotePlay.clientStopLocalEmote(emotecraft$getEmote().getData());
        }
    }

    default void emotecraft$playRawSound(ISound instance) {
        Minecraft.getMinecraft().getSoundHandler().playSound(instance);
    }

    default void emotecraft$tickClient() {}
}
