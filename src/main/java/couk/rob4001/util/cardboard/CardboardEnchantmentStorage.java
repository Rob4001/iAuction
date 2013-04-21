package couk.rob4001.util.cardboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class CardboardEnchantmentStorage extends CardboardMeta {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1576846577475524595L;
	private HashMap<CardboardEnchantment, Integer> stored;

	public CardboardEnchantmentStorage(ItemMeta im) {
		super(im);
		EnchantmentStorageMeta em = (EnchantmentStorageMeta) im;
		HashMap<CardboardEnchantment, Integer> map = new HashMap<CardboardEnchantment, Integer>();

		Map<Enchantment, Integer> enchantments = em.getStoredEnchants();

		for (Enchantment enchantment : enchantments.keySet()) {
			map.put(new CardboardEnchantment(enchantment),
					enchantments.get(enchantment));
		}
		this.stored = map;
	}

	@Override
	public ItemMeta unbox(Material material) {
		EnchantmentStorageMeta im = (EnchantmentStorageMeta) super
				.unbox(material);
		HashMap<Enchantment, Integer> map = new HashMap<Enchantment, Integer>();

		for (CardboardEnchantment cEnchantment : this.stored.keySet()) {
			map.put(cEnchantment.unbox(), this.stored.get(cEnchantment));
		}

		for (Entry<Enchantment, Integer> e : map.entrySet()) {
			im.addEnchant(e.getKey(), e.getValue(), true);
		}
		return im;
	}

}
