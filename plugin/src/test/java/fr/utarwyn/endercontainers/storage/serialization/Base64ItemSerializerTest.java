package fr.utarwyn.endercontainers.storage.serialization;

import fr.utarwyn.endercontainers.TestHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;

public class Base64ItemSerializerTest {

    private ItemSerializer serializer;

    @BeforeAll
    public static void setUpClass() {
        TestHelper.setUpServer();
    }

    @BeforeEach
    public void setUp() {
        this.serializer = new Base64ItemSerializer();
    }

    @Test
    public void serialize() throws IOException {
        ConcurrentMap<Integer, ItemStack> map = new ConcurrentHashMap<>();
        map.put(1, new ItemStack(Material.OAK_LOG, 10));
        map.put(17, new ItemStack(Material.DIRT, 20));

        assertThat(this.serializer.serialize(map))
                .isNotNull()
                .isBase64()
                .isEqualTo(
                        "rO0ABXcIAAAAAgAAAAFzcgAab3JnLmJ1a2tpdC51dGlsLmlvLldyYXBwZXLyUEfs8RJvBQIAAUwAA21hcHQAD0" +
                                "xqYXZhL3V0aWwvTWFwO3hwc3IANWNvbS5nb29nbGUuY29tbW9uLmNvbGxlY3QuSW1tdXRhYmxlTWFwJFNlcmlhbGl6ZWRGb3Jt" +
                                "AAAAAAAAAAACAAJMAARrZXlzdAASTGphdmEvbGFuZy9PYmplY3Q7TAAGdmFsdWVzcQB+AAR4cHVyABNbTGphdmEubGFuZy5PYm" +
                                "plY3Q7kM5YnxBzKWwCAAB4cAAAAAV0AAI9PXQAAXZ0AAR0eXBldAAGYW1vdW50dAAEbWV0YXVxAH4ABgAAAAV0AB5vcmcuYnVr" +
                                "a2l0LmludmVudG9yeS5JdGVtU3RhY2tzcgARamF2YS5sYW5nLkludGVnZXIS4qCk94GHOAIAAUkABXZhbHVleHIAEGphdmEubG" +
                                "FuZy5OdW1iZXKGrJUdC5TgiwIAAHhwAAAAAXQAB09BS19MT0dzcQB+AA8AAAAKc3EAfgAAc3EAfgADdXEAfgAGAAAABXEAfgAI" +
                                "dAAIZW5jaGFudHN0AAZkYW1hZ2V0AARsb3JldAALZGlzcGxheU5hbWV1cQB+AAYAAAAFdAAsZnIudXRhcnd5bi5lbmRlcmNvbn" +
                                "RhaW5lcnMubW9jay5JdGVtTWV0YU1vY2tzcgAlamF2YS51dGlsLkNvbGxlY3Rpb25zJFVubW9kaWZpYWJsZU1hcPGlqP509QdC" +
                                "AgABTAABbXEAfgABeHBzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD" +
                                "9AAAAAAAAAdwgAAAAQAAAAAHhzcQB+AA8AAAAAc3IAE2phdmEudXRpbC5BcnJheUxpc3R4gdIdmcdhnQMAAUkABHNpemV4cAAA" +
                                "AAB3BAAAAAB4dAAAdwQAAAARc3EAfgAAc3EAfgADdXEAfgAGAAAABXEAfgAIcQB+AAlxAH4ACnEAfgALcQB+AAx1cQB+AAYAAA" +
                                "AFcQB+AA5xAH4AEXQABERJUlRzcQB+AA8AAAAUc3EAfgAAc3EAfgADdXEAfgAGAAAABXEAfgAIcQB+ABdxAH4AGHEAfgAZcQB+" +
                                "ABp1cQB+AAYAAAAFcQB+ABxzcQB+AB1zcQB+AB8/QAAAAAAAAHcIAAAAEAAAAAB4cQB+ACFzcQB+ACIAAAAAdwQAAAAAeHEAfgAk"
                );
    }

    @Test
    public void deserialize() throws IOException {
        ConcurrentMap<Integer, ItemStack> result = this.serializer.deserialize(
                "rO0ABXcIAAAAAgAAAAFzcgAab3JnLmJ1a2tpdC51dGlsLmlvLldyYXBwZXLyUEfs8RJvBQIAAUwAA21hcHQAD0" +
                        "xqYXZhL3V0aWwvTWFwO3hwc3IANWNvbS5nb29nbGUuY29tbW9uLmNvbGxlY3QuSW1tdXRhYmxlTWFwJFNlcmlhbGl6ZWRGb3Jt" +
                        "AAAAAAAAAAACAAJMAARrZXlzdAASTGphdmEvbGFuZy9PYmplY3Q7TAAGdmFsdWVzcQB+AAR4cHVyABNbTGphdmEubGFuZy5PYm" +
                        "plY3Q7kM5YnxBzKWwCAAB4cAAAAAV0AAI9PXQAAXZ0AAR0eXBldAAGYW1vdW50dAAEbWV0YXVxAH4ABgAAAAV0AB5vcmcuYnVr" +
                        "a2l0LmludmVudG9yeS5JdGVtU3RhY2tzcgARamF2YS5sYW5nLkludGVnZXIS4qCk94GHOAIAAUkABXZhbHVleHIAEGphdmEubG" +
                        "FuZy5OdW1iZXKGrJUdC5TgiwIAAHhwAAAAAXQAB09BS19MT0dzcQB+AA8AAAAKc3EAfgAAc3EAfgADdXEAfgAGAAAABXEAfgAI" +
                        "dAAIZW5jaGFudHN0AAZkYW1hZ2V0AARsb3JldAALZGlzcGxheU5hbWV1cQB+AAYAAAAFdAAsZnIudXRhcnd5bi5lbmRlcmNvbn" +
                        "RhaW5lcnMubW9jay5JdGVtTWV0YU1vY2tzcgAlamF2YS51dGlsLkNvbGxlY3Rpb25zJFVubW9kaWZpYWJsZU1hcPGlqP509QdC" +
                        "AgABTAABbXEAfgABeHBzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD" +
                        "9AAAAAAAAAdwgAAAAQAAAAAHhzcQB+AA8AAAAAc3IAE2phdmEudXRpbC5BcnJheUxpc3R4gdIdmcdhnQMAAUkABHNpemV4cAAA" +
                        "AAB3BAAAAAB4dAAAdwQAAAARc3EAfgAAc3EAfgADdXEAfgAGAAAABXEAfgAIcQB+AAlxAH4ACnEAfgALcQB+AAx1cQB+AAYAAA" +
                        "AFcQB+AA5xAH4AEXQABERJUlRzcQB+AA8AAAAUc3EAfgAAc3EAfgADdXEAfgAGAAAABXEAfgAIcQB+ABdxAH4AGHEAfgAZcQB+" +
                        "ABp1cQB+AAYAAAAFcQB+ABxzcQB+AB1zcQB+AB8/QAAAAAAAAHcIAAAAEAAAAAB4cQB+ACFzcQB+ACIAAAAAdwQAAAAAeHEAfgAk"
        );

        ConcurrentMap<Integer, ItemStack> expected = new ConcurrentHashMap<>();
        expected.put(1, new ItemStack(Material.OAK_LOG, 10));
        expected.put(17, new ItemStack(Material.DIRT, 20));

        assertThat(result).isNotNull().isNotEmpty().hasSize(2)
                .containsExactlyEntriesOf(expected);
    }

}
