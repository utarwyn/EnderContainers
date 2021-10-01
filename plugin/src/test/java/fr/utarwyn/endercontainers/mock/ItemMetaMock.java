package fr.utarwyn.endercontainers.mock;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.*;

/**
 * Mocked item meta to manipulate items with test units.
 *
 * @author Utarwyn
 */
public class ItemMetaMock implements ItemMeta, Damageable {

    private String displayName;

    private List<String> lore;

    private int damage;

    private Map<Enchantment, Integer> enchants;

    public ItemMetaMock() {
        this.displayName = "";
        this.lore = new ArrayList<>();
        this.enchants = new HashMap<>();
    }

    public ItemMetaMock(ItemMeta meta) {
        this();
        this.enchants = new HashMap<>(meta.getEnchants());
        if (meta.hasDisplayName()) {
            this.displayName = meta.getDisplayName();
        }
        if (meta.hasLore()) {
            this.lore = meta.getLore();
        }
        if (meta instanceof Damageable) {
            this.damage = ((Damageable) meta).getDamage();
        }
    }

    @SuppressWarnings("unchecked")
    public static ItemMeta deserialize(Map<String, Object> args) {
        ItemMetaMock mock = new ItemMetaMock();
        mock.displayName = (String) args.get("displayName");
        mock.lore = (List<String>) args.get("lore");
        mock.enchants = (Map<Enchantment, Integer>) args.get("enchants");
        mock.damage = (Integer) args.get("damage");
        return mock;
    }

    @Override
    public boolean hasDamage() {
        return this.damage > 0;
    }

    @Override
    public int getDamage() {
        return this.damage;
    }

    @Override
    public void setDamage(int i) {
        this.damage = i;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result + ((lore == null) ? 0 : lore.hashCode());
        result = prime * result + enchants.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemMeta) {
            ItemMeta meta = (ItemMeta) obj;
            return isLoreEquals(meta) && isDisplayNameEqual(meta);
        } else {
            return false;
        }
    }

    @Override
    public boolean hasDisplayName() {
        return Objects.nonNull(this.displayName);
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(String s) {
        this.displayName = s;
    }

    @Override
    public boolean hasLocalizedName() {
        return false;
    }

    @Override
    public String getLocalizedName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocalizedName(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasLore() {
        return !this.lore.isEmpty();
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(this.lore);
    }

    @Override
    public void setLore(List<String> list) {
        this.lore = list;
    }

    @Override
    public boolean hasCustomModelData() {
        return false;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public void setCustomModelData(Integer integer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasEnchants() {
        return !this.enchants.isEmpty();
    }

    @Override
    public boolean hasEnchant(Enchantment enchantment) {
        return this.enchants.containsKey(enchantment);
    }

    @Override
    public int getEnchantLevel(Enchantment enchantment) {
        return this.enchants.getOrDefault(enchantment, 0);
    }

    @Override
    public Map<Enchantment, Integer> getEnchants() {
        return Collections.unmodifiableMap(this.enchants);
    }

    @Override
    public boolean addEnchant(Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
        Integer existingLevel = this.enchants.get(enchantment);
        if (Objects.nonNull(existingLevel) && existingLevel.equals(level)) {
            return false;
        }

        if (ignoreLevelRestriction ||
                (level >= enchantment.getStartLevel() && level <= enchantment.getMaxLevel())) {
            this.enchants.put(enchantment, level);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeEnchant(Enchantment enchantment) {
        return Objects.nonNull(this.enchants.remove(enchantment));
    }

    @Override
    public boolean hasConflictingEnchant(Enchantment enchantment) {
        boolean b = this.hasEnchants() && enchants.remove(enchantment) != null;
        if (enchants != null && enchants.isEmpty()) {
            enchants = null;
        }
        return b;
    }

    @Override
    public void addItemFlags(ItemFlag... itemFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeItemFlags(ItemFlag... itemFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<ItemFlag> getItemFlags() {
        return new HashSet<>();
    }

    @Override
    public boolean hasItemFlag(ItemFlag itemFlag) {
        return false;
    }

    @Override
    public boolean isUnbreakable() {
        return false;
    }

    @Override
    public void setUnbreakable(boolean b) {

    }

    @Override
    public boolean hasAttributeModifiers() {
        return false;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        return HashMultimap.create();
    }

    @Override
    public void setAttributeModifiers(Multimap<Attribute, AttributeModifier> multimap) {

    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot) {
        return HashMultimap.create();
    }

    @Override
    public Collection<AttributeModifier> getAttributeModifiers(Attribute attribute) {
        return new HashSet<>();
    }

    @Override
    public boolean addAttributeModifier(Attribute attribute, AttributeModifier attributeModifier) {
        return false;
    }

    @Override
    public boolean removeAttributeModifier(Attribute attribute) {
        return false;
    }

    @Override
    public boolean removeAttributeModifier(EquipmentSlot equipmentSlot) {
        return false;
    }

    @Override
    public boolean removeAttributeModifier(Attribute attribute, AttributeModifier attributeModifier) {
        return false;
    }

    @Override
    public CustomItemTagContainer getCustomTagContainer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setVersion(int i) {

    }

    @Override
    public ItemMetaMock clone() {
        return this;
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("displayName", this.getDisplayName());
        map.put("lore", this.getLore());
        map.put("enchants", this.getEnchants());
        map.put("damage", this.getDamage());
        return map;
    }

    private boolean isLoreEquals(ItemMeta meta) {
        if (!meta.hasLore()) {
            return !this.hasLore();
        }

        List<String> otherLore = meta.getLore();
        if (lore.size() == otherLore.size()) {
            for (int i = 0; i < lore.size(); i++) {
                if (!lore.get(i).equals(otherLore.get(i)))
                    return false;
            }
            return true;
        }
        return false;
    }

    private boolean isDisplayNameEqual(ItemMeta meta) {
        if (!meta.hasDisplayName()) return !this.hasDisplayName();
        return meta.hasDisplayName() && displayName.equals(meta.getDisplayName());
    }

}
