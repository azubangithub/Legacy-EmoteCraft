package io.github.kosmx.emotes.mixin;

import net.minecraft.client.resources.FolderResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.Locale;

@Mixin(FolderResourcePack.class)
public abstract class FolderResourcePackMixin {

    @Inject(method = "validatePath", at = @At("RETURN"), cancellable = true)
    private static void emotecraft$caseInsensitiveWindows(File file, String path, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            try {
                String canonical = file.getCanonicalPath().replace('\\', '/');
                if (canonical.toLowerCase(Locale.ROOT).endsWith(path.toLowerCase(Locale.ROOT))) {
                    cir.setReturnValue(true);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
