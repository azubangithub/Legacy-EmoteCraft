package io.github.kosmx.emotes.forge;

import io.github.kosmx.emotes.ForgeCommonEvents;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.network.ForgeNetworkHandler;
import io.github.kosmx.emotes.network.ForgeServerEmotePlay;
import io.github.kosmx.emotes.server.config.ConfigSerializer;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Serializer.INSTANCE = new Serializer<>(new ConfigSerializer<>(SerializableConfig::new), SerializableConfig.class);
        UniversalEmoteSerializer.loadEmotes();

        ForgeNetworkHandler.init();
        MinecraftForge.EVENT_BUS.register(new ForgeCommonEvents());
    }

    public void init(FMLInitializationEvent event) {
        ForgeServerEmotePlay.init();
    }

    public void postInit(FMLPostInitializationEvent event) {
        CommonData.LOGGER.info("Emotecraft 1.12.2 server initialized with {} emotes loaded!", EmoteHolder.list.size());
    }
}
