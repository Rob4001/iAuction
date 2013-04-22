package couk.rob4001.iAuction;

import java.io.Serializable;
import java.util.LinkedList;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

import couk.rob4001.util.InventoryUtil;

public class Auction implements Runnable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8259303586687210913L;
	public boolean started = false;
	private OfflinePlayer owner;
	private double bid;
	private Integer i;
	private Inventory inv;
	private transient BukkitTask tid;
	private OfflinePlayer winner = null;
	private double sbid;

	private transient Economy eco;

	public Auction(Player p, double price, int time, Inventory inventory) {
		this.owner = p;
		this.bid = price;
		this.i = time;
		this.inv = inventory;

	}

	public Inventory getInventory() {
		return this.inv;
	}

	public void start() {

		this.tid = iAuction
				.getInstance()
				.getServer()
				.getScheduler()
				.runTaskTimerAsynchronously(iAuction.getInstance(), this, 0L,
						20L);
		this.started = true;
		this.eco = iAuction.getInstance().getEco();
		iAuction.charge(this.eco, this.owner);
		Messaging.broadcast("auction.start", this.owner.getName(),
				InventoryUtil.parseItems(this.inv), this.eco.format(this.bid));
		Messaging.broadcast("auction.timeleft", this.i.toString());
	}

	@Override
	public void run() {
		this.i--;
		if (this.winner != null) {
			this.inv = InventoryUtil.changeTitle(this.inv,
					this.winner.getName(), Double.toString(this.bid),
					this.i.toString());
		} else {
			this.inv = InventoryUtil.changeTitle(this.inv,
					this.owner.getName(), Double.toString(this.bid),
					this.i.toString());
		}
		if (this.i == 0) {
			this.stop();
			return;
		}
		if (this.i <= 5) {
			Messaging.broadcast("auction.timeleft", this.i.toString());
			return;
		}
		if (this.i <= 60 && this.i % 10 == 0) {
			Messaging.broadcast("auction.timeleft", this.i.toString());
			return;
		}
		if (this.i % 30 == 0) {
			Messaging.broadcast("auction.timeleft", this.i.toString());
			return;
		}

	}

	public void stop() {
		if (this.winner != null) {
			iAuction.chargedDeposit(this.eco, this.owner, this.bid);
			this.eco.withdrawPlayer(this.winner.getName(), this.bid);

			iAuction.getInstance().lots.put(this.winner.getName(),
					InventoryUtil.box(this.inv));

			Messaging.broadcast("stop.winner", this.winner.getName());
			Messaging.playerMessage(this.winner.getPlayer(), "stop.enjoy");

			Messaging.playerMessage(this.owner.getPlayer(), "stop.sold",
					this.eco.format(this.bid));
		} else {
			iAuction.getInstance().lots.put(this.owner.getName(),
					InventoryUtil.box(this.inv));
			Messaging.broadcast("stop.nowinner");
			Messaging.playerMessage(this.owner.getPlayer(), "stop.returned");
		}
		this.tid.cancel();
		iAuction.removeCurrent();
	}

	public void bid(Player bidder, LinkedList<String> args) {
		double tempbid = this.bid
				+ iAuction.getInstance().getConfig()
						.getDouble("bid.defaultbid");
		double tempsbid = -1;

		if (args.size() >= 1) {
			if (this.isDouble(args.getFirst())) {
				tempbid = Double.parseDouble(args.getFirst());
				args.removeFirst();
			}
		}
		if (args.size() >= 1) {
			if (this.isDouble(args.getFirst())) {
				tempsbid = Double.parseDouble(args.getFirst());
				args.removeFirst();
			}
		}

		if (!iAuction.getInstance().getConfig().getBoolean("eco.usedecimal")) {
			tempbid = Math.round(tempbid);
			tempsbid = Math.round(tempsbid);
		}

		if (bidder == this.owner) {
			Messaging.playerMessage(bidder, "bidding.owner");
			return;
		}
		if (bidder == this.winner) {
			Messaging.playerMessage(bidder, "bidding.winner");
			return;
		}
		if (tempbid <= this.bid) {
			Messaging.playerMessage(bidder, "bidding.toolow");
			return;
		}

		if (tempbid <= this.sbid) {
			Messaging.broadcast("bidding.sbidraised", String.valueOf(tempbid));
			this.bid = tempbid;
			return;
		}

		if (tempsbid != -1) {
			if (!iAuction.getInstance().getConfig()
					.getBoolean("bid.sbidenable")) {
				Messaging.playerMessage(bidder, "bidding.sbidnotenabled");
				tempsbid = -1;
			} else {
				if (tempsbid <= tempbid || tempsbid <= this.sbid) {
					Messaging.playerMessage(bidder, "bidding.sbidtoolow");
					tempsbid = -1;
				}
			}
		}

		if (tempbid < this.bid
				+ iAuction.getInstance().getConfig()
						.getDouble("bid.minincrement")) {
			Messaging.playerMessage(
					bidder,
					"bidding.minincrement",
					this.eco.format(iAuction.getInstance().getConfig()
							.getDouble("bid.minincrement")));
			return;
		}

		if (this.eco.getBalance(bidder.getName()) <= tempbid
				|| this.eco.getBalance(bidder.getName()) <= tempsbid) {
			Messaging.playerMessage(bidder, "bidding.notenoughmoney");
			return;
		} else {
			this.winner = bidder;
			this.bid = tempbid;
			this.sbid = tempsbid;
			Messaging.broadcast("bidding.raised", String.valueOf(tempbid),
					bidder.getName());
			if (iAuction.getInstance().getConfig()
					.getBoolean("antisnipe.enabled")
					&& (this.i < iAuction.getInstance().getConfig()
							.getInt("antisnipe.endtime") && iAuction
							.getInstance().getConfig()
							.getInt("antisnipe.endtime") != 0)) {
				this.i += iAuction.getInstance().getConfig()
						.getInt("antisnipe.value");
			}
			return;
		}

	}

	private boolean isDouble(String main) {
		try {
			Double.parseDouble(main);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public void end(Player player, LinkedList<String> args) {
		if (player.hasPermission("auction.admin") || player == this.owner) {
			this.stop();
		} else {
			Messaging.playerMessage(player, "error.perm");
		}

	}

	public void cancel(Player player, LinkedList<String> args) {
		if (player.hasPermission("auction.admin") || player == this.owner) {
			this.winner = null;
			this.stop();
		} else {
			Messaging.playerMessage(player, "error.perm");
		}

	}

	public Player getOwner() {
		return this.owner.getPlayer();
	}

	public String getTime() {
		return this.i.toString();
	}

	public String getBid() {
		return Double.toString(this.bid);
	}

}
