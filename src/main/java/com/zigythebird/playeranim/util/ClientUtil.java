package com.zigythebird.playeranim.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class ClientUtil {
    public static EntityPlayerSP getClientPlayer() {
        return Minecraft.getMinecraft().player;
    }
}
