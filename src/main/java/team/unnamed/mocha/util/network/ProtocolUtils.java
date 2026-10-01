package team.unnamed.mocha.util.network;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ProtocolUtils {

    public static <T> List<T> readList(ByteBuf buf, Function<ByteBuf, T> reader) {
        int size = VarIntUtils.readVarInt(buf);
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(reader.apply(buf));
        }
        return list;
    }

    public static <T> void writeList(ByteBuf buf, List<T> list, BiConsumer<T, ByteBuf> writer) {
        VarIntUtils.writeVarInt(buf, list.size());
        for (T item : list) {
            writer.accept(item, buf);
        }
    }

    public static void writeEnum(Enum<?> enumVal, ByteBuf buf) {
        buf.writeByte(enumVal.ordinal());
    }

    public static <T> T readEnum(Class<T> enumClass, ByteBuf buf) {
        int ordinal = buf.readUnsignedByte();
        T[] constants = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= constants.length) {
            return constants[0];
        }
        return constants[ordinal];
    }

    public static String readString(ByteBuf buf) {
        int length = VarIntUtils.readVarInt(buf);
        if (length <= 0) return null;
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeString(ByteBuf buf, String s) {
        if (s == null) {
            VarIntUtils.writeVarInt(buf, 0);
            return;
        }
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        VarIntUtils.writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }
}
