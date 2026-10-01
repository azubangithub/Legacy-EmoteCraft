package io.github.kosmx.emotes.main.network;

import com.zigythebird.playeranim.util.ClientUtil;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.event.EventResult;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.events.client.ClientEmoteAPI;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClientEmotePlay extends ClientEmoteAPI {
    private static final Map<UUID, QueueEntry> QUEUE = new ConcurrentHashMap<>();

    public static void clientStartLocalEmote(EmoteHolder emoteHolder) {
        clientStartLocalEmote(emoteHolder.getEmote());
    }

    public static boolean clientStartLocalEmote(Animation emote) {
        return clientStartLocalEmote(emote, 0);
    }

    public static boolean clientStartLocalEmote(Animation emote, int tick) {
        EntityPlayerSP player = ClientUtil.getClientPlayer();
        if (player == null || !(player instanceof IPlayerEntity)) return false;
        IPlayerEntity pEntity = (IPlayerEntity) player;
        if (pEntity.emotecraft$isForcedEmote()) {
            return false;
        }

        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToStreamEmote(emote, player.getUniqueID());
        packetBuilder.configureEmoteTick(tick);
        ClientPacketManager.send(packetBuilder, null);
        ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(emote, tick, player.getUniqueID());
        pEntity.emotecraft$playEmote(emote, tick, false);
        return true;
    }

    public static void clientRepeatLocalEmote(Animation emote, float tick, UUID target) {
        EntityPlayerSP player = ClientUtil.getClientPlayer();
        if (player == null) return;
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToStreamEmote(emote, player.getUniqueID()).configureEmoteTick(tick);
        ClientPacketManager.send(packetBuilder, target);
    }

    public static boolean clientStopLocalEmote() {
        EntityPlayerSP player = ClientUtil.getClientPlayer();
        if (player instanceof IPlayerEntity) {
            IPlayerEntity pEntity = (IPlayerEntity) player;
            if (pEntity.isPlayingEmote()) {
                return clientStopLocalEmote(pEntity.emotecraft$getEmote().getData());
            }
        }
        return false;
    }

    public static boolean isForcedEmote() {
        EntityPlayerSP player = ClientUtil.getClientPlayer();
        if (player instanceof IPlayerEntity) {
            return ((IPlayerEntity) player).emotecraft$isForcedEmote();
        }
        return false;
    }

    public static boolean clientStopLocalEmote(Animation emoteData) {
        EntityPlayerSP player = ClientUtil.getClientPlayer();
        if (player instanceof IPlayerEntity && emoteData != null) {
            IPlayerEntity pEntity = (IPlayerEntity) player;
            if (!pEntity.emotecraft$isForcedEmote()) {
                EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
                packetBuilder.configureToSendStop(emoteData.uuid(), player.getUniqueID());
                ClientPacketManager.send(packetBuilder, null);
                pEntity.stopEmote();

                ClientEmoteEvents.LOCAL_EMOTE_STOP.invoker().onEmoteStop();
                return true;
            }
        }
        return false;
    }

    static void executeMessage(NetData data, INetworkInstance networkInstance) {
        CommonData.LOGGER.trace("[emotes client] Received message: {}", data);
        if (data.purpose == null) {
            CommonData.LOGGER.error("Packet execution is not possible without a purpose");
            return;
        }

        switch (data.purpose) {
            case STREAM:
                if (data.emoteData != null && (data.valid || !PlatformTools.getConfig().alwaysValidate.get())) {
                    receivePlayPacket(data.emoteData, data.player, data.tick, data.isForced);
                }
                break;
            case STOP:
                AbstractClientPlayer player = PlatformTools.getPlayerFromUUID(data.player);
                if (data.stopEmoteID != null) {
                    if (player instanceof IPlayerEntity) {
                        IPlayerEntity pEntity = (IPlayerEntity) player;
                        ClientEmoteEvents.EMOTE_STOP.invoker().onEmoteStop(data.stopEmoteID, player.getUniqueID());
                        pEntity.stopEmote(data.stopEmoteID);
                        if (pEntity.isMainPlayer() && !data.isForced) {
                            PlatformTools.addToast(new TextComponentTranslation("emotecraft.blockedEmote"));
                        }
                    } else {
                        QUEUE.remove(data.player);
                    }
                }
                break;
            case CONFIG:
                if (data.versions != null) {
                    networkInstance.setVersions(data.versions);
                    CommonData.LOGGER.warn("Legacy versions received: {}", data.versions);
                }
                break;
            case FILE:
                if (data.emoteData != null) {
                    EmoteHolder.addEmoteToList(data.emoteData, networkInstance);
                }
                break;
            case UNKNOWN:
            default:
                CommonData.LOGGER.error("Packet execution is not possible for unknown purpose");
                break;
        }
    }

    static void receivePlayPacket(Animation emoteData, UUID player, float tick, boolean isForced) {
        AbstractClientPlayer playerEntity = PlatformTools.getPlayerFromUUID(player);
        if (isEmoteAllowed(emoteData, player)) {
            EventResult result = ClientEmoteEvents.EMOTE_VERIFICATION.invoker().verify(emoteData, player);
            if (result == EventResult.FAIL) return;
            if (playerEntity instanceof IPlayerEntity) {
                ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(emoteData, tick, player);
                ((IPlayerEntity) playerEntity).emotecraft$playEmote(emoteData, tick, isForced);
            } else {
                QUEUE.put(player, new QueueEntry(emoteData, tick, getCurrentTick()));
            }
        }
    }

    public static boolean isEmoteAllowed(Animation emoteData, UUID player) {
        return (PlatformTools.getConfig().enablePlayerSafety.get() || !PlatformTools.isPlayerBlocked(player));
    }

    public static Pair<Animation, Float> getEmoteForUUID(UUID uuid) {
        if (QUEUE.containsKey(uuid)) {
            QueueEntry entry = QUEUE.get(uuid);
            Animation emoteData = entry.emoteData;
            float tick = entry.beginTick - entry.receivedTick + getCurrentTick();
            QUEUE.remove(uuid);
            if (!emoteData.isPlayingAt(tick)) return null;
            return Pair.of(emoteData, tick);
        }
        return null;
    }

    public static void checkQueue() {
        int currentTick = getCurrentTick();
        QUEUE.forEach((uuid, entry) -> {
            if (!entry.emoteData.isPlayingAt(entry.beginTick + currentTick) && entry.beginTick + currentTick > 0
                    || currentTick - entry.receivedTick > 24000) {
                QUEUE.remove(uuid);
            }
        });
    }

    public static int getCurrentTick() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.world != null ? (int) mc.world.getTotalWorldTime() : 0;
    }

    @Override
    protected boolean playEmoteImpl(Animation animation, int tick) {
        if (animation != null) {
            return clientStartLocalEmote(animation, tick);
        } else {
            return clientStopLocalEmote();
        }
    }

    @Override
    protected Collection<Animation> clientEmoteListImpl() {
        return EmoteHolder.list.values().stream().map(EmoteHolder::getEmote).collect(Collectors.toList());
    }

    static class QueueEntry {
        final Animation emoteData;
        final float beginTick;
        final int receivedTick;

        QueueEntry(Animation emoteData, float begin, int received) {
            this.emoteData = emoteData;
            this.beginTick = begin;
            this.receivedTick = received;
        }
    }
}
