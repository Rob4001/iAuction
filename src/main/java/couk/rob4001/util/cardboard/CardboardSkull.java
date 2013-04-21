package couk.rob4001.util.cardboard;

import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class CardboardSkull extends CardboardMeta {

	private static final long serialVersionUID = -160932300608898337L;
	private final String owner;

	public CardboardSkull(ItemMeta im) {
		super(im);
		this.owner = ((SkullMeta) im).getOwner();
	}

	@Override
	public ItemMeta unbox(Material material) {
		SkullMeta im = (SkullMeta) super.unbox(material);
		im.setOwner(this.owner);
		return im;
	}

}
