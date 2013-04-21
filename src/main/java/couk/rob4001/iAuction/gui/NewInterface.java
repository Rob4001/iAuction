package couk.rob4001.iAuction.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class NewInterface implements InventoryHolder {

	Inventory inventory;
	NewLayout layout;

	public NewInterface(NewLayout layout) {
		this.inventory = Bukkit.getServer().createInventory(this, 54,
				"NewAuction");
		this.inventory.setMaxStackSize(1000);
		this.layout = layout;
		layout.fillInventory(this.inventory);
	}

	@Override
	public Inventory getInventory() {
		return this.inventory;
	}

}
