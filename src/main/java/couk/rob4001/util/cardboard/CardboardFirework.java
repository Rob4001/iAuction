package couk.rob4001.util.cardboard;

import java.util.ArrayList;

import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class CardboardFirework extends CardboardMeta {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8197117437485464204L;
	private int power;
	private ArrayList<CardboardFireworkEffect> effects;

	public CardboardFirework(ItemMeta im) {
		super(im);
		FireworkMeta fm = (FireworkMeta) im;
		this.power = fm.getPower();
		this.effects = new ArrayList<CardboardFireworkEffect>();
		for (FireworkEffect fe : fm.getEffects()) {
			this.effects.add(new CardboardFireworkEffect(fe));
		}
	}

	@Override
	public ItemMeta unbox(Material material) {
		FireworkMeta im = (FireworkMeta) super.unbox(material);
		im.setPower(this.power);
		for (CardboardFireworkEffect eff : this.effects) {
			im.addEffect(eff.unbox());
		}
		return im;
	}

}
