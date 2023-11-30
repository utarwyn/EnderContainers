package fr.utarwyn.endercontainers.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocaleKeyTest {

    @Test
    void create() {
        assertThat(LocaleKey.CMD_BACKUP_CREATED.getKey()).isEqualTo("commands.backups.created");
    }

}
