package io.github.kosmx.emotes.main.emotePlay;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.emotePlay.instances.SoundDirectInstance;
import io.github.kosmx.emotes.main.emotePlay.instances.SoundEventInstance;
import net.minecraft.client.audio.ISound;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.raphimc.noteblocklib.data.MinecraftInstrument;
import net.raphimc.noteblocklib.format.nbs.model.NbsCustomInstrument;
import net.raphimc.noteblocklib.model.Note;

import java.io.File;

public class InstrumentConverter {
    public static ISound getInstrument(Note note, double x, double y, double z) {
        if (note.getInstrument() instanceof MinecraftInstrument) {
            MinecraftInstrument instrument = (MinecraftInstrument) note.getInstrument();
            SoundEvent event = getSoundEventForId(instrument.mcId());
            return new SoundEventInstance(event, note.getVolume(), note.getPitch(), x, y, z);
        } else if (note.getInstrument() instanceof NbsCustomInstrument) {
            NbsCustomInstrument instrument = (NbsCustomInstrument) note.getInstrument();
            String file = instrument.getSoundFilePathOr("").replace(File.separatorChar, '/');
            if (file.endsWith(".ogg")) {
                file = file.substring(0, file.length() - 4);
            }
            if (file.equalsIgnoreCase("pling")) {
                return new SoundEventInstance(SoundEvents.BLOCK_NOTE_PLING, note.getVolume(), note.getPitch(), x, y, z);
            }
            ResourceLocation sound = parseSoundFile(file);
            if (sound != null) {
                return new SoundDirectInstance(sound, note.getVolume(), note.getPitch(), x, y, z);
            }
            SoundEvent event = parseSoundName(instrument.getNameOr(""));
            if (event != null) {
                return new SoundEventInstance(event, note.getVolume(), note.getPitch(), x, y, z);
            }
            CommonData.LOGGER.warn("Failed to parse custom instrument: name={}, file={}", instrument.getNameOr(""), file);
            return new SoundEventInstance(SoundEvents.BLOCK_NOTE_HARP, note.getVolume(), note.getPitch(), x, y, z);
        } else {
            return new SoundEventInstance(SoundEvents.BLOCK_NOTE_HARP, note.getVolume(), note.getPitch(), x, y, z);
        }
    }

    private static SoundEvent getSoundEventForId(int mcId) {
        switch (mcId) {
            case 0: return SoundEvents.BLOCK_NOTE_HARP;
            case 1: return SoundEvents.BLOCK_NOTE_BASEDRUM;
            case 2: return SoundEvents.BLOCK_NOTE_SNARE;
            case 3: return SoundEvents.BLOCK_NOTE_HAT;
            case 4: return SoundEvents.BLOCK_NOTE_BASS;
            case 5: return SoundEvents.BLOCK_NOTE_FLUTE;
            case 6: return SoundEvents.BLOCK_NOTE_BELL;
            case 7: return SoundEvents.BLOCK_NOTE_GUITAR;
            case 8: return SoundEvents.BLOCK_NOTE_CHIME;
            case 9: return SoundEvents.BLOCK_NOTE_XYLOPHONE;
            case 15: return SoundEvents.BLOCK_NOTE_PLING;
            default: return SoundEvents.BLOCK_NOTE_HARP;
        }
    }

    private static ResourceLocation parseSoundFile(String file) {
        if (file.contains(":")) {
            String[] split = file.split(":", 2);
            return new ResourceLocation(split[0], split[1]);
        }
        return new ResourceLocation("minecraft", file);
    }

    private static SoundEvent parseSoundName(String name) {
        if (name == null || name.isEmpty()) return null;
        ResourceLocation res = parseSoundFile(name);
        return SoundEvent.REGISTRY.getObject(res);
    }
}
