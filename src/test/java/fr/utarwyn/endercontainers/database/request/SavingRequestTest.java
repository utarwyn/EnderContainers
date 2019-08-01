package fr.utarwyn.endercontainers.database.request;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class SavingRequestTest {

    @Test
    public void testFields() {
        SavingRequest request1 = new SavingRequest(null, "test");
        request1.fields("id", "test").values(null, null);

        SavingRequest request2 = new SavingRequest(null, "test");
        request2.fields("num", "key", "8569yrte_").values(null, null, null);

        SavingRequest request3 = new SavingRequest(null, "test");
        request3.fields("key").values("ABC").where("eza = ?");

        assertThat(request1.getRequest()).contains("(`id`,`test`)");
        assertThat(request2.getRequest()).contains("(`num`,`key`,`8569yrte_`)");
        assertThat(request3.getRequest()).contains("SET `key` = ?");
    }

    @Test
    public void testValues() {
        SavingRequest request1 = new SavingRequest(null, "test");
        request1.fields("field1", "field2", "field3").values(null, null, null);

        SavingRequest request2 = new SavingRequest(null, "test");
        request2.fields("field1").values(4);

        assertThat(request1.getRequest()).contains("VALUES (?,?,?)");
        assertThat(request2.getRequest()).contains("VALUES (?)");
    }

    @Test
    public void testConditions() {
        SavingRequest request1 = new SavingRequest(null, "test");
        request1.fields("field").values("eza").where("field = ?");

        SavingRequest request2 = new SavingRequest(null, "test");
        request2.fields("field1").values("eza").where("`eza` = ?", "`aze` = ?");

        SavingRequest request3 = new SavingRequest(null, "test");
        request3.fields("field1").values("eza").where("`max` > 10", "`min` < ?");

        assertThat(request1.getRequest()).startsWith("UPDATE");
        assertThat(request2.getRequest()).endsWith("WHERE `eza` = ? AND `aze` = ?");
        assertThat(request3.getRequest()).endsWith("WHERE `max` > 10 AND `min` < ?");
    }

    @Test
    public void testAttributes() {
        SavingRequest request1 = new SavingRequest(null, "test");
        request1.fields("field1", "field2").values("eza", 45);

        SavingRequest request2 = new SavingRequest(null, "test");
        request2.fields("field").values('p').where("field = ?", "eza > ?").attributes(1234, 42.3);

        assertThat(request1.getAttributes()).isNotEmpty().hasSize(2).containsExactly("eza", 45);
        assertThat(request2.getAttributes()).isNotEmpty().hasSize(3).containsExactly('p', 1234, 42.3);
    }

    @Test
    public void testReplaceIfExists() {
        SavingRequest request1 = new SavingRequest(null, "test");
        request1.fields("field1", "field2").values("eza", 45).replaceIfExists();

        SavingRequest request2 = new SavingRequest(null, "test");
        request2.fields("field2").values("eza").where("aze = ?").replaceIfExists();

        assertThat(request1.getRequest()).startsWith("REPLACE INTO");
        assertThat(request2.getRequest()).startsWith("UPDATE"); // No replace with conditions
    }

    @Test
    public void testIllegalParameters() {
        SavingRequest request1 = new SavingRequest(null, null);
        SavingRequest request2 = new SavingRequest(null, "test");
        SavingRequest request3 = new SavingRequest(null, "test").fields("eza", "aze").values(2);

        assertThatNullPointerException().isThrownBy(request1::getRequest)
                .withMessage("Table seems to be null")
                .withNoCause();

        assertThatIllegalArgumentException().isThrownBy(request2::getRequest)
                .withMessage("You must add at least one field and one value")
                .withNoCause();

        assertThatIllegalArgumentException().isThrownBy(request3::getRequest)
                .withMessage("Number of fields and values seems to be different")
                .withNoCause();
    }

}
