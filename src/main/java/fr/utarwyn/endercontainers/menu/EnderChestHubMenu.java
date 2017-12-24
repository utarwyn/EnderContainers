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
	 * The enderchest manager
	 */
	private EnderChestManager manager;

	/**
	 * The owner of all enderchests in the menu
	 */
	private UUID owner;

	/**
	 * Construct the Hub menu for a player
	 * @param owner The player who owns enderchests in the menu
	 */
	public EnderChestHubMenu(UUID owner) {
		super(Locale.menuMainTitle.replace("%player%", Objects.requireNonNull(UUIDFetcher.getName(owner))));

		this.owner = owner;
		this.manager = EnderContainers.getInstance().getInstance(EnderChestManager.class);
	}

	@Override
	public void prepare() {
		int nb = Math.min(Config.maxEnderchests, 54);

		for (int i = 0; i < nb; i++) {
			EnderChest ec = this.manager.getEnderChest(this.owner, i);
			// Reload chest's metas before.
			ec.reloadMeta();

			if (!ec.isAccessible() && Config.onlyShowAccessibleEnderchests)
				continue;

			this.setItem(i, this.getItemStackOf(ec));
		}
	}

	@Override
	public boolean onClick(Player player, int slot) {
		EUtil.runSync(() -> {
			EnderChest chest = this.manager.getEnderChest(this.owner, slot);

			// Reload the chest's metas before opening it.
			// It allows to check if the player has kept his permission
			// while he opened the Hub menu.
			chest.reloadMeta();

			if (chest.isAccessible()) {
				chest.openContainerFor(player);
				EUtil.playSound(player.getLocation(), "CLICK", "UI_BUTTON_CLICK");
			} else {
				EUtil.playSound(player.getLocation(), "ANVIL_BREAK", "BLOCK_ANVIL_BREAK");
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

}