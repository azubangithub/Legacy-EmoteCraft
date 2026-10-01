package io.github.kosmx.emotes.gui;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The "Play Emote" screen — shows a searchable list of all emotes.
 * Matches the original mod's newer design with name, description, and author displayed
 * in each list entry, plus player model icon thumbnails.
 */
public class FullMenuScreen extends GuiScreen {
    protected final GuiScreen parent;
    private EmoteListSlot listSlot;
    private GuiTextField searchField;
    private final List<EmoteHolder> filteredEmotes = new ArrayList<>();
    private int selectedIndex = -1;

    public FullMenuScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        updateFilteredList();

        // Full-width list, leaving margins
        int listTop = 36;
        int listBottom = this.height - 32;
        this.listSlot = new EmoteListSlot(this.mc, this.width, this.height, listTop, listBottom, 52);

        // Search field spans full width matching modern menu
        int searchWidth = Math.max(200, this.width - 40);
        this.searchField = new GuiTextField(0, this.fontRenderer, (this.width - searchWidth) / 2, 10, searchWidth, 18);
        this.searchField.setMaxStringLength(60);
        this.searchField.setFocused(false);

        // Bottom buttons
        int btnWidth = 140;
        int btnHeight = 20;
        int bottomY = this.height - 28;
        int gap = 6;

        // Two buttons: Cancel and Config Emotes
        int totalWidth = btnWidth * 2 + gap;
        int startX = (this.width - totalWidth) / 2;

        this.buttonList.add(new GuiButton(1, startX, bottomY, btnWidth, btnHeight, I18n.format("gui.cancel")));
        this.buttonList.add(new GuiButton(2, startX + btnWidth + gap, bottomY, btnWidth, btnHeight, I18n.format("emotecraft.config_emotes")));
    }

    private void updateFilteredList() {
        filteredEmotes.clear();
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        for (EmoteHolder holder : EmoteHolder.list) {
            if (holder != null && holder.name != null) {
                String name = holder.name.getUnformattedText().toLowerCase();
                String author = holder.author != null ? holder.author.getUnformattedText().toLowerCase() : "";
                if (query.isEmpty() || name.contains(query) || author.contains(query)) {
                    filteredEmotes.add(holder);
                }
            }
        }
        java.text.Collator collator = java.text.Collator.getInstance();
        filteredEmotes.sort((a, b) -> {
            String nameA = a.name != null ? a.name.getUnformattedText() : "";
            String nameB = b.name != null ? b.name.getUnformattedText() : "";
            return collator.compare(nameA, nameB);
        });
        if (selectedIndex >= filteredEmotes.size()) {
            selectedIndex = filteredEmotes.size() - 1;
        }
    }

    private EmoteHolder getSelectedEmote() {
        if (selectedIndex >= 0 && selectedIndex < filteredEmotes.size()) {
            return filteredEmotes.get(selectedIndex);
        }
        return null;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            // Cancel
            this.mc.displayGuiScreen(this.parent);
        } else if (button.id == 2) {
            // Config Emotes
            this.mc.displayGuiScreen(new ConfigEmotesScreen(this));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.listSlot.drawScreen(mouseX, mouseY, partialTicks);

        // Search field
        this.searchField.drawTextBox();
        if (this.searchField.getText().isEmpty() && !this.searchField.isFocused()) {
            this.fontRenderer.drawStringWithShadow(
                    I18n.format("emotecraft.search"),
                    this.searchField.x + 4,
                    this.searchField.y + 5,
                    0x888888
            );
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {
            updateFilteredList();
            return;
        }

        if (keyCode == org.lwjgl.input.Keyboard.KEY_F5) {
            io.github.kosmx.emotes.main.EmoteReloader.reloadEmotes(true, this::updateFilteredList);
            return;
        }

        if (keyCode == 1) {
            this.mc.displayGuiScreen(this.parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.searchField != null) {
            this.searchField.updateCursorCounter();
        }
        io.github.kosmx.emotes.main.EmoteWatcher.checkAndTick(this::updateFilteredList);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.listSlot != null) {
            this.listSlot.handleMouseInput();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * Inner class for the scrollable emote list.
     * Each entry shows: icon | name | description | author (in orange)
     */
    class EmoteListSlot extends GuiSlot {
        public EmoteListSlot(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn) {
            super(mcIn, width, height, topIn, bottomIn, slotHeightIn);
        }

        @Override
        public int getListWidth() {
            return this.width - 40;
        }

        @Override
        protected int getScrollBarX() {
            return this.width - 12;
        }

        @Override
        protected int getSize() {
            return filteredEmotes.size();
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            selectedIndex = slotIndex;

            if (isDoubleClick) {
                EmoteHolder holder = getSelectedEmote();
                if (holder != null) {
                    holder.playEmote();
                    mc.displayGuiScreen(null);
                }
            }
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return slotIndex == selectedIndex;
        }

        @Override
        protected void drawBackground() {}

        @Override
        protected void drawContainerBackground(net.minecraft.client.renderer.Tessellator tessellator) {}

        @Override
        protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
            net.minecraft.client.gui.Gui.drawRect(this.left, startY, this.right, endY, 0xD0101010);
        }

        @Override
        protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float partialTicks) {
            if (entryID < 0 || entryID >= filteredEmotes.size()) return;
            EmoteHolder holder = filteredEmotes.get(entryID);

            int iconSize = 40;
            int textX = insideLeft + iconSize + 8;
            int nameColor = isSelected(entryID) ? 0xFFFF55 : 0xFFFFFF;

            // Draw icon
            ResourceLocation icon = holder.getIconIdentifier();
            if (icon != null) {
                mc.getTextureManager().bindTexture(icon);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                Gui.drawScaledCustomSizeModalRect(insideLeft + 2, yPos + 4, 0.0F, 0.0F, 256, 256, iconSize, iconSize, 256, 256);
            }

            // Draw name (bold-style with shadow)
            mc.fontRenderer.drawStringWithShadow(holder.name.getFormattedText(), textX, yPos + 4, nameColor);

            // Draw description (gray, truncated)
            String desc = holder.description != null ? holder.description.getUnformattedText() : "";
            if (!desc.isEmpty()) {
                // Truncate if too long
                int maxDescWidth = FullMenuScreen.this.width - textX - 30;
                String trimmed = mc.fontRenderer.trimStringToWidth(desc, maxDescWidth);
                mc.fontRenderer.drawStringWithShadow(trimmed, textX, yPos + 16, 0xAAAAAA);
            }

            // Draw author (orange)
            String author = holder.author != null ? holder.author.getUnformattedText() : "";
            if (!author.isEmpty()) {
                String authorLine = I18n.format("emotecraft.emote.author") + author;
                mc.fontRenderer.drawStringWithShadow(authorLine, textX, yPos + 28, 0xFF8800);
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
