package io.github.kosmx.emotes.forge;

import io.github.kosmx.emotes.ForgeCommonEvents;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.gui.ClientKeyEventHandler;
import io.github.kosmx.emotes.gui.KeyBindings;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.config.ClientConfigSerializer;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import io.github.kosmx.emotes.network.ForgeNetworkHandler;
import io.github.kosmx.emotes.network.ForgeServerEmotePlay;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        Serializer.INSTANCE = new Serializer<>(new ClientConfigSerializer(), ClientConfig.class);
        ClientPacketManager.init();
        EmoteHolder.addEmoteToList(UniversalEmoteSerializer.loadEmotes().values(), null);

        ClientConfig config = (ClientConfig) Serializer.getConfig();
        if (config != null) {
            boolean hasAny = false;
            for (int i = 0; i < 8; i++) {
                if (config.fastMenuEmotes[0][i] != null) {
                    hasAny = true;
                    break;
                }
            }
            if (!hasAny) {
                int slot = 0;
                for (EmoteHolder holder : EmoteHolder.list) {
                    if (slot >= 8) break;
                    config.fastMenuEmotes[0][slot++] = holder.getUuid();
                }
            }
        }

        ForgeNetworkHandler.init();
        io.github.kosmx.emotes.network.ForgeClientNetworkHandler.register(ForgeNetworkHandler.getPlayChannel(), ForgeNetworkHandler.getStreamChannel());
        MinecraftForge.EVENT_BUS.register(new ForgeCommonEvents());

        KeyBindings.init();
        MinecraftForge.EVENT_BUS.register(new ClientKeyEventHandler());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        ForgeServerEmotePlay.init();
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new CommandEmotesReload());
        io.github.kosmx.emotes.main.EmoteWatcher.init();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        CommonData.LOGGER.info("Emotecraft 1.12.2 client initialized with {} emotes loaded!", EmoteHolder.list.size());
    }
}
