package couk.rob4001.util;

import java.util.ArrayList;

import net.milkbowl.vault.item.Items;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.flobi.WhatIsIt.WhatIsIt;


import couk.rob4001.iAuction.iAuction;
import couk.rob4001.iAuction.gui.BasicCollectInterface;
import couk.rob4001.iAuction.gui.BasicTempInterface;
import couk.rob4001.util.cardboard.CardboardBox;

public class InventoryUtil {
	
	public static Inventory changeTitle(Inventory inv ,String s){
		if(s.length() >32){
			s = s.substring(0,31);
		}
		Inventory inv2=Bukkit.createInventory(inv.getHolder(), inv.getSize(), s);
		inv2.setContents(inv.getContents());
		return inv2;
	}
	
	public static Inventory changeTitle(Inventory inv, String name,String bid,String time){
		String part = " | Bid: "+bid +" | T:" + time;
		int length = 32 -part.length();

		if(name.length() > length){
			name = name.substring(0,length-1);
		}
		
		return changeTitle(inv,name+part);
	}
	
	public static Inventory changeHolder(Inventory inv, InventoryHolder hold){
		Inventory inv2=Bukkit.createInventory(hold, inv.getSize(), inv.getTitle());
		inv2.setContents(inv.getContents());
		return inv2;
	}
	
	public static ArrayList<CardboardBox> box(Inventory inv){
		ArrayList<CardboardBox> cb = new ArrayList<CardboardBox>();
		for(ItemStack is:inv.getContents()){
			if(is!=null){
			cb.add(new CardboardBox(is));
			}
		}
		return cb;
	}
	
	public static BasicCollectInterface setupCollect(Player player){
		
		BasicTempInterface tmp = new BasicTempInterface();

		Inventory inv = Bukkit.createInventory(tmp, iAuction.getInstance().getConfig().getInt("start.rows") * 9,
				player.getDisplayName());
		ArrayList<CardboardBox> box = iAuction.getInstance().lots
				.get(player.getDisplayName());
		ItemStack[] is = new ItemStack[box.size()];
		for (int i = 0; i < box.size(); i++) {
			is[i] = box.get(i).unbox();
		}

		inv.setContents(is);
		tmp.setInv(inv);
		
		return new BasicCollectInterface(inv,player);
	}

	public static String parseItems(Inventory inv) {
		int count = iAuction.getInstance().getConfig().getInt("list.maxinfoitems");
		
		boolean first = true;
		boolean more = false;
		String msg = "";
		for(ItemStack is:inv.getContents()){
			if (is != null && count > 0){
				if(first){
					msg+= parseItem(is);
					first = false;
				}else{
					msg+= ",";
					msg+= parseItem(is);
				}
				count--;
			}else if(is !=null){
				more = true;
			}
			
		}
		if (more == true){
			msg += "And More!";
		}
		return msg;
	}

	private static String parseItem(ItemStack is) {
		String item = "";
		item +=is.getAmount() + " X ";
		boolean ench = is.getItemMeta().hasEnchants();
		if(ench){
			item += "Enchanted ";
		}
		if(is.getItemMeta().hasDisplayName()){
			item+= is.getItemMeta().getDisplayName()+ " ";
			item+= "["+matName(is)+"] ";
		}else{
			item+= matName(is)+" ";
		}
		
		
		return item;
	}

	private static String matName(ItemStack is) {
		if (iAuction.wiienabled){
			return WhatIsIt.itemName(is, true);
		}else if (Items.itemByStack(is)!=null){
			return Items.itemByStack(is).getName();
		}else{
			return is.getType().toString();
		}
	}

}
