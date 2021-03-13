package fr.utarwyn.endercontainers.mock;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class SkullItemMetaMock extends ItemMetaMock implements SkullMeta {

    public SkullItemMetaMock() {
        super();
    }

    public SkullItemMetaMock(ItemMeta meta) {
        super(meta);
    }

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public boolean hasOwner() {
        return false;
    }

    @Override
    public boolean setOwner(String owner) {
        return false;
    }

    @Override
    public OfflinePlayer getOwningPlayer() {
        return null;
    }

    @Override
    public boolean setOwningPlayer(OfflinePlayer owner) {
        return false;
    }

    @Override
    public SkullItemMetaMock clone() {
        return this;
    }

}
