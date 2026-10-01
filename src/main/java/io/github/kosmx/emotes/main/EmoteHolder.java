package io.github.kosmx.emotes.main;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.tools.UUIDMap;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.serializer.EmoteSerializer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class EmoteHolder implements Supplier<UUID> {
    public final Animation emote;
    public final ITextComponent name;
    public final ITextComponent description;
    public final ITextComponent author;
    public final List<ITextComponent> folder;
    public final List<ITextComponent> bages;
    public final ITextComponent fileName;

    public AtomicInteger hash = null;
    public static UUIDMap<EmoteHolder> list = new UUIDMap<>();
    public ResourceLocation iconIdentifier = null;
    private INetworkInstance fromInstance = null;

    @SuppressWarnings("unchecked")
    public EmoteHolder(Animation emote) {
        this.emote = emote;
        this.name = emote.data().get("name").map(McUtils::fromJson).orElse(new TextComponentString(""));
        this.description = emote.data().get("description").map(McUtils::fromJson).orElse(new TextComponentString(""));
        this.author = emote.data().get("author").map(McUtils::fromJson).orElse(new TextComponentString(""));
        this.folder = computeFolderPath((String) emote.data().getRaw(EmoteSerializer.FOLDER_PATH_KEY));
        this.bages = computeBages((List<String>) emote.data().getRaw("bages"));
        this.fileName = emote.data().get(EmoteSerializer.FILENAME_KEY).map(McUtils::fromJson).orElse(null);
    }

    private static List<ITextComponent> computeFolderPath(String folderPath) {
        if (folderPath == null || folderPath.trim().isEmpty()) return Collections.emptyList();
        String[] parts = folderPath.split("/");
        List<ITextComponent> list = new ArrayList<>();
        for (String p : parts) {
            list.add(new TextComponentString(p));
        }
        return Collections.unmodifiableList(list);
    }

    private static List<ITextComponent> computeBages(List<String> bages) {
        if (bages == null || bages.isEmpty()) return Collections.emptyList();
        List<ITextComponent> components = new ArrayList<>(bages.size());
        for (String element : bages) {
            try {
                components.add(McUtils.fromJson(element));
            } catch (Throwable th) {
                CommonData.LOGGER.warn("Failed to serialize badge!", th);
            }
        }
        return Collections.unmodifiableList(components);
    }

    public static void clearEmotes() {
        clearEmotes(null);
    }

    public static void clearEmotes(INetworkInstance networkInstance) {
        EmoteHolder.list.removeIf(emoteHolder -> {
            if (emoteHolder.fromInstance != networkInstance) return false;
            if (FMLCommonHandler.instance().getSide().isClient()) {
                ClientEmoteUtil.closeIcon(emoteHolder);
            }
            return true;
        });
    }

    public ResourceLocation getIconIdentifier() {
        return ClientEmoteUtil.getIconIdentifier(this);
    }

    public Animation getEmote() {
        return emote;
    }

    public static EmoteHolder getEmoteFromUuid(UUID uuid) {
        return list.get(uuid);
    }

    public static EmoteHolder findIfPresent(Animation animation) {
        if (animation == null) return null;
        EmoteHolder fast = getEmoteFromUuid(animation.uuid());
        if (fast != null && fast.emote != null && fast.emote.equals(animation)) {
            return fast;
        }
        for (EmoteHolder holder : EmoteHolder.list) {
            if (holder.emote != null && holder.emote.equals(animation)) {
                return holder;
            }
        }
        return null;
    }

    public static void addEmoteToList(Iterable<Animation> emotes, INetworkInstance fromInstance) {
        for (Animation emote : emotes) addEmoteToList(emote, fromInstance);
    }

    public static EmoteHolder addEmoteToList(Animation emote, INetworkInstance fromInstance) {
        EmoteHolder old = findIfPresent(emote);
        if (old != null) return old;

        EmoteHolder newEmote = new EmoteHolder(emote);
        newEmote.fromInstance = fromInstance;
        list.add(newEmote);
        return newEmote;
    }

    public static boolean playEmote(EntityPlayer player, Animation emote) {
        return ClientEmoteUtil.playEmote(player, emote);
    }

    public static boolean canRunEmote(EntityPlayer player) {
        return ClientEmoteUtil.canRunEmote(player);
    }

    public boolean playEmote() {
        return playEmote(null, this.emote);
    }

    @Override
    public int hashCode() {
        if (hash == null) hash = new AtomicInteger(this.emote.hashCode());
        return hash.get();
    }

    public UUID getUuid() {
        return this.emote.uuid();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EmoteHolder && this.emote.equals(((EmoteHolder) o).emote);
    }

    @Override
    public UUID get() {
        return this.emote.get();
    }

    public static void handleKeyPress(int key) {
        ClientEmoteUtil.handleKeyPress(key);
    }

    public static EmoteHolder getNonNull(UUID emote) {
        EmoteHolder emoteHolder = list.get(emote);
        if (emoteHolder == null) return new Empty(emote);
        return emoteHolder;
    }

    public static class Empty extends EmoteHolder {
        public Empty(UUID uuid) {
            super(new Animation(new ExtraAnimationData(
                    ExtraAnimationData.NAME_KEY, "{\"color\":\"red\",\"text\":\"INVALID\"}"
            ), 0, Animation.LoopType.PLAY_ONCE, Collections.emptyMap(), UniversalAnimLoader.NO_KEYFRAMES, new HashMap<>(), new HashMap<>()));
            emote.data().put(ExtraAnimationData.UUID_KEY, uuid);
        }
    }
}
