package io.github.kosmx.emotes.mixin;

import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase {

    public EntityPlayerMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void emotecraft$onPlayerUpdate(CallbackInfo ci) {
        if (this.world.isRemote && this instanceof IPlayerEntity) {
            ((IPlayerEntity) this).emotecraft$tickClient();
        }
    }
}
