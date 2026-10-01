package io.github.kosmx.emotes.forge;

import io.github.kosmx.emotes.main.EmoteReloader;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandEmotesReload extends CommandBase {

    @Override
    public String getName() {
        return "emotes";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("emotecraft");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/emotes reload";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Available to all players on client
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            EmoteReloader.reloadEmotes(true, null);
        } else {
            sender.sendMessage(new TextComponentString("Usage: /emotes reload"));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "reload");
        }
        return Collections.emptyList();
    }
}
