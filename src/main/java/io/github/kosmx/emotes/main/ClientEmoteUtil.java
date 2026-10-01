package io.github.kosmx.emotes.main;

import com.zigythebird.playeranim.util.ClientUtil;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class ClientEmoteUtil {

    public static ResourceLocation getIconIdentifier(EmoteHolder holder) {
        if (holder.iconIdentifier == null && holder.emote.data().getRaw("iconData") instanceof ByteBuffer) {
            registerIcon(holder, (ByteBuffer) holder.emote.data().getRaw("iconData"));
        }
        return holder.iconIdentifier;
    }

    public static void registerIcon(EmoteHolder holder, ByteBuffer buffer) {
        try (InputStream stream = new ByteArrayInputStream(AbstractNetworkInstance.safeGetBytesFromBuffer(buffer))) {
            BufferedImage image = ImageIO.read(stream);
            if (image != null) {
                DynamicTexture texture = new DynamicTexture(image);
                holder.iconIdentifier = McUtils.newIdentifier("icon_" + holder.hashCode());
                Minecraft.getMinecraft().getTextureManager().loadTexture(holder.iconIdentifier, texture);
            }
        } catch (Throwable th) {
            CommonData.LOGGER.warn("Can't open emote {} icon!", holder.emote, th);
        }
    }

    public static void closeIcon(EmoteHolder holder) {
        if (holder.iconIdentifier == null) return;
        ResourceLocation id = holder.iconIdentifier;
        Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().getTextureManager().deleteTexture(id));
        holder.iconIdentifier = null;
    }

    public static boolean playEmote(EntityPlayer player, Animation emote) {
        if (player == null) player = ClientUtil.getClientPlayer();
        return canPlayEmote(player) && ClientEmotePlay.clientStartLocalEmote(emote);
    }

    public static boolean canPlayEmote(EntityPlayer entity) {
        if (!canRunEmote(entity)) return false;
        return entity == ClientUtil.getClientPlayer();
    }

    public static boolean canRunEmote(EntityPlayer player) {
        if (player == null) return false;
        double dx = player.posX - player.prevPosX;
        double dy = (player.posY - player.prevPosY) * PlatformTools.getConfig().yRatio.get();
        double dz = player.posZ - player.prevPosZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz) <= PlatformTools.getConfig().stopThreshold.get();
    }

    public static void handleKeyPress(int key) {
        if (canRunEmote(ClientUtil.getClientPlayer())) {
            UUID uuid = PlatformTools.getConfig().emoteKeyMap.getL(key);
            if (uuid != null) {
                EmoteHolder emoteHolder = EmoteHolder.list.get(uuid);
                if (emoteHolder != null) {
                    playEmote(ClientUtil.getClientPlayer(), emoteHolder.emote);
                }
            }
        }
    }
}
