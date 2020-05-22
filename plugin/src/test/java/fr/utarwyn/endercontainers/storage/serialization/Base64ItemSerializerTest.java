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
                        "vbGxlY3QuSW1tdXRhYmxlTWFwJFNlcmlhbGl6ZWRGb3JtAAAAAAAAAAACAAJbAARrZXlzdA" +
                        "ATW0xqYXZhL2xhbmcvT2JqZWN0O1sABnZhbHVlc3EAfgAEeHB1cgATW0xqYXZhLmxhbmcuT" +
                        "2JqZWN0O5DOWJ8QcylsAgAAeHAAAAAFdAACPT10AAF2dAAEdHlwZXQABmFtb3VudHQABG1l" +
                        "dGF1cQB+AAYAAAAFdAAeb3JnLmJ1a2tpdC5pbnZlbnRvcnkuSXRlbVN0YWNrc3IAEWphdmE" +
                        "ubGFuZy5JbnRlZ2VyEuKgpPeBhzgCAAFJAAV2YWx1ZXhyABBqYXZhLmxhbmcuTnVtYmVyhq" +
                        "yVHQuU4IsCAAB4cAAAAAF0AAdPQUtfTE9Hc3EAfgAPAAAACnNxAH4AAHNxAH4AA3VxAH4AB" +
                        "gAAAAVxAH4ACHQACGVuY2hhbnRzdAAGZGFtYWdldAAEbG9yZXQAC2Rpc3BsYXlOYW1ldXEA" +
                        "fgAGAAAABXQALGZyLnV0YXJ3eW4uZW5kZXJjb250YWluZXJzLm1vY2suSXRlbU1ldGFNb2N" +
                        "rc3IAJWphdmEudXRpbC5Db2xsZWN0aW9ucyRVbm1vZGlmaWFibGVNYXDxpaj+dPUHQgIAAU" +
                        "wAAW1xAH4AAXhwc3IAEWphdmEudXRpbC5IYXNoTWFwBQfawcMWYNEDAAJGAApsb2FkRmFjd" +
                        "G9ySQAJdGhyZXNob2xkeHA/QAAAAAAAAHcIAAAAEAAAAAB4c3EAfgAPAAAAAHNyABNqYXZh" +
                        "LnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAAAdwQAAAAAeHQAAHcEAAA" +
                        "AEXNxAH4AAHNxAH4AA3VxAH4ABgAAAAVxAH4ACHEAfgAJcQB+AApxAH4AC3EAfgAMdXEAfg" +
                        "AGAAAABXEAfgAOcQB+ABF0AAVHUkFTU3NxAH4ADwAAABRzcQB+AABzcQB+AAN1cQB+AAYAA" +
                        "AAFcQB+AAhxAH4AF3EAfgAYcQB+ABlxAH4AGnVxAH4ABgAAAAVxAH4AHHNxAH4AHXNxAH4A" +
                        "Hz9AAAAAAAAAdwgAAAAQAAAAAHhxAH4AIXNxAH4AIgAAAAB3BAAAAAB4cQB+ACQ=");
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
