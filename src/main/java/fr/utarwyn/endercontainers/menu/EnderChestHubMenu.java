package fr.utarwyn.endercontainers.menu;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.enderchest.EnderChestManager;
import fr.utarwyn.endercontainers.util.EUtil;
import fr.utarwyn.endercontainers.util.Locale;
import fr.utarwyn.endercontainers.util.uuid.UUIDFetcher;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the menu with the list of all enderchests.
 * @since 2.0.0
 * @author Utarwyn
 */
public class EnderChestHubMenu extends AbstractMenu {

	/**
	 * Static fields which represents the maximum number of items per page
	 */
	private static final int PER_PAGE = 52;

	/**
	 * Represents the item to go to the previous page
	 */
	private static final ItemStack PREV_PAGE_ITEM = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

	/**
	 * Represents the item to go to the next page
	 */
	private static final ItemStack NEXT_PAGE_ITEM = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

	/**
	 * The enderchest manager
	 */
	private EnderChestManager manager;

	/**
	 * The owner of all enderchests in the menu
	 */
	private UUID owner;

	/**
	 * Current page for the player who has opened the menu
	 */
	private int page;

	/**
	 * Construct the Hub menu for a player
	 * @param owner The player who owns enderchests in the menu
	 */
	public EnderChestHubMenu(UUID owner) {
		super(Locale.menuMainTitle.replace("%player%", Objects.requireNonNull(UUIDFetcher.getName(owner))));

		this.owner = owner;
		this.manager = EnderContainers.getInstance().getInstance(EnderChestManager.class);
		this.page = 1;
	}

	static {
		SkullMeta prevSkullMeta = (SkullMeta) PREV_PAGE_ITEM.getItemMeta();
		prevSkullMeta.setOwner("MHF_ArrowLeft");
		prevSkullMeta.setDisplayName(ChatColor.RED + "≪ Previous page");
		PREV_PAGE_ITEM.setItemMeta(prevSkullMeta);

		SkullMeta nextSkullMeta = (SkullMeta) NEXT_PAGE_ITEM.getItemMeta();
		nextSkullMeta.setOwner("MHF_ArrowRight");
		nextSkullMeta.setDisplayName(ChatColor.RED + "Next page ≫");
		NEXT_PAGE_ITEM.setItemMeta(nextSkullMeta);
	}

	@Override
	public void prepare() {
		// Calculate the number of enderchests to display in the inventory
		int min = this.getFirstChestIndex();
		int nb = Math.min(min + PER_PAGE + 2, Config.maxEnderchests);

		// Clear any previous items
		this.clear();

		// Adding chest items
		for (int num = min; num < nb; num++) {
			EnderChest ec = this.manager.getEnderChest(this.owner, num);
			// Reload chest's metas before.
			ec.reloadMeta();

			if (!ec.isAccessible() && Config.onlyShowAccessibleEnderchests)
				continue;

			this.setItem(num - min, this.getItemStackOf(ec));
		}

		// Adding previous page item (if the user is not on the first page)
		if (this.page > 1) {
			this.removeItemAt(52);
			this.setItem(52, PREV_PAGE_ITEM);
			this.removeItemAt(53);
		}
		// Adding next page item (if there is more chests than the current page can display)
		if (min + PER_PAGE < Config.maxEnderchests) {
			this.removeItemAt(53);
			this.setItem(53, NEXT_PAGE_ITEM);
		}
	}

	@Override
	public boolean onClick(Player player, int slot) {
		EUtil.runSync(() -> {
			// Check for previous/next page
			if (slot >= PER_PAGE && this.getItemAt(slot).getType() == Material.SKULL_ITEM) {
				if (slot == PER_PAGE) this.page--;
				else if (slot == PER_PAGE + 1) this.page++;

				this.prepare();
				this.updateInventory();
				return;
			}

			// Check for a chest (with the slot and the index of the first chest in the menu)
			EnderChest chest = this.manager.getEnderChest(this.owner, this.getFirstChestIndex() + slot);

			// Reload the chest's metas before opening it.
			// It allows to check if the player has kept his permission
			// while he opened the Hub menu.
			chest.reloadMeta();

			if (chest.isAccessible()) {
				chest.openContainerFor(player);
				EUtil.playSound(player, "CLICK", "UI_BUTTON_CLICK");
			} else {
				EUtil.playSound(player, "ANVIL_BREAK", "BLOCK_ANVIL_BREAK");
			}
		});

		return true;
	}

	@Override
	public void onClose(Player player) {

	}

	/**
	 * Generate an itemstack with informations of an enderchest
	 * (capacity, accessibility, number, etc.)
	 *
	 * @param ec The enderchest processed
	 * @return The itemstack generated
	 */
	private ItemStack getItemStackOf(EnderChest ec) {
		boolean accessible = ec.isAccessible();
		double fillPerc = ec.getFillPercentage();

		ChatColor chatColor = this.getPercentageColor(fillPerc);
		DyeColor dyeColor = EUtil.getDyeColorFromChatColor(chatColor);

		if (!accessible) dyeColor = DyeColor.BLACK;

		ItemStack itemstack = new ItemStack(Material.STAINED_GLASS_PANE, 1, dyeColor.getWoolData());
		ChatColor titleColor = accessible ? ChatColor.GREEN : ChatColor.RED;

		ItemMeta meta = itemstack.getItemMeta();
		String counter = chatColor + "(" + ec.getSize() + "/" + ec.getMaxSize() + ")" + titleColor;
		String percent = chatColor + "(" + String.format("%.0f", ec.getFillPercentage()*100) + "%)" + titleColor;

		String metaTitle = titleColor + Locale.menuPaneTitle;

		metaTitle = metaTitle.replace("%num%", String.valueOf(ec.getNum() + 1)).replace("%counter%", counter).replace("%percent%", percent);
		meta.setDisplayName(metaTitle);

		List<String> lore = new ArrayList<>();

		if (!accessible)
			lore.add(Locale.menuChestLocked);

		if (ec.isFull())
			lore.add(Locale.menuChestFull);
		else if (ec.isEmpty())
			lore.add(Locale.menuChestEmpty);

		meta.setLore(lore);

		itemstack.setItemMeta(meta);
		return itemstack;
	}

	/**
	 * Get the color in terms of a percentage between 0 and 1
	 * @param perc The percentage
	 * @return The chat color generated
	 */
	private ChatColor getPercentageColor(double perc) {
		if (perc >= 1)
			return ChatColor.DARK_RED;
		if (perc >= .5)
			return ChatColor.GOLD;

		return ChatColor.GREEN;
	}

	/**
	 * Calculate the minimum chest's number in terms of the current page
	 * @return The first chest's index for the current page
	 */
	private int getFirstChestIndex() {
		return this.page == 1 ? 0 : Math.max(0, (this.page - 1) * PER_PAGE + 1);
	}

}