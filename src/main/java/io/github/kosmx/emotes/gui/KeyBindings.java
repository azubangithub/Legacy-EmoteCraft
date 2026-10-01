package io.github.kosmx.emotes.gui;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindings {
    public static final String CATEGORY = "category.emotecraft.keybinding";
    public static KeyBinding KEY_OPEN_MENU;
    public static KeyBinding KEY_STOP_EMOTE;
    public static KeyBinding KEY_RELOAD_EMOTES;

    public static void init() {
        KEY_OPEN_MENU = new KeyBinding("key.emotecraft.fastchoose", Keyboard.KEY_B, CATEGORY);
        KEY_STOP_EMOTE = new KeyBinding("key.emotecraft.stop", Keyboard.KEY_NONE, CATEGORY);
        KEY_RELOAD_EMOTES = new KeyBinding("key.emotecraft.reload", Keyboard.KEY_NONE, CATEGORY);

        ClientRegistry.registerKeyBinding(KEY_OPEN_MENU);
        ClientRegistry.registerKeyBinding(KEY_STOP_EMOTE);
        ClientRegistry.registerKeyBinding(KEY_RELOAD_EMOTES);
    }
}
