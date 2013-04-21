package couk.rob4001.iAuction.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BasicNewLayout {

	int acceptSlot;
	private int rows;

	public BasicNewLayout(int rows) {
		if (rows > 6) {
			rows = 6;
		} else if (rows <= 1) {
			rows = 1;
		}
		this.rows = rows;
		this.acceptSlot = rows * 9 - 1;
	}

	public void fillInventory(Inventory inventory) {
		inventory.setItem(this.acceptSlot, new ItemStack(279, 1));
	}

	public int getRows() {
		return this.rows;
	}

	public void cleanInventory(Inventory inventory) {
		inventory.setItem(this.acceptSlot, new ItemStack(0));
	}

}
