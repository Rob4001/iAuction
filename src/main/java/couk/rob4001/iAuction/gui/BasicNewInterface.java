package couk.rob4001.iAuction.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import couk.rob4001.iAuction.Auction;
import couk.rob4001.iAuction.Messaging;
import couk.rob4001.iAuction.iAuction;

public class BasicNewInterface extends BasicInterface {

	Inventory inventory;
	BasicNewLayout layout;
	private double price;
	private int time;

	public BasicNewInterface(BasicNewLayout layout, double price, int time) {
		this.price = price;
		this.time = time;
		this.inventory = Bukkit.getServer().createInventory(this,
				layout.getRows() * 9,
				"New Auction | P:" + price + " | T:" + time);
		this.layout = layout;
		layout.fillInventory(this.inventory);
	}

	@Override
	public Inventory getInventory() {
		return this.inventory;
	}

	@Override
	public void onClick(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			if (e.getRawSlot() == this.layout.acceptSlot) {
				p.closeInventory();
				GUIListener.cancelInventoryClickEvent(e);

				this.layout.cleanInventory(this.inventory);
				int count = 0;
				for (ItemStack is : this.inventory.getContents()) {
					if (is != null) {
						count++;
					}
				}
				if (count != 0) {
					iAuction.queue(new Auction(p, this.price, this.time,
							this.inventory));
				} else {
					Messaging.playerMessage(p, "start.noitems");
				}
			}
		}

	}

	@Override
	public void onClose(InventoryCloseEvent e) {
		if (e.getPlayer() instanceof Player) {
			Player p = (Player) e.getPlayer();

			this.layout.cleanInventory(this.inventory);

			for (ItemStack item : this.inventory.getContents()) {
				if (item != null) {
					p.getWorld().dropItemNaturally(p.getLocation(), item);
				}
			}
		}

	}

}
