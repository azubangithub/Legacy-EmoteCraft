package io.github.kosmx.emotes.network;

import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ForgeServerEmotePlay extends AbstractServerEmotePlay<ServerPlayerNetworkInstance> {
    private static final ForgeServerEmotePlay INSTANCE = new ForgeServerEmotePlay();
    private final Map<UUID, ServerPlayerNetworkInstance> playerInstances = new ConcurrentHashMap<>();

    public static ForgeServerEmotePlay getInstance() {
        return INSTANCE;
    }

    public static void init() {
        getInstance();
    }

    public ServerPlayerNetworkInstance getOrCreateInstance(EntityPlayerMP player) {
        if (player == null) return null;
        return playerInstances.computeIfAbsent(player.getUniqueID(), uuid -> new ServerPlayerNetworkInstance(player));
    }

    public void removeInstance(UUID uuid) {
        playerInstances.remove(uuid);
    }

    @Override
    protected UUID getUUIDFromPlayer(ServerPlayerNetworkInstance player) {
        return player.getPlayer().getUniqueID();
    }

    @Override
    protected ServerPlayerNetworkInstance getPlayerFromUUID(UUID player) {
        ServerPlayerNetworkInstance instance = playerInstances.get(player);
        if (instance != null) return instance;

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            EntityPlayerMP mp = server.getPlayerList().getPlayerByUUID(player);
            if (mp != null) {
                return getOrCreateInstance(mp);
            }
        }
        return null;
    }

    @Override
    protected void sendForEveryoneElse(NetData data, ServerPlayerNetworkInstance player) {
        EntityPlayerMP entityPlayer = player.getPlayer();
        if (entityPlayer.world instanceof WorldServer) {
            WorldServer ws = (WorldServer) entityPlayer.world;
            Set<? extends net.minecraft.entity.player.EntityPlayer> tracking = ws.getEntityTracker().getTrackingPlayers(entityPlayer);
            for (net.minecraft.entity.player.EntityPlayer tracker : tracking) {
                if (tracker instanceof EntityPlayerMP) {
                    ServerPlayerNetworkInstance targetInstance = getOrCreateInstance((EntityPlayerMP) tracker);
                    if (targetInstance != player) {
                        sendForPlayer(data, player, targetInstance);
                    }
                }
            }
        }
    }

    public void onStartTracking(EntityPlayerMP tracked, EntityPlayerMP tracker) {
        playerStartTracking(getOrCreateInstance(tracked), getOrCreateInstance(tracker));
    }
}
