package couk.rob4001.iauction;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import couk.rob4001.utility.functions;
import couk.rob4001.utility.items;

public class AuctionCommand {
	
	private iAuction plugin;
	public AuctionCommand(iAuction p){
		this.plugin = p;
	}
	
	private boolean suspendAllAuctions = false;
	  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    	Player player = null;
	    	Auction auction = null;
			// TODO: Figure out auction for context.
			// In the mean time, use public auction.
	    	AuctionScope scope = plugin.getScope(sender);
			auction = scope.getCurrentAuction();
			String userScope = "public_auction";
			String playerName = "";

	    	if (sender instanceof Player) {
	    		player = (Player) sender;
				playerName = player.getName();
	    	} else {
				playerName = "*console*";
	    	}

			if (
					cmd.getName().equalsIgnoreCase("auction") &&
					args.length > 0 &&
					args[0].equalsIgnoreCase("on")
			) {
				int index = plugin.voluntarilyDisabledUsers.indexOf(playerName);
				if (index != -1) {
					plugin.voluntarilyDisabledUsers.remove(index);
				}
				Messaging.sendMessage("auction-enabled", sender, null);
				functions.saveObject(plugin.voluntarilyDisabledUsers, "voluntarilyDisabledUsers.ser");
				return true;
			}
	     
	    	if (plugin.voluntarilyDisabledUsers.contains(playerName)) {
	    		plugin.voluntarilyDisabledUsers.remove(plugin.voluntarilyDisabledUsers.indexOf(playerName));
				Messaging.sendMessage("auction-fail-disabled", sender, null);
				plugin.voluntarilyDisabledUsers.add(playerName);
				functions.saveObject(plugin.voluntarilyDisabledUsers, "voluntarilyDisabledUsers.ser");
				return true;
			}
	    	
	    	if (suspendAllAuctions) {
		    	if (args.length == 1 && args[0].equalsIgnoreCase("resume")) {
					if (player != null && !iAuction.perms.has(player, "auction.admin")) {
		    			Messaging.sendMessage("no-permission", sender, null);
		    			return true;
					}
					// Suspend globally:
					suspendAllAuctions = false;
	    			Messaging.sendMessage("unsuspension-global", (Player) null, null);
					return true;
				}
				Messaging.sendMessage("suspension-global", sender, null);
	    		return true;
	    	}
	    	
	    	if (player != null && iAuction.suspendedUsers.contains(playerName.toLowerCase())) {
				Messaging.sendMessage("suspension-user", player, null);
				return true;
	    	}

	    	if (cmd.getName().equalsIgnoreCase("auction")) {
	    		if (args.length > 0) {
	    			if (args[0].equalsIgnoreCase("reload")) {
	    				if (player != null && !iAuction.perms.has(player, "auction.admin")) {
	    	    			Messaging.sendMessage("no-permission", sender, null);
	    	    			return true;
	    				}
	    				plugin.reloadConfig();
	    				plugin.reloadTextConfig();
		    			Messaging.sendMessage("plugin-reloaded", sender, null);
	    				return true;
	    			} else if (args[0].equalsIgnoreCase("orphans")) {
	    				if (player != null && !iAuction.perms.has(player, "auction.admin")) {
	    	    			Messaging.sendMessage("no-permission", sender, null);
	    	    			return true;
	    				}
	    				
	    				for(int i = 0; i < iAuction.orphanLots.size(); i++) {
	    					if (iAuction.orphanLots.get(i) != null) {
	    						AuctionLot lot = iAuction.orphanLots.get(i);
	    		    			Messaging.sendMessage(lot.getOwner() + ": " + lot.getQuantity() + " " + items.itemName(lot.getTypeStack()), sender, null);
	    					}
	    				}

	    				return true;
	    			} else if (args[0].equalsIgnoreCase("resume")) {
	    				if (player != null && !iAuction.perms.has(player, "auction.admin")) {
	    	    			Messaging.sendMessage("no-permission", sender, null);
	    	    			return true;
	    				}

						if (!iAuction.suspendedUsers.contains(args[1].toLowerCase())) {
			    			Messaging.sendMessage("unsuspension-user-fail-not-suspended", sender, null);
			    			return true;
						}

						iAuction.suspendedUsers.remove(args[1].toLowerCase());
						functions.saveObject(iAuction.suspendedUsers, "suspendedUsers.ser");
		    			Messaging.sendMessage("unsuspension-user", plugin.getServer().getPlayer(args[1]), null);
		    			Messaging.sendMessage("unsuspension-user-success", sender, null);
	    				
	    				return true;
	    			} else if (args[0].equalsIgnoreCase("suspend")) {
	    				if (player != null && !iAuction.perms.has(player, "auction.admin")) {
	    	    			Messaging.sendMessage("no-permission", sender, null);
	    	    			return true;
	    				}
	    				if (args.length > 1) {
	    					// Suspend a player:
	    					if (iAuction.suspendedUsers.contains(args[1].toLowerCase())) {
	    		    			Messaging.sendMessage("suspension-user-fail-already-suspended", sender, null);
	    		    			return true;
	    					}
	    					
	    					Player playerToSuspend = plugin.getServer().getPlayer(args[1]);
	    					
	    					if (playerToSuspend == null || !playerToSuspend.isOnline()) {
	    		    			Messaging.sendMessage("suspension-user-fail-is-offline", sender, null);
	    		    			return true;
	    					}
	    					
	    					if (iAuction.perms.has(playerToSuspend, "auction.admin")) {
	    		    			Messaging.sendMessage("suspension-user-fail-is-admin", sender, null);
	    		    			return true;
	    					}
	    					
	    					iAuction.suspendedUsers.add(args[1].toLowerCase());
	    					functions.saveObject(iAuction.suspendedUsers, "suspendedUsers.ser");
			    			Messaging.sendMessage("suspension-user", playerToSuspend, null);
			    			Messaging.sendMessage("suspension-user-success", sender, null);
	    					
	    					return true;
	    				}
	    				// Suspend globally:
	    				suspendAllAuctions = true;
	    				
	    				// Clear queued auctions first.
	    				for(AuctionScope as:plugin.getAuctionScopes()){
	    					as.getQueue().clear();
	    					if (as.getCurrentAuction()!=null){
	    						as.getCurrentAuction().cancel(player);
	    					}
	    				}


		    			Messaging.sendMessage("suspension-global", (Player) null, null);

		    			return true;
	    			} else if (
	        				args[0].equalsIgnoreCase("start") || 
	        				args[0].equalsIgnoreCase("s") ||
	        				args[0].equalsIgnoreCase("this") ||
	        				args[0].equalsIgnoreCase("all") ||
	        				args[0].matches("[0-9]+")
	    			) {
	    				// Start new auction!
	    	    		if (player == null) {
	    	    			Messaging.sendMessage("auction-fail-console", sender, null);
	    	    			return true;
	    	    		}
	    	    		if (!plugin.getConfig().getBoolean("allow-gamemode-creative") && player.getGameMode() == GameMode.CREATIVE) {
	    	    			Messaging.sendMessage("auction-fail-gamemode-creative", sender, null);
	    	    			return true;
	    	    		}
	    	    			
	    				if (!iAuction.perms.has(player, "auction.start")) {
	    	    			Messaging.sendMessage("no-permission", sender, null);
	    	    			return true;
	    				}
	    				//TODO:Change this to scope
						plugin.getScope(player).queueAuction(new Auction(plugin, player, args, userScope), player, auction);
						
						return true;
	    			} else if (args[0].equalsIgnoreCase("cancel") || args[0].equalsIgnoreCase("c")) {
	    				if (auction == null) {
	    					Messaging.sendMessage("auction-fail-no-auction-exists", sender, auction);
	    					return true;
	    				}
						if (player == null || player.getName().equalsIgnoreCase(auction.getOwner()) || iAuction.perms.has(player, "auction.admin")) {
							if (plugin.getConfig().getInt("cancel-prevention-seconds") > auction.getRemainingTime() || plugin.getConfig().getInt("cancel-prevention-percent") > (double)auction.getRemainingTime() / (double)auction.getTotalTime() * 100D) {
		    					Messaging.sendMessage("auction-fail-cancel-prevention", player, auction);
							} else {
		    					auction.cancel(player);
		    					//TODO:Not needed?
		    					//publicAuction = null;
							}
						} else {
	    					Messaging.sendMessage("auction-fail-not-owner-cancel", player, auction);
						}
	    				return true;
	    			} else if (args[0].equalsIgnoreCase("end") || args[0].equalsIgnoreCase("e")) {
	    				if (auction == null) {
	    					Messaging.sendMessage("auction-fail-no-auction-exists", player, auction);
	        				return true;
	    				}
	    				if (!plugin.getConfig().getBoolean("allow-early-end")) {
	    					Messaging.sendMessage("auction-fail-no-early-end", player, auction);
	        				return true;
	    				}
						if (player.getName().equalsIgnoreCase(auction.getOwner())) {
	    					auction.end(player);
	    					//TODO not needed?
	    					//publicAuction = null;
						} else {
	    					Messaging.sendMessage("auction-fail-not-owner-end", player, auction);
						}
	    				return true;
	    			} else if (
	        				args[0].equalsIgnoreCase("stfu") ||
	        				args[0].equalsIgnoreCase("quiet") ||
	        				args[0].equalsIgnoreCase("off") ||
	        				args[0].equalsIgnoreCase("silent") ||
	        				args[0].equalsIgnoreCase("silence")
	    			) {
	    				if (plugin.voluntarilyDisabledUsers.indexOf(playerName) == -1) {
	    					Messaging.sendMessage("auction-disabled", sender, null);
	    					plugin.voluntarilyDisabledUsers.add(playerName);
	    					functions.saveObject(plugin.voluntarilyDisabledUsers, "voluntarilyDisabledUsers.ser");
	    				}
	    				return true;
	    			} else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("i")) {
	    				if (auction == null) {
	    					Messaging.sendMessage("auction-info-no-auction", player, auction);
	    					return true;
	    				}
						auction.info(sender);
	    				return true;
	    			}
	    		}
				Messaging.sendMessage("auction-help", sender, auction);
	    		return true;
	    	} else if (cmd.getName().equalsIgnoreCase("bid")) {
	    		if (player == null) {
	    			Messaging.sendMessage("bid-fail-console", plugin.getServer().getConsoleSender(), null);
	    			return true;
	    		} 
	    		if (!plugin.getConfig().getBoolean("allow-gamemode-creative") && player.getGameMode().equals(GameMode.CREATIVE)) {
	    			Messaging.sendMessage("bid-fail-gamemode-creative", sender, null);
	    			return true;
	    		}
				if (!iAuction.perms.has(player, "auction.bid")) {
	    			Messaging.sendMessage("no-permission", sender, null);
	    			return true;
				}
	    		if (auction == null) {
	    			Messaging.sendMessage("bid-fail-no-auction", player, null);
	    			return true;
	    		}
	    		auction.Bid(player, args);
	    		return true;
	    	}
	    	return false;
	    }
}
