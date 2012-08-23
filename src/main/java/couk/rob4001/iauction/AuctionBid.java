package couk.rob4001.iauction;

import org.bukkit.entity.Player;

import couk.rob4001.utility.functions;

public class AuctionBid {
	private Auction auction;
	private String bidderName;
	private long bidAmount = 0;
	private long maxBidAmount = 0;
	private String error;
	private String[] args;
	private double reserve = 0;

	public AuctionBid(Auction theAuction, Player player, String[] inputArgs) {
		auction = theAuction;
		bidderName = player.getName();
		args = inputArgs;
		if (!validateBidder())
			return;
		if (!parseArgs())
			return;
		if (!reserveBidFunds())
			return;
	}

	private boolean reserveBidFunds() {
		long amountToReserve = 0;
		AuctionBid currentBid = auction.getCurrentBid();
		if (currentBid != null
				&& currentBid.getBidder().equalsIgnoreCase(bidderName)) {
			// Same bidder: only reserve difference.
			if (maxBidAmount > currentBid.getMaxBidAmount()) {
				amountToReserve = maxBidAmount - currentBid.getMaxBidAmount();
			} else {
				// Nothing needing reservation.
				return true;
			}
		} else {
			amountToReserve = maxBidAmount;
		}
		if (functions.withdrawPlayer(bidderName, amountToReserve)) {
			reserve = functions.getUnsafeMoney(amountToReserve);
			return true;
		} else {
			error = "bid-fail-cant-allocate-funds";
			return false;
		}
	}

	public void cancelBid() {
		// Refund reserve.
		functions.depositPlayer(bidderName, reserve);
		reserve = 0;
	}

	public void winBid() {
		Double unsafeBidAmount = functions.getUnsafeMoney(bidAmount);

		// Extract taxes:
		Double taxes = 0D;
		if (iAuction.config.getDouble("auction-end-tax-percent") > 0D) {
			taxes = unsafeBidAmount
					* (iAuction.config.getDouble("auction-end-tax-percent") / 100D);
			Messaging.sendMessage("auction-end-tax", auction.getOwner(),
					auction);
			unsafeBidAmount -= taxes;
			if (!iAuction.config.getString("deposit-tax-to-user").isEmpty())
				iAuction.econ
						.depositPlayer(iAuction.config
								.getString("deposit-tax-to-user"), taxes);
		}

		// Apply winnings to auction owner.
		iAuction.econ.depositPlayer(auction.getOwner(), unsafeBidAmount);

		// Refund remaining reserve.
		iAuction.econ.depositPlayer(bidderName, reserve - unsafeBidAmount
				- taxes);

		reserve = 0;
	}

	private Boolean validateBidder() {
		if (bidderName == null) {
			error = "bid-fail-no-bidder";
			return false;
		}
		return true;
	}

	private Boolean parseArgs() {
		if (!parseArgBid())
			return false;
		if (!parseArgMaxBid())
			return false;
		return true;
	}

	public Boolean raiseOwnBid(AuctionBid otherBid) {
		if (bidderName.equalsIgnoreCase(otherBid.bidderName)) {
			// Move reserve money here.
			reserve = reserve + otherBid.reserve;
			otherBid.reserve = 0;

			// Maxbid only updates up.
			maxBidAmount = Math.max(maxBidAmount, otherBid.maxBidAmount);
			otherBid.maxBidAmount = maxBidAmount;

			if (bidAmount > otherBid.bidAmount) {
				// The bid has been raised.
				return true;
			} else {
				// Put the reserve on the other bid because we're cancelling
				// this one.
				otherBid.reserve = reserve;
				reserve = 0;

				return false;
			}
		} else {
			// Don't take reserve unless it's the same person's money.
			return false;
		}
	}

	public Boolean raiseBid(Long newBidAmount) {
		if (newBidAmount <= maxBidAmount && newBidAmount >= bidAmount) {
			bidAmount = newBidAmount;
			return true;
		} else {
			return false;
		}
	}

	private Boolean parseArgBid() {
		if (args.length > 0) {
			if (args[0].matches(iAuction.decimalRegex)) {
				bidAmount = functions.getSafeMoney(Double.parseDouble(args[0]));
			} else {
				error = "parse-error-invalid-bid";
				return false;
			}
		} else {
			// Leaving it up to automatic:
			bidAmount = 0;
		}
		// If the person bids 0, make it automatically the next increment
		// (unless it's the current bidder).
		if (bidAmount == 0) {
			AuctionBid currentBid = auction.getCurrentBid();
			if (currentBid == null) {
				// Use the starting bid if no one has bid yet.
				bidAmount = auction.getStartingBid();
				if (bidAmount == 0) {
					// Unless the starting bid is 0, then use the minimum bid
					// increment.
					bidAmount = auction.getMinBidIncrement();
				}
			} else if (currentBid.getBidder().equalsIgnoreCase(bidderName)) {
				// We are the current bidder, so use previous. Don't auto-up our
				// own bid.
				bidAmount = currentBid.bidAmount;
			} else {
				bidAmount = currentBid.getBidAmount()
						+ auction.getMinBidIncrement();
			}
		}
		if (bidAmount <= 0) {
			error = "parse-error-invalid-bid";
			return false;
		}
		return true;
	}

	private Boolean parseArgMaxBid() {
		if (!iAuction.config.getBoolean("allow-max-bids")) {
			// Just ignore it.
			maxBidAmount = bidAmount;
			return true;
		}
		if (args.length > 1) {
			if (args[1].matches(iAuction.decimalRegex)) {
				maxBidAmount = functions.getSafeMoney(Double
						.parseDouble(args[1]));
			} else {
				error = "parse-error-invalid-max-bid";
				return false;
			}
		}
		maxBidAmount = Math.max(bidAmount, maxBidAmount);
		if (maxBidAmount <= 0) {
			error = "parse-error-invalid-max-bid";
			return false;
		}
		return true;
	}

	public String getError() {
		return error;
	}

	public String getBidder() {
		return bidderName;
	}

	public long getBidAmount() {
		return bidAmount;
	}

	public long getMaxBidAmount() {
		return maxBidAmount;
	}

	public void setReserve(double newReserve) {
		reserve = newReserve;
	}

	public double getReserve() {
		return reserve;
	}
}
