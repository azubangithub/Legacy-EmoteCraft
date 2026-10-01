package io.github.kosmx.emotes.mixin;

import com.mojang.authlib.GameProfile;
import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.animation.PlayerAnimationProcessor;
import com.zigythebird.playeranim.util.ClientUtil;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends EntityPlayer implements IAnimatedPlayer, IPlayerEntity {

    @Unique
    private int emotecraft$age = 0;

    @Unique
    private final PlayerAnimManager emotecraft$animManager = new PlayerAnimManager((AbstractClientPlayer) (Object) this);

    @Unique
    private final AnimationProcessor emotecraft$animProcessor = new PlayerAnimationProcessor((AbstractClientPlayer) (Object) this);

    @Unique
    private final EmotePlayer emotecraft$container = new EmotePlayer((AbstractClientPlayer) (Object) this);

    @Unique
    private boolean emotecraft$isForced = false;

    public AbstractClientPlayerMixin(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void emotecraft$init(World worldIn, GameProfile playerProfile, CallbackInfo ci) {
        this.emotecraft$animManager.addAnimLayer(1000, this.emotecraft$container);
    }

    @Override
    public PlayerAnimManager playerAnimLib$getAnimManager() {
        return this.emotecraft$animManager;
    }

    @Override
    public AnimationProcessor playerAnimLib$getAnimProcessor() {
        return this.emotecraft$animProcessor;
    }

    @Override
    public void emotecraft$playEmote(Animation emote, float tick, boolean isForced) {
        stopEmote();
        this.emotecraft$container.triggerAnimation(emote, tick);
        this.initEmotePerspective();
        if (this.isMainPlayer()) {
            this.emotecraft$isForced = isForced;
        }
    }

    @Override
    public EmotePlayer emotecraft$getEmote() {
        return this.emotecraft$container;
    }

    @Override
    public void emotecraft$tickClient() {
        this.emotecraft$animProcessor.handleAnimations(0, true);

        if (this.emotecraft$age <= 1) {
            if (this.emotecraft$age++ == 1) {
                Pair<Animation, Float> p = ClientEmotePlay.getEmoteForUUID(getUniqueID());
                if (p != null) {
                    ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(p.left(), p.right(), getUniqueID());
                    this.emotecraft$playEmote(p.left(), p.right(), false);
                }
                if (!this.isMainPlayer() && ClientUtil.getClientPlayer() != null) {
                    IPlayerEntity clientPlayer = (IPlayerEntity) ClientUtil.getClientPlayer();
                    if (clientPlayer.isPlayingEmote()) {
                        ClientEmotePlay.clientRepeatLocalEmote(clientPlayer.emotecraft$getEmote().getData(), clientPlayer.emotecraft$getEmote().getAnimationTicks(), this.getUniqueID());
                    }
                }
            }
        }

        if (isPlayingEmote() && isMainPlayer()) {
            if (emotecraft$getEmote().perspective && PlatformTools.getPerspective() != PlatformTools.getConfig().getCameraType()) {
                emotecraft$getEmote().perspective = false;
            }

            // Check if player enters invalid pose (sneaking, sleeping, flying, etc.)
            // Matches modern PlayerMixin.updatePlayerPose behavior
            if (this.isSneaking() || net.minecraft.client.Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown()
                    || this.isPlayerSleeping() || this.isElytraFlying() || this.isDead) {
                this.emotecraft$playerEntersInvalidPose();
            }

            if (!EmoteHolder.canRunEmote((AbstractClientPlayer) (Object) this)) {
                ClientEmotePlay.clientStopLocalEmote(emotecraft$getEmote().getData());
            }
        }
    }

    @Override
    public boolean emotecraft$isForcedEmote() {
        return this.isPlayingEmote() && this.emotecraft$isForced;
    }
}
