package couk.rob4001.util.cardboard;
 
import java.io.Serializable;

import org.bukkit.inventory.ItemStack;
 
/**
* A serializable ItemStack
*/
public class CardboardBox implements Serializable {
    private static final long serialVersionUID = 729890133797629668L;
 
    private final int type, amount;
    private final short damage;
 


	private final CardboardMeta meta;
 
    public CardboardBox(ItemStack item) {
        this.type = item.getTypeId();
        this.amount = item.getAmount();
        this.damage = item.getDurability();
        
        this.meta = CardboardMeta.box(item.getItemMeta());
    }
 
    public ItemStack unbox() {
        ItemStack item = new ItemStack(type, amount, damage);
 
        item.setItemMeta(meta.unbox(item.getType()));
 
        return item;
    }
}