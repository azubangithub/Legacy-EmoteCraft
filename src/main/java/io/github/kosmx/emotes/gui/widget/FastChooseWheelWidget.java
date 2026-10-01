package io.github.kosmx.emotes.gui.widget;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FastChooseWheelWidget {
    private int x;
    private int y;
    private int size;
    private boolean hovered;
    public int page = 0;
    private boolean isEditor = false;
    private EmoteHolder selectedEmoteForConfig = null;

    private final List<FastChooseElement> elements = new ArrayList<>();
    private ResourceLocation texture;

    public FastChooseWheelWidget(int x, int y, int size) {
        this(x, y, size, false);
    }

    public FastChooseWheelWidget(int x, int y, int size, boolean isEditor) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.isEditor = isEditor;

        // Modern 8-slice wheel angles matching ModernChooseWheel
        elements.add(new FastChooseElement(0, 0.0f));
        elements.add(new FastChooseElement(1, 45.0f));
        elements.add(new FastChooseElement(2, 90.0f));
        elements.add(new FastChooseElement(3, 135.0f));
        elements.add(new FastChooseElement(4, 180.0f));
        elements.add(new FastChooseElement(5, 225.0f));
        elements.add(new FastChooseElement(6, 270.0f));
        elements.add(new FastChooseElement(7, 315.0f));
    }

    public void setPositionAndSize(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public void setSelectedEmoteForConfig(EmoteHolder holder) {
        this.selectedEmoteForConfig = holder;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    private void updateTexture() {
        boolean dark = PlatformTools.getConfig().dark.get();
        boolean old = PlatformTools.getConfig().oldChooseWheel.get();
        if (old) {
            this.texture = dark ? McUtils.newIdentifier("textures/gui/fastchoose_dark.png") : McUtils.newIdentifier("textures/gui/fastchoose_light.png");
        } else {
            this.texture = dark ? McUtils.newIdentifier("textures/gui/fastchoose_dark_new.png") : McUtils.newIdentifier("textures/gui/fastchoose_light_new.png");
        }
    }

    public FastChooseElement getActivePart(int mouseX, int mouseY) {
        int rx = mouseX - x - size / 2;
        int ry = mouseY - y - size / 2;

        double distanceFromCenter = Math.sqrt(rx * rx + ry * ry);
        if (distanceFromCenter < size * 0.17 || distanceFromCenter > size / 2.0) {
            return null;
        }

        double pi = Math.PI;
        float degrees = (float) (Math.abs(((Math.atan2(ry, rx) - pi) / (2 * pi)) * 360 - 270) % 360);
        int i = (int) Math.floor((degrees + 22.5) / 45.0) % 8;
        return i >= 0 && i < elements.size() ? elements.get(i) : null;
    }

    public int getPageButton(int mouseX, int mouseY) {
        int rx = mouseX - x - size / 2;
        int ry = mouseY - y - size / 2;
        double distanceFromCenter = Math.sqrt(rx * rx + ry * ry);
        if (distanceFromCenter < size * 0.17) {
            if (rx > 3) return 1;  // next
            if (rx < -3) return 0; // prev
        }
        return -1;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX <= x + size && mouseY <= y + size;
    }

    private void checkHovered(int mouseX, int mouseY) {
        this.hovered = isHovered(mouseX, mouseY);
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        updateTexture();
        checkHovered(mouseX, mouseY);

        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(texture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();

        // Draw wheel background (256x256 in a 512x512 texture)
        Gui.drawScaledCustomSizeModalRect(x, y, 0.0F, 0.0F, 256, 256, size, size, 512, 512);

        // Hover highlight
        if (this.hovered) {
            FastChooseElement part = getActivePart(mouseX, mouseY);
            if (part != null) {
                part.renderHover(texture);
            }
        }

        // Render emote icons/names in each slice
        for (FastChooseElement element : elements) {
            if (element.hasEmote()) {
                element.render();
            }
        }

        // Center page display: white text with shadow matching ModernChooseWheel
        String pageStr = String.valueOf(page + 1);
        int strWidth = mc.fontRenderer.getStringWidth(pageStr);
        int centerX = x + size / 2;
        int centerY = y + size / 2;

        mc.fontRenderer.drawStringWithShadow(pageStr, centerX - (float) strWidth / 2.0F, centerY - 4, -1);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        checkHovered(mouseX, mouseY);
        if (!this.hovered) return false;

        FastChooseElement element = getActivePart(mouseX, mouseY);
        if (element != null) {
            if (isEditor) {
                if (button == 0) {
                    // Set emote
                    if (selectedEmoteForConfig != null) {
                        element.setEmote(selectedEmoteForConfig);
                        PlatformTools.saveConfig();
                        return true;
                    }
                } else if (button == 1) {
                    // Right click clears
                    element.clearEmote();
                    PlatformTools.saveConfig();
                    return true;
                }
            } else {
                if (button == 0 && element.hasEmote()) {
                    EmoteHolder holder = element.getEmote();
                    if (holder != null) {
                        return holder.playEmote();
                    }
                }
            }
        } else {
            // Check page button in center
            int pageBtn = getPageButton(mouseX, mouseY);
            int maxPages = PlatformTools.getConfig().fastMenuEmotes.length;
            if (pageBtn == 0) {
                page = (page > 0) ? page - 1 : maxPages - 1;
                return true;
            } else if (pageBtn == 1) {
                page = (page < maxPages - 1) ? page + 1 : 0;
                return true;
            }
        }
        return false;
    }

    public void handleMouseWheel(int dWheel) {
        if (!PlatformTools.getConfig().scrollPage.get()) {
            return;
        }
        int maxPages = PlatformTools.getConfig().fastMenuEmotes.length;
        if (dWheel < 0) {
            page = (page < maxPages - 1) ? page + 1 : 0;
        } else if (dWheel > 0) {
            page = (page > 0) ? page - 1 : maxPages - 1;
        }
    }

    private void drawTextureSelect(ResourceLocation t, int px, int py, int u, int v, int w, int h) {
        Gui.drawScaledCustomSizeModalRect(
                x + px * size / 512,
                y + py * size / 512,
                (float) u, (float) v,
                w * 128, h * 128,
                w * size / 2, h * size / 2,
                512, 512
        );
    }

    public class FastChooseElement {
        public final int id;
        public final float angle;

        public FastChooseElement(int id, float angle) {
            this.id = id;
            this.angle = angle;
        }

        public boolean hasEmote() {
            if (page < 0 || page >= PlatformTools.getConfig().fastMenuEmotes.length) return false;
            return PlatformTools.getConfig().fastMenuEmotes[page][id] != null;
        }

        public EmoteHolder getEmote() {
            if (!hasEmote()) return null;
            UUID uuid = PlatformTools.getConfig().fastMenuEmotes[page][id];
            return uuid != null ? EmoteHolder.list.get(uuid) : null;
        }

        public void setEmote(EmoteHolder emote) {
            if (page >= 0 && page < PlatformTools.getConfig().fastMenuEmotes.length) {
                PlatformTools.getConfig().fastMenuEmotes[page][id] = emote == null ? null : emote.getUuid();
            }
        }

        public void clearEmote() {
            setEmote(null);
        }

        public void render() {
            Minecraft mc = Minecraft.getMinecraft();
            EmoteHolder holder = getEmote();
            if (holder == null) return;

            ResourceLocation icon = holder.getIconIdentifier();
            if (icon != null && PlatformTools.getConfig().showIcons.get()) {
                int s = size / 10;
                int iconX = (int) (((float) (x + size / 2)) + size * 0.36F * Math.sin(this.angle * Math.PI / 180.0)) - s;
                int iconY = (int) (((float) (y + size / 2)) + size * 0.36F * Math.cos(this.angle * Math.PI / 180.0)) - s;

                mc.getTextureManager().bindTexture(icon);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                Gui.drawScaledCustomSizeModalRect(iconX, iconY, 0.0F, 0.0F, 256, 256, s * 2, s * 2, 256, 256);
            } else {
                String text = holder.name.getFormattedText();
                float textX = (float) ((x + size / 2) + size * 0.38F * Math.sin(this.angle * Math.PI / 180.0));
                float textY = (float) ((y + size / 2) + size * 0.38F * Math.cos(this.angle * Math.PI / 180.0));
                int color = PlatformTools.getConfig().dark.get() ? 0xFFFFFF : 0x222222;
                int strWidth = mc.fontRenderer.getStringWidth(text);
                mc.fontRenderer.drawString(text, (int) (textX - strWidth / 2.0F), (int) (textY - 4), color);
            }
        }

        public void renderHover(ResourceLocation tex) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.getTextureManager().bindTexture(tex);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            boolean old = PlatformTools.getConfig().oldChooseWheel.get();
            if (old) {
                int textX = ((id & 1) == 0) ? 0 : 256;
                int textY = ((id & 1) == 0) ? 256 : 0;
                int offX = ((id & 4) == 0) ? 0 : 128;
                int offY = ((id & 2) == 0) ? 0 : 128;

                int drawX = x + offX * size / 256;
                int drawY = y + offY * size / 256;
                int quadSize = size / 2;

                Gui.drawScaledCustomSizeModalRect(drawX, drawY, (float) (textX + offX), (float) (textY + offY), 128, 128, quadSize, quadSize, 512, 512);
            } else {
                switch (id) {
                    case 0: drawTextureSelect(tex, 0, 256, 0, 384, 2, 1); break;
                    case 1: drawTextureSelect(tex, 256, 256, 384, 384, 1, 1); break;
                    case 2: drawTextureSelect(tex, 256, 0, 384, 0, 1, 2); break;
                    case 3: drawTextureSelect(tex, 256, 0, 384, 256, 1, 1); break;
                    case 4: drawTextureSelect(tex, 0, 0, 0, 256, 2, 1); break;
                    case 5: drawTextureSelect(tex, 0, 0, 256, 256, 1, 1); break;
                    case 6: drawTextureSelect(tex, 0, 0, 256, 0, 1, 2); break;
                    case 7: drawTextureSelect(tex, 0, 256, 256, 384, 1, 1); break;
                }
            }
        }
    }
}
