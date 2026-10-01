package io.github.kosmx.emotes.main.emotePlay;

import io.github.kosmx.emotes.common.nbsplayer.NbsPlayer;
import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.model.Song;
import net.raphimc.noteblocklib.util.TimerHack;

public class MinecraftNbsPlayer extends NbsPlayer {
    protected final AbstractClientPlayer player;

    public MinecraftNbsPlayer(AbstractClientPlayer player, Song song) {
        super(song);
        this.player = player;
    }

    @Override
    public void start(int delay, int tick) {
        TimerHack.ENABLED = false;
        super.start(delay, tick);
    }

    @Override
    protected boolean preTick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != this.player.world) {
            stop();
            return false;
        }
        return !mc.isGamePaused();
    }

    public ITextComponent getNowPlaying() {
        String author = getSong().getAuthorOr(getSong().getOriginalAuthorOr(""));
        String name = getSong().getTitleOrFileNameOr("");

        if (author.isEmpty()) {
            if (!name.isEmpty()) {
                return new TextComponentString(name);
            } else {
                return null;
            }
        } else if (!name.isEmpty()) {
            return new TextComponentString(String.format("%s - %s", author, name));
        }

        return null;
    }

    @Override
    protected void playNote(Note note) {
        ISound sound = InstrumentConverter.getInstrument(note, this.player.posX, this.player.posY, this.player.posZ);
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (this.player instanceof IPlayerEntity) {
                ((IPlayerEntity) this.player).emotecraft$playRawSound(sound);
            }
        });
    }
}
