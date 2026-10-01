package io.github.kosmx.emotes.network;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.nio.ByteBuffer;

@SideOnly(Side.CLIENT)
public class ForgeClientNetworkHandler {

    public static void register(FMLEventChannel playChannel, FMLEventChannel streamChannel) {
        if (playChannel != null) {
            playChannel.register(new ClientPacketListener(false));
        }
        if (streamChannel != null) {
            streamChannel.register(new ClientPacketListener(true));
        }
    }

    public static class ClientPacketListener {
        private final boolean isStream;

        public ClientPacketListener(boolean isStream) {
            this.isStream = isStream;
        }

        @SubscribeEvent
        public void onClientCustomPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
            PacketBuffer buf = new PacketBuffer(event.getPacket().payload());
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);

            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                if (isStream) {
                    try {
                        ClientNetwork.INSTANCE.receiveStreamMessage(ByteBuffer.wrap(bytes));
                    } catch (Exception e) {
                        CommonData.LOGGER.error("Failed to receive stream packet on client", e);
                    }
                } else {
                    ClientPacketManager.receiveMessage(ByteBuffer.wrap(bytes), null, ClientNetwork.INSTANCE);
                }
            });
        }
    }
}
