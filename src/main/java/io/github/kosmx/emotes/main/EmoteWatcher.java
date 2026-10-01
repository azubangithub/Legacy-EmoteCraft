package io.github.kosmx.emotes.main;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.server.services.InstanceService;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * File system WatchService listener for the emotes directory.
 * Automatically detects added, modified, or removed emote files and triggers a hot-reload.
 */
public class EmoteWatcher {
    private static WatchService watchService;
    private static final Map<WatchKey, Path> keyMap = new HashMap<>();
    private static long lastChangeTime = 0;
    private static boolean changePending = false;
    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        try {
            Path emoteDir = InstanceService.INSTANCE.getExternalEmoteDir();
            if (!Files.isDirectory(emoteDir)) {
                Files.createDirectories(emoteDir);
            }
            watchService = emoteDir.getFileSystem().newWatchService();
            registerAll(emoteDir);
            initialized = true;
        } catch (Throwable t) {
            CommonData.LOGGER.warn("Failed to initialize EmoteWatcher", t);
        }
    }

    private static void registerAll(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
                keyMap.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static synchronized void checkAndTick(Runnable onReloadComplete) {
        if (!initialized) {
            init();
            return;
        }
        if (watchService == null) return;

        try {
            WatchKey key;
            boolean detectedNewEvent = false;
            while ((key = watchService.poll()) != null) {
                Path dir = keyMap.get(key);
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                    detectedNewEvent = true;
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE && dir != null) {
                        try {
                            Path child = dir.resolve((Path) event.context());
                            if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                                registerAll(child);
                            }
                        } catch (Throwable ignored) {}
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    keyMap.remove(key);
                }
            }

            if (detectedNewEvent) {
                lastChangeTime = System.currentTimeMillis();
                changePending = true;
            }

            // Debounce: wait 500ms after the last file write event to let Blockbench/Notepad finish writing
            if (changePending && (System.currentTimeMillis() - lastChangeTime >= 500)) {
                changePending = false;
                EmoteReloader.reloadEmotes(true, onReloadComplete);
            }
        } catch (Throwable t) {
            CommonData.LOGGER.warn("Error while polling EmoteWatcher", t);
        }
    }
}
