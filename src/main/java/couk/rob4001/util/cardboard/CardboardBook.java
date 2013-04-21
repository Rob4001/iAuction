package couk.rob4001.util.cardboard;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class CardboardBook extends CardboardMeta {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6760653207974935396L;
	private final String author, title;
	private List<String> pages;

	public CardboardBook(ItemMeta im) {
		super(im);
		BookMeta bm = (BookMeta) im;
		this.author = bm.getAuthor();
		this.title = bm.getTitle();
		this.pages = bm.getPages();
	}

	@Override
	public ItemMeta unbox(Material material) {
		BookMeta im = (BookMeta) super.unbox(material);
		im.setAuthor(this.author);
		im.setTitle(this.title);
		im.setPages(this.pages);
		return im;
	}

}
