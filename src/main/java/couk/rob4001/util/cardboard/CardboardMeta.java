package couk.rob4001.util.cardboard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class CardboardMeta implements Serializable {

	private static final long serialVersionUID = -6517528980134985755L;
	private final String display;
	private final List<String> lore;
	private final HashMap<CardboardEnchantment, Integer> enchants;

	public CardboardMeta(ItemMeta im) {
		this.display = im.getDisplayName();
		this.lore = im.getLore();
		HashMap<CardboardEnchantment, Integer> map = new HashMap<CardboardEnchantment, Integer>();

		Map<Enchantment, Integer> enchantments = im.getEnchants();

		for (Enchantment enchantment : enchantments.keySet()) {
			map.put(new CardboardEnchantment(enchantment),
					enchantments.get(enchantment));
		}
		this.enchants = map;
	}

	public ItemMeta unbox(Material material) {
		ItemMeta im = Bukkit.getServer().getItemFactory().getItemMeta(material);
		im.setDisplayName(this.display);
		im.setLore(this.lore);

		HashMap<Enchantment, Integer> map = new HashMap<Enchantment, Integer>();

		for (CardboardEnchantment cEnchantment : this.enchants.keySet()) {
			map.put(cEnchantment.unbox(), this.enchants.get(cEnchantment));
		}

		for (Entry<Enchantment, Integer> e : map.entrySet()) {
			im.addEnchant(e.getKey(), e.getValue(), true);
		}

		return im;
	}

	public static CardboardMeta box(ItemMeta im) {
		if (im instanceof BookMeta)
			return new CardboardBook(im);
		if (im instanceof LeatherArmorMeta)
			return new CardboardLeatherArmor(im);
		if (im instanceof MapMeta)
			return new CardboardMap(im);
		if (im instanceof SkullMeta)
			return new CardboardSkull(im);
		if (im instanceof FireworkMeta)
			return new CardboardFirework(im);
		if (im instanceof FireworkEffectMeta)
			return new CardboardFireworkEffectMeta(im);
		if (im instanceof EnchantmentStorageMeta)
			return new CardboardEnchantmentStorage(im);
		return new CardboardMeta(im);
	}

}
