package io.github.kosmx.emotes.main;

import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.config.ClientConfigSerializer;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import io.github.kosmx.emotes.server.config.ConfigSerializer;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

public abstract class EmotecraftMod {
    public static void init(boolean isClient) {
        if (isClient) {
            Serializer.INSTANCE = new Serializer<>(new ClientConfigSerializer(), ClientConfig.class);
            ClientPacketManager.init();
        } else {
            Serializer.INSTANCE = new Serializer<>(new ConfigSerializer<>(SerializableConfig::new), SerializableConfig.class);
        }
        UniversalEmoteSerializer.loadEmotes();
    }
}
