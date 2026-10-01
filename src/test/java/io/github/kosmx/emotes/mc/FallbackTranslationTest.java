package io.github.kosmx.emotes.mc;

import net.minecraft.util.text.ITextComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FallbackTranslationTest {

    @Test
    public void testFallbackWhenUntranslated() {
        String json = "{\"translate\":\"spemotes.emote.name.SPE_Apologize\",\"fallback\":\"Извиниться\"}";
        ITextComponent component = McUtils.fromJson(json);

        assertNotNull(component);
        // Since spemotes key is not in language map, it should return the fallback
        assertEquals("Извиниться", component.getUnformattedComponentText());
    }

    @Test
    public void testRegularString() {
        String name = "Waving";
        ITextComponent component = McUtils.fromJson(name);

        assertNotNull(component);
        assertEquals("Waving", component.getUnformattedComponentText());
    }

    @Test
    public void testJsonWithoutFallback() {
        String json = "{\"text\":\"Hello World\"}";
        ITextComponent component = McUtils.fromJson(json);

        assertNotNull(component);
        assertEquals("Hello World", component.getUnformattedComponentText());
    }
}
