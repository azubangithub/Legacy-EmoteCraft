package io.github.kosmx.emotes;

import io.github.kosmx.emotes.network.ForgeServerEmotePlay;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class ForgeCommonEvents {
    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntityPlayerMP && event.getEntityPlayer() instanceof EntityPlayerMP) {
            ForgeServerEmotePlay.getInstance().onStartTracking((EntityPlayerMP) event.getTarget(), (EntityPlayerMP) event.getEntityPlayer());
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            ForgeServerEmotePlay.getInstance().removeInstance(event.player.getUniqueID());
        }
    }
}
