package fr.utarwyn.endercontainers.configuration;

import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalizedExceptionTest {

    @Test
    public void createWithoutParameter() {
        try {
            throw new LocalizedException(LocaleKey.CMD_UPDATE);
        } catch (LocalizedException e) {
            assertThat(e.getKey()).isEqualTo(LocaleKey.CMD_UPDATE);
            assertThat(e.getParameters()).isNull();
        }
    }

    @Test
    public void createWithParameters() {
        try {
            throw new LocalizedException(LocaleKey.CMD_NO_UPDATE, Collections.singletonMap("key", "value"));
        } catch (LocalizedException e) {
            assertThat(e.getKey()).isEqualTo(LocaleKey.CMD_NO_UPDATE);
            assertThat(e.getParameters()).hasSize(1).containsEntry("key", "value");
        }
    }

}
