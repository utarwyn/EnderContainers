package fr.utarwyn.endercontainers.enderchest;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.menu.EnderChestMenu;
import fr.utarwyn.endercontainers.menu.OfflineEnderChestMenu;
import fr.utarwyn.endercontainers.storage.StorageWrapper;
import fr.utarwyn.endercontainers.storage.player.PlayerData;
import fr.utarwyn.endercontainers.util.EUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

/**
 * Class used to create a custom enderchest
 * @since 2.0.0
 * @author Utarwyn
 */
public class EnderChest {

	/**
	 * The number of the enderchest
	 */
	private int num;

	/**
	 * Owner of the enderchest
	 */
	private UUID owner;

	/**
	 * Menu whichs represents the chest generated at the creation of it.
	 */
	private EnderChestMenu container;

	/**
	 * The number of rows of the chest's menu.
	 */
	private int rows;

	/**
	 * True if the chest is accessible
	 */
	private boolean accessible;

	/**
	 * Allows to create a new enderchest
	 * @param owner The owner's UUID of the chest
	 * @param num The number of the enderchest
	 */
	EnderChest(UUID owner, int num) {
		this.owner = owner;
		this.num = num;

		this.load();
	}

	/**
	 * Returns the number of the chest
	 * @return Number of the enderchest
	 */
	public int getNum() {
		return this.num;
	}

	/**
	 * Returns the owner of the chest
	 * @return UUID of the chest's owner
	 */
	public UUID getOwner() {
		return this.owner;
	}

	/**
	 * Returns the size of the chest
	 * @return The size of the chest (Number of filled slots)
	 */
	public int getSize() {
		return this.container.getFilledSlotsNb();
	}

	/**
	 * Returns the number of rows
	 * @return Number of rows
	 */
	public int getRows() {
		return this.rows;
	}

	/**
	 * Returns the number of slots in the chest
	 * @return Number of slots in the chest.
	 */
	public int getMaxSize() {
		return this.rows * 9;
	}

	/**
	 * Returns the fill percentage of the inventory
	 * @return The fill percentage
	 */
	public double getFillPercentage() {
		return (double) this.container.getFilledSlotsNb() / (this.rows * 9);
	}

	/**
	 * Returns all contents of the chest as a map
	 * @return All contents of the enderchest
	 */
	public Map<Integer, ItemStack> getContents() {
		return this.container.getContents();
	}

	/**
	 * Returns true if the chest was enabled by default
	 * @return True if the chest is unlocked for everyone
	 */
	public boolean isDefault() {
		return this.num < Config.defaultEnderchests;
	}

	/**
	 * Returns the accessibility of the chest
	 * @return True if the chest is accessible
	 */
	public boolean isAccessible() {
		return this.accessible;
	}

	/**
	 * Returns true if the container is full
	 * @return True if the container is full
	 */
	public boolean isFull() {
		return this.getSize() == this.getMaxSize();
	}

	/**
	 * Returns true if the container is empty
	 * @return True if the container is empty
	 */
	public boolean isEmpty() {
		return this.getSize() == 0;
	}

	/**
	 * Returns true if the chest was not used
	 * (not used mean that the owner is offline and the container has no viewer
	 *  so it have to be cleared from the memory)
	 *
	 * @see EnderChestPurgeTask Task which uses this method to clear unused data.
	 * @return True if the chest is unused
	 */
	boolean isUnused() {
		return this.getOwnerPlayer() == null && this.container.getViewers().isEmpty();
	}

	/**
	 * Destroys the container linked to this enderchest to liberate the memory.
	 */
	void destroyContainer() {
		if (this.container != null) {
			this.container.destroy();
		}
	}

	/**
	 * Open the chest container for a specific player
	 * @param player The player who wants to open the container
	 */
	public void openContainerFor(Player player) {
		this.container.open(player);
	}

	/**
	 * Save the enderchest. This method has to be called asynchronously if possible!
	 */
	public void save() {
		PlayerData pData = StorageWrapper.get(PlayerData.class, this.owner);
		assert pData != null;

		pData.saveEnderchest(this);
	}

	/**
	 * Reload all metas of the chest
	 * (that means the number of rows and the accessibility of the chest)
	 */
	public void reloadMeta() {
		PlayerData pData = StorageWrapper.get(PlayerData.class, this.owner);
		assert pData != null;

		// Detection of number of rows ...
		Integer rowsNb = this.generateRowsNb();
		this.rows = rowsNb != null ? rowsNb : pData.getEnderchestRows(this);

		// Load the accessibility of the enderchest ...
		Boolean accessibility = this.genereateAccessibility();

		/*
		 * If the accessibility cannot be resolved, it means that the player is offline.
		 * So, it means that the enderchest will be opened by an administrator,
		 * and in this case, the chest have to be accessible.
		 */
		this.accessible = accessibility != null ? accessibility : true;

		// Reload the number of rows of the container
		if (this.container != null) this.container.setRows(this.rows);
	}

	/**
	 * Load the chest at the creation of it
	 */
	private void load() {
		// Load chest's owner data ...
		PlayerData pData = StorageWrapper.get(PlayerData.class, this.owner);
		assert pData != null;

		// Reload metas of the chest!
		this.reloadMeta();

		// Load the container ...
		if (EUtil.isPlayerOnline(this.owner))
			// ... for an online player ...
			this.container = new EnderChestMenu(this);
		else
			// ... or for an offline player!
			this.container = new OfflineEnderChestMenu(this);

		// Load items in the container ...
		for (Map.Entry<Integer, ItemStack> entry : pData.getEnderchestContents(this).entrySet())
			this.container.setItem(entry.getKey(), entry.getValue());
	}

	/**
	 * Get the number of rows accessible if the player
	 * is connected on the server. This number is generated in terms
	 * of permissions of the player.
	 * (That's why it have to be connected)
	 *
	 * @return The number of rows generated or null if the player is not connected
	 */
	private Integer generateRowsNb() {
		Player player = this.getOwnerPlayer();

		// No player connected for this chest, use the cache instead.
		if (player == null) return null;
		// Use the vanilla chest!
		if (this.num == 0 && Config.useVanillaEnderchest) return 3;

		for (int row = 6; row > 0; row--)
			if (EUtil.playerHasPerm(player, "slot" + this.num + ".row" + row) || EUtil.playerHasPerm(player, "slots.row" + row))
				return row;

		return 3;
	}

	/**
	 * Get the accessibility of the chest if the player
	 * is connected on the server. This value is generated in terms
	 * of permissions of the player.
	 * (That's why it have to be connected)
	 *
	 * @return The accessibility generated or null if the player is not connected
	 */
	private Boolean genereateAccessibility() {
		Player player = this.getOwnerPlayer();

		if (this.isDefault()) return true;
		if (player == null) return null;

		return EUtil.playerHasPerm(player, "open." + this.getNum());
	}

	/**
	 * Get the owner as a Player object
	 * @return The Player object of the owner if he is connected, null otherwise
	 */
	private Player getOwnerPlayer() {
		return Bukkit.getPlayer(this.owner);
	}

}
