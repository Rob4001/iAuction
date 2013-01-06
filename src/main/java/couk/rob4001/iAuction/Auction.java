package couk.rob4001.iAuction;

import java.io.Serializable;
import java.util.LinkedList;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

import couk.rob4001.util.InventoryUtil;

public class Auction implements Runnable,Serializable{

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
		return inv;
	}
	
public void start(){
	
	tid = iAuction.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(iAuction.getInstance(), this, 0L, 20L);
	started = true;
	this.eco = iAuction.getInstance().getEco();
	Messaging.broadcast("auction.start", owner.getName(),InventoryUtil.parseItems(inv),eco.format(bid));
	Messaging.broadcast("auction.timeleft", i.toString());
}




public void run() {
	i--;
	if (winner != null){
	inv = InventoryUtil.changeTitle(inv, winner.getName(), Double.toString(bid), i.toString());
	}else{
		inv = InventoryUtil.changeTitle(inv, owner.getName(), Double.toString(bid), i.toString());
	}
	if (i == 0) {
		stop();
		return;
	}
	if (i <= 5) {
		Messaging.broadcast("auction.timeleft", i.toString());
		return;
	}
	if (i <= 60 && i % 10 == 0) {
		Messaging.broadcast("auction.timeleft", i.toString());
		return;
	}
	if (i % 30 == 0) {
		Messaging.broadcast("auction.timeleft", i.toString());
		return;
	}

}

public void stop(){
	if (winner != null) {
		eco.withdrawPlayer(winner.getName(), bid);
		eco.depositPlayer(owner.getName(), bid);

		iAuction.getInstance().lots.put(winner.getName(), InventoryUtil.box(inv));
		
		Messaging.broadcast("stop.winner", winner.getName());
		Messaging.playerMessage(winner.getPlayer(), "stop.enjoy");
		
		Messaging.playerMessage(owner.getPlayer(), "stop.sold",eco.format(bid));
	} else {
		iAuction.getInstance().lots.put(owner.getName(), InventoryUtil.box(inv));
		Messaging.broadcast("stop.nowinner");
		Messaging.playerMessage(owner.getPlayer(), "stop.returned");
	}
	tid.cancel();
	iAuction.removeCurrent();
}

public void bid(Player bidder, LinkedList<String> args) {
	double tempbid = bid + iAuction.getInstance().getConfig().getDouble("bid.defaultbid");
	double tempsbid = -1;

	if (args.size() >= 1) {
		if (isDouble(args.getFirst())) {
			tempbid = Double.parseDouble(args.getFirst());
			args.removeFirst();
		}
	}
	if (args.size() >= 1) {
		if (isDouble(args.getFirst())) {
			tempsbid = Double.parseDouble(args.getFirst());
			args.removeFirst();
		}
	}

	if (!iAuction.getInstance().getConfig().getBoolean("eco.usedecimal")) {
		tempbid = Math.round(tempbid);
		tempsbid = Math.round(tempsbid);
	}

	if (bidder == owner) {
		Messaging.playerMessage(bidder, "bidding.owner");
		return;
	}
	if (bidder == winner) {
		Messaging.playerMessage(bidder,"bidding.winner");
		return;
	}
	if (tempbid <= bid) {
		Messaging.playerMessage(bidder,"bidding.toolow");
		return;
	}

	if (tempbid <= sbid) {
		Messaging.broadcast("bidding.sbidraised", String.valueOf(tempbid));
		bid = tempbid;
		return;
	}

	if (tempsbid != -1) {
		if (!iAuction.getInstance().getConfig().getBoolean("bid.sbidenable")) {
			Messaging.playerMessage(bidder,"bidding.sbidnotenabled");
			tempsbid = -1;
		} else {
			if (tempsbid <= tempbid || tempsbid <= sbid) {
				Messaging.playerMessage(bidder,"bidding.sbidtoolow");
				tempsbid = -1;
			}
		}
	}

	if (tempbid < bid + iAuction.getInstance().getConfig().getDouble("bid.minincrement")) {
		Messaging.playerMessage(bidder,
				"bidding.minincrement",
				eco.format(
						iAuction.getInstance().getConfig().getDouble(
								"bid.minincrement")));
		return;
	}

	if (eco.getBalance(bidder.getName()) <= tempbid||eco.getBalance(bidder.getName()) <= tempsbid) {
		Messaging.playerMessage(bidder,"bidding.notenoughmoney");
		return;
	} else {
		winner = bidder;
		bid = tempbid;
		sbid = tempsbid;
		Messaging.broadcast("bidding.raised", String.valueOf(tempbid),
				bidder.getName());
		if (iAuction.getInstance().getConfig().getBoolean("antisnipe.enabled")
				&& (i < iAuction.getInstance().getConfig().getInt("antisnipe.endtime") && iAuction.getInstance()
						.getConfig().getInt("antisnipe.endtime") != 0))
			i += iAuction.getInstance().getConfig().getInt("antisnipe.value");
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
	if (player.hasPermission("auction.admin") || player == owner) {
		stop();
	} else {
		Messaging.playerMessage(player,"error.perm");
	}
	
}

public void cancel(Player player, LinkedList<String> args) {
	if (player.hasPermission("auction.admin") || player == owner) {
		winner = null;
		stop();
	} else {
		Messaging.playerMessage(player,"error.perm");
	}

}

public Player getOwner() {
	return owner.getPlayer();
}

public String getTime() {
	return i.toString();
}

public String getBid() {
	return Double.toString(bid);
}




}
