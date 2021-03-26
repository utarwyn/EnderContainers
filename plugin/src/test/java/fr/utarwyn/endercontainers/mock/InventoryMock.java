package fr.utarwyn.endercontainers.mock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class InventoryMock implements Inventory {

    private final InventoryHolder holder;

    private final Map<Integer, ItemStack> contents;

    private final int size;

    private int maxStackSize;

    public InventoryMock(InventoryHolder holder, int size) {
        this.holder = holder;
        this.size = size;
        this.contents = new HashMap<>(size);
        this.maxStackSize = 0;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public int getMaxStackSize() {
        return this.maxStackSize;
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStackSize = size;
    }

    @Override
    public ItemStack getItem(int index) {
        return this.contents.get(index);
    }

    @Override
    public void setItem(int index, ItemStack item) {
        if (index < this.size) {
            this.contents.put(index, item);
        }
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... items) throws IllegalArgumentException {
        return new HashMap<>(this.contents);
    }

    @Override
    public HashMap<Integer, ItemStack> removeItem(ItemStack... items) throws IllegalArgumentException {
        return new HashMap<>(this.contents);
    }

    @Override
    public ItemStack[] getContents() {
        ItemStack[] items = new ItemStack[this.size];
        for (int i = 0; i < this.size; i++) {
            items[i] = this.getItem(i);
        }
        return items;
    }

    @Override
    public void setContents(ItemStack[] items) throws IllegalArgumentException {
        this.clear();
        for (int i = 0; i < items.length; i++) {
            this.setItem(i, items[i]);
        }
    }

    @Override
    public ItemStack[] getStorageContents() {
        return new ItemStack[0];
    }

    @Override
    public void setStorageContents(ItemStack[] items) throws IllegalArgumentException {

    }

    @Override
    public boolean contains(Material material) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean contains(ItemStack item) {
        return this.contents.containsValue(item);
    }

    @Override
    public boolean contains(Material material, int amount) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean contains(ItemStack item, int amount) {
        return false;
    }

    @Override
    public boolean containsAtLeast(ItemStack item, int amount) {
        return false;
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(Material material) throws IllegalArgumentException {
        return null;
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(ItemStack item) {
        return null;
    }

    @Override
    public int first(Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public int first(ItemStack item) {
        return 0;
    }

    @Override
    public int firstEmpty() {
        return 0;
    }

    @Override
    public void remove(Material material) throws IllegalArgumentException {

    }

    @Override
    public void remove(ItemStack item) {

    }

    @Override
    public void clear(int index) {
        this.contents.remove(index);
    }

    @Override
    public void clear() {
        this.contents.clear();
    }

    @Override
    public List<HumanEntity> getViewers() {
        return new ArrayList<>();
    }

    @Override
    public InventoryType getType() {
        return null;
    }

    @Override
    public InventoryHolder getHolder() {
        return this.holder;
    }

    @Override
    public ListIterator<ItemStack> iterator() {
        return new ArrayList<>(this.contents.values()).listIterator();
    }

    @Override
    public ListIterator<ItemStack> iterator(int index) {
        return Collections.singletonList(this.contents.get(index)).listIterator();
    }

    @Override
    public Location getLocation() {
        return null;
    }

}
