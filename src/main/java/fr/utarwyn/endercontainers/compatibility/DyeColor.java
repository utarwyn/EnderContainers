package fr.utarwyn.endercontainers.compatibility;

import org.bukkit.ChatColor;

public enum DyeColor implements CompatibilityObject {

	WHITE(ChatColor.WHITE),
	ORANGE(ChatColor.GOLD),
	MAGENTA(ChatColor.LIGHT_PURPLE),
	LIGHT_BLUE(ChatColor.BLUE),
	YELLOW(ChatColor.YELLOW),
	LIME(ChatColor.GREEN),
	PINK(ChatColor.LIGHT_PURPLE),
	GRAY(ChatColor.GRAY),
	LIGHT_GRAY(ChatColor.GRAY, "SILVER"),
	CYAN(ChatColor.AQUA),
	PURPLE(ChatColor.DARK_PURPLE),
	BLUE(ChatColor.DARK_BLUE),
	BROWN(ChatColor.GOLD),
	GREEN(ChatColor.DARK_GREEN),
	RED(ChatColor.DARK_RED),
	BLACK(ChatColor.BLACK);

	private String oldDyeKey;

	private ChatColor chatColor;

	private org.bukkit.DyeColor bukkitColor;

	DyeColor(ChatColor chatColor, String oldDyeKey) {
		this.oldDyeKey = oldDyeKey;
		this.chatColor = chatColor;
	}

	DyeColor(ChatColor chatColor) {
		this(chatColor, null);
	}

	@Override
	public org.bukkit.DyeColor get() {
		if (this.bukkitColor == null) {
			this.bukkitColor = org.bukkit.DyeColor.valueOf(
					ServerVersion.isOlderThan(ServerVersion.V1_13) && this.oldDyeKey != null
							? this.oldDyeKey : this.name()
			);
		}

		return this.bukkitColor;
	}

	public ChatColor toChatColor() {
		return this.chatColor;
	}

	public static DyeColor fromChatColor(ChatColor chatColor) {
		for (DyeColor dyeColor : values()) {
			if (dyeColor.toChatColor().equals(chatColor)) {
				return dyeColor;
			}
		}

		return null;
	}

}
