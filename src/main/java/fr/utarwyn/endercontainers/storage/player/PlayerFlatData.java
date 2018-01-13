package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.storage.FlatFile;
import fr.utarwyn.endercontainers.util.ItemSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

/**
 * Storage wrapper for player data (flatfile)
 * @since 2.0.0
 * @author Utarwyn
 */
public class PlayerFlatData extends PlayerData {

	private FlatFile flatFile;

	PlayerFlatData(UUID uuid) {
		super(uuid);
	}

	@Override
	protected void load() {
		this.flatFile = new FlatFile("data/" + this.getMinimalUUID() + ".yml");
	}

	@Override
	protected void save() {
		this.flatFile.save();
	}

	@Override
	public HashMap<Integer, ItemStack> getEnderchestContents(EnderChest enderChest) {
		String path = "enderchests." + enderChest.getNum() + ".contents";

		if (!this.flatFile.getConfiguration().contains(path))
			return new HashMap<>();

		return ItemSerializer.deserialize(this.flatFile.getConfiguration().getString(path));
	}

	@Override
	public int getEnderchestRows(EnderChest enderChest) {
		String path = "enderchests." + enderChest.getNum() + ".rows";
		return this.flatFile.getConfiguration().contains(path) ? this.flatFile.getConfiguration().getInt(path) : 3;
	}

	@Override
	public void saveEnderchest(EnderChest enderChest) {
		String path = "enderchests." + enderChest.getNum() + ".";

		this.flatFile.getConfiguration().set(path + "rows", enderChest.getRows());
		this.flatFile.getConfiguration().set(path + "position", enderChest.getNum());
		this.flatFile.getConfiguration().set(path + "contents", ItemSerializer.serialize(enderChest.getContents()));
		this.flatFile.getConfiguration().set(path + "lastlocking", System.currentTimeMillis() / 1000); // UNIX format

		this.save();
	}

}
