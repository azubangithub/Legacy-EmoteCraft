package io.github.kosmx.emotes.compat.lfgm.mixin;

import com.wildfire.api.IGenderArmor;
import com.wildfire.main.entitydata.EntityConfig;
import com.wildfire.physics.BreastPhysics;
import io.github.kosmx.emotes.compat.lfgm.LFGMPhysicsHandler;
import io.github.kosmx.emotes.compat.lfgm.LFGMPhysicsResult;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BreastPhysics.class, remap = false)
public abstract class BreastPhysicsMixin {

    @Shadow private EntityConfig entityConfig;
    @Shadow private float targetBounceY;
    @Shadow private float targetBounceX;
    @Shadow private float targetRotVel;
    @Shadow private float velocity;
    @Shadow private float velocityX;
    @Shadow private float rotVelocity;

    @Inject(
        method = "update",
        at = @At(value = "INVOKE", target = "Lcom/wildfire/main/entitydata/EntityConfig;getFloppiness()F")
    )
    private void emotecraft$applyEmoteModelPhysics(EntityLivingBase entity, IGenderArmor armor, CallbackInfo ci) {
        LFGMPhysicsResult result = LFGMPhysicsHandler.computeEmotePhysics(
            (BreastPhysics) (Object) this,
            this.entityConfig,
            entity,
            armor
        );
        if (result != null) {
            this.targetBounceY += result.deltaTargetY;
            this.targetBounceX += result.deltaTargetX;
            this.targetRotVel += result.deltaTargetRot;
            this.velocity += result.impulseY;
            this.velocityX += result.impulseX;
            this.rotVelocity += result.impulseRot;
        }
    }
}
