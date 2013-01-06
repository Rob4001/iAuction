package couk.rob4001.util.cardboard;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class CardboardBook extends CardboardMeta{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6760653207974935396L;
	private final String author,title;
	private List<String> pages;

	public CardboardBook(ItemMeta im) {
		super(im);
		BookMeta bm = (BookMeta)im;
		author = bm.getAuthor();
		title = bm.getTitle();
		pages = bm.getPages();
	}
	
	@Override
	public ItemMeta unbox(Material material) {
		BookMeta im = (BookMeta)super.unbox(material);
		im.setAuthor(author);
		im.setTitle(title);
		im.setPages(pages);
		return im;
	}

}
