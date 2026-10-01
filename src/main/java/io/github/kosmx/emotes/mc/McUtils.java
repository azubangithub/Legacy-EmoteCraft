package io.github.kosmx.emotes.mc;

import com.google.gson.JsonElement;
import io.github.kosmx.emotes.common.CommonData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class McUtils {
    public static final ITextComponent MOD_NAME = new TextComponentString(CommonData.MOD_NAME);
    public static final ITextComponent SLASH = new TextComponentString("/");
    public static final ITextComponent BACK = new TextComponentString("<");
    static {
        BACK.getStyle().setBold(true);
    }

    public static ITextComponent fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new TextComponentString("");
        }
        try {
            JsonElement element = new JsonParser().parse(json);
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("translate")) {
                    String key = obj.get("translate").getAsString();
                    String fallback = obj.has("fallback") ? obj.get("fallback").getAsString() : null;
                    if (fallback != null) {
                        FallbackTranslationTextComponent comp = new FallbackTranslationTextComponent(key, fallback);
                        try {
                            ITextComponent parsed = ITextComponent.Serializer.jsonToComponent(json);
                            if (parsed != null && parsed.getStyle() != null) {
                                comp.setStyle(parsed.getStyle());
                            }
                        } catch (Throwable ignored) {}
                        return comp;
                    }
                } else if (obj.has("fallback") && !obj.has("text")) {
                    TextComponentString comp = new TextComponentString(obj.get("fallback").getAsString());
                    try {
                        ITextComponent parsed = ITextComponent.Serializer.jsonToComponent(json);
                        if (parsed != null && parsed.getStyle() != null) {
                            comp.setStyle(parsed.getStyle());
                        }
                    } catch (Throwable ignored) {}
                    return comp;
                }
            }
            ITextComponent component = ITextComponent.Serializer.jsonToComponent(json);
            return component != null ? component : new TextComponentString(json);
        } catch (Throwable t) {
            return new TextComponentString(json);
        }
    }

    public static ITextComponent fromJson(Object obj) {
        if (obj == null) {
            return new TextComponentString("");
        } else if (obj instanceof String) {
            return fromJson((String) obj);
        } else if (obj instanceof JsonElement) {
            return fromJson(((JsonElement) obj).toString());
        } else {
            throw new IllegalArgumentException("Cannot create Text from " + obj.getClass().getName());
        }
    }

    public static ResourceLocation newIdentifier(String path) {
        return new ResourceLocation(CommonData.MOD_ID, path);
    }
}
