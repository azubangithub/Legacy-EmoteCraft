package com.zigythebird.playeranimcore.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.zigythebird.playeranimcore.enums.AnimationFormat;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ExtraAnimationData {
    private final Map<String, Object> data;

    public static final String NAME_KEY = "name";
    public static final String UUID_KEY = "uuid";
    public static final String FORMAT_KEY = "format";
    public static final String BEGIN_TICK_KEY = "beginTick";
    public static final String END_TICK_KEY = "endTick";
    public static final String EASING_BEFORE_KEY = "easeBeforeKeyframe";
    public static final String APPLY_BEND_TO_OTHER_BONES_KEY = "applyBendToOtherBones";

    public ExtraAnimationData(Map<String, Object> data) {
        this.data = data;
    }

    public ExtraAnimationData(String key, Object value) {
        this(new HashMap<>(Collections.singletonMap(key, value)));
    }

    public ExtraAnimationData() {
        this(new HashMap<>(1)); // Mutable, 1 for name
    }

    public Map<String, Object> data() {
        return this.data;
    }

    public Map<String, Object> getData() {
        return this.data;
    }

    @Nullable
    public String name() {
        Object d = data().get(NAME_KEY);
        String name;
        if (d instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) d;
            name = jsonObject.get("fallback").getAsString();
        } else {
            name = (String) d;
        }
        return name != null ? name.toLowerCase(Locale.ROOT).replace("\"", "").replace(" ", "_") : null;
    }

    public boolean has(String name) {
        return data().containsKey(name);
    }

    public Object getRaw(String name) {
        return data().get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        Object obj = getRaw(key);
        if (obj == null) return Optional.empty();

        try {
            return Optional.of((T) obj);
        } catch (Throwable ignored) {}

        return Optional.empty();
    }

    public <T> T getNullable(String key) {
        return this.<T>get(key).orElse(null);
    }

    public List<?> getList(String key) {
        Object obj = getRaw(key);
        if (obj == null) return Collections.emptyList();
        if (obj instanceof JsonArray) {
            JsonArray json = (JsonArray) obj;
            List<JsonElement> list = new ArrayList<>();
            for (JsonElement el : json) list.add(el);
            return list;
        }
        if (obj instanceof List<?>) {
            return (List<?>) obj;
        }
        throw new ClassCastException(obj.getClass().getName());
    }

    public void put(String name, Object object) {
        data.put(name, object);
    }

    public void fromJson(JsonObject node, boolean root) {
        for (Map.Entry<String, JsonElement> entry : node.entrySet()) {
            String key = entry.getKey();
            if (root && ("version".equalsIgnoreCase(key) || "emote".equalsIgnoreCase(key))) continue;
            data().put(key, getValue(entry.getValue()));
        }
    }

    public Object getValue(JsonElement element) {
        if (element instanceof JsonPrimitive) {
            JsonPrimitive p = (JsonPrimitive) element;
            if (p.isBoolean()) {
                return p.getAsBoolean();
            } else if (p.isString()) {
                return p.getAsString();
            } else if (p.isNumber()) {
                return p.getAsFloat();
            }
        }
        if (element instanceof JsonArray) {
            JsonArray array = (JsonArray) element;
            List<Object> list = new ArrayList<>(array.size());
            for (JsonElement element1 : array) {
                list.add(getValue(element1));
            }
            return list;
        }
        return element.toString();
    }

    public ExtraAnimationData copy() {
        return new ExtraAnimationData(new HashMap<>(data()));
    }

    public boolean isDisableAxisIfNotModified() {
        return this.<Boolean>get("disableAxisIfNotModified").orElse(true);
    }

    public boolean isAnimationPlayerAnimatorFormat() {
        return this.<AnimationFormat>get(ExtraAnimationData.FORMAT_KEY).orElse(null) == AnimationFormat.PLAYER_ANIMATOR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtraAnimationData)) return false;
        ExtraAnimationData that = (ExtraAnimationData) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "ExtraAnimationData{" +
                "data=" + data +
                '}';
    }
}
