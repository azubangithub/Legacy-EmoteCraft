package io.github.kosmx.emotes.server.serializer;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.server.serializer.type.BinaryFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class EmoteSerializerTest {

    @Test
    public void testReadJsonEmoteAndBinaryRoundTrip() throws Exception {
        // Read waving.json from classpath
        try (InputStream stream = getClass().getResourceAsStream("/assets/emotecraft/emotes/waving.json")) {
            Assertions.assertNotNull(stream, "waving.json should exist in assets");
            
            List<Animation> emotes = UniversalEmoteSerializer.readData(stream, "waving.json");
            Assertions.assertFalse(emotes.isEmpty(), "Emote list should not be empty");
            Animation waving = emotes.get(0);
            Assertions.assertNotNull(waving);
            Assertions.assertTrue(waving.length() > 0, "Animation should have non-zero length");
            Assertions.assertTrue(waving.boneAnimations().containsKey("right_arm"), "Animation should have right_arm animated");

            // Test writing to .emotecraft binary format
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BinaryFormat binaryFormat = new BinaryFormat();
            binaryFormat.write(waving, out, "waving.emotecraft");
            byte[] binaryBytes = out.toByteArray();
            Assertions.assertTrue(binaryBytes.length > 0, "Binary output should not be empty");

            // Test reading back from binary format
            ByteArrayInputStream in = new ByteArrayInputStream(binaryBytes);
            List<Animation> readBackList = UniversalEmoteSerializer.readData(in, "waving.emotecraft");
            Assertions.assertEquals(1, readBackList.size(), "Should read exactly 1 animation from binary");
            Animation readBack = readBackList.get(0);
            Assertions.assertEquals(waving.length(), readBack.length(), 0.01f, "Animation length should match after round-trip");
            Assertions.assertTrue(readBack.boneAnimations().containsKey("right_arm"), "Round-trip should retain right_arm");
        }
    }
}
