package couk.rob4001.util.cardboard;

import org.bukkit.Material;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class CardboardFireworkEffectMeta extends CardboardMeta {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6725457657724399501L;
	private CardboardFireworkEffect effect;

	public CardboardFireworkEffectMeta(ItemMeta im) {
		super(im);
		FireworkEffectMeta fm = (FireworkEffectMeta)im;
		effect = new CardboardFireworkEffect(fm.getEffect());
	}
	
	public ItemMeta unbox(Material material) {
		FireworkEffectMeta im = (FireworkEffectMeta)super.unbox(material);
		im.setEffect(effect.unbox());
		return im;
	}

}
