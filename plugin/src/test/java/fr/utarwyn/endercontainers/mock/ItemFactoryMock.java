package fr.utarwyn.endercontainers.mock;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

/**
 * @author Utarwyn <maximemalgorn@gmail.com>
 */
public class ItemFactoryMock implements ItemFactory {

    @Override
    public ItemMeta getItemMeta(Material material) {
        if (material == Material.PLAYER_HEAD) {
            return new SkullItemMetaMock();
        } else {
            return new ItemMetaMock();
        }
    }

    @Override
    public boolean isApplicable(ItemMeta meta, ItemStack stack) throws IllegalArgumentException {
        return isApplicable(meta, Objects.requireNonNull(stack).getType());
    }

    @Override
    public boolean isApplicable(ItemMeta meta, Material material) throws IllegalArgumentException {
        return meta instanceof ItemMetaMock;
    }

    @Override
    public boolean equals(ItemMeta meta1, ItemMeta meta2) throws IllegalArgumentException {
        return meta1 != null && meta1.equals(meta2);
    }

    @Override
    public ItemMeta asMetaFor(ItemMeta meta, ItemStack stack) throws IllegalArgumentException {
        return asMetaFor(meta, stack.getType());
    }

    @Override
    public ItemMeta asMetaFor(ItemMeta meta, Material material) throws IllegalArgumentException {
        if (material == Material.PLAYER_HEAD) {
            return new SkullItemMetaMock(meta);
        } else {
            return new ItemMetaMock(meta);
        }
    }

    @Override
    public Color getDefaultLeatherColor() {
        return Color.fromRGB(10511680);
    }

    @Override
    public Material updateMaterial(ItemMeta meta, Material material)
            throws IllegalArgumentException {
        return material;
    }

}
