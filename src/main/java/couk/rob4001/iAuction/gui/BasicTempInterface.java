package couk.rob4001.iAuction.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BasicTempInterface implements InventoryHolder {

	private Inventory inv;

	@Override
	public Inventory getInventory() {
		return this.inv;
	}

	public void setInv(Inventory inv) {
		this.inv = inv;

	}

}
