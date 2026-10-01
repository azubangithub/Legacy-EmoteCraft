package io.github.kosmx.emotes.gui;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.common.SerializableConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen matching Screenshot 4 ("Other Options"):
 * Title: "Other Options"
 * Scrollable list of config toggle buttons with category headers:
 * - "Basic Options"
 * - "Expert Options"
 * Bottom buttons: "Reset", "Done", "Export Menu"
 */
public class ModOptionsScreen extends GuiScreen {
    private static final net.minecraft.util.ResourceLocation BUTTON_TEXTURES = new net.minecraft.util.ResourceLocation("textures/gui/widgets.png");
    protected final GuiScreen parent;
    private OptionsListSlot optionsSlot;
    private final List<OptionRow> rows = new ArrayList<>();
    private String hoveredTooltip = null;

    public ModOptionsScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        buildRows();

        int top = 32;
        int bottom = this.height - 32;
        this.optionsSlot = new OptionsListSlot(this.mc, this.width, this.height, top, bottom, 24);

        int btnW = 100;
        int btnH = 20;
        int bottomY = this.height - 25;
        int gap = 10;
        int totalW = btnW * 3 + gap * 2;
        int startX = (this.width - totalW) / 2;

        this.buttonList.add(new GuiButton(1, startX, bottomY, btnW, btnH, I18n.format("controls.reset")));
        this.buttonList.add(new GuiButton(2, startX + btnW + gap, bottomY, btnW, btnH, I18n.format("gui.done")));
        GuiButton exportBtn = new GuiButton(3, startX + (btnW + gap) * 2, bottomY, btnW, btnH, I18n.format("emotecraft.options.export", "Export Menu"));
        exportBtn.enabled = false;
        this.buttonList.add(exportBtn);
    }

    private void buildRows() {
        rows.clear();

        // Basic Options category
        rows.add(new HeaderRow(I18n.format("emotecraft.otherconfig.category.general")));
        PlatformTools.getConfig().iterateGeneral(entry -> {
            if (entry.showEntry() || PlatformTools.getConfig().showHiddenConfig.get()) {
                if (entry.get() instanceof Boolean) {
                    //noinspection unchecked
                    rows.add(new BooleanOptionRow((SerializableConfig.ConfigEntry<Boolean>) entry));
                } else if (entry instanceof SerializableConfig.FloatConfigEntry) {
                    rows.add(new FloatOptionRow((SerializableConfig.FloatConfigEntry) entry));
                }
            }
        });

        // Expert Options category
        rows.add(new HeaderRow(I18n.format("emotecraft.otherconfig.category.expert")));
        PlatformTools.getConfig().iterateExpert(entry -> {
            if (entry.showEntry() || PlatformTools.getConfig().showHiddenConfig.get()) {
                if (entry.get() instanceof Boolean) {
                    //noinspection unchecked
                    rows.add(new BooleanOptionRow((SerializableConfig.ConfigEntry<Boolean>) entry));
                } else if (entry instanceof SerializableConfig.FloatConfigEntry) {
                    rows.add(new FloatOptionRow((SerializableConfig.FloatConfigEntry) entry));
                }
            }
        });
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            // Reset to default
            PlatformTools.getConfig().iterate(SerializableConfig.ConfigEntry::resetToDefault);
            PlatformTools.saveConfig();
            buildRows();
        } else if (button.id == 2) {
            // Done
            PlatformTools.saveConfig();
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.hoveredTooltip = null;

        if (this.optionsSlot != null) {
            this.optionsSlot.drawScreen(mouseX, mouseY, partialTicks);
        }

        this.drawCenteredString(this.fontRenderer, I18n.format("emotecraft.otherconfig"), this.width / 2, 12, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw tooltip if hovered
        if (this.hoveredTooltip != null && !this.hoveredTooltip.isEmpty()) {
            String processed = this.hoveredTooltip.replace("\\n", "\n");
            java.util.List<String> lines = new java.util.ArrayList<>();
            for (String part : processed.split("\n", -1)) {
                if (part.isEmpty()) {
                    lines.add("");
                } else {
                    lines.addAll(this.fontRenderer.listFormattedStringToWidth(part, 220));
                }
            }
            this.drawHoveringText(lines, mouseX, mouseY);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            PlatformTools.saveConfig();
            this.mc.displayGuiScreen(this.parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.optionsSlot != null) {
            this.optionsSlot.handleMouseInput();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.optionsSlot != null && mouseY >= this.optionsSlot.top && mouseY <= this.optionsSlot.bottom) {
            int slotIndex = this.optionsSlot.getSlotIndexFromScreenCoords(mouseX, mouseY);
            if (slotIndex >= 0 && slotIndex < rows.size()) {
                rows.get(slotIndex).mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    interface OptionRow {
        void render(int index, int left, int y, int rowWidth, int rowHeight, int mouseX, int mouseY);
        boolean mouseClicked(int mouseX, int mouseY, int mouseButton);
    }

    class HeaderRow implements OptionRow {
        final String title;

        HeaderRow(String title) {
            this.title = title;
        }

        @Override
        public void render(int index, int left, int y, int rowWidth, int rowHeight, int mouseX, int mouseY) {
            mc.fontRenderer.drawStringWithShadow(title, left + 4, y + 8, 0xFFFFAA);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
            return false;
        }
    }

    class BooleanOptionRow implements OptionRow {
        final SerializableConfig.ConfigEntry<Boolean> entry;
        final String label;

        BooleanOptionRow(SerializableConfig.ConfigEntry<Boolean> entry) {
            this.entry = entry;
            String key = "emotecraft.otherconfig." + entry.getName();
            this.label = I18n.hasKey(key) ? I18n.format(key) : entry.getName();
        }

        String getDisplayText() {
            String state = entry.get() ? I18n.format("options.on") : I18n.format("options.off");
            return label + ": " + state;
        }

        @Override
        public void render(int index, int left, int y, int rowWidth, int rowHeight, int mouseX, int mouseY) {
            int btnW = Math.min(310, rowWidth - 20);
            int btnX = left + (rowWidth - btnW) / 2;
            int btnY = y + 2;
            int btnH = 20;

            boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;

            // Draw button background
            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int textureY = hovered ? 2 : 1;
            drawTexturedModalRect(btnX, btnY, 0, 46 + textureY * 20, btnW / 2, btnH);
            drawTexturedModalRect(btnX + btnW / 2, btnY, 200 - btnW / 2, 46 + textureY * 20, btnW / 2, btnH);

            int textColor = hovered ? 0xFFFFA0 : 0xE0E0E0;
            drawCenteredString(mc.fontRenderer, getDisplayText(), btnX + btnW / 2, btnY + (btnH - 8) / 2, textColor);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (hovered && entry.hasTooltip) {
                String tooltipKey = "emotecraft.otherconfig." + entry.getName() + ".tooltip";
                if (I18n.hasKey(tooltipKey)) {
                    hoveredTooltip = I18n.format(tooltipKey);
                }
            }
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
            int rowWidth = optionsSlot.getListWidth();
            int btnW = Math.min(310, rowWidth - 20);
            int btnX = (width - btnW) / 2;
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseButton == 0) {
                entry.set(!entry.get());
                PlatformTools.saveConfig();
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(
                        net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1.0F
                ));
                return true;
            }
            return false;
        }
    }

    class FloatOptionRow implements OptionRow {
        final SerializableConfig.FloatConfigEntry entry;
        final String label;
        private boolean dragging = false;

        FloatOptionRow(SerializableConfig.FloatConfigEntry entry) {
            this.entry = entry;
            String key = "emotecraft.otherconfig." + entry.getName();
            this.label = I18n.hasKey(key) ? I18n.format(key) : entry.getName();
        }

        String getDisplayText() {
            java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
            return label + ": " + df.format(entry.getTextVal());
        }

        @Override
        public void render(int index, int left, int y, int rowWidth, int rowHeight, int mouseX, int mouseY) {
            int btnW = Math.min(310, rowWidth - 20);
            int btnX = left + (rowWidth - btnW) / 2;
            int btnY = y + 2;
            int btnH = 20;

            boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;

            if (dragging) {
                if (org.lwjgl.input.Mouse.isButtonDown(0)) {
                    double pct = net.minecraft.util.math.MathHelper.clamp((double) (mouseX - btnX) / (double) btnW, 0.0, 1.0);
                    double val = entry.min + pct * (entry.max - entry.min);
                    entry.setConfigVal(val);
                } else {
                    dragging = false;
                    PlatformTools.saveConfig();
                }
            }

            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            // Draw track (button background)
            drawTexturedModalRect(btnX, btnY, 0, 46, btnW / 2, btnH);
            drawTexturedModalRect(btnX + btnW / 2, btnY, 200 - btnW / 2, 46, btnW / 2, btnH);

            // Draw slider thumb
            double pct = net.minecraft.util.math.MathHelper.clamp((entry.getConfigVal() - entry.min) / (entry.max - entry.min), 0.0, 1.0);
            int thumbX = btnX + (int) (pct * (btnW - 8));
            drawTexturedModalRect(thumbX, btnY, 0, 66, 4, 20);
            drawTexturedModalRect(thumbX + 4, btnY, 196, 66, 4, 20);

            int textColor = hovered ? 0xFFFFA0 : 0xE0E0E0;
            drawCenteredString(mc.fontRenderer, getDisplayText(), btnX + btnW / 2, btnY + (btnH - 8) / 2, textColor);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (hovered && entry.hasTooltip) {
                String tooltipKey = "emotecraft.otherconfig." + entry.getName() + ".tooltip";
                if (I18n.hasKey(tooltipKey)) {
                    hoveredTooltip = I18n.format(tooltipKey);
                }
            }
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
            int rowWidth = optionsSlot.getListWidth();
            int btnW = Math.min(310, rowWidth - 20);
            int btnX = (width - btnW) / 2;
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseButton == 0) {
                dragging = true;
                double pct = net.minecraft.util.math.MathHelper.clamp((double) (mouseX - btnX) / (double) btnW, 0.0, 1.0);
                double val = entry.min + pct * (entry.max - entry.min);
                entry.setConfigVal(val);
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(
                        net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1.0F
                ));
                return true;
            }
            return false;
        }
    }

    class OptionsListSlot extends GuiSlot {
        public OptionsListSlot(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn) {
            super(mcIn, width, height, topIn, bottomIn, slotHeightIn);
            this.setHasListHeader(false, 0);
        }

        @Override
        protected int getSize() {
            return rows.size();
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            // Handled in ModOptionsScreen.mouseClicked to prevent duplicate event invocation
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return false;
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
        public int getListWidth() {
            return Math.min(330, width - 20);
        }

        @Override
        protected int getScrollBarX() {
            return (width + getListWidth()) / 2 + 6;
        }

        @Override
        protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float partialTicks) {
            if (entryID >= 0 && entryID < rows.size()) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                rows.get(entryID).render(entryID, insideLeft, yPos, getListWidth(), insideSlotHeight, mouseXIn, mouseYIn);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }
}
