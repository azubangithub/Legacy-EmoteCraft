package io.github.kosmx.emotes.mc;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.translation.I18n;

public class FallbackTranslationTextComponent extends TextComponentBase {
    private final String key;
    private final String fallback;
    private final Object[] formatArgs;

    public FallbackTranslationTextComponent(String key, String fallback, Object... formatArgs) {
        this.key = key;
        this.fallback = fallback;
        this.formatArgs = formatArgs != null ? formatArgs : new Object[0];
    }

    public String getKey() {
        return this.key;
    }

    public String getFallback() {
        return this.fallback;
    }

    public Object[] getFormatArgs() {
        return this.formatArgs;
    }

    @Override
    public String getUnformattedComponentText() {
        if (I18n.canTranslate(this.key)) {
            return I18n.translateToLocalFormatted(this.key, this.formatArgs);
        } else if (this.fallback != null && !this.fallback.isEmpty()) {
            if (this.formatArgs.length > 0) {
                try {
                    return String.format(this.fallback, this.formatArgs);
                } catch (Exception ignored) {
                    return this.fallback;
                }
            }
            return this.fallback;
        } else {
            return this.key;
        }
    }

    @Override
    public FallbackTranslationTextComponent createCopy() {
        Object[] copiedArgs = new Object[this.formatArgs.length];
        for (int i = 0; i < this.formatArgs.length; ++i) {
            if (this.formatArgs[i] instanceof ITextComponent) {
                copiedArgs[i] = ((ITextComponent) this.formatArgs[i]).createCopy();
            } else {
                copiedArgs[i] = this.formatArgs[i];
            }
        }
        FallbackTranslationTextComponent copy = new FallbackTranslationTextComponent(this.key, this.fallback, copiedArgs);
        copy.setStyle(this.getStyle().createShallowCopy());
        for (ITextComponent child : this.getSiblings()) {
            copy.appendSibling(child.createCopy());
        }
        return copy;
    }
}
