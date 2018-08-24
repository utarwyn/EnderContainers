package fr.utarwyn.endercontainers.commands.backup;

import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.commands.AbstractCommand;

public abstract class AbstractBackupCommand extends AbstractCommand {

	protected BackupManager manager;

	AbstractBackupCommand(String name, BackupManager manager, String... aliases) {
		super(name, aliases);
		this.manager = manager;
	}

}
