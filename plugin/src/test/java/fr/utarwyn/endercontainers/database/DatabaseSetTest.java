package fr.utarwyn.endercontainers.database;

import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatabaseSetTest {

    private final static String KEY = "test";

    private final static String KEY_UNKNOWN = "unknown";

    private DatabaseSet set;

    @Before
    public void setUp() {
        this.set = new DatabaseSet();
    }

    @Test
    public void getString() {
        // Correct value type
        this.set.setObject(KEY, "hello world");
        assertThat(this.set.getString(KEY))
                .isNotNull().isEqualTo("hello world");

        // Unvalid value type
        this.set.setObject(KEY, 3);
        assertThat(this.set.getString(KEY)).isNull();

        // Unknown key
        assertThat(this.set.getString(KEY_UNKNOWN)).isNull();
    }

    @Test
    public void getInteger() {
        // Correct value type
        this.set.setObject(KEY, 55);
        assertThat(this.set.getInteger(KEY)).isNotNull().isEqualTo(55);

        // Unvalid value type
        this.set.setObject(KEY, "test");
        assertThat(this.set.getInteger(KEY)).isNull();

        // Unknown key
        assertThat(this.set.getInteger(KEY_UNKNOWN)).isNull();
    }

    @Test
    public void getTimestamp() {
        // Correct value type
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        this.set.setObject(KEY, ts);
        assertThat(this.set.getTimestamp(KEY)).isNotNull().isEqualTo(ts);

        // Unvalid value type
        this.set.setObject(KEY, 50);
        assertThat(this.set.getTimestamp(KEY)).isNull();

        // Unknown key
        assertThat(this.set.getTimestamp(KEY_UNKNOWN)).isNull();
    }

    @Test
    public void getKeys() {
        // No keys by default
        assertThat(this.set.getKeys()).isNotNull().isEmpty();

        // Test with some keys
        this.set.setObject("key1", 10);
        this.set.setObject("key2", "test");
        this.set.setObject("key3", 54.3);

        assertThat(this.set.getKeys()).isNotNull().isNotEmpty()
                .hasSize(3).containsExactly("key1", "key2", "key3");
    }

    @Test
    public void getValues() {
        // No values by default
        assertThat(this.set.getValues()).isNotNull().isEmpty();

        // Test with some values
        this.set.setObject("key1", 10);
        this.set.setObject("key2", "test");
        this.set.setObject("key3", 54.3);

        assertThat(this.set.getValues()).isNotNull().isNotEmpty()
                .hasSize(3).containsExactly(10, "test", 54.3);
    }

    @Test
    public void stringRepresentation() {
        // Default toString
        assertThat(this.set.toString()).isNotNull()
                .matches("\\{DatabaseSet #([0-9]+) \\(\\)}");

        // toString with some objects
        this.set.setObject("key1", 10);
        this.set.setObject("key2", "test");
        this.set.setObject("key3", 54.3);

        assertThat(this.set.toString()).isNotNull()
                .matches("\\{DatabaseSet #([0-9]+) \\((.+)\\)}")
                .contains("key1=10 key2=test key3=54.3");
    }

    @Test
    public void equals() {
        DatabaseSet other = new DatabaseSet();
        other.setObject(KEY, "test");

        assertThat(this.set)
                .isNotEqualTo(null)
                .isNotEqualTo(other)
                .isEqualTo(new DatabaseSet());
    }

    @Test
    public void resultSetToDatabaseSet() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);

        // Construct a fake result set
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(anyInt()))
                .thenReturn("field1").thenReturn("field2")
                .thenReturn("field1").thenReturn("field2");
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(resultSet.getObject(anyInt()))
                .thenReturn("test").thenReturn(500)
                .thenReturn("test2").thenReturn(26);

        // Check if the result is OK
        List<DatabaseSet> sets = DatabaseSet.resultSetToDatabaseSet(resultSet);

        assertThat(sets).isNotNull().isNotEmpty().hasSize(2);
        assertThat(sets.get(0).getString("field1")).isEqualTo("test");
        assertThat(sets.get(0).getInteger("field2")).isEqualTo(500);
        assertThat(sets.get(1).getString("field1")).isEqualTo("test2");
        assertThat(sets.get(1).getInteger("field2")).isEqualTo(26);
    }

}
