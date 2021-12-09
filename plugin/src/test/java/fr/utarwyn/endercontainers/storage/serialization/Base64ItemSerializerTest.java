package fr.utarwyn.endercontainers.storage.serialization;

import fr.utarwyn.endercontainers.TestHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;

public class Base64ItemSerializerTest {

    private ItemSerializer serializer;

    @BeforeClass
    public static void setUpClass() {
        TestHelper.setUpServer();
    }

    @Before
    public void setUp() {
        this.serializer = new Base64ItemSerializer();
    }

    @Test
    public void serialize() throws IOException {
        ConcurrentMap<Integer, ItemStack> map = new ConcurrentHashMap<>();
        map.put(1, new ItemStack(Material.OAK_LOG, 10));
        map.put(17, new ItemStack(Material.GRASS, 20));

        assertThat(this.serializer.serialize(map))
                .isNotNull()
                .isBase64()
                .isEqualTo("rO0ABXcIAAAAAgAAAAFzcgAab3JnLmJ1a2tpdC51dGlsLmlvLldyYXBwZXLyUEfs8RJv" +
                        "BQIAAUwAA21hcHQAD0xqYXZhL3V0aWwvTWFwO3hwc3IANWNvbS5nb29nbGUuY29tbW9uLmN" +
                        "vbGxlY3QuSW1tdXRhYmxlTWFwJFNlcmlhbGl6ZWRGb3JtAAAAAAAAAAACAAJMAARrZXlzdA" +
                        "ASTGphdmEvbGFuZy9PYmplY3Q7TAAGdmFsdWVzcQB+AAR4cHVyABNbTGphdmEubGFuZy5PY" +
                        "mplY3Q7kM5YnxBzKWwCAAB4cAAAAAV0AAI9PXQAAXZ0AAR0eXBldAAGYW1vdW50dAAEbWV0" +
                        "YXVxAH4ABgAAAAV0AB5vcmcuYnVra2l0LmludmVudG9yeS5JdGVtU3RhY2tzcgARamF2YS5" +
                        "sYW5nLkludGVnZXIS4qCk94GHOAIAAUkABXZhbHVleHIAEGphdmEubGFuZy5OdW1iZXKGrJ" +
                        "UdC5TgiwIAAHhwAAAAAXQAB09BS19MT0dzcQB+AA8AAAAKc3EAfgAAc3EAfgADdXEAfgAGA" +
                        "AAABXEAfgAIdAAIZW5jaGFudHN0AAZkYW1hZ2V0AARsb3JldAALZGlzcGxheU5hbWV1cQB+" +
                        "AAYAAAAFdAAsZnIudXRhcnd5bi5lbmRlcmNvbnRhaW5lcnMubW9jay5JdGVtTWV0YU1vY2t" +
                        "zcgAlamF2YS51dGlsLkNvbGxlY3Rpb25zJFVubW9kaWZpYWJsZU1hcPGlqP509QdCAgABTA" +
                        "ABbXEAfgABeHBzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b" +
                        "3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHhzcQB+AA8AAAAAc3IAE2phdmEu" +
                        "dXRpbC5BcnJheUxpc3R4gdIdmcdhnQMAAUkABHNpemV4cAAAAAB3BAAAAAB4dAAAdwQAAAA" +
                        "Rc3EAfgAAc3EAfgADdXEAfgAGAAAABXEAfgAIcQB+AAlxAH4ACnEAfgALcQB+AAx1cQB+AA" +
                        "YAAAAFcQB+AA5xAH4AEXQABUdSQVNTc3EAfgAPAAAAFHNxAH4AAHNxAH4AA3VxAH4ABgAAA" +
                        "AVxAH4ACHEAfgAXcQB+ABhxAH4AGXEAfgAadXEAfgAGAAAABXEAfgAcc3EAfgAdc3EAfgAf" +
                        "P0AAAAAAAAB3CAAAABAAAAAAeHEAfgAhc3EAfgAiAAAAAHcEAAAAAHhxAH4AJA==");
    }

    @Test
    public void deserialize() throws IOException {
        ConcurrentMap<Integer, ItemStack> result = this.serializer.deserialize(
                "rO0ABXcIAAAAAgAAAAZzcgAab3JnLmJ1a2tpdC51dGlsLmlvLldyYXBwZXLyUEfs8RJvBQIAAUwAA21" +
                        "hcHQAD0xqYXZhL3V0aWwvTWFwO3hwc3IANWNvbS5nb29nbGUuY29tbW9uLmNvbGxlY3QuSW" +
                        "1tdXRhYmxlTWFwJFNlcmlhbGl6ZWRGb3JtAAAAAAAAAAACAAJbAARrZXlzdAATW0xqYXZhL" +
                        "2xhbmcvT2JqZWN0O1sABnZhbHVlc3EAfgAEeHB1cgATW0xqYXZhLmxhbmcuT2JqZWN0O5DO" +
                        "WJ8QcylsAgAAeHAAAAAEdAACPT10AAF2dAAEdHlwZXQABG1ldGF1cQB+AAYAAAAEdAAeb3J" +
                        "nLmJ1a2tpdC5pbnZlbnRvcnkuSXRlbVN0YWNrc3IAEWphdmEubGFuZy5JbnRlZ2VyEuKgpP" +
                        "eBhzgCAAFJAAV2YWx1ZXhyABBqYXZhLmxhbmcuTnVtYmVyhqyVHQuU4IsCAAB4cAAAAAF0A" +
                        "AVHUkFTU3NxAH4AAHNxAH4AA3VxAH4ABgAAAAVxAH4ACHQACGVuY2hhbnRzdAAGZGFtYWdl" +
                        "dAAEbG9yZXQAC2Rpc3BsYXlOYW1ldXEAfgAGAAAABXQALGZyLnV0YXJ3eW4uZW5kZXJjb25" +
                        "0YWluZXJzLm1vY2suSXRlbU1ldGFNb2Nrc3IAJWphdmEudXRpbC5Db2xsZWN0aW9ucyRVbm" +
                        "1vZGlmaWFibGVNYXDxpaj+dPUHQgIAAUwAAW1xAH4AAXhwc3IAEWphdmEudXRpbC5IYXNoT" +
                        "WFwBQfawcMWYNEDAAJGAApsb2FkRmFjdG9ySQAJdGhyZXNob2xkeHA/QAAAAAAAAHcIAAAA" +
                        "EAAAAAB4c3EAfgAOAAAAAHNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAAR" +
                        "zaXpleHAAAAAAdwQAAAAAeHQAAHcEAAAAF3NxAH4AAHNxAH4AA3VxAH4ABgAAAAVxAH4ACH" +
                        "EAfgAJcQB+AAp0AAZhbW91bnRxAH4AC3VxAH4ABgAAAAVxAH4ADXEAfgAQdAAHT0FLX0xPR" +
                        "3NxAH4ADgAAAAdzcQB+AABzcQB+AAN1cQB+AAYAAAAFcQB+AAhxAH4AFXEAfgAWcQB+ABdx" +
                        "AH4AGHVxAH4ABgAAAAVxAH4AGnNxAH4AG3NxAH4AHT9AAAAAAAAAdwgAAAAQAAAAAHhxAH4" +
                        "AH3NxAH4AIAAAAAB3BAAAAAB4cQB+ACI="
        );

        ConcurrentMap<Integer, ItemStack> expected = new ConcurrentHashMap<>();
        expected.put(6, new ItemStack(Material.GRASS, 1));
        expected.put(23, new ItemStack(Material.OAK_LOG, 7));

        assertThat(result).isNotNull().isNotEmpty().hasSize(2)
                .containsExactlyEntriesOf(expected);
    }

}
