package couk.rob4001.iauction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import couk.rob4001.iauction.chat.ChatManager;
import couk.rob4001.utility.functions;
import couk.rob4001.utility.items;

public class Messaging {
	
	private static iAuction plugin;
	private static FileConfiguration textConfig;
	private static File auctionLog;
	
	public Messaging (iAuction p){
		plugin = p;
		textConfig = p.textConfig;
		auctionLog = new File(plugin.getDataFolder(), "auctions.log");
	}
	
	public static void sendMessage(String messageKey, String playerName, Auction auction) {
		if (playerName == null) {
			sendMessage(messageKey, (CommandSender) null, auction);
		} else {
			sendMessage(messageKey, plugin.getServer().getPlayer(playerName), auction);
		}
		
	}
	 public static void sendMessage(String messageKey, CommandSender player, Auction auction) {

	    	if (player != null) {
		    	if (player instanceof Player) {
			    	if (plugin.voluntarilyDisabledUsers.indexOf(player.getName()) != -1) {
			    		// Don't send this user any messages.
			    		return;
					}
		    	} else {
			    	if (plugin.voluntarilyDisabledUsers.indexOf("*console*") != -1) {
			    		// Don't send console any messages.
			    		return;
					}
		    	}
	    	}
	    	

	    	if (messageKey == null) {
	    		return;
	    	}
	    	
	    	String owner = null;
	    	String quantity = null;
	    	String lotType = null;
	    	String startingBid = null;
	    	String minBidIncrement = null;
	    	String currentBidder = null;
	    	String currentBid = null;
	    	String currentMaxBid = null;
	    	String timeRemaining = null;
	    	String auctionScope = null;
	    	String durabilityRemaining = null;
	    	String endAuctionTax = null;
	    	String startAucitonTax = functions.formatAmount(plugin.getConfig().getDouble("auction-start-tax"));

	    	if (auction != null) {
	    		ItemStack typeLot = auction.getLotType();
	    		
	    		if (auction.getOwner() != null) owner = auction.getOwner();
	    		quantity = Integer.toString(auction.getLotQuantity());
	    		lotType = items.itemName(typeLot);
	    		if (auction.getStartingBid() == 0) {
		    		startingBid = functions.formatAmount(auction.getMinBidIncrement());
	    		} else {
		    		startingBid = functions.formatAmount(auction.getStartingBid());
	    		}
	    		minBidIncrement = functions.formatAmount(auction.getMinBidIncrement());
				
	    		if (auction.getRemainingTime() >= 60) {
	    			timeRemaining = textConfig.getString("time-format-minsec");
	    			timeRemaining = timeRemaining.replace("%s", Integer.toString(auction.getRemainingTime() % 60));
	    			timeRemaining = timeRemaining.replace("%m", Integer.toString((auction.getRemainingTime() - (auction.getRemainingTime() % 60)) / 60));
	    		} else {
	    			timeRemaining = textConfig.getString("time-format-seconly");
	    			timeRemaining = timeRemaining.replace("%s", Integer.toString(auction.getRemainingTime()));
	    		}
		
				if (auction.getCurrentBid() != null) {
					currentBidder = auction.getCurrentBid().getBidder();
					currentBid = functions.formatAmount(auction.getCurrentBid().getBidAmount());
					currentMaxBid = functions.formatAmount(auction.getCurrentBid().getMaxBidAmount());
					endAuctionTax = functions.formatAmount((long) Math.floor(auction.getCurrentBid().getMaxBidAmount() * (plugin.getConfig().getDouble("auction-end-tax-percent") / 100D)));
				} else {
					currentBidder = "noone";
					currentBid = startingBid;
					currentMaxBid = startingBid;
					endAuctionTax = "-";
				}
				auctionScope = auction.getScope();
	        	durabilityRemaining = "-";
				if (typeLot != null) {
					if (typeLot.getType().getMaxDurability() > 0) {
				        DecimalFormat decimalFormat = new DecimalFormat("#%");
				        durabilityRemaining = decimalFormat.format((1 - ((double) typeLot.getDurability() / (double) typeLot.getType().getMaxDurability())));
					}
				}
	    	} else {
	        	owner = "-";
	        	quantity = "-";
	        	lotType = "-";
	        	startingBid = "-";
	        	minBidIncrement = "-";
	        	currentBidder = "-";
	        	currentBid = "-";
	        	currentMaxBid = "-";
	        	timeRemaining = "-";
	        	auctionScope = "no_auction";
	        	durabilityRemaining = "-";
	        	endAuctionTax = "-";
	    	}
	    	
	    	List<String> messageList = textConfig.getStringList(messageKey);
	    	
	    	String originalMessage = null;
	    	if (messageList == null || messageList.size() == 0) {
	    		originalMessage = textConfig.getString(messageKey.toString());
	    		
	    		
	    		if (originalMessage == null || originalMessage.length() == 0) {
	        		messageList = new ArrayList<String>();
	    			messageList.add(messageKey.toString());
	    		} else {
	        		messageList = Arrays.asList(originalMessage.split("(\r?\n|\r)"));
	    		}
	    	}
	    	
	    	for (Iterator<String> i = messageList.iterator(); i.hasNext(); ) {
	    		String messageListItem = i.next();
		    	originalMessage = chatPrep(messageListItem);
		    	String message = originalMessage;
		
				message = message.replace("%O", owner);
				message = message.replace("%q", quantity);
				message = message.replace("%i", lotType);
				message = message.replace("%s", startingBid);
				message = message.replace("%n", minBidIncrement);
				message = message.replace("%b", currentBid);
				message = message.replace("%B", currentBidder);
				message = message.replace("%h", currentMaxBid);
				message = message.replace("%t", timeRemaining);
				message = message.replace("%D", durabilityRemaining);
				message = message.replace("%x", startAucitonTax);
				message = message.replace("%X", endAuctionTax);
		
				if (messageKey == "auction-info-enchantment") {
	    			if (auction != null && auction.getLotType() != null) {
		        		Map<Enchantment, Integer> enchantments = auction.getLotType().getEnchantments();
		        		for (Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
		        			message = originalMessage.replace("%E", items.enchantmentName(enchantment));
			            	if (player == null) {
			            		ChatManager.broadcast(message);
			            	} else {
			        	    	player.sendMessage(message);
			            	}
			            	log(auctionScope, player, message);
		        		}
	    			}
				} else {
			    	if (player == null) {
			    		ChatManager.broadcast(message);
			    	} else {
				    	player.sendMessage(message);
			    	}
			    	log(auctionScope, player, message);
				}
	    	}
	    	
	    }
	 /**
		 * Prepares chat, prepending prefix and processing colors.
		 * 
		 * @param String message to prepare
		 * @return String prepared message
		 */
	    public static String chatPrep(String message) {
	    	message = textConfig.getString("chat-prefix") + message;
	    	message = ChatColor.translateAlternateColorCodes('&', message);
	    	return message;
	    }
	    public static String chatPrepClean(String message) {
	    	message = textConfig.getString("chat-prefix") + message;
	    	message = ChatColor.translateAlternateColorCodes('&', message);
	    	message = ChatColor.stripColor(message);
	    	return message;
	    }
	    
	    private static void log(String scope, CommandSender player, String message) {
	    	if (plugin.getConfig().getBoolean("log-auctions")) {
	    		String playerName = null;
	    		
				BufferedWriter out = null;
				try {
			    	if (!auctionLog.exists()) {
						auctionLog.createNewFile();
						auctionLog.setWritable(true);
			    	}
			    	
					out = new BufferedWriter(new FileWriter(auctionLog.getAbsolutePath(), true));

					if (player == null) {
						playerName = "BROADCAST";
					} else {
						playerName = player.getName();
					}
					
					out.append(scope + " (" + playerName + "): " + ChatColor.stripColor(message) + "\n");
					out.close();

				} catch (IOException e) {
					
				}
		    	
				
	    	}
			
		}
}
