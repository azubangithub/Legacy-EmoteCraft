package io.github.kosmx.emotes.network;

import io.github.kosmx.emotes.common.CommonData;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.nio.ByteBuffer;

public class ForgeNetworkHandler {
    public static final String EMOTE_CHANNEL = "emotecraft:play";
    public static final String STREAM_CHANNEL = "emotecraft:stream";

    private static FMLEventChannel playChannel;
    private static FMLEventChannel streamChannel;

    public static void init() {
        playChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(EMOTE_CHANNEL);
        streamChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(STREAM_CHANNEL);

        // Play channel: each packet is a complete emote packet
        playChannel.register(new ServerPlayPacketListener());
        // Stream channel: packets are chunks that need reassembly
        streamChannel.register(new ServerStreamPacketListener());
    }

    public static FMLEventChannel getPlayChannel() {
        return playChannel;
    }

    public static FMLEventChannel getStreamChannel() {
        return streamChannel;
    }

    public static void sendToServer(ByteBuffer buffer, boolean isStream) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String channelName = isStream ? STREAM_CHANNEL : EMOTE_CHANNEL;
        FMLEventChannel channel = isStream ? streamChannel : playChannel;
        FMLProxyPacket packet = new FMLProxyPacket(new PacketBuffer(Unpooled.wrappedBuffer(bytes)), channelName);
        if (channel != null) {
            channel.sendToServer(packet);
        }
    }

    public static void sendToPlayer(EntityPlayerMP player, ByteBuffer buffer, boolean isStream) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String channelName = isStream ? STREAM_CHANNEL : EMOTE_CHANNEL;
        FMLEventChannel channel = isStream ? streamChannel : playChannel;
        FMLProxyPacket packet = new FMLProxyPacket(new PacketBuffer(Unpooled.wrappedBuffer(bytes)), channelName);
        if (channel != null && player != null) {
            channel.sendTo(packet, player);
        }
    }

    /**
     * Handles complete emote packets received on the play channel.
     */
    public static class ServerPlayPacketListener {
        @SubscribeEvent
        public void onServerCustomPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
            if (event.getHandler() instanceof NetHandlerPlayServer) {
                EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;
                PacketBuffer buf = new PacketBuffer(event.getPacket().payload());
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);

                player.getServer().addScheduledTask(() -> {
                    try {
                        ServerPlayerNetworkInstance instance = ForgeServerEmotePlay.getInstance().getOrCreateInstance(player);
                        ForgeServerEmotePlay.getInstance().receiveMessage(bytes, instance);
                    } catch (Exception e) {
                        CommonData.LOGGER.error("Failed to receive emote packet on server", e);
                    }
                });
            }
        }
    }

    /**
     * Handles stream chunk packets received on the stream channel.
     * Chunks are reassembled per-player using EmoteStreamHelper.
     * Once a complete packet is assembled, it is forwarded to receiveMessage.
     */
    public static class ServerStreamPacketListener {
        @SubscribeEvent
        public void onServerCustomPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
            if (event.getHandler() instanceof NetHandlerPlayServer) {
                EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;
                PacketBuffer buf = new PacketBuffer(event.getPacket().payload());
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);

                player.getServer().addScheduledTask(() -> {
                    try {
                        ServerPlayerNetworkInstance instance = ForgeServerEmotePlay.getInstance().getOrCreateInstance(player);
                        // Feed the chunk to the stream helper for reassembly
                        ByteBuffer completePacket = instance.receiveStreamChunk(bytes);
                        if (completePacket != null) {
                            // Stream fully assembled — process as a complete emote packet
                            byte[] completeBytes = new byte[completePacket.remaining()];
                            completePacket.get(completeBytes);
                            ForgeServerEmotePlay.getInstance().receiveMessage(completeBytes, instance);
                        }
                    } catch (Exception e) {
                        CommonData.LOGGER.error("Failed to receive stream packet on server", e);
                    }
                });
            }
        }
    }
}
