package fr.utarwyn.endercontainers.configuration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LocaleKeyTest {

    @Test
    public void create() {
        assertThat(LocaleKey.CMD_BACKUP_CREATED.getKey()).isEqualTo("commands.backups.created");
    }

}
