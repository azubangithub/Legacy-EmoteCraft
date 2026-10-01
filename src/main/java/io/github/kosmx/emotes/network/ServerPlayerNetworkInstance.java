package io.github.kosmx.emotes.network;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.EmoteStreamHelper;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class ServerPlayerNetworkInstance extends AbstractNetworkInstance implements IServerNetworkInstance {
    private final EntityPlayerMP player;
    private final EmotePlayTracker emoteTracker = new EmotePlayTracker();

    /**
     * Stream helper for reassembling chunked emote packets on the server side.
     * Stream chunks arrive on the stream channel and need to be pieced back together
     * before the complete packet can be processed.
     */
    private final EmoteStreamHelper streamHelper = new EmoteStreamHelper() {
        @Override
        protected int getMaxPacketSize() {
            return maxDataSize();
        }

        @Override
        protected void sendPlayPacket(ByteBuffer buffer) {
            // Server doesn't send stream packets to itself
        }

        @Override
        protected void sendStreamChunk(ByteBuffer buffer) {
            // Server doesn't send stream packets to itself
        }
    };

    public ServerPlayerNetworkInstance(EntityPlayerMP player) {
        this.player = player;
    }

    public EntityPlayerMP getPlayer() {
        return this.player;
    }

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return this.emoteTracker;
    }

    @Override
    public boolean trackPlayState() {
        return true;
    }

    @Override
    public boolean isServerTrackingPlayState() {
        return true;
    }

    @Override
    public boolean isActive() {
        return this.player.connection != null && !this.player.hasDisconnected();
    }

    /**
     * 1.12.2 has a hard limit of 32767 bytes for custom payload packets.
     */
    @Override
    public int maxDataSize() {
        return 32767;
    }

    /**
     * Receive a stream chunk and reassemble. Returns the complete packet bytes
     * when all chunks have been received, or null if more chunks are needed.
     */
    public ByteBuffer receiveStreamChunk(byte[] bytes) {
        return streamHelper.receiveStream(ByteBuffer.wrap(bytes));
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, UUID target) throws IOException {
        if (target != null) builder.configureTarget(target);
        byte[] bytes = builder.build().write().array();
        sendMessage(bytes, target);
    }

    @Override
    public void sendMessage(byte[] bytes, UUID target) {
        ForgeNetworkHandler.sendToPlayer(this.player, ByteBuffer.wrap(bytes), false);
    }

    @Override
    public void sendMessage(ByteBuffer byteBuffer, UUID target) {
        ForgeNetworkHandler.sendToPlayer(this.player, byteBuffer, false);
    }
}
