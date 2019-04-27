package fr.utarwyn.endercontainers.storage.player;

import fr.utarwyn.endercontainers.database.DatabaseSet;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.util.ItemSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Storage wrapper for player data (MySQL)
 * @since 2.0.0
 * @author Utarwyn
 */
public class PlayerMySQLData extends PlayerData {

	private List<DatabaseSet> enderchestsDataset;

	PlayerMySQLData(UUID uuid) {
		super(uuid);
	}

	@Override
	protected void load() {
		this.enderchestsDataset = getDatabaseManager().getEnderchestsOf(this.getUUID());
		if (this.enderchestsDataset == null)
			this.enderchestsDataset = new ArrayList<>();
	}

	@Override
	protected void save() {

	}

	@Override
	public HashMap<Integer, ItemStack> getEnderchestContents(EnderChest enderChest) {
		for (DatabaseSet chestSet : this.enderchestsDataset)
			if (chestSet.getInteger("num") == enderChest.getNum())
				return ItemSerializer.deserialize(chestSet.getString("contents"));

		return new HashMap<>();
	}

	@Override
	public int getEnderchestRows(EnderChest enderChest) {
		for (DatabaseSet chestSet : this.enderchestsDataset)
			if (chestSet.getInteger("num") == enderChest.getNum())
				return chestSet.getInteger("rows");

		return 3;
	}

	@Override
	public void saveEnderchest(EnderChest enderChest) {
		boolean insert = true;

		for (DatabaseSet set : this.enderchestsDataset)
			if (set.getInteger("num") == enderChest.getNum() && set.getString("owner").equals(enderChest.getOwner().toString())) {
				insert = false;
				break;
			}

		String contents = ItemSerializer.serialize(enderChest.getContents());

		getDatabaseManager().saveEnderchest(
				insert,
				enderChest.getOwner(), enderChest.getNum(), enderChest.getRows(),
				contents
		);

		// If this is a new enderchest, we need to store it in memory.
		if (insert) {
			DatabaseSet set = new DatabaseSet();
			set.setObject("num", enderChest.getNum());
			set.setObject("owner", enderChest.getOwner().toString());
			set.setObject("contents", contents);
			set.setObject("rows", enderChest.getRows());

			this.enderchestsDataset.add(set);
		}
	}

}

