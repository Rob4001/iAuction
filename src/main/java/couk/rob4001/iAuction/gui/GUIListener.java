package couk.rob4001.iAuction.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent e) {

		InventoryHolder holder = e.getInventory().getHolder();

		if (holder instanceof BasicInterface) {
			((BasicInterface) holder).onClick(e);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClose(InventoryCloseEvent e) {

		InventoryHolder holder = e.getInventory().getHolder();

		if (holder instanceof BasicInterface) {
			((BasicInterface) holder).onClose(e);
		}
	}

	public static void cancelInventoryClickEvent(InventoryClickEvent event) {
		event.setCancelled(true);
	}

}
