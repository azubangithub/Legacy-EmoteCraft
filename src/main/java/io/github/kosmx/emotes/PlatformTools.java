package io.github.kosmx.emotes;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.services.InstanceService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.Sys;

import java.awt.*;
import java.io.File;
import java.util.UUID;

public final class PlatformTools {
    public static INetworkInstance getClientNetworkController() {
        return io.github.kosmx.emotes.network.ClientNetwork.INSTANCE;
    }

    public static AbstractClientPlayer getPlayerFromUUID(UUID uuid) {
        WorldClient level = Minecraft.getMinecraft().world;
        if (level == null) return null;
        return (AbstractClientPlayer) level.getPlayerEntityByUUID(uuid);
    }

    public static void openExternalEmotesDir() {
        File dir = InstanceService.INSTANCE.getExternalEmoteDir().toFile();
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(dir);
                return;
            } catch (Throwable ignored) {}
        }
        Sys.openURL("file://" + dir.getAbsolutePath());
    }

    public static ClientConfig getConfig() {
        return (ClientConfig) Serializer.getConfig();
    }

    public static void saveConfig() {
        if (Serializer.INSTANCE != null) {
            Serializer.INSTANCE.saveConfig();
        }
    }

    public static boolean isPlayerBlocked(UUID uuid) {
        return false;
    }

    public static int getPerspective() {
        return Minecraft.getMinecraft().gameSettings.thirdPersonView;
    }

    public static void setPerspective(int p) {
        Minecraft.getMinecraft().gameSettings.thirdPersonView = p;
    }

    public static void addToast(ITextComponent title, ITextComponent message) {
        GuiToast toastGui = Minecraft.getMinecraft().getToastGui();
        if (toastGui != null) {
            SystemToast.addOrUpdate(toastGui, SystemToast.Type.TUTORIAL_HINT, title, message);
        }
    }

    public static void addToast(ITextComponent message) {
        PlatformTools.addToast(McUtils.MOD_NAME, message);
    }
}
