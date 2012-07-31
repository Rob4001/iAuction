package couk.rob4001.iauction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.item.Items;


import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import couk.rob4001.iauction.chat.ChatManager;

public class iAuction extends JavaPlugin {

	private YamlConfiguration langconfig;
	private Economy economy;
	private final ArrayList<Auction> auctions = new ArrayList<Auction>();
	private final ArrayList<Material> bi = new ArrayList<Material>();

	@Override
	public void onEnable() {
		setupConfig();

		setupLanguages();

		if (!setupEconomy()) {
			getLogger().log(Level.SEVERE, Messaging.get("error.economy"));
			this.setEnabled(false);
			return;
		}
		setupBlackList();
		new ChatManager(this);

		this.getCommand("auction").setExecutor(new AuctionCommand(this));

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
		
		for(Player p:this.getServer().getOnlinePlayers()){
			ChatManager.addListener(p);
		}
	}

	private void setupConfig() {
		this.getConfig().addDefault("start.defaulttime", 60);
		this.getConfig().addDefault("auction.max", 1);
		this.getConfig().addDefault("start.defaulttime", 60);
		this.getConfig().addDefault("bid.defaultbid", 5);
		this.getConfig().addDefault("bid.sbidenable", false);
		this.getConfig().addDefault("bid.minincrement", 5);
		this.getConfig().addDefault("antisnipe.enabled", false);
		this.getConfig().addDefault("antisnipe.endtime", 10);
		this.getConfig().addDefault("antisnipe.value",5);
		this.getConfig().addDefault("start.defaulttime", 60);
		this.getConfig().addDefault("start.defaultprice", 10);
		this.getConfig().addDefault("eco.useDecimal", true);
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

	@Override
	public void onDisable() {
		for (Auction auction : auctions) {
			auction.stop();
		}
	}

	public Auction getAuction(int id) {
		for (Auction a : auctions) {
			if (a.getID() == id)
				return a;
		}
		return null;
	}

	public List<Auction> getAuctions() {
		return auctions;
	}

	public boolean registerAuction(Auction auc) {
		if (auctions.size() <= getConfig().getInt("auction.max")) {
			for (int i = 1; i <= getConfig().getInt("auction.max"); i++) {
				boolean found = false;
				for (Auction auction : auctions) {
					if (auction.getID() == i) {
						found = true;
						;
					}
				}
				if (!found) {
					auc.setID(i);
					auctions.add(auc);
					return true;
				}
			}

		}
		return false;

	}

	public void removeAuction(Auction auction) {
		auctions.remove(auction);
	}

	// Listeners
	@EventHandler(priority = EventPriority.MONITOR)
	public void quitChecker(PlayerQuitEvent e) {
		for (Auction auction : auctions) {
			if (auction.getOwner() == e.getPlayer()) {
				auction.stop();
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent e) {
		ChatManager.addListener(e.getPlayer());
	}

	// Economy Stuff
	private Boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	public Economy getEco() {
		return this.economy;
	}

	// Chat and Messaging Stuff

	public YamlConfiguration getLangConfig() {
		return langconfig;
	}

	public String nameEnch(Enchantment key) {
		String fin = "";

		switch (key.getId()) {
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

	private void setupLanguages() {
		langconfig = YamlConfiguration.loadConfiguration(new File(
				getDataFolder(), "lang.yml"));

		langconfig.addDefault("tag", "&f[&aiAuction&f] ");
		langconfig
				.addDefault(
						"error.economy",
						"&4Economy Failure! Make sure you have an economy and Vault in your plugins folder");
		langconfig.addDefault("error.perm",
				"&4 You do not have permission for that!");
		langconfig.addDefault("timer.timeleft", "&b{0} &9seconds left to bid!");
		langconfig.addDefault("info.item",
				"&9Auctioned Item: &b{0} &9[&b{1}&9]");
		langconfig.addDefault("info.enchantments", " &9with {0}");
		langconfig.addDefault("info.amount", "&9Amount: &b{0}");
		langconfig.addDefault("info.bid", "&9Current Bid: &b{0}");
		langconfig.addDefault("info.auctioneer", "&9Owner: &b{0}");
		langconfig.addDefault("info.winner", "&9Current Winnder: &b{0}");
		langconfig.addDefault("list.noauctions",
				"There are no auctions running atm");
		langconfig.addDefault("list.title", "ID  Item           Amount    Bid");
		langconfig.addDefault("list.entry", "{0}   {1}:{2}     {4}  {5}");
		langconfig.addDefault("start.overmax",
				"There are too many auctions started atm");
		langconfig.addDefault("start.noitems", "You dont have enough items");
		langconfig.addDefault("bidding.owner",
				"You are the owner of the auction");
		langconfig.addDefault("bidding.winner", "You are already winning");
		langconfig.addDefault("bidding.toolow", "You have not bid enough");
		langconfig.addDefault("bidding.sbidtoolow",
				"your secret bid is too low");
		langconfig.addDefault("bidding.sbidnotenabled",
				"Secret bidding not enabled on this server");
		langconfig.addDefault("error.minincrement",
				"You have to bid {0} more than the current bid");
		langconfig.addDefault("bidding.notenoughmoney",
				"You do not have enough money!");
		langconfig.addDefault("bidding.raised", "Bid raised to {0} by {1}");
		langconfig
				.addDefault("error.console", "Console commands not supported");
		langconfig.addDefault("error.auctionid",
				"No auction with that ID running!");
		langconfig.addDefault("help.title","&e -----[ &aAuction Help&e ]----- ");
		langconfig.addDefault("help.help","&b/auction help ? &e- Returns this");
		langconfig.addDefault("help.start1","&b/auction <id> start|s &e-t <time> -i <item> -a <amount> ");
		langconfig.addDefault("help.start2","&e-p <price> -h(hand)  All arguments are optional");
		langconfig.addDefault("help.bid","&b/auction <id> bid|b &e<bid>&e  - Bids the auction.");
		langconfig.addDefault("help.end","&b/auction <id> end|e&e - Ends current auction.");
		langconfig.addDefault("help.info","&b/auction <id> info|i&e - Returns auction information."); 
		langconfig.addDefault("help.list","&b/auction list|l&e - Returns returns a list of auctions"); 
		langconfig.addDefault("help.listen","&b/auction listen/mute&e - Enables/Disables messaging"); 
		langconfig.addDefault("help.cancel","&b/auction <id> cancel&e - Cancels the auction"); 
		langconfig.addDefault("stop.nowinner", "&9-- Auction ended with no bids --");
		langconfig.addDefault("stop.returned", "&9Your items have been returned to you!");
		langconfig.addDefault("stop.winner", "&9-- Auction Ended -- Winner [ &b {0} &9 ] -- ");
		langconfig.addDefault("stop.enjoy", "&9Enjoy your items!");
		langconfig.addDefault("stop.sold", "&9Your items have been sold for &b{0} ");
		langconfig.addDefault("listen.on", "&9Now listening to Auctions");
		langconfig.addDefault("listen.off", "&9Now muted auctions");
		langconfig.addDefault("start.noitemstack", "&4Items stack error");
		
		langconfig.options().copyDefaults(true);

		try {
			langconfig.save(new File(getDataFolder(), "lang.yml"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Messaging.plugin = this;
		Messaging.tag = this.getLangConfig().getString("tag");

	}

	// Other Features
	// Black list
	public ArrayList<Material> getBannedItems() {
		return bi;
	}

	private void setupBlackList() {
		List<String> bil = getConfig().getStringList("blacklist");
		for (String b : bil) {
			bi.add(Items.itemByString(b).material);
		}

	}

}
