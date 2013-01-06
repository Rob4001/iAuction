package couk.rob4001.iAuction.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class NewLayout {
	
	//Slots
	
	int[] itemSlots = {0,1,2,3,4,5,
			9,10,11,12,13,14,
			18,19,20,21,22,23,
			27,28,29,30,31,32,
			36,37,38,39,40,41,
			45,46,47,48,49,50
	};
	int[] separatorSlots = {6,15,24,33,42,51};
	int acceptSlot = 53;
	int[] timeSlots = {7,16,25,34};
	int timeSlot = 43;
	int[] priceSlots = {8,17,26,35};
	int priceSlot = 44;
	
	int[] prices = {1,10,100,1000};
	int[] times ={1,5,10,60};
	
	public void fillInventory(Inventory inventory){
		for ( int slot : separatorSlots ) 	inventory.setItem( slot , new ItemStack( 280, 0 ) );
		for ( int x=0 ; x < 4;x++) inventory.setItem( (x*9) +7 , new ItemStack( 347, times[x]) );
		for ( int x=0 ; x < 4;x++) inventory.setItem( (x*9) +8 , new ItemStack( 371,prices[x] ) );
		inventory.setItem( acceptSlot , new ItemStack( 279, 1) );
		inventory.setItem( timeSlot , new ItemStack( 347, 1) );
		inventory.setItem( priceSlot , new ItemStack( 371, 1) );
	}
	
	
	

}
