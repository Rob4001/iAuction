package couk.rob4001.iauction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import couk.rob4001.utility.functions;

public class iAuction extends JavaPlugin {
	private static final Logger log = Logger.getLogger("Minecraft");
	// public Auction publicAuction;

	// Got to figure out a better way to store these:
	// public static long defaultStartingBid = 0;
	// public static long defaultBidIncrement = 100;
	// public static int defaultAuctionTime = 60;
	// public static long maxStartingBid = 10000;
	// public static long minIncrement = 1;
	// public static long maxIncrement = 100;
	// public static int maxTime = 60;
	// public static int minTime = 15;
	// public static int maxAuctionQueueLength = 2;
	// public static int minAuctionIntervalSecs = 10;
	// public static boolean allowBidOnOwn = false;
	// public static boolean useOldBidLogic = false;
	// public static boolean logAuctions = false;
	// public static boolean allowEarlyEnd = false;
	public static int decimalPlaces = 2;
	public static String decimalRegex = "^[0-9]{0,13}(\\.[0-9]{1,"
			+ decimalPlaces + "})?$";
	// public static boolean allowMaxBids = true;
	// public static boolean allowGamemodeChange = false;
	// public static boolean allowWorldChange = false;
	// public static List<String> bannedItems = new ArrayList<String>();
	// public static double taxPerAuction = 0;
	// public static double taxPercentage = 0;
	// public static String taxDestinationUser = "";
	// public static Location currentBidPlayerLocation;
	// public static GameMode currentBidPlayerGamemode;
	// public static Location currentAuctionOwnerLocation;
	// public static GameMode currentAuctionOwnerGamemode;
	// public static int cancelPreventionSeconds = 15;
	// public static double cancelPreventionPercent = 50;

	// Config files info.
	// private static File configFile = null;
	// private static InputStream defConfigStream;
	public static FileConfiguration config = null;
	// private static File textConfigFile = null;
	// private static InputStream defTextConfigStream;
	public FileConfiguration textConfig = null;
	// private static YamlConfiguration defConfig = null;
	// private static YamlConfiguration defTextConfig = null;
	public static File dataFolder;
	// private static ConsoleCommandSender console;
	public static Server server;
	public static int queueTimer;

	// TODO: Save these items when updated so we don't loose info when
	// restarting.
	public static ArrayList<AuctionLot> orphanLots = new ArrayList<AuctionLot>();;
	public ArrayList<String> voluntarilyDisabledUsers = new ArrayList<String>();;
	public static ArrayList<String> suspendedUsers = new ArrayList<String>();
	public ArrayList<AuctionScope> auctionScopes = new ArrayList<AuctionScope>();;

	// Eliminate orphan lots (i.e. try to give the items to a player again).
	public static void killOrphan(Player player) {
		// List of orphans to potentially kill.
		ArrayList<AuctionLot> orphanDeathRow = orphanLots;

		// New orphanage.
		orphanLots = new ArrayList<AuctionLot>();

		// KILL THEM ALL!
		for (int i = 0; i < orphanDeathRow.size(); i++) {
			// Hmm, only if they're actually orphans though.
			if (orphanDeathRow.get(i) != null) {
				// And only if they belong to player.
				AuctionLot lot = orphanDeathRow.get(i);

				if (lot.getOwner().equalsIgnoreCase(player.getName())) {
					lot.cancelLot();
					orphanDeathRow.set(i, null);
				} else {
					// This one's still alive, put it back in the orphanage.
					orphanLots.add(lot);
				}
			}
		}
		functions.saveObject(orphanLots, "orphanLots.ser");
	}

	// Vault objects
	public static Economy econ = null;
	public Permission perms = null;

	public void onEnable() {
		server = getServer();
		// console = server.getConsoleSender();
		dataFolder = getDataFolder();
		// final Plugin plugin = this;

		setupEconomy();
		setupPermissions();
		loadConfig();
		loadScopes();
		
		// Initialize Messaging class.
		new Messaging(this);

		if (server.getPluginManager().getPlugin("WhatIsIt") == null) {
			log.log(Level.SEVERE, Messaging.chatPrepClean(textConfig
					.getString("no-whatisit")));
			server.getPluginManager().disablePlugin(this);
			return;
		}

		if (econ == null) {
			log.log(Level.SEVERE,
					Messaging.chatPrepClean(textConfig.getString("no-economy")));
			server.getPluginManager().disablePlugin(this);
			return;
		}

		orphanLots = functions.loadArrayListAuctionLot("orphanLots.ser", true);
		voluntarilyDisabledUsers = functions
				.loadArrayListString("voluntarilyDisabledUsers.ser");
		suspendedUsers = functions.loadArrayListString("suspendedUsers.ser");

		// Load up the Plugin metrics
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
		Messaging.sendMessage("plugin-enabled", getServer().getConsoleSender(),
				null);

		getCommand("auction").setExecutor(new AuctionCommand(this));
		getCommand("bid").setExecutor(new AuctionCommand(this));
	}

	/**
	 * Loads config.yml and language.yml configuration files.
	 */
	private void loadConfig() {

		getConfig().options().copyDefaults(true);
		saveConfig();
		config = getConfig();
		getTextConfig().options().copyDefaults(true);
		saveTextConfig();

		// logAuctions = getConfig().getBoolean("log-auctions");

		decimalPlaces = Math.min(
				Math.max(getConfig().getInt("decimal-places"), 0), 5);
		getConfig().set("decimal-places", decimalPlaces);
		if (decimalPlaces < 1) {
			decimalRegex = "^[0-9]{1,13}$";
		} else if (decimalPlaces == 1) {
			decimalRegex = "^[0-9]{0,13}(\\.[0-9])?$";
		} else {
			decimalRegex = "^[0-9]{0,13}(\\.[0-9]{1," + decimalPlaces + "})?$";
		}
		// defaultStartingBid =
		// functions.getSafeMoney(config.getDouble("default-starting-bid"));
		// defaultBidIncrement =
		// functions.getSafeMoney(config.getDouble("default-bid-increment"));
		// defaultAuctionTime = config.getInt("default-auction-time");
		// maxStartingBid =
		// functions.getSafeMoney(config.getDouble("max-starting-bid"));
		// minIncrement =
		// functions.getSafeMoney(config.getDouble("min-bid-increment"));
		// maxIncrement =
		// functions.getSafeMoney(config.getDouble("max-bid-increment"));
		// maxTime = config.getInt("max-auction-time");
		// minTime = config.getInt("min-auction-time");
		// maxAuctionQueueLength = config.getInt("max-auction-queue-length");
		// minAuctionIntervalSecs = config.getInt("min-auction-interval-secs");
		// allowBidOnOwn = config.getBoolean("allow-bid-on-own-auction");
		// useOldBidLogic = config.getBoolean("use-old-bid-logic");
		// allowEarlyEnd = config.getBoolean("allow-early-end");
		// allowCreativeMode = config.getBoolean("allow-gamemode-creative");
		// allowDamagedItems = config.getBoolean("allow-damaged-items");
		// bannedItems = config.getStringList("banned-items");
		// taxPerAuction = config.getDouble("auction-start-tax");
		// taxPercentage = config.getDouble("auction-end-tax-percent");
		// allowMaxBids = config.getBoolean("allow-max-bids");
		// allowGamemodeChange = config.getBoolean("allow-gamemode-change");
		// allowWorldChange = config.getBoolean("allow-world-change");
		// taxDestinationUser = config.getString("deposit-tax-to-user");
		// cancelPreventionSeconds = config.getInt("cancel-prevention-seconds");
		// cancelPreventionPercent =
		// config.getDouble("cancel-prevention-percent");
	}

	public FileConfiguration getTextConfig() {
		if (textConfig == null) {
			reloadTextConfig();
		}
		return textConfig;
	}

	public void reloadTextConfig() {
		textConfig = YamlConfiguration.loadConfiguration(new File(this
				.getDataFolder(), "language.yml"));

		InputStream defConfigStream = getResource("language.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);

			textConfig.setDefaults(defConfig);
		}
	}

	public void saveTextConfig() {
		File file = new File(this.getDataFolder(), "language.yml");
		try {
			getTextConfig().save(file);
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE,
					"Could not save language config to " + file, ex);
		}
	}

	public void onDisable() {
		getServer().getScheduler().cancelTask(queueTimer);
		Messaging.sendMessage("plugin-disabled",
				getServer().getConsoleSender(), null);

		functions.saveObject(orphanLots, "orphanLots.ser");
	}

	// private static void broadcastMessage(String message) {
	// Player[] onlinePlayers = server.getOnlinePlayers();
	//
	// for (Player player : onlinePlayers) {
	// if (voluntarilyDisabledUsers.indexOf(player.getName()) == -1) {
	// player.sendMessage(message);
	// }
	// }
	//
	// if (voluntarilyDisabledUsers.indexOf("*console*") == -1) {
	// console.sendMessage(message);
	// }
	// }

	private boolean setupEconomy() {
		if (server.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = server.getServicesManager()
				.getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = server.getServicesManager()
				.getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	// Scope handling
	public AuctionScope getScope(CommandSender sender) {
		// // TODO: Figure out auction for context.
		// In the mean time, use public auction.
		return auctionScopes.get(0);
	}

	public ArrayList<AuctionScope> getAuctionScopes() {
		return auctionScopes;
	}

	private void loadScopes() {
		// TODO Actually Get stuff from config currently just using global

		AuctionScope scope = new AuctionScope(this, getConfig());
		auctionScopes.add(scope);
		getServer().getPluginManager().registerEvents(scope, this);
	}

}
