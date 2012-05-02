package net.umc.Rob4001.iAuction;

import java.text.DecimalFormat;

import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import net.umc.dev.InventoryManager;


import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionCommand implements CommandExecutor {
	private iAuction plugin;

	AuctionCommand(iAuction pl) {
		this.plugin = pl;
	}

	double roundTwoDecimals(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d)).doubleValue();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if ((sender instanceof Player)) {
			Player player = (Player) sender;
			if (args.length == 0) {
				helpcom(player);
				return true;
			}
			String set = args[0];

			if ((set.equalsIgnoreCase("start")) || (set.equalsIgnoreCase("s"))) {
				auctionStart(player, args);
				return true;
			}
			if ((set.equalsIgnoreCase("help")) || (set.equalsIgnoreCase("?"))) {
				helpcom(player);
				return true;
			}
			if (this.plugin.getAuc() != null) {
				if ((set.equalsIgnoreCase("end"))
						|| (set.equalsIgnoreCase("e"))) {
					auctionEnd(player, args);
					return true;
				}
				if ((set.equalsIgnoreCase("cancel"))
						|| (set.equalsIgnoreCase("c"))) {
					auctionCancel(player, args);
					return true;
				}
				if ((set.equalsIgnoreCase("info"))
						|| (set.equalsIgnoreCase("i"))) {
					auctionInfo(player, args);
					return true;
				}
				if ((set.equalsIgnoreCase("bid"))
						|| (set.equalsIgnoreCase("b"))) {
					auctionBid(player, args);
					return true;
				}
			} else {
				this.plugin.warn(player, "No auction Running!");
				return true;
			}
		} else {
			sender.sendMessage("Console Commands not accepted!");
			return true;
		}
		return false;
	}

	private void auctionInfo(Player player, String[] args) {
		if (!player.hasPermission("auction.info")) {
			this.plugin.warn(player, "You do not have permission!");
			return;
		}
		this.plugin.getAuc().Info(player);
	}

	private void auctionBid(Player player, String[] args) {
		if (args.length >= 2) {
			if (!player.hasPermission("auction.bid")) {
				this.plugin.warn(player, "You do not have permission!");
				return;
			}

			float bid = 0.0F;

			bid = Float.parseFloat(args[1]);

			if (this.plugin.getConfig().getBoolean("decimalcurrency")) {
				bid = (float) (Math.round(bid*100)/100.0);
			} else {
				bid = (long) bid;
			}

			if (bid < 0.0F) {
				this.plugin.warn(player, "Invalid Starting Bid");
				return;
			}
			if ((bid > (float) this.plugin.getConfig()
					.getLong("bidding.maxbid"))
					&& (this.plugin.getConfig().getLong("bidding.maxbid") != 0.0D)) {
				this.plugin.warn(player, "Starting Bid is too high!");
				return;
			}
			if ((bid < (float) this.plugin.getConfig()
					.getLong("bidding.minbid"))
					&& (this.plugin.getConfig().getLong("bidding.minbid") != 0.0D)) {
				this.plugin.warn(player, "Starting Bid is too low!");
				return;
			}
			
			if (this.plugin.getEco().has(player.getName(), bid)) {
				this.plugin.getAuc().bid(player, bid);
			} else
				this.plugin.warn(player, "You do not have enough money");
		} else {
			this.plugin.warn(player, "Not enough Arguments!");
		}
	}

	private void auctionEnd(Player player, String[] args) {
		if ((player.hasPermission("auction.admin"))
				|| (player.equals(this.plugin.getAuc().owner)))
			this.plugin.getAuc().stop();
		else
			this.plugin.warn(player, "You do not have permission!");
	}

	private void auctionCancel(Player player, String[] args) {
		if ((player.hasPermission("auction.admin"))
				|| (player.equals(this.plugin.getAuc().owner)))
			this.plugin.getAuc().cancel();
		else
			this.plugin.warn(player, "You do not have permission!");
	}

	private void auctionStart(Player player, String[] args) {
		if (this.plugin.getAuc() != null) {
			this.plugin.warn(player, "There is already a Auction Running");
			return;
		}
		if (!plugin.getConfig().getBoolean("allowincreative")&&player.getGameMode()==GameMode.CREATIVE){
			plugin.warn(player, "You are not allowed to auction in creative mode!");
			return;
		}
		
		if (args.length >= 5) {
			if (!player.hasPermission("auction.start")) {
				this.plugin.warn(player, "You do not have permission!");
				return;
			}

			ItemStack lot = null;
			int time = 0;
			float bid = 0.0F;

			time = Integer.parseInt(args[1]);

			int amount = Integer.parseInt(args[3]);
			if (amount < 1) {
				this.plugin.warn(player, "Invalid amount");
				return;
			}
			
			
			if(args[2].equalsIgnoreCase("hand")){
				if(player.getInventory().getItemInHand().getTypeId() == 0 ){
					plugin.warn(player, "There is nothing in your hand!");
					return;
				}
			    lot = player.getItemInHand().clone();
			    
			    lot.setAmount(amount);
			}else{
				ItemInfo ii = Items.itemByString(args[2]);
				if (ii == null) {
					this.plugin.warn(player, "Invalid Item or Amount");
					return;
				}
			lot = new ItemStack(ii.getType(),amount,ii.getSubTypeId());
			}
			
			if(plugin.getBannedItems().contains(lot.getType())){
				this.plugin.warn(player, "You are not allowed to auction this item!");
				return;
			}
			
			try{
			bid = Float.parseFloat(args[4]);
			}catch(NumberFormatException e){
				this.plugin.warn(player, "Invalid bid: Please only use numbers and decimal point!");
				return;
			}

			if (this.plugin.getConfig().getBoolean("decimalcurrency")) {
				bid = (float) ((Math.round(bid*100))/100.0);
			} else {
				bid = (long) bid;
			}

			

			if (!new InventoryManager(player).contains(lot, true, true)) {
				this.plugin.warn(player, "You do not have enough of that item!");
				return;
			}
			if (bid < 0.0F) {
				this.plugin.warn(player, "Invalid Starting Bid");
				return;
			}
			if (time < 0) {
				this.plugin.warn(player, "Invalid Time");
				return;
			}
			if ((bid > (float) this.plugin.getConfig().getLong("start.maxbid"))
					&& (this.plugin.getConfig().getLong("start.maxbid") != 0.0D)) {
				this.plugin.warn(player, "Starting Bid is too high!");
				return;
			}
			if ((time > this.plugin.getConfig().getInt("start.maxtime"))
					&& (this.plugin.getConfig().getInt("start.maxtime") != 0.0D)) {
				this.plugin.warn(player, "Time is too high!!");
				return;
			}
			if ((time < this.plugin.getConfig().getInt("start.mintime"))
					&& (this.plugin.getConfig().getInt("start.mintime") != 0.0D)) {
				this.plugin.warn(player, "Time is too low!");
				return;
			}

			this.plugin.New(lot, player, time, bid);
		} else {
			this.plugin.warn(player, "Not enough Arguments!");
		}
	}

	private void helpcom(Player player) {
		player.sendMessage(plugin.auxcolour + " -----[ " + plugin.titlecolour + "Auction Help" + plugin.auxcolour + " ]----- ");
		player.sendMessage(plugin.helpcolour + "/auction help ? " + plugin.auxcolour + "- Returns this");
		player.sendMessage(plugin.helpcolour + "/auction start|s " + plugin.helpvaluecolour + "<time> <item/hand> <amount> <starting price>");
		player.sendMessage(plugin.auxcolour + "Starts an auction for " + plugin.helpvaluecolour + "<time>" + plugin.auxcolour + " seconds with " + plugin.helpvaluecolour + "<amount>");
		player.sendMessage(plugin.auxcolour + "of " + plugin.helpvaluecolour + "<item/hand>" + plugin.auxcolour + " for " + plugin.helpvaluecolour + "<starting price>");
		player.sendMessage(plugin.helpcolour + "/auction bid|b " + plugin.helpvaluecolour + "<bid>" + plugin.auxcolour + "  - Bids the auction.");
		player.sendMessage(plugin.helpcolour + "/auction end|e" + plugin.auxcolour + " - Ends current auction.");
		player.sendMessage(plugin.helpcolour + "/auction info|i" + plugin.auxcolour + " - Returns auction information."); 
	}
}
