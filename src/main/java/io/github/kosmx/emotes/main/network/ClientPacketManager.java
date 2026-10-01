package io.github.kosmx.emotes.main.network;

import com.zigythebird.playeranimcore.event.EventResult;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.events.client.ClientNetworkEvents;
import io.github.kosmx.emotes.api.proxy.EmotesProxyManager;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.main.EmoteHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public final class ClientPacketManager extends EmotesProxyManager {

    private static INetworkInstance defaultNetwork;

    public static void init() {
        defaultNetwork = PlatformTools.getClientNetworkController();
        setManager(new ClientPacketManager());
    }

    private ClientPacketManager() {}

    private static boolean useAlwaysAlt() {
        return false;
    }

    public static void send(EmotePacket.Builder packetBuilder, UUID target) {
        if (ClientNetworkEvents.PACKET_SEND.invoker().onPacketSend(packetBuilder) == EventResult.FAIL) {
            CommonData.LOGGER.warn("Sending the packet has been canceled by the event!");
            return;
        }
        if (defaultNetwork == null) {
            defaultNetwork = PlatformTools.getClientNetworkController();
        }
        if (defaultNetwork == null || !defaultNetwork.isActive() || useAlwaysAlt()) {
            for (INetworkInstance network : networkInstances) {
                if (network.isActive()) {
                    if (target == null || !network.isServerTrackingPlayState()) {
                        try {
                            EmotePacket.Builder builder = packetBuilder.copy();
                            if (!network.sendPlayerID()) builder.removePlayerID();
                            builder.setSizeLimit(network.maxDataSize(), false);
                            builder.setVersion(network.getRemoteVersions());
                            network.sendMessage(builder, target);
                        } catch (IOException exception) {
                            CommonData.LOGGER.error("Error while sending packet!", exception);
                        }
                    }
                }
            }
        }
        if (defaultNetwork != null && defaultNetwork.isActive() && (target == null || !defaultNetwork.isServerTrackingPlayState())) {
            if (!defaultNetwork.sendPlayerID()) packetBuilder.removePlayerID();
            try {
                packetBuilder.setSizeLimit(defaultNetwork.maxDataSize(), false);
                packetBuilder.setVersion(defaultNetwork.getRemoteVersions());
                defaultNetwork.sendMessage(packetBuilder, target);
            } catch (IOException exception) {
                CommonData.LOGGER.error("Error while sending packet!", exception);
            }
        }
    }

    public static void receiveMessage(ByteBuffer buffer, UUID player, INetworkInstance networkInstance) {
        try {
            NetData data = new EmotePacket.Builder().setThreshold(PlatformTools.getConfig().validThreshold.get()).build().read(buffer);
            if (!networkInstance.trustReceivedPlayer()) {
                data.player = null;
            }
            if (player != null) {
                data.player = player;
            }
            if (data.player == null && data.purpose != null && data.purpose.playerBound) {
                throw new IOException("Didn't receive any player information");
            }

            try {
                ClientEmotePlay.executeMessage(data, networkInstance);
            } catch (Exception e) {
                CommonData.LOGGER.error("Critical error has occurred while receiving emote!", e);
            }
        } catch (IOException e) {
            CommonData.LOGGER.warn("Error while receiving packet!", e);
        }
    }

    @Override
    protected void dispatchReceive(ByteBuffer buffer, UUID player, INetworkInstance networkInstance) {
        receiveMessage(buffer, player, networkInstance);
    }

    public static boolean isRemoteAvailable() {
        if (defaultNetwork == null) {
            defaultNetwork = PlatformTools.getClientNetworkController();
        }
        return defaultNetwork != null && defaultNetwork.isActive();
    }

    public static boolean isRemoteTracking() {
        return isRemoteAvailable() && defaultNetwork.isServerTrackingPlayState();
    }

    public static boolean isAvailableProxy() {
        for (INetworkInstance instance : networkInstances) {
            if (instance.isActive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDisconnectFromServer(INetworkInstance networkInstance) {
        if (networkInstance == null) throw new NullPointerException("network instance must be non-null");
        EmoteHolder.clearEmotes(networkInstance);
    }
}
