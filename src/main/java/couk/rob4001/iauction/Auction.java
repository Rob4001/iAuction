package couk.rob4001.iauction;

import java.util.Arrays;
import java.util.LinkedList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import couk.rob4001.utility.functions;
import couk.rob4001.utility.items;

public class Auction implements Runnable {
	protected iAuction plugin;
	LinkedList<String> args;
	private String ownerName;
	private AuctionScope scope;

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

	public AuctionScope getScope() {
		return scope;
	}

	public Auction(iAuction plugin, Player auctionOwner, String[] inputArgs,
			AuctionScope scope) {

		ownerName = auctionOwner.getName();
		args = new LinkedList<String>(Arrays.asList(inputArgs));
		;
		this.plugin = plugin;
		this.scope = scope;

		// Remove the optional "start" arg:
		if (!args.isEmpty()) {
			if (args.getFirst().equalsIgnoreCase("start")
					|| args.getFirst().equalsIgnoreCase("s")) {
				args.removeFirst();
			}
		}

	}

	public Boolean start() {

		ItemStack typeStack = lot.getTypeStack();

		// Check banned items:
		for (int i = 0; i < scope.config.getStringList("banned-items").size(); i++) {
			if (items.isSameItem(typeStack,
					scope.config.getStringList("banned-items").get(i))) {
				Messaging.sendMessage("auction-fail-banned", ownerName, this);
				return false;
			}
		}

		if (scope.config.getDouble("auction-start-tax") > 0D) {
			if (!iAuction.econ.has(ownerName,
					scope.config.getDouble("auction-start-tax"))) {
				Messaging
						.sendMessage("auction-fail-start-tax", ownerName, this);
				return false;
			}
		}

		if (!lot.AddItems(quantity, true)) {
			Messaging.sendMessage("auction-fail-insufficient-supply",
					ownerName, this);
			return false;
		}

		if (scope.config.getDouble("auction-start-tax") > 0D) {
			if (iAuction.econ.has(ownerName,
					scope.config.getDouble("auction-start-tax"))) {
				Messaging.sendMessage("auction-start-tax", getOwner(), this);
				iAuction.econ.withdrawPlayer(ownerName,
						scope.config.getDouble("auction-start-tax"));
				if (!scope.config.getString("deposit-tax-to-user").isEmpty())
					iAuction.econ.depositPlayer(
							scope.config.getString("deposit-tax-to-user"),
							scope.config.getDouble("auction-start-tax"));
			}
		}

		active = true;
		scope.currentAuctionOwnerLocation = iAuction.server
				.getPlayer(ownerName).getLocation().clone();
		scope.currentAuctionOwnerGamemode = iAuction.server
				.getPlayer(ownerName).getGameMode();
		Messaging.sendMessage("auction-start", (CommandSender) null, this);

		// Set timer:
		countdown = time;

		countdownTimer = plugin.getServer().getScheduler()
				.scheduleAsyncRepeatingTask(plugin, this, 20L, 20L);

		info(null);
		return true;
	}

	@Override
	public void run() {
		countdown--;
		if (countdown == 0) {
			end(null);
			return;
		}
		if (countdown < 4) {
			Messaging.sendMessage("timer-countdown-notification",
					(CommandSender) null, this);
			return;
		}
		if (time >= 20) {
			if (countdown == (int) (this.time / 2)) {
				Messaging.sendMessage("timer-countdown-notification",
						(CommandSender) null, this);
			}
		}
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
		if (lot != null)
			lot.cancelLot();
		if (currentBid != null)
			currentBid.cancelBid();
		dispose();
	}

	public void end(Player ender) {
		if (currentBid == null || lot == null) {
			Messaging.sendMessage("auction-end-nobids", (CommandSender) null,
					this);
			if (lot != null)
				lot.cancelLot();
			if (currentBid != null)
				currentBid.cancelBid();
		} else {
			Messaging.sendMessage("auction-end", (CommandSender) null, this);
			lot.winLot(currentBid.getBidder());
			currentBid.winBid();
		}
		dispose();
	}

	private void dispose() {
		plugin.getServer().getScheduler().cancelTask(countdownTimer);
		scope.detachAuction(this);
	}

	public Boolean isValid() {
		if (!parseHeldItem())
			return false;
		if (!parseArgs())
			return false;
		if (!isValidOwner())
			return false;
		if (!isValidAmount())
			return false;
		if (!isValidStartingBid())
			return false;
		if (!isValidIncrement())
			return false;
		if (!isValidTime())
			return false;
		return true;
	}

	public void Bid(Player bidder, String[] inputArgs) {
		AuctionBid bid = new AuctionBid(this, bidder, inputArgs);
		if (bid.getError() != null) {
			failBid(bid, bid.getError());
			return;
		}
		if (ownerName.equals(bidder.getName())
				&& !scope.config.getBoolean("allow-bid-on-own-auction")) {
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

		if (scope.config.getBoolean("use-old-bid-logic")) {
			if (bid.getMaxBidAmount() > currentBid.getMaxBidAmount()) {
				winner = bid;
				looser = currentBid;
			} else {
				winner = currentBid;
				looser = bid;
			}
			winner.raiseBid(Math.max(
					winner.getBidAmount(),
					Math.min(winner.getMaxBidAmount(), looser.getBidAmount()
							+ minBidIncrement)));
		} else {
			// If you follow what this does, congratulations.
			long baseBid = 0;
			if (bid.getBidAmount() >= currentBid.getBidAmount()
					+ minBidIncrement) {
				baseBid = bid.getBidAmount();
			} else {
				baseBid = currentBid.getBidAmount() + minBidIncrement;
			}

			Integer prevSteps = (int) Math.floor((double) (currentBid
					.getMaxBidAmount() - baseBid + minBidIncrement)
					/ minBidIncrement / 2);
			Integer newSteps = (int) Math
					.floor((double) (bid.getMaxBidAmount() - baseBid)
							/ minBidIncrement / 2);

			if (newSteps >= prevSteps) {
				winner = bid;
				winner.raiseBid(baseBid
						+ (Math.max(0, prevSteps) * minBidIncrement * 2));
				looser = currentBid;
			} else {
				winner = currentBid;
				winner.raiseBid(baseBid
						+ (Math.max(0, newSteps + 1) * minBidIncrement * 2)
						- minBidIncrement);
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
					Messaging.sendMessage("bid-auto-outbid",
							(CommandSender) null, this);
					failBid(bid, "bid-fail-auto-outbid");
				} else {
					Messaging.sendMessage("bid-fail-too-low", bid.getBidder(),
							this);
					failBid(bid, null);
				}
			}
		} else {
			// Seriously don't know what could cause this, but might as well
			// take care of it.
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
		scope.currentBidPlayerLocation = iAuction.server
				.getPlayer(newBid.getBidder()).getLocation().clone();
		scope.currentBidPlayerGamemode = iAuction.server.getPlayer(
				newBid.getBidder()).getGameMode();
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

		if (!scope.config.getBoolean("allow-damaged-items")
				&& itemType.getType().getMaxDurability() > 0
				&& itemType.getDurability() > 0) {
			Messaging.sendMessage("auction-fail-damaged-item", owner, this);
			lot = null;
			return false;
		}

		return true;
	}

	private Boolean parseArgs() {
		// (amount) (starting price) (increment) (time)
		if (!parseArgAmount())
			return false;
		if (!parseArgStartingBid())
			return false;
		if (!parseArgIncrement())
			return false;
		if (!parseArgTime())
			return false;
		return true;
	}

	private Boolean isValidOwner() {
		if (ownerName == null) {
			Messaging.sendMessage("auction-fail-invalid-owner", (Player) plugin
					.getServer().getConsoleSender(), this);
			return false;
		}
		return true;
	}

	private Boolean isValidAmount() {
		if (quantity <= 0) {
			Messaging.sendMessage("auction-fail-quantity-too-low", ownerName,
					this);
			return false;
		}
		if (!items.hasAmount(ownerName, quantity, lot.getTypeStack())) {
			Messaging.sendMessage("auction-fail-insufficient-supply2",
					ownerName, this);
			return false;
		}
		return true;
	}

	private Boolean isValidStartingBid() {
		if (startingBid < 0) {
			Messaging.sendMessage("auction-fail-starting-bid-too-low",
					ownerName, this);
			return false;
		} else if (startingBid > 0
				&& startingBid > functions.getSafeMoney(scope.config
						.getDouble("max-starting-bid"))) {
			Messaging.sendMessage("auction-fail-starting-bid-too-high",
					ownerName, this);
			return false;
		}
		return true;
	}

	private Boolean isValidIncrement() {
		if (getMinBidIncrement() < functions.getSafeMoney(scope.config
				.getDouble("min-bid-increment"))) {
			Messaging.sendMessage("auction-fail-increment-too-low", ownerName,
					this);
			return false;
		}
		if (getMinBidIncrement() > functions.getSafeMoney(scope.config
				.getDouble("max-bid-increment"))) {
			Messaging.sendMessage("auction-fail-increment-too-high", ownerName,
					this);
			return false;
		}
		return true;
	}

	private Boolean isValidTime() {
		if (time < scope.config.getInt("min-auction-time")) {
			Messaging.sendMessage("auction-fail-time-too-low", ownerName, this);
			return false;
		}
		if (time > scope.config.getInt("max-auction-time")) {
			Messaging
					.sendMessage("auction-fail-time-too-high", ownerName, this);
			return false;
		}
		return true;
	}

	private Boolean parseArgAmount() {
		if (quantity > 0)
			return true;

		ItemStack lotType = lot.getTypeStack();
		if (!args.isEmpty()) {
			if (args.getFirst().equalsIgnoreCase("this")) {
				quantity = lotType.getAmount();
			} else if (args.getFirst().equalsIgnoreCase("all")) {
				quantity = items.getAmount(ownerName, lotType);
			} else if (args.getFirst().matches("[0-9]{1,7}")) {
				quantity = Integer.parseInt(args.getFirst());
			} else {
				Messaging.sendMessage("parse-error-invalid-quantity",
						ownerName, this);
				return false;
			}
		} else {
			quantity = lotType.getAmount();
		}
		if (quantity < 0) {
			Messaging.sendMessage("parse-error-invalid-quantity", ownerName,
					this);
			return false;
		}
		return true;
	}

	private Boolean parseArgStartingBid() {
		if (startingBid > 0)
			return true;

		if (args.size() > 1) {
			if (args.get(1).matches(iAuction.decimalRegex)) {
				startingBid = functions.getSafeMoney(Double.parseDouble(args
						.get(1)));
			} else {
				Messaging.sendMessage("parse-error-invalid-starting-bid",
						ownerName, this);
				return false;
			}
		} else {
			startingBid = functions.getSafeMoney(scope.config
					.getDouble("default-starting-bid"));
		}
		if (startingBid < 0) {
			Messaging.sendMessage("parse-error-invalid-starting-bid",
					ownerName, this);
			return false;
		}
		return true;
	}

	private Boolean parseArgIncrement() {
		if (minBidIncrement > 0)
			return true;

		if (args.size() > 2) {
			if (args.get(2).matches(iAuction.decimalRegex)) {
				minBidIncrement = functions.getSafeMoney(Double
						.parseDouble(args.get(2)));
			} else {
				Messaging.sendMessage("parse-error-invalid-bid-increment",
						ownerName, this);
				return false;
			}
		} else {
			minBidIncrement = functions.getSafeMoney(scope.config
					.getDouble("default-bid-increment"));
		}
		if (minBidIncrement < 0) {
			Messaging.sendMessage("parse-error-invalid-bid-increment",
					ownerName, this);
			return false;
		}
		return true;
	}

	private Boolean parseArgTime() {
		if (time > 0)
			return true;

		if (args.size() > 3) {
			if (args.get(3).matches("[0-9]{1,7}")) {
				time = Integer.parseInt(args.get(3));
			} else {
				Messaging.sendMessage("parse-error-invalid-time", ownerName,
						this);
				return false;
			}
		} else {
			time = scope.config.getInt("default-auction-time");
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
