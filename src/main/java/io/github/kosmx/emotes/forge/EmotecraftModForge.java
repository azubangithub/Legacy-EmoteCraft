package io.github.kosmx.emotes.forge;

import io.github.kosmx.emotes.common.CommonData;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
        modid = CommonData.MOD_ID,
        name = CommonData.MOD_NAME,
        version = "2.4.12",
        acceptedMinecraftVersions = "[1.12.2]"
)
public class EmotecraftModForge {

    @Mod.Instance(CommonData.MOD_ID)
    public static EmotecraftModForge INSTANCE;

    @SidedProxy(
            clientSide = "io.github.kosmx.emotes.forge.ClientProxy",
            serverSide = "io.github.kosmx.emotes.forge.CommonProxy"
    )
    public static CommonProxy PROXY;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PROXY.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PROXY.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PROXY.postInit(event);
    }
}
