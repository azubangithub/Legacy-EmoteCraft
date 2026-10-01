package io.github.kosmx.emotes.gui;

import io.github.kosmx.emotes.gui.widget.FastChooseWheelWidget;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class FastMenuScreen extends GuiScreen {
    protected final GuiScreen parent;
    protected FastChooseWheelWidget fastMenu;
    private static int currentPage = 0;

    public FastMenuScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        int size = (int) Math.min(this.width * 0.70, this.height * 0.70);
        int centerX = (this.width - size) / 2;
        int centerY = (this.height - size) / 2 - 10;

        this.fastMenu = new FastChooseWheelWidget(centerX, centerY, size, false);
        this.fastMenu.page = currentPage;

        int btnWidth = 140;
        int btnHeight = 20;
        int bottomY = this.height - 25;

        // Screenshot 1: Single "All Emotes" button at bottom center
        this.buttonList.add(new GuiButton(1, (this.width - btnWidth) / 2, bottomY, btnWidth, btnHeight, I18n.format("emotecraft.emotelist")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            this.mc.displayGuiScreen(new FullMenuScreen(this));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        if (!ClientPacketManager.isRemoteAvailable()) {
            String warning = I18n.format("emotecraft.no_server");
            this.drawCenteredString(this.fontRenderer, warning, this.width / 2, 8, 0xFFFFAA);
        }

        if (this.fastMenu != null) {
            currentPage = this.fastMenu.page;
            this.fastMenu.render(mouseX, mouseY, partialTicks);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.fastMenu != null) {
            boolean handled = this.fastMenu.mouseClicked(mouseX, mouseY, mouseButton);
            if (handled && mouseButton == 0) {
                // If an emote slice was clicked, close screen
                if (this.fastMenu.getActivePart(mouseX, mouseY) != null) {
                    this.mc.displayGuiScreen(null);
                }
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0 && this.fastMenu != null) {
            this.fastMenu.handleMouseWheel(dWheel);
            currentPage = this.fastMenu.page;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 || keyCode == KeyBindings.KEY_OPEN_MENU.getKeyCode()) {
            this.mc.displayGuiScreen(this.parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
