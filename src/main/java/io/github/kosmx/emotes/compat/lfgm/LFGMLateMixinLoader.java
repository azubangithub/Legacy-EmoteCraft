package io.github.kosmx.emotes.compat.lfgm;

import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;
import zone.rong.mixinbooter.MixinLoader;

import java.util.Collections;
import java.util.List;

@MixinLoader
public class LFGMLateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("emotecraft.lfgm.mixins.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        if ("emotecraft.lfgm.mixins.json".equals(mixinConfig)) {
            return Loader.isModLoaded("wildfire_gender");
        }
        return true;
    }
}
