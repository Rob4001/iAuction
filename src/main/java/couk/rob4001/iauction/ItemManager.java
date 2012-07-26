package couk.rob4001.iauction;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemManager {

	public static void giveItem(Player winner, ItemStack lot) {
		
		HashMap<Integer, ItemStack> remains = winner.getInventory()
				.addItem(lot);
		if(lot.getType()==Material.POTION &&isSplash(lot.getDurability())){
			int amt = lot.getAmount();
			short dmg = lot.getDurability();
			for (int i =0 ; i< amt;i++){
				
				winner.getInventory().getItem(winner.getInventory().first(lot)).setDurability(dmg |= 0x4000);
				amt -= lot.getMaxStackSize();
			}
		}
		for (ItemStack is : remains.values()) {
			winner.getWorld().dropItemNaturally(winner.getLocation(), is);
		}

	}

	public static boolean takeItem(Player owner, ItemStack lot) {
		HashMap<Integer, ItemStack> remains = owner.getInventory().removeItem(
				lot);
		return remains.size() == 0;
	}

	public static boolean has(Player player, ItemStack lot) {
		int remainder = lot.getAmount();
		for (ItemStack is : player.getInventory().getContents()) {

			if (is == null)
				continue;
			if (is.getType() == lot.getType()
					&& lot.getDurability() == is.getDurability()
					&& is.getEnchantments().equals(lot.getEnchantments())) {
				remainder -= is.getAmount();
			}
		}
		return remainder <= 0;

	}
	
	private static boolean isSplash(short durability) {
		String maxAmpStr = Integer.toBinaryString(durability);
		char[] arr = maxAmpStr.toCharArray();
		boolean[] binaryarray = new boolean[arr.length];
		for (int i = 0; i < maxAmpStr.length(); i++) {
			if (arr[i] == '1') {
				binaryarray[i] = true;
			} else if (arr[i] == '0') {
				binaryarray[i] = false;
			}
		}

		if (binaryarray.length > 14) {
			return true;
		} else {
			return false;
		}
	}

}
