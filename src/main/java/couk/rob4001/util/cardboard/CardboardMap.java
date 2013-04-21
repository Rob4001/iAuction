package couk.rob4001.util.cardboard;

import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;

public class CardboardMap extends CardboardMeta {

	private static final long serialVersionUID = -160932300608898337L;
	private final boolean scale;

	public CardboardMap(ItemMeta im) {
		super(im);
		this.scale = ((MapMeta) im).isScaling();
	}

	@Override
	public ItemMeta unbox(Material material) {
		MapMeta im = (MapMeta) super.unbox(material);
		im.setScaling(this.scale);
		return im;
	}

}