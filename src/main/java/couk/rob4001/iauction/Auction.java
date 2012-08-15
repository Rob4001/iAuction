package couk.rob4001.iauction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import couk.rob4001.utility.functions;
import couk.rob4001.utility.items;

public class Auction {
	protected iAuction plugin;
	private String[] args;
	private String ownerName;
	private String scope;

	private long startingBid = 0;
	private long minBidIncrement = 0;
	private int quantity = 0;
	private int time = 0;
	private boolean active = false;
	
	private AuctionLot lot;
	private AuctionBid currentBid;
	
	// Scheduled timers:
	private int countdown = 0;
	private int countdownTimer = 0;
	
	public String getScope() {
		return scope;
	}
	
	public Auction(iAuction plugin, Player auctionOwner, String[] inputArgs, String scope) {
		ownerName = auctionOwner.getName();
		args = inputArgs;
		this.plugin = plugin; 
		this.scope = scope;

		// Remove the optional "start" arg:
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("s")) {
				args = new String[inputArgs.length - 1];
				System.arraycopy(inputArgs, 1, args, 0, inputArgs.length - 1);
			}
		}
		
	}
	public Boolean start() {
		
		ItemStack typeStack = lot.getTypeStack();
		
		// Check banned items:
		for (int i = 0; i < iAuction.config.getStringList("banned-items").size(); i++) {
			if (items.isSameItem(typeStack, iAuction.config.getStringList("banned-items").get(i))) {
				Messaging.sendMessage("auction-fail-banned", ownerName, this);
				return false;
			}
		}
		
		if (iAuction.config.getDouble("auction-start-tax") > 0D) {
			if (!iAuction.econ.has(ownerName, iAuction.config.getDouble("auction-start-tax"))) {
				Messaging.sendMessage("auction-fail-start-tax", ownerName, this);
				return false;
			}
		}
		
		if (!lot.AddItems(quantity, true)) {
			Messaging.sendMessage("auction-fail-insufficient-supply", ownerName, this);
			return false;
		}

		if (iAuction.config.getDouble("auction-start-tax") > 0D) {
			if (iAuction.econ.has(ownerName, iAuction.config.getDouble("auction-start-tax"))) {
				Messaging.sendMessage("auction-start-tax", getOwner(), this);
				iAuction.econ.withdrawPlayer(ownerName, iAuction.config.getDouble("auction-start-tax"));
				if (!iAuction.config.getString("deposit-tax-to-user").isEmpty()) iAuction.econ.depositPlayer(iAuction.config.getString("deposit-tax-to-user"), iAuction.config.getDouble("auction-start-tax"));
			}
		}

		active = true;
		iAuction.currentAuctionOwnerLocation = iAuction.server.getPlayer(ownerName).getLocation().clone();
		iAuction.currentAuctionOwnerGamemode = iAuction.server.getPlayer(ownerName).getGameMode();
		Messaging.sendMessage("auction-start", (CommandSender) null, this);
		
		// Set timer:
		final Auction thisAuction = this;
		countdown = time;
		
		countdownTimer = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
		    public void run() {
		    	thisAuction.countdown--;
		    	if (thisAuction.countdown == 0) {
		    		thisAuction.end(null);
		    		return;
		    	}
		    	if (thisAuction.countdown < 4) {
			    	Messaging.sendMessage("timer-countdown-notification", (CommandSender) null, thisAuction);
			    	return;
		    	}
		    	if (thisAuction.time >= 20) {
		    		if (thisAuction.countdown == (int) (thisAuction.time / 2)) {
				    	Messaging.sendMessage("timer-countdown-notification", (CommandSender) null, thisAuction);
		    		}
		    	}
		    }
		}, 20L, 20L);

		info(null);
		return true;
	}
	public void info(CommandSender sender) {
		ItemStack itemType = this.getLotType();
		short maxDurability = itemType.getType().getMaxDurability();
		short currentDurability = itemType.getDurability();
		if (!active) {
			Messaging.sendMessage("auction-info-no-auction", sender, this);
		} else if (currentBid == null) {
			Messaging.sendMessage("auction-info-header-nobids", sender, this);
			Messaging.sendMessage("auction-info-enchantment", sender, this);
			if (maxDurability > 0 && currentDurability > 0) {
				Messaging.sendMessage("auction-info-damage", sender, this);
			}
			Messaging.sendMessage("auction-info-footer-nobids", sender, this);
		} else {
			Messaging.sendMessage("auction-info-header", sender, this);
			Messaging.sendMessage("auction-info-enchantment", sender, this);
			if (maxDurability > 0 && currentDurability > 0) {
				Messaging.sendMessage("auction-info-damage", sender, this);
			}
			Messaging.sendMessage("auction-info-footer", sender, this);
		}
	}
	public void cancel(Player canceller) {
		Messaging.sendMessage("auction-cancel", (CommandSender) null, this);
		if (lot != null) lot.cancelLot();
		if (currentBid != null) currentBid.cancelBid();
		dispose();
	}
	public void end(Player ender) {
		if (currentBid == null || lot == null) {
			Messaging.sendMessage("auction-end-nobids", (CommandSender) null, this);
			if (lot != null) lot.cancelLot();
			if (currentBid != null) currentBid.cancelBid();
		} else {
			Messaging.sendMessage("auction-end", (CommandSender) null, this);
			lot.winLot(currentBid.getBidder());
			currentBid.winBid();
		}
		dispose();
	}
	private void dispose() {
		plugin.getServer().getScheduler().cancelTask(countdownTimer);
		plugin.detachAuction(this);
	}
	public Boolean isValid() {
		if (!parseHeldItem()) return false;
		if (!parseArgs()) return false;
		if (!isValidOwner()) return false;
		if (!isValidAmount()) return false;
		if (!isValidStartingBid()) return false;
		if (!isValidIncrement()) return false;
		if (!isValidTime()) return false;
		return true;
	}
	public void Bid(Player bidder, String[] inputArgs) {
		AuctionBid bid = new AuctionBid(this, bidder, inputArgs);
		if (bid.getError() != null) {
			failBid(bid, bid.getError());
			return;
		}
		if (ownerName.equals(bidder.getName()) && !iAuction.config.getBoolean("allow-bid-on-own-auction")) {
			failBid(bid, "bid-fail-is-auction-owner");
			return;
		}
		if (currentBid == null) {
			if (bid.getBidAmount() < getStartingBid()) {
				failBid(bid, "bid-fail-under-starting-bid");
				return;
			}
			setNewBid(bid, "bid-success-no-challenger");
			return;
		}
		long previousBidAmount = currentBid.getBidAmount();
		long previousMaxBidAmount = currentBid.getMaxBidAmount();
		if (currentBid.getBidder().equals(bidder.getName())) {
			if (bid.raiseOwnBid(currentBid)) {
				setNewBid(bid, "bid-success-update-own-bid");
			} else {
				if (previousMaxBidAmount < currentBid.getMaxBidAmount()) {
					failBid(bid, "bid-success-update-own-maxbid");
				} else {
					failBid(bid, "bid-fail-already-current-bidder");
				}
			}
			return;
		}
		AuctionBid winner = null;
		AuctionBid looser = null;
		
		if (iAuction.config.getBoolean("use-old-bid-logic")) {
			if (bid.getMaxBidAmount() > currentBid.getMaxBidAmount()) {
				winner = bid;
				looser = currentBid;
			} else {
				winner = currentBid;
				looser = bid;
			}
			winner.raiseBid(Math.max(winner.getBidAmount(), Math.min(winner.getMaxBidAmount(), looser.getBidAmount() + minBidIncrement)));
		} else {
			// If you follow what this does, congratulations.  
			long baseBid = 0;
			if (bid.getBidAmount() >= currentBid.getBidAmount() + minBidIncrement) {
				baseBid = bid.getBidAmount();
			} else {
				baseBid = currentBid.getBidAmount() + minBidIncrement;
			}
			
			Integer prevSteps = (int) Math.floor((double)(currentBid.getMaxBidAmount() - baseBid + minBidIncrement) / minBidIncrement / 2);
			Integer newSteps = (int) Math.floor((double)(bid.getMaxBidAmount() - baseBid) / minBidIncrement / 2);

			if (newSteps >= prevSteps) {
				winner = bid;
				winner.raiseBid(baseBid + (Math.max(0, prevSteps) * minBidIncrement * 2));
				looser = currentBid;
			} else {
				winner = currentBid;
				winner.raiseBid(baseBid + (Math.max(0, newSteps + 1) * minBidIncrement * 2) - minBidIncrement);
				looser = bid;
			}
			
		}

		if (previousBidAmount <= winner.getBidAmount()) {
			// Did the new bid win?
			if (winner.equals(bid)) {
				setNewBid(bid, "bid-success-outbid");
			} else {
				// Did the old bid have to raise the bid to stay winner?
				if (previousBidAmount < winner.getBidAmount()) {
					Messaging.sendMessage("bid-auto-outbid", (CommandSender) null, this);
					failBid(bid, "bid-fail-auto-outbid");
				} else {
					Messaging.sendMessage("bid-fail-too-low", bid.getBidder(), this);
					failBid(bid, null);
				}
			}
		} else {
			// Seriously don't know what could cause this, but might as well take care of it.
			Messaging.sendMessage("bid-fail-too-low", bid.getBidder(), this);
		}
		
		
		
	}
	private void failBid(AuctionBid newBid, String reason) {
		newBid.cancelBid();
		Messaging.sendMessage(reason, newBid.getBidder(), this);
	}
	private void setNewBid(AuctionBid newBid, String reason) {
		if (currentBid != null) {
			currentBid.cancelBid();
		}
		currentBid = newBid;
		iAuction.currentBidPlayerLocation = iAuction.server.getPlayer(newBid.getBidder()).getLocation().clone();
		iAuction.currentBidPlayerGamemode = iAuction.server.getPlayer(newBid.getBidder()).getGameMode();
		Messaging.sendMessage(reason, (CommandSender) null, this);
	}
	private Boolean parseHeldItem() {
		Player owner = iAuction.server.getPlayer(ownerName);
		if (lot != null) {
			return true;
		}
		ItemStack heldItem = owner.getItemInHand();
		if (heldItem == null || heldItem.getAmount() == 0) {
			Messaging.sendMessage("auction-fail-hand-is-empty", owner, this);
			return false;
		}
		lot = new AuctionLot(heldItem, ownerName);
		
		ItemStack itemType = lot.getTypeStack();
		
		if (
				!iAuction.config.getBoolean("allow-damaged-items") &&
				itemType.getType().getMaxDurability() > 0 &&
				itemType.getDurability() > 0
		) {
			Messaging.sendMessage("auction-fail-damaged-item", owner, this);
			lot = null;
			return false;
		}
		
		return true;
	}
	private Boolean parseArgs() {
		// (amount) (starting price) (increment) (time)
		if (!parseArgAmount()) return false;
		if (!parseArgStartingBid()) return false;
		if (!parseArgIncrement()) return false;
		if (!parseArgTime()) return false;
		return true;
	}
	private Boolean isValidOwner() {
		if (ownerName == null) {
			Messaging.sendMessage("auction-fail-invalid-owner", (Player) plugin.getServer().getConsoleSender(), this);
			return false;
		}
		return true;
	}
	private Boolean isValidAmount() {
		if (quantity <= 0) {
			Messaging.sendMessage("auction-fail-quantity-too-low", ownerName, this);
			return false;
		}
		if (!items.hasAmount(ownerName, quantity, lot.getTypeStack())) {
			Messaging.sendMessage("auction-fail-insufficient-supply2", ownerName, this);
			return false;
		}
		return true;
	}
	private Boolean isValidStartingBid() {
		if (startingBid < 0) {
			Messaging.sendMessage("auction-fail-starting-bid-too-low", ownerName, this);
			return false;
		} else if (startingBid > 0 && startingBid > functions.getSafeMoney(iAuction.config.getDouble("max-starting-bid")) ) {
			Messaging.sendMessage("auction-fail-starting-bid-too-high", ownerName, this);
			return false;
		}
		return true;
	}
	private Boolean isValidIncrement() {
		if (getMinBidIncrement() < functions.getSafeMoney(iAuction.config.getDouble("min-bid-increment"))) {
			Messaging.sendMessage("auction-fail-increment-too-low", ownerName, this);
			return false;
		}
		if (getMinBidIncrement() > functions.getSafeMoney(iAuction.config.getDouble("max-bid-increment"))) {
			Messaging.sendMessage("auction-fail-increment-too-high", ownerName, this);
			return false;
		}
		return true;
	}
	private Boolean isValidTime() {
		if (time < iAuction.config.getInt("min-auction-time")) {
			Messaging.sendMessage("auction-fail-time-too-low", ownerName, this);
			return false;
		}
		if (time > iAuction.config.getInt("max-auction-time")) {
			Messaging.sendMessage("auction-fail-time-too-high", ownerName, this);
			return false;
		}
		return true;
	}
	private Boolean parseArgAmount() {
		if (quantity > 0) return true;

		ItemStack lotType = lot.getTypeStack();
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("this")) {
				quantity = lotType.getAmount();
			} else if (args[0].equalsIgnoreCase("all")) {
				quantity = items.getAmount(ownerName, lotType);
			} else if (args[0].matches("[0-9]{1,7}")) {
				quantity = Integer.parseInt(args[0]);
			} else {
				Messaging.sendMessage("parse-error-invalid-quantity", ownerName, this);
				return false;
			}
		} else {
			quantity = lotType.getAmount();
		}
		if (quantity < 0) {
			Messaging.sendMessage("parse-error-invalid-quantity", ownerName, this);
			return false;
		}
		return true;
	}
	private Boolean parseArgStartingBid() {
		if (startingBid > 0) return true;
		
		if (args.length > 1) {
			if (args[1].matches(iAuction.decimalRegex)) {
				startingBid = functions.getSafeMoney(Double.parseDouble(args[1]));
			} else {
				Messaging.sendMessage("parse-error-invalid-starting-bid", ownerName, this);
				return false;
			}
		} else {
			startingBid = functions.getSafeMoney(iAuction.config.getDouble("default-starting-bid"));
		}
		if (startingBid < 0) {
			Messaging.sendMessage("parse-error-invalid-starting-bid", ownerName, this);
			return false;
		}
		return true;
	}
	private Boolean parseArgIncrement() {
		if (minBidIncrement > 0) return true;

		if (args.length > 2) {
			if (args[2].matches(iAuction.decimalRegex)) {
				minBidIncrement = functions.getSafeMoney(Double.parseDouble(args[2]));
			} else {
				Messaging.sendMessage("parse-error-invalid-bid-increment", ownerName, this);
				return false;
			}
		} else {
			minBidIncrement = functions.getSafeMoney(iAuction.config.getDouble("default-bid-increment"));
		}
		if (minBidIncrement < 0) {
			Messaging.sendMessage("parse-error-invalid-bid-increment", ownerName, this);
			return false;
		}
		return true;
	}
	private Boolean parseArgTime() {
		if (time > 0) return true;

		if (args.length > 3) {
			if (args[3].matches("[0-9]{1,7}")) {
				time = Integer.parseInt(args[3]);
			} else {
				Messaging.sendMessage("parse-error-invalid-time", ownerName, this);
				return false;
			}
		} else {
			time = iAuction.config.getInt("default-auction-time");
		}
		if (time < 0) {
			Messaging.sendMessage("parse-error-invalid-time", ownerName, this);
			return false;
		}
		return true;
	}
	public long getMinBidIncrement() {
		return minBidIncrement;
	}
	
	public ItemStack getLotType() {
		if (lot == null) {
			return null;
		}
		return lot.getTypeStack();
	}
	
	public int getLotQuantity() {
		if (lot == null) {
			return 0;
		}
		return lot.getQuantity();
	}
	public long getStartingBid() {
		long effectiveStartingBid = startingBid;
		if (effectiveStartingBid == 0) {
			effectiveStartingBid = minBidIncrement; 
		}
		return effectiveStartingBid;
	}
	public AuctionBid getCurrentBid() {
		return currentBid;
	}
	public String getOwner() {
		return ownerName;
	}
	public int getRemainingTime() {
		return countdown;
	}
	public int getTotalTime() {
		return time;
	}
}
