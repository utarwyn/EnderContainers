package fr.utarwyn.endercontainers.commands;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.Managers;
import fr.utarwyn.endercontainers.backup.Backup;
import fr.utarwyn.endercontainers.backup.BackupManager;
import fr.utarwyn.endercontainers.dependencies.CitizensDependency;
import fr.utarwyn.endercontainers.dependencies.DependenciesManager;
import fr.utarwyn.endercontainers.dependencies.Dependency;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.Locale;
import fr.utarwyn.endercontainers.util.PluginMsg;
import fr.utarwyn.endercontainers.util.Updater;
import fr.utarwyn.endercontainers.util.uuid.UUIDFetcher;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Class used to manage the /endercontainers command
 * @since 1.0.0
 * @author Utarwyn
 */
public class EnderContainersCommand implements CommandExecutor {

	/**
	 * The enderchest manager
	 */
	private EnderChestManager chestManager;

	/**
	 * The backup manager
	 */
	private BackupManager backupManager;

	/**
	 * Construct the main Endercontainers command
	 */
	public EnderContainersCommand() {
		this.chestManager = EnderContainers.getInstance().getInstance(EnderChestManager.class);
		this.backupManager = EnderContainers.getInstance().getInstance(BackupManager.class);
	}

	/**
	 * Method called when the command /ecp is called.
	 * @param sender The entity who performed the command
	 * @param cmd The command object
	 * @param label The label used in the chat
	 * @param args Arguments used with the commande
	 * @return True if the command was performed successfully.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!Config.enabled) {
			PluginMsg.errorMessage(sender, Locale.pluginDisabled);
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage(Config.PREFIX + "Created by §3Utarwyn§7. Version §e" + EnderContainers.getInstance().getDescription().getVersion() + "§7.");
			sender.sendMessage(Config.PREFIX + "To show plugin's help: §d/ecp help§7.");
			return true;
		}

		String arg = args[0];

		switch (arg) {
			case "help":
			case "h":
			case "?":
				int page = (args.length > 1 && StringUtils.isNumeric(args[1])) ? Integer.parseInt(args[1]) : 1;
				this.getHelp(sender, page);
				break;

			case "open":
				if (!(sender instanceof Player)) {
					PluginMsg.errorMessage(sender, Locale.nopermConsole);
					return true;
				}

				Player player = (Player) sender;

				if (args.length <= 1) {
					PluginMsg.errorMessage(player, "Usage: /ecp open <player>");
					return true;
				}

				if (!EUtil.playerHasPerm(player, "openchests") && !player.isOp()) {
					PluginMsg.accessDenied(player);
					return true;
				}

				if (Config.disabledWorlds.contains(player.getWorld().getName())) {
					PluginMsg.errorMessage(player, Locale.pluginWorldDisabled);
					return true;
				}

				// Check if chests' owner is online or not
				// and open the online or offline menu.
				EUtil.runAsync(() -> {
					Player playerToSpec = Bukkit.getPlayer(args[1]);

					if (playerToSpec == null || !playerToSpec.isOnline()) {
						UUID uuid = UUIDFetcher.getUUID(args[1]);
						if (uuid != null)
							this.chestManager.openHubMenuFor(uuid, player);
						else
							player.sendMessage(Config.PREFIX + "§cPlayer §6" + args[1] + " §cwas not found.");
					} else
						this.chestManager.openHubMenuFor(playerToSpec.getUniqueId(), player);
				});

				break;

			case "backups":
				if (!EUtil.senderHasPerm(sender, "backups.view")) {
					PluginMsg.accessDenied(sender);
					return true;
				}

				this.viewBackups(sender);
				break;

			case "backup":
				if (!EUtil.senderHasPerm(sender, "backups.info")) {
					PluginMsg.accessDenied(sender);
					return true;
				}

				if (args.length < 2) {
					PluginMsg.errorMessage(sender, "Usage: /ecp backup <name>");
					return true;
				} else {
					String name = args[1];
					this.getBackupInformation(sender, name);
				}
				break;

			case "createbackup":
			case "newbackup":
				if (!EUtil.senderHasPerm(sender, "backups.create")) {
					PluginMsg.accessDenied(sender);
					return true;
				}

				if (args.length < 2) {
					PluginMsg.errorMessage(sender, "Usage: /ecp createbackup <name>");
					return true;
				} else {
					String name = args[1];

					sender.sendMessage(Config.PREFIX + Locale.backupCreationStarting);

					EUtil.runAsync(() -> {
						if (this.backupManager.createBackup(name, sender.getName()))
							sender.sendMessage(Config.PREFIX + Locale.backupCreated.replace("%backup%", name));
						else
							PluginMsg.errorMessage(sender, Locale.backupExists.replace("%backup%", name));
					});
				}
				break;

			case "applybackup":
			case "loadbackup":
				if (!EUtil.senderHasPerm(sender, "backups.apply")) {
					PluginMsg.accessDenied(sender);
					return true;
				}

				if (args.length < 2) {
					PluginMsg.errorMessage(sender, "Usage: /ecp applybackup <name>");
					return true;
				} else {
					String name = args[1];

					sender.sendMessage(Config.PREFIX + Locale.backupLoadingStarted);

					EUtil.runAsync(() -> {
						if (this.backupManager.applyBackup(name))
							sender.sendMessage(Config.PREFIX + Locale.backupLoaded.replace("%backup%", name));
						else
							sender.sendMessage(Config.PREFIX + ChatColor.RED + Locale.backupUnknown.replace("%backup%", name));
					});
				}
				break;

			case "rmbackup":
			case "removebackup":
				if (!EUtil.senderHasPerm(sender, "backups.remove")) {
					PluginMsg.accessDenied(sender);
					return true;
				}

				if (args.length < 2) {
					PluginMsg.errorMessage(sender, "Usage: /ecp rmbackup <name>");
					return true;
				} else {
					String name = args[1];

					EUtil.runAsync(() -> {
						if (this.backupManager.removeBackup(name))
							sender.sendMessage(Config.PREFIX + Locale.backupRemoved.replace("%backup_name%", name));
						else
							sender.sendMessage(Config.PREFIX + ChatColor.RED + Locale.backupUnknown.replace("%backup_name%", name));
					});
				}
				break;

			case "npc":
				Dependency dependency = EnderContainers.getInstance().getInstance(DependenciesManager.class).getDependencyByName("Citizens");
				if (dependency != null) {
					CitizensDependency dep = (CitizensDependency) dependency;
					if (!(sender instanceof Player)) {
						PluginMsg.errorMessage(sender, Locale.nopermConsole);
						return true;
					}

					if (!sender.isOp()) {
						PluginMsg.accessDenied(sender);
						return true;
					}

					if (args.length <= 1) {
						PluginMsg.errorMessage(sender, "Usage: /ecp npc <link|info|unlink>");
						return true;
					}

					dep.onCommand((Player) sender, args[1], Arrays.copyOfRange(args, 2, args.length));
				}
				break;

			case "reload":
			case "rl":
				if (!EUtil.senderHasPerm(sender, "reload")) {
					PluginMsg.accessDenied(sender);
					return true;
				}

				if (!Config.get().reload()) {
					sender.sendMessage(Config.PREFIX + "§cError when reloading config! See the console for more info!");
					sender.sendMessage(Config.PREFIX + "§8Plugin now disabled.");
					return true;
				}

				if (!Locale.get().reload()) {
					sender.sendMessage(Config.PREFIX + "§cError when reloading plugin locale! See the console for more info!");
					sender.sendMessage(Config.PREFIX + "§8Plugin now disabled.");
					return true;
				}

				Managers.reloadAll();

				sender.sendMessage(Config.PREFIX + "§a" + Locale.configReloaded);
				break;

			case "update":
				if (!Updater.getInstance().isUpToDate()) {
					sender.sendMessage(Config.PREFIX + "§aThere is a newer version available: §2§l" + Updater.getInstance().getNewestVersion() + "§a.");
					sender.sendMessage(Config.PREFIX + "&7Click here to download it: " + Config.DOWNLOAD_LINK);
				} else {
					sender.sendMessage(Config.PREFIX + "§7" + Locale.noUpdate);
				}
				break;

			default:
				sender.sendMessage(Config.PREFIX + "§c" + Locale.cmdUnknown + ": §d/ecp help§c.");
				break;
		}

		return true;
	}

	/**
	 * Send the plugin's help to a specific {@link org.bukkit.command.CommandSender}
	 * @param p The guide will be sent to this entity
	 * @param page The page to display to the entity
	 */
	private void getHelp(CommandSender p, int page) {
		int maxpage = 2;
		if (maxpage < page) page = maxpage;

		PluginMsg.pluginBar(p);
		p.sendMessage(" ");

		if (page == 1) {
			sendFormattedHelpLine(p, "Open your enderchest", "§e/enderchest [number]", "cmd");
			sendFormattedHelpLine(p, "Open an enderchest", "§b/ecp open <player>", "backups.openchests");

			sendFormattedHelpLine(p, "List backups", "§b/ecp backups", "backups.view");
			sendFormattedHelpLine(p, "Create a backup", "§b/ecp createbackup <name>", "backups.create");
			sendFormattedHelpLine(p, "Show backup information", "§b/ecp backup <name>", "backups.info");
		} else if (page == 2) {
			sendFormattedHelpLine(p, "Load a backup", "§b/ecp applybackup <name>", "backups.apply");
			sendFormattedHelpLine(p, "Remove a backup", "§b/ecp rmbackup <name>", "backups.remove");

			p.sendMessage(" ");

			sendFormattedHelpLine(p, "Check for updates", "§e/ecp update", "update");
			sendFormattedHelpLine(p, "Reload the plugin", "§e/ecp reload", "reload");
		}

		p.sendMessage(" ");
		p.sendMessage(" §r §8✿ Plugin's help (" + page + "/" + maxpage + ")");
		PluginMsg.endBar(p);
	}

	/**
	 * Send a customized line at an entity for the plugin's help
	 * @param sender The entity which receive the line
	 * @param title Title of the action item
	 * @param command Command to perform to run the action
	 * @param perm Permission which player have to had to perform the see the help line
	 */
	private void sendFormattedHelpLine(CommandSender sender, String title, String command, String perm) {
		StringBuilder message = new StringBuilder(" §7  ");
		StringBuilder hiddenCommand = new StringBuilder("§c§k");

		message.append(title).append(": ");

		for (int i = 0; i < command.length(); i++)
			hiddenCommand.append("-");

		if (EUtil.senderHasPerm(sender, perm))
			message.append(command);
		else
			message.append(hiddenCommand);

		sender.sendMessage(message.toString());
	}

	/**
	 * List all backups to a specific entity
	 * @param sender The entity used to display backups' information
	 */
	private void viewBackups(CommandSender sender) {
		List<Backup> backups = this.backupManager.getBackups();

		if (backups.size() > 0) {
			PluginMsg.pluginBar(sender);
			sender.sendMessage(" ");

			for (Backup backup : backups)
				sender.sendMessage(
						" §r §7 Backup §b" + backup.getName() + "§7. Created by §e" + backup.getCreatedBy() + "§7."
				);

			sender.sendMessage(" ");
			sender.sendMessage(" §r §7" + Locale.backupInfo.replace("%command%", "/ecp backup <name>"));
			sender.sendMessage(" §r §8♣ Backup list (page 1)");
			PluginMsg.endBar(sender);
		} else {
			sender.sendMessage(Config.PREFIX + ChatColor.RED + Locale.backupZero.replace("%command%", "/ecp createbackup"));
		}
	}

	/**
	 * Display information about a specific backup
	 * @param p The entity used to display the backup's information
	 * @param name Name of the backup to process.
	 */
	private void getBackupInformation(CommandSender p, String name) {
		Backup backup = this.backupManager.getBackupByName(name);

		if (backup == null) {
			p.sendMessage(Config.PREFIX + ChatColor.RED + Locale.backupUnknown.replace("%backup_name%", name));
			return;
		}

		PluginMsg.pluginBar(p);
		p.sendMessage(" ");
		p.sendMessage(" §7  " + Locale.backupLabelName + ": §r" + backup.getName() + " §7(" + backup.getType() + ")");
		p.sendMessage(" §7  " + Locale.backupLabelDate + ": §r" + backup.getDate());
		p.sendMessage(" §7  " + Locale.backupLabelBy + ": §e" + backup.getCreatedBy());
		p.sendMessage(" ");
		p.sendMessage(" §8  " + Locale.backupLabelLoadCmd + ": §d/ecp applybackup " + name);
		p.sendMessage(" §8  " + Locale.backupLabelRmCmd + ": §c/ecp rmbackup " + name);
		p.sendMessage(" ");
		PluginMsg.endBar(p);
	}

}
