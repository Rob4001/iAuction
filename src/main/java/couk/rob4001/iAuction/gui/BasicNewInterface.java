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
public class BasicNewInterface extends BasicInterface{


	Inventory inventory;
	BasicNewLayout layout;
	private double price;
	private int time;

	public BasicNewInterface( BasicNewLayout layout ,double price,int time){
		this.price = price;
		this.time = time;
		this.inventory = Bukkit.getServer().createInventory( this, layout.getRows()*9, "New Auction | P:"+ price +" | T:"+ time );
		this.layout = layout; 
		layout.fillInventory( inventory );
	}

	public Inventory getInventory() {
		return inventory;
	}

	@Override
	public void onClick(InventoryClickEvent e) {
		if(e.getWhoClicked() instanceof Player){
			Player p = (Player)e.getWhoClicked();
			if (e.getRawSlot() == layout.acceptSlot){
			p.closeInventory();
			GUIListener.cancelInventoryClickEvent(e);
			
			layout.cleanInventory(inventory);
			int count = 0;
			for(ItemStack is:inventory.getContents()){
				if (is != null)count ++;
			}
			if (count != 0){
			iAuction.queue(new Auction(p,price,time,inventory));
			}else{
				Messaging.playerMessage(p, "start.noitems");
			}
			}
		}
		
	}

	@Override
	public void onClose(InventoryCloseEvent e) {
		if(e.getPlayer() instanceof Player){
			Player p = (Player)e.getPlayer();
			
			layout.cleanInventory(inventory);
			
			for (ItemStack item: inventory.getContents()){
				if(item != null){
				p.getWorld().dropItemNaturally(p.getLocation(), item);
				}
			}
		}
		
	}

	
	

}
