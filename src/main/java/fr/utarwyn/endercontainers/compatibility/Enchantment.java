package fr.utarwyn.endercontainers.compatibility;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public enum Enchantment implements CompatibilityObject {

	PROTECTION_ENVIRONMENTAL(0),
	PROTECTION_FIRE(1),
	PROTECTION_FALL(2),
	PROTECTION_EXPLOSIONS(3),
	PROTECTION_PROJECTILE(4),
	OXYGEN(5),
	WATER_WORKER(6),
	THORNS(7),
	DEPTH_STRIDER(8),
	FROST_WALKER(9),
	BINDING_CURSE(10),
	DAMAGE_ALL(16),
	DAMAGE_UNDEAD(17),
	DAMAGE_ARTHROPODS(18),
	KNOCKBACK(19),
	FIRE_ASPECT(20),
	LOOT_BONUS_MOBS(21),
	SWEEPING_EDGE(22),
	DIG_SPEED(32),
	SILK_TOUCH(33),
	DURABILITY(34),
	LOOT_BONUS_BLOCKS(35),
	ARROW_DAMAGE(48),
	ARROW_KNOCKBACK(49),
	ARROW_FIRE(50),
	ARROW_INFINITE(51),
	LUCK(61),
	LURE(62),
	LOYALTY(65),
	IMPALING(66),
	RIPTIDE(67),
	CHANNELING(68),
	MENDING(70),
	VANISHING_CURSE(71);

	private int id;

	private org.bukkit.enchantments.Enchantment bukkitEnchantment;

	Enchantment(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	@Override
	public org.bukkit.enchantments.Enchantment get() {
		if (this.bukkitEnchantment == null) {
			this.bukkitEnchantment = org.bukkit.enchantments.Enchantment.getByName(this.name());
		}

		return this.bukkitEnchantment;
	}

	public static Enchantment fromId(int id) {
		for (Enchantment enchantment : values()) {
			if (enchantment.getId() == id) {
				return enchantment;
			}
		}

		return null;
	}

	public static Enchantment fromBukkitEnchantment(org.bukkit.enchantments.Enchantment bukkitEnchantment) {
		for (Enchantment enchantment : values()) {
			if (enchantment.get().equals(bukkitEnchantment)) {
				return enchantment;
			}
		}

		return null;
	}

	public static Map<Enchantment, Integer> wrapItemEnchantments(ItemStack itemStack) {
		Map<Enchantment, Integer> enchants = new HashMap<>();

		for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> ench : itemStack.getEnchantments().entrySet()) {
			enchants.put(fromBukkitEnchantment(ench.getKey()), ench.getValue());
		}

		return enchants;
	}

}
