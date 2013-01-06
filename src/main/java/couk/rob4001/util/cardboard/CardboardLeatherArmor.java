package couk.rob4001.util.cardboard;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class CardboardLeatherArmor extends CardboardMeta {

	/**
	 * 
	 */
	private static final long serialVersionUID = 627664964441801688L;
	private final int color;

	public CardboardLeatherArmor(ItemMeta im) {
		super(im);
		LeatherArmorMeta lm = (LeatherArmorMeta)im;
		color = lm.getColor().asRGB();
		
	}
	
	@Override
	public ItemMeta unbox(Material material) {
		LeatherArmorMeta im = (LeatherArmorMeta)super.unbox(material);
		im.setColor(Color.fromRGB(color));
		return im;
	}

}
