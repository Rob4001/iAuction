package couk.rob4001.iauction;

import java.util.LinkedList;
import java.util.Map.Entry;

import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import couk.rob4001.iauction.chat.ChatManager;

public class Auction implements Runnable {

	private Integer id;
	private Integer i; // time remaining
	private iAuction plugin;
	private final Player owner;
	private Player winner;
	private double bid;
	private double sbid;
	private final ItemStack lot;
	private int tid;

	public Auction(iAuction plugin, ItemStack lot, double price, Player owner,
			int time) {
		this.owner = owner;
		this.i = time;
		this.bid = price;
		this.lot = lot;
		this.plugin = plugin;

	}

	public void start() {

		// Adjust time
		i++;

		// Take item
		ItemManager.takeItem(owner, lot);

		// Info
		ItemInfo ii = Items.itemByStack(lot);

		StringBuilder item = new StringBuilder(Messaging.get("info.item",
				ii.getName(), String.valueOf(ii.getId()),
				String.valueOf(ii.getSubTypeId())));
		if (!lot.getEnchantments().isEmpty()) {
			StringBuilder enchs = new StringBuilder();
			boolean comma = false;
			for (Entry<Enchantment, Integer> ench : lot.getEnchantments()
					.entrySet()) {
				if (comma) {
					enchs.append(", ");
				} else {
					comma = true;
				}
				enchs.append(plugin.nameEnch(ench.getKey()) + "-"
						+ ench.getValue());
			}
			item.append(Messaging.get("info.enchantments", enchs.toString()));
		}
		ChatManager.broadcast(item.toString());
		Messaging.broadcast("info.amount",
				String.valueOf(lot.getAmount()));
		Messaging.broadcast("info.bid", String.valueOf(bid));
		if (winner != null) {
			Messaging.broadcast("info.winner",
					String.valueOf(winner.getName()));
		}
		Messaging.broadcast("info.auctioneer", owner.getName());
		tid = this.plugin.getServer().getScheduler()
				.scheduleAsyncRepeatingTask(plugin, this, 0L, 20L);

	}

	@Override
	public void run() {
		i--;
		if (i == 0) {
			stop();
			return;
		}
		if (i <= 5) {
			Messaging.broadcast("timer.timeleft", i.toString());
			return;
		}
		if (i <= 60 && i % 10 == 0) {
			Messaging.broadcast("timer.timeleft", i.toString());
			return;
		}
		if (i % 30 == 0) {
			Messaging.broadcast("timer.timeleft", i.toString());
			return;
		}

	}

	public void stop() {
		
		if (winner != null) {
			plugin.getEco().withdrawPlayer(winner.getName(), bid);
			plugin.getEco().depositPlayer(owner.getName(), bid);
			ItemManager.giveItem(winner, lot);
			Messaging.broadcast("stop.winner", winner.getDisplayName());
			winner.sendMessage(Messaging.get("stop.enjoy"));
			owner.sendMessage(Messaging.get("stop.sold",plugin.getEco().format(bid)));
		} else {
			ItemManager.giveItem(owner, lot);
			Messaging.broadcast("stop.nowinner");
			owner.sendMessage(Messaging.get("stop.returned"));
		}

		plugin.getServer().getScheduler().cancelTask(tid);
		plugin.removeAuction(this);

	}

	public Player getOwner() {
		return owner;
	}

	public ItemStack getLot() {
		return lot;
	}

	public void Info(Player player) {
		ItemInfo ii = Items.itemByStack(lot);

		StringBuilder item = new StringBuilder(Messaging.get("info.item",
				ii.getName(), String.valueOf(ii.getId()),
				String.valueOf(ii.getSubTypeId())));
		if (!lot.getEnchantments().isEmpty()) {
			StringBuilder enchs = new StringBuilder();
			boolean comma = false;
			for (Entry<Enchantment, Integer> ench : lot.getEnchantments()
					.entrySet()) {
				if (comma) {
					enchs.append(", ");
				} else {
					comma = true;
				}
				enchs.append(plugin.nameEnch(ench.getKey()) + "-"
						+ ench.getValue());
			}
			item.append(Messaging.get("info.enchantments", enchs.toString()));
		}
		player.sendMessage(item.toString());
		player.sendMessage(Messaging.get("info.amount",
				String.valueOf(lot.getAmount())));
		player.sendMessage(Messaging.get("info.bid", String.valueOf(bid)));
		if (winner != null) {
			player.sendMessage(Messaging.get("info.winner",
					String.valueOf(winner.getName())));
		}
		player.sendMessage(Messaging.get("info.auctioneer", owner.getName()));
	}

	public void info(Player player, LinkedList<String> args) {
		ItemInfo ii = Items.itemByStack(lot);

		StringBuilder item = new StringBuilder(Messaging.get("info.item",
				ii.getName(), String.valueOf(ii.getId()),
				String.valueOf(ii.getSubTypeId())));
		if (!lot.getEnchantments().isEmpty()) {
			StringBuilder enchs = new StringBuilder();
			boolean comma = false;
			for (Entry<Enchantment, Integer> ench : lot.getEnchantments()
					.entrySet()) {
				if (comma) {
					enchs.append(", ");
				} else {
					comma = true;
				}
				enchs.append(plugin.nameEnch(ench.getKey()) + "-"
						+ ench.getValue());
			}
			item.append(Messaging.get("info.enchantments", enchs.toString()));
		}
		player.sendMessage(item.toString());
		player.sendMessage(Messaging.get("info.amount",
				String.valueOf(lot.getAmount())));
		player.sendMessage(Messaging.get("info.bid", String.valueOf(bid)));
		if (winner != null){
		player.sendMessage(Messaging.get("info.winner",
				String.valueOf(winner.getName())));
		}
		player.sendMessage(Messaging.get("info.auctioneer", owner.getName()));

	}

	public void cancel(Player player, LinkedList<String> args) {
		if (player.hasPermission("auction.admin") || player == owner) {
			winner = null;
			stop();
		} else {
			player.sendMessage(Messaging.get("error.perm"));
		}

	}

	public void end(Player player, LinkedList<String> args) {
		if (player.hasPermission("auction.admin") || player == owner) {
			stop();
		} else {
			player.sendMessage(Messaging.get("error.perm"));
		}

	}

	public void bid(Player bidder, LinkedList<String> args) {
		double tempbid = bid + plugin.getConfig().getDouble("bid.defaultbid");
		double tempsbid = -1;
		for (int i = 0; i < args.size(); i++) {
			String arg = args.get(i);
			if (arg.equalsIgnoreCase("-s") || arg.equalsIgnoreCase("sbid")) {
				tempsbid = Double.parseDouble(args.get(i + 1));
				i += 2;
			}
			if (isDouble(arg)) {
				tempbid = Double.parseDouble(arg);
			}

		}

		// if (fin) {
		// bidder.sendMessage(plugin.errorcolour + "The auction has ended");
		// return;
		// }
		if (bidder == owner) {
			bidder.sendMessage(Messaging.get("bidding.owner"));
			return;
		}
		if (bidder == winner) {
			bidder.sendMessage(Messaging.get("bidding.winner"));
			return;
		}
		if (tempbid <= bid) {
			bidder.sendMessage(Messaging.get("bidding.toolow"));
			return;
		}

		if (tempbid <= sbid) {
			Messaging.broadcast("bidding.sbidraised",
					String.valueOf(tempbid));
			bid = tempbid;
			return;
		}

		if (tempsbid <= tempbid || tempsbid <= sbid) {
			bidder.sendMessage(Messaging.get("bidding.sbidtoolow"));
			tempsbid = -1;
		}
		if (!plugin.getConfig().getBoolean("bid.sbidenable")) {
			bidder.sendMessage(Messaging.get("bidding.sbidnotenabled"));
			tempsbid = -1;
		}

		if (tempbid < bid + plugin.getConfig().getDouble("bid.minincrement")) {
			bidder.sendMessage(Messaging.get(
					"error.minincrement",
					plugin.getEco().format(
							this.plugin.getConfig().getDouble(
									"bid.minincrement"))));
			return;
		}

		if (plugin.getEco().getBalance(bidder.getName()) <= tempbid) {
			bidder.sendMessage(Messaging.get("bidding.notenoughmoney"));
			return;
		} else {
			winner = bidder;
			bid = tempbid;
			sbid = tempsbid;
			Messaging.broadcast("bidding.raised",
					String.valueOf(tempbid), bidder.getName());
			if (plugin.getConfig().getBoolean("antisnipe.enabled")
					&& (i < plugin.getConfig().getInt("antisnipe.endtime") && plugin
							.getConfig().getInt("antisnipe.endtime") != 0))
				i += plugin.getConfig().getInt("antisnipe.value");
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

	public double getBid() {
		return bid;
	}
	
	public Integer getID(){
		return id;
	}
	
	public void setID(Integer id){
		this.id= id;
	}

}
