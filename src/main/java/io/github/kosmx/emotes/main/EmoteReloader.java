package io.github.kosmx.emotes.main;

import com.google.common.base.Stopwatch;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;

import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Emote hot-reloader for dynamic reloading without restarting Minecraft.
 */
public class EmoteReloader {
    private static final DecimalFormat FORMAT = new DecimalFormat("#0.000");
    private static final AtomicBoolean IS_RELOADING = new AtomicBoolean(false);

    public static boolean isReloading() {
        return IS_RELOADING.get();
    }

    public static CompletableFuture<Void> reloadEmotes(boolean notify, Runnable onComplete) {
        if (!IS_RELOADING.compareAndSet(false, true)) {
            if (notify) {
                PlatformTools.addToast(new TextComponentTranslation("emotecraft.reloading.wait"));
            }
            return CompletableFuture.completedFuture(null);
        }

        if (notify) {
            PlatformTools.addToast(new TextComponentTranslation("emotecraft.reloading"));
        }

        Stopwatch stopwatch = Stopwatch.createStarted();

        return CompletableFuture.supplyAsync(UniversalEmoteSerializer::loadEmotes)
                .thenAcceptAsync(emotes -> {
                    EmoteHolder.clearEmotes();
                    EmoteHolder.addEmoteToList(emotes.values(), null);
                }, r -> Minecraft.getMinecraft().addScheduledTask(r))
                .thenRunAsync(() -> {
                    IS_RELOADING.set(false);
                    double seconds = (double) stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) / 1000.0;
                    if (notify) {
                        PlatformTools.addToast(new TextComponentTranslation("emotecraft.reloading.done", FORMAT.format(seconds)));
                    }
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }, r -> Minecraft.getMinecraft().addScheduledTask(r))
                .exceptionally(th -> {
                    IS_RELOADING.set(false);
                    CommonData.LOGGER.error("Failed to reload emotes!", th);
                    return null;
                });
    }
}
