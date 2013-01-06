package couk.rob4001.iAuction.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public abstract class BasicInterface implements InventoryHolder {
	
	public abstract void onClick(InventoryClickEvent e);

	public abstract void onClose(InventoryCloseEvent e);

}
