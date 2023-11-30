package fr.utarwyn.endercontainers.database.request;

import fr.utarwyn.endercontainers.database.Database;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteRequestTest {

    @Test
    public void conditions() {
        DeleteRequest request1 = new DeleteRequest(null, "`test` = ?");
        DeleteRequest request2 = new DeleteRequest(null, "`eaz` > 50", "`test` = ?");

        request1.from("test");
        request2.from("test").attributes(20);

        assertThat(request1.getRequest()).endsWith("WHERE `test` = ?");
        assertThat(request2.getRequest()).endsWith("WHERE `eaz` > 50 AND `test` = ?");
    }

    @Test
    public void from() {
        DeleteRequest request = new DeleteRequest(null, "`test` = ?").from("data");
        assertThat(request.getRequest()).startsWith("DELETE FROM `data`");
    }

    @Test
    public void attributes() {
        DeleteRequest request1 = new DeleteRequest(null, "test");
        DeleteRequest request2 = new DeleteRequest(null, "field = ?", "eza > ?");
        request2.attributes(1234, 42.3);

        assertThat(request1.getAttributes()).isEmpty();
        assertThat(request2.getAttributes()).isNotEmpty().hasSize(2).containsExactly(1234, 42.3);
    }

    @Test
    public void execute() throws SQLException {
        Database database = mock(Database.class);
        DeleteRequest request = new DeleteRequest(database, "test = ?");

        request.from("table").attributes("qwerty");

        when(database.execUpdateStatement(request)).thenReturn(true);
        assertThat(request.execute()).isTrue();
    }

    @Test
    public void withoutCondition() {
        DeleteRequest request = new DeleteRequest(null).from("table");
        assertThat(request.getRequest()).isEqualTo("DELETE FROM `table`");
    }

    @Test
    public void illegalParameters() {
        DeleteRequest request = new DeleteRequest(null, "null");
        assertThatNullPointerException().isThrownBy(request::getRequest)
                .withMessage("Table seems to be null")
                .withNoCause();
    }

}
