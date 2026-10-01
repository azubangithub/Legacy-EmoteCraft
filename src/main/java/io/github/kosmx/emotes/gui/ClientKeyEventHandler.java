package io.github.kosmx.emotes.gui;

import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class ClientKeyEventHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return;

        if (KeyBindings.KEY_OPEN_MENU.isPressed()) {
            if (io.github.kosmx.emotes.PlatformTools.getConfig().alwaysOpenEmoteScreen.get() || mc.player == mc.getRenderViewEntity()) {
                mc.displayGuiScreen(new FastMenuScreen(null));
            }
            return;
        }

        if (KeyBindings.KEY_STOP_EMOTE.isPressed()) {
            ClientEmotePlay.clientStopLocalEmote();
            return;
        }

        if (KeyBindings.KEY_RELOAD_EMOTES.isPressed()) {
            io.github.kosmx.emotes.main.EmoteReloader.reloadEmotes(true, null);
            return;
        }

        if (Keyboard.getEventKeyState()) {
            int key = Keyboard.getEventKey();
            if (key != Keyboard.KEY_NONE) {
                EmoteHolder.handleKeyPress(key);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isClient()) {
            if (event.player instanceof IPlayerEntity) {
                ((IPlayerEntity) event.player).emotecraft$tickClient();
            }
            if (event.player == Minecraft.getMinecraft().player && event.player.ticksExisted % 20 == 0) {
                io.github.kosmx.emotes.main.EmoteWatcher.checkAndTick(null);
            }
        }
    }
}
