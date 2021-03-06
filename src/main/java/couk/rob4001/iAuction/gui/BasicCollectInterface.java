package couk.rob4001.iAuction.gui;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import couk.rob4001.iAuction.iAuction;
import couk.rob4001.util.InventoryUtil;
import couk.rob4001.util.cardboard.CardboardBox;

public class BasicCollectInterface extends BasicInterface {

	private Inventory inventory;
	private Player owner;

	public BasicCollectInterface(Inventory inv, Player owner) {
		this.inventory = InventoryUtil.changeHolder(inv, this);
		this.owner = owner;
	}

	@Override
	public Inventory getInventory() {
		return this.inventory;
	}

	@Override
	public void onClick(InventoryClickEvent e) {

		if (e.getRawSlot() >= this.inventory.getSize()) {
			if (e.getWhoClicked() instanceof Player) {
				if (e.getCurrentItem().getType() != Material.AIR) {
					e.setCancelled(true);
				}

			}

		}

	}

	@Override
	public void onClose(InventoryCloseEvent e) {
		for (ItemStack item : this.inventory.getContents()) {
			if (item != null) {
				ArrayList<CardboardBox> cb = new ArrayList<CardboardBox>();
				for (ItemStack is : this.inventory.getContents()) {
					if (is != null) {
						cb.add(new CardboardBox(is));
					}
				}
				iAuction.getInstance().lots.put(this.owner.getName(), cb);
				return;
			}
		}
		iAuction.getInstance().lots.remove(this.owner.getName());

	}

	public void open() {
		this.owner.openInventory(this.inventory);
	}

}
