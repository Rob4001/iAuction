package net.umc.Rob4001.iAuction;

import java.util.Map.Entry;

import net.umc.dev.InventoryManager;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Auction implements Runnable {
	public int time;
	public ItemStack lot;
	public Player owner;
	public float bid;

	public int timeLeft;
	double half;
	public Player winner;
	private iAuction plugin;
	private boolean fin;

	public Auction(iAuction pl, ItemStack l, Player o, int t, float b) {
		lot = l;
		owner = o;
		time = t;
		bid = b;
		plugin = pl;

		timeLeft = time;
		half = java.lang.Math.floor(time / 2);
		new InventoryManager(owner).remove(lot, true, true);
		fin = false;
		startInfo();

	}

	private void startInfo() {
		plugin.broadcast(plugin.auxcolour + "-----[ " + plugin.titlecolour
				+ "Auction Information" + plugin.auxcolour + " ]-----");
		StringBuilder itemthing = new StringBuilder(plugin.fieldcolour
				+ "Auctioned Item: " + plugin.valuecolour + net.milkbowl.vault.item.Items.itemByStack(lot).getName()
				+ plugin.fieldcolour + " [" + plugin.valuecolour
				+ lot.getTypeId() + plugin.fieldcolour + "]");

		if (!lot.getEnchantments().isEmpty()) {
			itemthing.append(" with ");
			for (Entry<Enchantment, Integer> ench : lot.getEnchantments()
					.entrySet()) {
				itemthing.append(" , " + nameEnch(ench.getKey()) + "-"
						+ ench.getValue());
			}
		}
		plugin.broadcast(itemthing.toString());
		plugin.broadcast(plugin.fieldcolour + "Amount: " + plugin.valuecolour
				+ lot.getAmount());
		plugin.broadcast(plugin.fieldcolour + "Current bid: "
				+ plugin.valuecolour + plugin.getEco().format(bid));
		plugin.broadcast(plugin.fieldcolour + "Owner: " + plugin.valuecolour
				+ owner.getDisplayName());
	}

	@Override
	public void run() {
		if (half <= 10) {
			if (timeLeft == time || timeLeft == 10 || timeLeft == 3
					|| timeLeft == 2) {
				plugin.broadcast("" + plugin.valuecolour + timeLeft
						+ plugin.fieldcolour + " seconds left to bid!");
			}
		} else {
			if (timeLeft == time || timeLeft == half || timeLeft == 10
					|| timeLeft == 3 || timeLeft == 2) {
				plugin.broadcast("" + plugin.valuecolour + timeLeft
						+ plugin.fieldcolour + " seconds left to bid!");
			}
		}
		if (timeLeft == 1) {
			plugin.broadcast("" + plugin.valuecolour + timeLeft
					+ plugin.fieldcolour + " seconds left to bid!");
		}
		if (timeLeft == 0) {
			fin = true;
			stop();
		}
		timeLeft -= 1;

	}

	public void stop() {
		if (winner == null) {
			new InventoryManager(owner).addItem(lot);
			plugin.broadcast(plugin.fieldcolour
					+ "-- Auction ended with no bids --");
			owner.sendMessage(plugin.fieldcolour
					+ "Your items have been returned to you!");
		} else {
			if (plugin.getEco().getBalance(winner.getName()) >= bid) {
				plugin.getEco().withdrawPlayer(winner.getName(), bid);
				plugin.getEco().depositPlayer(owner.getName(), bid);
				new InventoryManager(winner).addItem(lot);
				plugin.broadcast(plugin.fieldcolour
						+ "-- Auction Ended -- Winner [ " + plugin.valuecolour
						+ winner.getDisplayName() + plugin.fieldcolour
						+ " ] -- ");
				winner.sendMessage(plugin.fieldcolour + "Enjoy your items!");
				owner.sendMessage(plugin.fieldcolour
						+ "Your items have been sold for " + plugin.valuecolour
						+ plugin.getEco().format(bid) + plugin.fieldcolour
						+ "!");
			} else {
				new InventoryManager(owner).addItem(lot);
				owner.sendMessage(plugin.fieldcolour
						+ "Error processing payment. Auction canceled.");
				winner.sendMessage(plugin.fieldcolour
						+ "Error processing payment. Auction canceled.");
			}
		}
		plugin.resetAuc();

	}

	public void cancel() {
		winner = null;
		stop();
	}

	public boolean bid(Player bidder, float b) {
		if (fin){
			bidder.sendMessage(plugin.errorcolour
					+ "The auction has ended");
			return true;
		}
		if (bidder == owner) {
			bidder.sendMessage(plugin.errorcolour
					+ "You can not bid on your own auction!");
			return true;
		}
		if (bidder == winner) {
			bidder.sendMessage(plugin.errorcolour
					+ "You are already winning this auction!");
			return true;
		}
		if (b > bid) {
			if(b < bid + this.plugin.getConfig().getLong("bidding.minincrement")){
				this.plugin.warn(bidder, "You have to bid " + this.plugin.getEco().format(this.plugin.getConfig().getInt("bidding.minincrement")) + "higher than the last bid");
				return true;
			}

			if (plugin.getEco().getBalance(bidder.getName()) <= b) {
				bidder.sendMessage(plugin.errorcolour
						+ "You don't have enough founds for this bid.");
				return true;
			} else {
				winner = bidder;
				bid = b;
				plugin.broadcast(plugin.fieldcolour + "Bid raised to "
						+ plugin.valuecolour + plugin.getEco().format(bid)
						+ " by " + plugin.valuecolour + bidder.getDisplayName());
				if (plugin.getConfig().getBoolean("antisnipe.enabled")&&(timeLeft < plugin.getConfig().getInt("antisnipe.endtime")&&plugin.getConfig().getInt("antisnipe.endtime")!=0))timeLeft += plugin.getConfig().getInt("antisnipe.value");
				return true;
			}
		} else {
			bidder.sendMessage(plugin.errorcolour + "Bid is too low!");
			return true;
		}

	}

	public void Info(Player player) {
		player.sendMessage(plugin.auxcolour + "-----[ " + plugin.titlecolour
				+ "Auction Information" + plugin.auxcolour + " ]-----");
		StringBuilder itemthing = new StringBuilder(plugin.fieldcolour
				+ "Auctioned Item: " + plugin.valuecolour + net.milkbowl.vault.item.Items.itemByStack(lot).getName()
				+ plugin.fieldcolour + " [" + plugin.valuecolour
				+ lot.getTypeId() + plugin.fieldcolour + "]");
		if (!lot.getEnchantments().isEmpty()) {
			itemthing.append(" with ");
			for (Entry<Enchantment, Integer> ench : lot.getEnchantments()
					.entrySet()) {
				itemthing.append(" , " + nameEnch(ench.getKey()) + "-"
						+ ench.getValue());
			}
		}
		player.sendMessage(itemthing.toString());
		player.sendMessage(plugin.fieldcolour + "Amount: " + plugin.valuecolour
				+ lot.getAmount());
		player.sendMessage(plugin.fieldcolour + "Current bid: "
				+ plugin.valuecolour + plugin.getEco().format(bid));
		player.sendMessage(plugin.fieldcolour + "Owner: " + plugin.valuecolour
				+ owner.getDisplayName());
		if (winner != null)
			player.sendMessage(plugin.fieldcolour + "Current Winner: "
					+ plugin.valuecolour + winner.getDisplayName());
	}
	
	public static String nameEnch(Enchantment ench) {

		String fin = "";

		switch (ench.getId()) {
		case 0:
			fin = "Protection";
			break;
		case 1:
			fin = "Fire Protection";
			break;
		case 2:
			fin = "Feather Falling";
			break;
		case 3:
			fin = "Blast Protection";
			break;
		case 4:
			fin = "Projectile Protection";
			break;
		case 5:
			fin = "Respiration";
			break;
		case 6:
			fin = "Aqua Affinity";
			break;
		case 16:
			fin = "Sharpness";
			break;
		case 17:
			fin = "Smite";
			break;
		case 18:
			fin = "Bane of Arthropods";
			break;
		case 19:
			fin = "Knockback";
			break;
		case 20:
			fin = "Fire Aspect";
			break;
		case 21:
			fin = "Looting";
			break;
		case 48:
			fin = "Power";
			break;
		case 49:
			fin = "Punch";
			break;
		case 50:
			fin = "Flame";
			break;
		case 51:
			fin = "Infinity";
			break;
		case 32:
			fin = "Efficiency";
			break;
		case 33:
			fin = "Silk Touch";
			break;
		case 34:
			fin = "Unbreaking";
			break;
		case 35:
			fin = "Fortune";
			break;
		default:
			fin = "UNKNOWN";
			break;
		}

		return fin;

	}

}
