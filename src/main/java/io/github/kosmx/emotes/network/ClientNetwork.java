package io.github.kosmx.emotes.network;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.EmoteStreamHelper;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.main.EmoteHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class ClientNetwork extends AbstractNetworkInstance {
    public static final ClientNetwork INSTANCE = new ClientNetwork();

    private final EmoteStreamHelper streamHelper = new EmoteStreamHelper() {
        @Override
        protected int getMaxPacketSize() {
            return maxDataSize();
        }

        @Override
        protected void sendPlayPacket(ByteBuffer buffer) {
            sendPlayPacketRaw(buffer);
        }

        @Override
        protected void sendStreamChunk(ByteBuffer buffer) {
            sendStreamPacketRaw(buffer);
        }
    };

    private boolean isServerTracking = false;
    private boolean active = true;

    /**
     * 1.12.2 CPacketCustomPayload has a hard limit of 32767 bytes.
     * The default MAX_PACKET_SIZE (1MB) is for modern MC versions.
     */
    @Override
    public int maxDataSize() {
        return 32767;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isServerTrackingPlayState() {
        return isServerTracking;
    }

    public void setServerTracking(boolean tracking) {
        this.isServerTracking = tracking;
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, UUID target) throws IOException {
        if (target != null) builder.configureTarget(target);
        EmotePacket writer = builder.build();
        sendMessage(writer.write(), target);
    }

    @Override
    protected void sendMessage(byte[] bytes, UUID target) {
        sendMessage(ByteBuffer.wrap(bytes), target);
    }

    @Override
    public void sendMessage(ByteBuffer byteBuffer, UUID target) {
        if (byteBuffer.remaining() >= maxDataSize()) {
            // Packet is too large for a single custom payload — use stream chunking
            streamHelper.sendMessage(byteBuffer);
        } else {
            sendPlayPacketRaw(byteBuffer);
        }
    }

    public void sendPlayPacketRaw(ByteBuffer buffer) {
        ForgeNetworkHandler.sendToServer(buffer, false);
    }

    public void sendStreamPacketRaw(ByteBuffer buffer) {
        ForgeNetworkHandler.sendToServer(buffer, true);
    }

    public void receiveStreamMessage(ByteBuffer buff) throws IOException {
        ByteBuffer buffer = streamHelper.receiveStream(buff);
        if (buffer != null) {
            receiveMessage(buffer, null);
        }
    }

    public void receiveConfigMessage(ByteBuffer buf) throws IOException {
        io.github.kosmx.emotes.common.network.objects.NetData packet = new EmotePacket.Builder().build().read(buf);
        if (packet.purpose == PacketTask.CONFIG) {
            setVersions(packet.versions);
        } else if (packet.purpose == PacketTask.FILE) {
            EmoteHolder.addEmoteToList(packet.emoteData, this);
        }
    }
}
