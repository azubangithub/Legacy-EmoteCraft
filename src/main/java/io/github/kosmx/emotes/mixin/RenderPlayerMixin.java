package io.github.kosmx.emotes.mixin;

import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public abstract class RenderPlayerMixin {

    @Inject(method = "doRender", at = @At("HEAD"))
    private void emotecraft$capturePartialTicks(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (entity instanceof IAnimatedPlayer) {
            PlayerAnimManager manager = ((IAnimatedPlayer) entity).playerAnimLib$getAnimManager();
            if (manager != null) {
                manager.setTickDelta(partialTicks);
            }
        }
    }
}
