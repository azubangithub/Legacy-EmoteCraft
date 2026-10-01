package io.github.kosmx.emotes.main.emotePlay.instances;

import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class SoundEventInstance extends PositionedSound {
    public SoundEventInstance(SoundEvent soundEvent, float volume, float pitch, double x, double y, double z) {
        super(soundEvent, SoundCategory.PLAYERS);
        this.volume = volume;
        this.pitch = pitch;
        this.xPosF = (float) x;
        this.yPosF = (float) y;
        this.zPosF = (float) z;
        this.repeat = false;
        this.repeatDelay = 0;
        this.attenuationType = AttenuationType.LINEAR;
    }
}
