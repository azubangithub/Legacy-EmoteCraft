package io.github.kosmx.emotes.main.config;

import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.common.tools.BiMap;

import java.util.UUID;

public class ClientConfig extends SerializableConfig {
    public final ConfigEntry<Boolean> dark = new ConfigEntry<>("dark", false, false, basics);
    public final ConfigEntry<Boolean> oldChooseWheel = new ConfigEntry<>("oldChooseWheel", false, false, basics);
    public final ConfigEntry<Boolean> enablePerspective = new ConfigEntry<>("perspective", true, false, basics);
    public final ConfigEntry<Boolean> frontAsTPPerspective = new ConfigEntry<>("default3rdPersonFront", false, false, basics);
    public final ConfigEntry<Boolean> showIcons = new ConfigEntry<>("showicon", "showIcon", true, false, basics);
    public final ConfigEntry<Boolean> checkPose = new ConfigEntry<>("checkPose", true, true, expert);
    public final ConfigEntry<Boolean> alwaysOpenEmoteScreen = new ConfigEntry<>("alwaysOpenScreen", false, true, basics);
    public final ConfigEntry<Boolean> alwaysValidate = new ConfigEntry<>("alwaysValidateEmote", false, true, expert);
    public final ConfigEntry<Boolean> enablePlayerSafety = new ConfigEntry<>("playersafety", "playersafety", false, false, null, true);
    public final ConfigEntry<Float> stopThreshold = new FloatConfigEntry("stopthreshold", "stopThreshold", 0.04f, true, expert, "options.generic_value", -3.912f, 8f, 0f) {
        @Override
        public double getConfigVal() {
            return Math.log(this.get());
        }

        @Override
        public void setConfigVal(double newVal) {
            this.set((float) Math.exp(newVal));
        }
    };
    public final ConfigEntry<Float> yRatio = new FloatConfigEntry("yratio", "yRatio", 0.75f, true, expert, "options.percent_value", 0, 100, 1) {
        @Override
        public double getConfigVal() {
            return this.get() * 100f;
        }

        @Override
        public void setConfigVal(double newVal) {
            this.set((float) (newVal / 100f));
        }

        @Override
        public double getTextVal() {
            return this.getConfigVal();
        }
    };
    public final ConfigEntry<Boolean> showHiddenConfig = new ConfigEntry<>("showHiddenConfig", false, true, expert, false);
    public final ConfigEntry<Boolean> exportBuiltin = new ConfigEntry<>("exportBuiltin", false, expert, true);
    public final ConfigEntry<Boolean> scrollPage = new ConfigEntry<>("scrollPage", true, true, basics);
    public final ConfigEntry<Boolean> enableNSFW = new ConfigEntry<>("enableNSFW", false, true, basics);

    public ClientConfig() {
        loadEmotesServerSide.set(false);
    }

    public BiMap<UUID, Integer> emoteKeyMap = new BiMap<>();
    public UUID[][] fastMenuEmotes = new UUID[15][8];

    public final ConfigEntry<Boolean> hideWarningMessage = new ConfigEntry<>("hideWarning", false, expert, true);

    public int getCameraType() {
        return frontAsTPPerspective.get() ? 2 : 1;
    }
}
