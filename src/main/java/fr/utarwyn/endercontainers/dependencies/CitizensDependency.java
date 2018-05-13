package fr.utarwyn.endercontainers.dependencies;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.PluginMsg;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Dependency used to interact with the Citizens plugin
 * @deprecated
 *
 * @since 1.0.8
 * @author Utarwyn
 */
@Deprecated
public class CitizensDependency extends Dependency implements Listener {

	/**
	 * The enderchest manager
	 */
	private EnderChestManager manager;

	/**
	 * The file used to store the configuration of the dependency
	 */
	private File configurationFile;

	/**
	 * The configuration object
	 */
	private YamlConfiguration configuration;

	/**
	 * The collection which represents all chat edit mode
	 */
	private Set<PlayerChatEditMode> playersChatEditMode;

	/**
	 * The collection which store all links established between EnderContainers and Citizens
	 */
	private Set<NPCLink> NPCLinks;

	/**
	 * Construct the dependency object
	 */
	public CitizensDependency() {
		super("Citizens");

		File file = this.getConfigFile();
		if (file == null) return;

		this.configuration = YamlConfiguration.loadConfiguration(file);

		this.playersChatEditMode = new HashSet<>();
		this.NPCLinks = new HashSet<>();

		Bukkit.getPluginManager().registerEvents(this, EnderContainers.getInstance());
		this.reloadLinks();
	}

	/**
	 * Called when a player type a command that starts with "/ecp npc"
	 * @param player The player who type the command
	 * @param subcommand The subcommand performed
	 * @param args Arguments passed after of the subcommand
	 */
	public void onCommand(Player player, String subcommand, String[] args) {
		switch (subcommand) {
			case "link":
				NPC selectedNPC = CitizensAPI.getDefaultNPCSelector().getSelected(player);

				if (selectedNPC == null || !selectedNPC.isSpawned()) {
					PluginMsg.errorMessage(player, "You should select a NPC with the command §6/npc select§c to continue.");
					return;
				}

				if (getEditModeFor(player) != null) {
					PluginMsg.errorMessage(player, "You are already creating a link configuration.");
					return;
				}

				PluginMsg.pluginBar(player);
				player.sendMessage(" ");
				player.sendMessage(" ");
				player.sendMessage(" §r §eYou want to link NPC §a'" + selectedNPC.getName() + "' §ewith the plugin.");
				player.sendMessage(" §r §bStep 1: §eWhat type of inventory this NPC will allow to open?");
				player.sendMessage(" §r §7Please answer by typing §6main§7 or §6enderchest§7 in the chat below.");
				player.sendMessage(" §r §9You can type §2cancel§9 to stop this configuration.");
				player.sendMessage(" ");
				player.sendMessage(" ");
				PluginMsg.endBar(player);

				startEditModeFor(player);
				break;

			case "info":
				selectedNPC = CitizensAPI.getDefaultNPCSelector().getSelected(player);

				if (selectedNPC == null || !selectedNPC.isSpawned()) {
					PluginMsg.errorMessage(player, "You should select a NPC with the command §6/npc select§c to continue.");
					return;
				}

				NPCLink link = getNPCLinkById(selectedNPC.getId());
				if (link == null) {
					PluginMsg.errorMessage(player, "NPC §6" + selectedNPC.getName() + "§c is not linked with EnderContainers.");
					return;
				}

				PluginMsg.pluginBar(player);
				player.sendMessage(" ");
				player.sendMessage(" ");
				player.sendMessage(" §r §aNPC: §e" + selectedNPC.getName() + " (" + link.getNPCId() + ")");
				player.sendMessage(" §r §aType: §e" + link.getType().toUpperCase());
				player.sendMessage(" §r §aDelay: §e" + link.getDelay() + "s");

				if (link.getEnderchestNumber() > -1)
					player.sendMessage(" §r §aChest number: §e" + link.getEnderchestNumber());
				else
					player.sendMessage(" ");

				player.sendMessage(" ");
				player.sendMessage(" ");
				PluginMsg.endBar(player);
				break;

			case "unlink":
				selectedNPC = CitizensAPI.getDefaultNPCSelector().getSelected(player);

				if (selectedNPC == null || !selectedNPC.isSpawned()) {
					PluginMsg.errorMessage(player, "You should select a NPC with the command §6/npc select§c to continue.");
					return;
				}

				link = getNPCLinkById(selectedNPC.getId());
				if (link == null) {
					PluginMsg.errorMessage(player, "NPC §6" + selectedNPC.getName() + "§c is not linked with EnderContainers.");
					return;
				}

				removeNPCLink(link.getNPCId());
				player.sendMessage(Config.PREFIX + "§aYou have successfully unlinked the NPC §e'" + selectedNPC.getName() + "'§a with EnderContainers.");

				break;

			default:
				PluginMsg.errorMessage(player, "Usage: /endc npc <link|info|unlink>");
				break;
		}
	}

	/**
	 * Called when a player click on a Citizens NPC
	 * @param player The player who click on the NPC
	 * @param npc The NPC clicked
	 * @return True if the action has been processed by the method
	 */
	private boolean onNPCClicked(final Player player, NPC npc) {
		NPCLink link = getNPCLinkById(npc.getId());
		if (link == null) return false;

		if (this.manager == null)
			this.manager = EnderContainers.getInstance().getInstance(EnderChestManager.class);

		switch (link.getType()) {
			case "main":
				if (link.getDelay() == 0)
					this.manager.openHubMenuFor(player);
				else {
					player.sendMessage(Config.PREFIX + "Opening enderchest in §6" + link.getDelay() + "§7 seconds...");
					Bukkit.getScheduler().scheduleSyncDelayedTask(
							EnderContainers.getInstance(),
							() -> this.manager.openHubMenuFor(player),
							link.getDelay() * 20
					);
				}

				return true;

			case "enderchest":
				final int n = link.getEnderchestNumber();
				if (n <= -1 || n > (Config.maxEnderchests - 1)) return false;

				if (link.getDelay() == 0)
					this.manager.openEnderchestFor(player, n);
				else {
					player.sendMessage(Config.PREFIX + "Opening enderchest in §6" + link.getDelay() + "§7 seconds...");

					Bukkit.getScheduler().scheduleSyncDelayedTask(
							EnderContainers.getInstance(),
							() -> this.manager.openEnderchestFor(player, n),
							link.getDelay() * 20
					);
				}

				return true;
		}

		return false;
	}

	/* ----  EDIT MODE  ---- */

	/**
	 * Start the edit mode for a specific player
	 * @param player the player who wants to start the edit mode
	 */
	private void startEditModeFor(Player player) {
		playersChatEditMode.add(new PlayerChatEditMode(player));
	}

	/**
	 * Get the {@link PlayerChatEditMode} for a specific player
	 * @param player The player
	 * @return The {@link PlayerChatEditMode} found otherwise null
	 */
	private PlayerChatEditMode getEditModeFor(Player player) {
		for (PlayerChatEditMode mode : playersChatEditMode) {
			if (mode.getPlayer().getUniqueId().equals(player.getUniqueId()))
				return mode;
		}

		return null;
	}

	/**
	 * Stop the edit mode for a player
	 * @param player The player who wants to stop the edit mode
	 */
	private void stopEditModeFor(Player player) {
		PlayerChatEditMode mode = getEditModeFor(player);
		if (mode != null) playersChatEditMode.remove(mode);
	}

	/* ----  NPC MANAGEMENT  ---- */

	/**
	 * Save a NPC link into the configuration file
	 * @param npcId The ID of the Citizens NPC
	 * @param linkType The type of the link with the NPC
	 * @param delay The delay to open an enderchest when clicking on the NPC
	 * @param enderchestNumber The number of the enderchest to open (if null the Hub will be opened)
	 */
	private void saveNPCLink(Integer npcId, String linkType, Integer delay, Integer enderchestNumber) {
		if (this.configuration.isConfigurationSection(String.valueOf(npcId)))
			this.configuration.set(String.valueOf(npcId), null);

		this.configuration.set(npcId + ".type", linkType);
		this.configuration.set(npcId + ".delay", delay);
		if (enderchestNumber != null)
			this.configuration.set(npcId + ".chestNumber", enderchestNumber);

		try {
			this.configuration.save(this.configurationFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.reloadLinks();
	}

	/**
	 * Remove a specific link from the configuration (and the memory) with a NPC's ID
	 * @param npcId The ID of the NPC
	 */
	private void removeNPCLink(Integer npcId) {
		NPCLink link = getNPCLinkById(npcId);
		if (link == null) return;

		this.configuration.set(String.valueOf(npcId), null);

		try {
			this.configuration.save(this.configurationFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		NPCLinks.remove(link);
	}

	/**
	 * Get a link by its NPC id
	 * @param npcId The NPC id
	 * @return The found link with the npc id passed to the method
	 */
	private NPCLink getNPCLinkById(Integer npcId) {
		for (NPCLink npcLink : NPCLinks) {
			if (Objects.equals(npcLink.getNPCId(), npcId))
				return npcLink;
		}

		return null;
	}

	/**
	 * Reload all links from the configuration
	 */
	private void reloadLinks() {
		NPCLinks.clear();

		for (String linkId : this.configuration.getKeys(false)) {
			Integer npcId = Integer.parseInt(linkId);
			String type = this.configuration.getString(linkId + ".type");
			Integer delay = this.configuration.getInt(linkId + ".delay");

			Integer enderchestNumber = this.configuration.contains(linkId + ".chestNumber") ? this.configuration.getInt(linkId + ".chestNumber") : -1;
			NPCLinks.add(new NPCLink(npcId, type, delay, enderchestNumber));
		}
	}

	/**
	 * Method called when a player types in the chat
	 * @param e The event object
	 */
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();

		PlayerChatEditMode mode = getEditModeFor(player);
		if (mode == null) return;

		e.setCancelled(true);

		if (e.getMessage().equals("cancel")) {
			stopEditModeFor(player);
			player.sendMessage(Config.PREFIX + "You have stopped the link configuration.");
			return;
		}

		switch (mode.getCurrentStep()) {
			case 1:
				if (!e.getMessage().equals("main") && !e.getMessage().equals("enderchest")) {
					player.sendMessage(Config.PREFIX + "§cPlease type §6main§c or §6enderchest§c.");
					return;
				}

				mode.setCurrentAnswer(e.getMessage());
				mode.nextStep();

				PluginMsg.pluginBar(player);
				player.sendMessage(" ");
				player.sendMessage(" §r §eYou have chosen the type §a" + e.getMessage() + "§e.");
				player.sendMessage(" ");

				if (e.getMessage().equals("main")) {
					player.sendMessage(" §r §bStep 2: §eDelay before opening the inventory when a player interacts with a NPC? (in seconds)");
					player.sendMessage(" §r §7Please answer by typing in the chat below. §6(or 'no', if you don't want to have a delay)");
				} else {
					player.sendMessage(" §r §bStep 2: §eWhat enderchest number this NPC will open ?");
					player.sendMessage(" §r §7Please answer by typing in the chat below. §6(a number between 0 and " + (Config.maxEnderchests - 1) + ")");
					mode.setCurrentStep(3);
				}

				player.sendMessage(" ");
				player.sendMessage(" ");
				player.sendMessage(" ");
				PluginMsg.endBar(player);
				break;

			case 2:
				if (!e.getMessage().equals("no") && !StringUtils.isNumeric(e.getMessage())) {
					player.sendMessage(Config.PREFIX + "§cPlease type the delay (seconds) or §6no§c if you don't want to have a delay.");
					return;
				}

				NPC selectedNPC = CitizensAPI.getDefaultNPCSelector().getSelected(player);

				mode.setCurrentAnswer(e.getMessage());
				stopEditModeFor(player);

				String delayAnswer = mode.getAnswerForStep(2);
				int delay = StringUtils.isNumeric(delayAnswer) ? Integer.parseInt(delayAnswer) : 0;

				saveNPCLink(selectedNPC.getId(), mode.getAnswerForStep(1), delay, -1);

				player.sendMessage(Config.PREFIX + "§eNPC §a'" + selectedNPC.getName() + "'§e linked with EnderContainers. Enjoy!");
				break;

			case 3:
				if (!StringUtils.isNumeric(e.getMessage()) || Integer.parseInt(e.getMessage()) < 0 || Integer.parseInt(e.getMessage()) > Config.maxEnderchests - 1) {
					player.sendMessage(Config.PREFIX + "§cPlease type an enderchest number between §60 and " + (Config.maxEnderchests - 1) + "§c.");
					return;
				}

				mode.setCurrentAnswer(e.getMessage());
				mode.nextStep();

				PluginMsg.pluginBar(player);
				player.sendMessage(" ");
				player.sendMessage(" ");
				player.sendMessage(" §r §eYou have chosen the number §a" + e.getMessage() + "§e.");
				player.sendMessage(" §r §bStep 3: §eDelay before opening the inventory when a player interacts with a NPC? (in seconds)");
				player.sendMessage(" §r §7Please answer by typing in the chat below. §6(or 'no', if you don't want to have a delay)");
				player.sendMessage(" ");
				player.sendMessage(" ");
				player.sendMessage(" ");
				PluginMsg.endBar(player);
				break;

			case 4:
				if (!e.getMessage().equals("no") && !StringUtils.isNumeric(e.getMessage())) {
					player.sendMessage(Config.PREFIX + "§cPlease type the delay (seconds) or §6no§c if you don't want to have a delay.");
					return;
				}

				selectedNPC = CitizensAPI.getDefaultNPCSelector().getSelected(player);

				mode.setCurrentAnswer(e.getMessage());
				stopEditModeFor(player);

				delayAnswer = mode.getAnswerForStep(4);
				String chestNumberAnswer = mode.getAnswerForStep(3);

				delay = StringUtils.isNumeric(delayAnswer) ? Integer.parseInt(delayAnswer) : 0;
				int chestNumber = StringUtils.isNumeric(chestNumberAnswer) ? Integer.parseInt(chestNumberAnswer) : -1;

				saveNPCLink(selectedNPC.getId(), mode.getAnswerForStep(1), delay, chestNumber);

				player.sendMessage(Config.PREFIX + "§eNPC §a'" + selectedNPC.getName() + "'§e linked with EnderContainers. Enjoy!");
				break;
		}
	}

	/**
	 * Method called when a player leaves the server
	 * @param e The event object
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if (getEditModeFor(player) != null) stopEditModeFor(player);
	}

	/**
	 * Method called when a player performs a right click on a Citizens NPC
	 * @param e The event object
	 */
	@EventHandler
	public void onNPCRightClicked(NPCRightClickEvent e) {
		if (onNPCClicked(e.getClicker(), e.getNPC()))
			e.setCancelled(true);
	}

	/**
	 * Method called when a player performs a left click on a Citizens NPC
	 * @param e The event object
	 */
	@EventHandler
	public void onNPCLeftClicked(NPCLeftClickEvent e) {
		if (onNPCClicked(e.getClicker(), e.getNPC()))
			e.setCancelled(true);
	}

	/**
	 * Method called when a Citizens NPC is removed
	 * @param e The event object
	 */
	@EventHandler
	public void onNPCRemoved(NPCRemoveEvent e) {
		NPC npc = e.getNPC();

		if (getNPCLinkById(npc.getId()) != null)
			removeNPCLink(npc.getId());
	}

	/**
	 * Called when the dependency is enabling
	 */
	@Override
	public void onEnable() {

	}

	/**
	 * Called when the dependency is disabling
	 */
	@Override
	public void onDisable() {

	}

	/**
	 * Called when a player wants to open its enderchest by interacting with an enderchest block
	 * @param block The block clicked by the player
	 * @param player The player who interacts with the chest
	 * @return True if the block chest can be opened
	 */
	@Override
	public boolean onBlockChestOpened(Block block, Player player, boolean sendMessage) {
		return true;
	}

	/**
	 * Get/generate the file object for the configuration
	 * @return The File object generated/stored
	 */
	private File getConfigFile() {
		if (this.configurationFile != null)
			return this.configurationFile;

		File f = new File(EnderContainers.getInstance().getDataFolder(), "npcs.yml");

		try {
			if (!f.exists() && !f.createNewFile())
				return null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.configurationFile = f;
		return f;
	}

	/**
	 * Class to manage edit mode in chat for players
	 */
	private class PlayerChatEditMode {

		/**
		 * The player who inited the edit mode
		 */
		private Player player;

		/**
		 * A map which collects data sent by the player
		 */
		private Map<Integer, String> answers;

		/**
		 * Store the current step of answer for the player
		 */
		private int currentStep;

		/**
		 * Construct the edit mode for a specific player
		 * @param player The player
		 */
		PlayerChatEditMode(Player player) {
			this.player = player;
			this.answers = new HashMap<>();
			this.currentStep = 1;
		}

		/**
		 * Get the associated answer with a step number
		 * @param step The step number where to get the answer
		 * @return The answer as string sent by the player
		 */
		String getAnswerForStep(int step) {
			return this.answers.get(step);
		}

		/**
		 * Returns the player
		 * @return The player
		 */
		public Player getPlayer() {
			return this.player;
		}

		/**
		 * Returns the current step number
		 * @return The current step number
		 */
		int getCurrentStep() {
			return this.currentStep;
		}

		/**
		 * Define the current step
		 * @param step The step to define
		 */
		void setCurrentStep(int step) {
			this.currentStep = step;
		}

		/**
		 * Define the answer for the current step
		 * (Called by the PlayerChat event with the answer of the player)
		 * @param answer The answer sent by the player
		 */
		void setCurrentAnswer(String answer) {
			this.answers.put(this.getCurrentStep(), answer);
		}

		/**
		 * Pass to next step
		 */
		void nextStep() {
			this.setCurrentStep(this.getCurrentStep() + 1);
		}

	}

	/**
	 * Class used to create a link between a Citizens NPC and the plugin EnderContainers
	 */
	private class NPCLink {

		/**
		 * The ID of the NPC
		 */
		private int npcId;

		/**
		 * The type of the link
		 */
		private String type;

		/**
		 * The number of the enderchest
		 */
		private int enderchestNumber;

		/**
		 * The delay needed to open the linked menu
		 */
		private int delay;

		/**
		 * Construct a NPC link
		 * @param npcId The NPC ID
		 * @param type The type of link
		 * @param delay The delay associated with the link
		 * @param enderchestNumber The number of the chest to open when the link will be used.
		 */
		NPCLink(int npcId, String type, int delay, int enderchestNumber) {
			this.npcId = npcId;
			this.type = type;
			this.delay = delay;
			this.enderchestNumber = enderchestNumber;
		}

		/**
		 * Get the ID of the NPC
		 * @return The ID of the NPC
		 */
		int getNPCId() {
			return this.npcId;
		}

		/**
		 * Get the type of the link
		 * @return The type of the link
		 */
		String getType() {
			return this.type;
		}

		/**
		 * Get the delay associated to the link
		 * @return The delay associated to the link
		 */
		int getDelay() {
			return this.delay;
		}

		/**
		 * Get the number of the enderchest linked
		 * @return The number of the enderchest linked
		 */
		int getEnderchestNumber() {
			return this.enderchestNumber;
		}

	}

}
