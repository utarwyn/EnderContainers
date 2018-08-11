package fr.utarwyn.endercontainers.menu;

import fr.utarwyn.endercontainers.Config;
import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.compatibility.CompatibilityHelper;
import fr.utarwyn.endercontainers.compatibility.ServerVersion;
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
	 * Bukkit skull material
	 */
	private static final Material SKULL_MATERIAL;

	/**
	 * Represents the item to go to the previous page
	 */
	private static final ItemStack PREV_PAGE_ITEM;

	/**
	 * Represents the item to go to the next page
	 */
	private static final ItemStack NEXT_PAGE_ITEM;

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
		if (ServerVersion.is(ServerVersion.V1_13)) {
			SKULL_MATERIAL = Material.PLAYER_HEAD;
		} else {
			SKULL_MATERIAL = CompatibilityHelper.matchMaterial("SKULL_ITEM");
		}

		PREV_PAGE_ITEM = new ItemStack(SKULL_MATERIAL, 1, (short) 3);
		NEXT_PAGE_ITEM = new ItemStack(SKULL_MATERIAL, 1, (short) 3);

		SkullMeta prevSkullMeta = (SkullMeta) PREV_PAGE_ITEM.getItemMeta();
		prevSkullMeta.setOwner("MHF_ArrowLeft");
		prevSkullMeta.setDisplayName(ChatColor.RED + Locale.menuPreviousPage);
		PREV_PAGE_ITEM.setItemMeta(prevSkullMeta);

		SkullMeta nextSkullMeta = (SkullMeta) NEXT_PAGE_ITEM.getItemMeta();
		nextSkullMeta.setOwner("MHF_ArrowRight");
		nextSkullMeta.setDisplayName(ChatColor.RED + Locale.menuNextPage);
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
		int nbForNext = min + PER_PAGE;
		if (this.page == 1) nbForNext += 2;

		if (nbForNext < Config.maxEnderchests) {
			this.removeItemAt(53);
			this.setItem(53, NEXT_PAGE_ITEM);
		}
	}

	@Override
	public boolean onClick(Player player, int slot) {
		EUtil.runSync(() -> {
			// Check for previous/next page
			if (slot >= PER_PAGE && this.getItemAt(slot).getType() == SKULL_MATERIAL) {
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
		this.destroy();
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
		DyeColor dyeColor = ec.isAccessible() ? this.getDyePercentageColor(ec.getFillPercentage()) : DyeColor.BLACK;

		ItemStack itemStack;

		// TODO: maybe add an option to personalize the material here (instead of a glass pane)?
		if (ServerVersion.is(ServerVersion.V1_13)) {
			itemStack = new ItemStack(CompatibilityHelper.matchMaterial(dyeColor.name() + "_STAINED_GLASS_PANE"));
		} else {
			itemStack = new ItemStack(CompatibilityHelper.matchMaterial("STAINED_GLASS_PANE"), 1, dyeColor.getWoolData());
		}

		ItemMeta meta = itemStack.getItemMeta();

		List<String> lore = new ArrayList<>();

		// Update lore with the chest's status
		// TODO: maybe allow users to integrally personalize the description!
		if (!accessible)
			lore.add(Locale.menuChestLocked);
		if (ec.isFull())
			lore.add(Locale.menuChestFull);
		else if (ec.isEmpty())
			lore.add(Locale.menuChestEmpty);

		// Update itemstack metadata
		meta.setDisplayName(this.formatPaneTitle(ec, Locale.menuPaneTitle));
		meta.setLore(lore);

		itemStack.setItemMeta(meta);
		return itemStack;
	}

	/**
	 * Format the pane title from the configuration to have info about an enderchest.
	 *
	 * @param chest Enderchest represented by the pane
	 * @param title Original title got from the configuration
	 * @return Formatted title ready to be displayed in the menu
	 */
	private String formatPaneTitle(EnderChest chest, String title) {
		ChatColor fillingColor = this.getPercentageColor(chest.getFillPercentage());
		ChatColor accessibilityColor = chest.isAccessible() ? ChatColor.GREEN : ChatColor.RED;

		// Adding the color before all text
		title = accessibilityColor + title;

		// Separate all placeholders to improve performance
		if (title.contains("%num%")) {
			title = title.replace("%num%", String.valueOf(chest.getNum() + 1));
		}

		if (title.contains("%counter%")) {
			title = title.replace("%counter%", fillingColor + "(" + chest.getSize() + "/" + chest.getMaxSize() + ")" + accessibilityColor);
		}

		if (title.contains("%percent%")) {
			title = title.replace("%percent%", fillingColor + "(" + String.format("%.0f", chest.getFillPercentage() * 100) + "%)" + accessibilityColor);
		}

		// TODO: items number placeholder?

		return title;
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
	 * Get the dye color in terms of a percentage between 0 and 1
	 * @param perc The percentage
	 * @return The dye color used for the itemstack
	 */
	private DyeColor getDyePercentageColor(double perc) {
		if (perc >= 1)
			return DyeColor.RED;
		if (perc >= .5)
			return DyeColor.ORANGE;

		return DyeColor.LIME;
	}

	/**
	 * Calculate the minimum chest's number in terms of the current page
	 * @return The first chest's index for the current page
	 */
	private int getFirstChestIndex() {
		return this.page == 1 ? 0 : Math.max(0, (this.page - 1) * PER_PAGE + 1);
	}

}