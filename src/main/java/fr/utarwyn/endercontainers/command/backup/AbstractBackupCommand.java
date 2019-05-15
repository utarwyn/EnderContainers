package fr.utarwyn.endercontainers.command.backup;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.command.AbstractCommand;

public abstract class AbstractBackupCommand extends AbstractCommand {

	protected BackupManager manager;

	AbstractBackupCommand(String name, BackupManager manager, String... aliases) {
		super(name, aliases);
		this.manager = manager;
	}

}
