package io.github.kosmx.emotes.common.network;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public abstract class EmoteStreamHelper {
    protected abstract int getMaxPacketSize();

    /**
     * If packet is not too large, this method will be invoked with the original packet
     * @param buffer to send
     */
    protected abstract void sendPlayPacket(ByteBuffer buffer);

    /**
     * Send a part of a stream packet
     * @param buffer to send
     */
    protected abstract void sendStreamChunk(ByteBuffer buffer);

    protected ByteBuffer receiveStream = null;

    public void sendMessage(ByteBuffer bytes) {
        if (bytes.limit() < getMaxPacketSize()) { // definitely small enough for vanilla packet
            sendPlayPacket(bytes);
            return;
        }

        int length = bytes.limit();


        {
            int chunkSize = Math.min(getMaxPacketSize() - 4, bytes.remaining());
            ByteBuffer tmpBuffer = ByteBuffer.allocate(chunkSize + 4);
            tmpBuffer.putInt(length);
            byte[] targetArray = new byte[chunkSize];
            bytes.get(targetArray);
            tmpBuffer.put(targetArray);
            ((Buffer) tmpBuffer).flip();
            sendStreamChunk(tmpBuffer);
        }
        while (bytes.hasRemaining()) {
            byte[] targetArray = new byte[Math.min(getMaxPacketSize(), bytes.remaining())];
            bytes.get(targetArray);
            sendStreamChunk(ByteBuffer.wrap(targetArray));
        }
    }

    /**
     * Receive stream data
     * @param rec Received chunk
     * @return null or the complete packet
     */
    public ByteBuffer receiveStream(ByteBuffer rec) {
        if (receiveStream == null) {
            int len = rec.getInt();
            receiveStream = ByteBuffer.allocate(len);
        }

        if (rec.remaining() > receiveStream.remaining()) {
            receiveStream = null;
            return null;
        }

        receiveStream.put(rec);
        if (!receiveStream.hasRemaining()) {
            ((Buffer)receiveStream).flip();
            ByteBuffer tmp = receiveStream;
            receiveStream = null;
            return tmp;
        }
        return null;
    }
}
