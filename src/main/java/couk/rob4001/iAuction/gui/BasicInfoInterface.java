package couk.rob4001.iAuction.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import couk.rob4001.util.InventoryUtil;

public class BasicInfoInterface extends BasicInterface {

	Inventory inventory;

	public BasicInfoInterface(Inventory inv) {

		this.inventory = InventoryUtil.changeHolder(inv, this);
	}

	@Override
	public Inventory getInventory() {
		return this.inventory;
	}

	@Override
	public void onClick(InventoryClickEvent e) {
		GUIListener.cancelInventoryClickEvent(e);
	}

	@Override
	public void onClose(InventoryCloseEvent e) {
	}

}
