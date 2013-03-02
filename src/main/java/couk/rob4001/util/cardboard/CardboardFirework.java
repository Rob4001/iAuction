package couk.rob4001.util.cardboard;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;

public class CardboardFirework extends CardboardMeta {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8197117437485464204L;
	private int power;
	private ArrayList<CardboardFireworkEffect> effects;

	public CardboardFirework(ItemMeta im) {
		super(im);
		FireworkMeta fm = (FireworkMeta)im;
		power = fm.getPower();
		effects = new ArrayList<CardboardFireworkEffect>();
		for (FireworkEffect fe:fm.getEffects()){
			effects.add(new CardboardFireworkEffect(fe));
		}
	}
	
	@Override
	public ItemMeta unbox(Material material) {
		FireworkMeta im = (FireworkMeta)super.unbox(material);
		im.setPower(power);
		for(CardboardFireworkEffect eff:effects){
			im.addEffect(eff.unbox());
		}
		return im;
	}

}
