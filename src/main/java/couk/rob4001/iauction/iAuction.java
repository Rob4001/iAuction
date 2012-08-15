package couk.rob4001.iauction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import couk.rob4001.utility.functions;

public class iAuction extends JavaPlugin {
	private static final Logger log = Logger.getLogger("Minecraft");
	public Auction publicAuction;

	// Got to figure out a better way to store these:
//	public static long defaultStartingBid = 0;
//	public static long defaultBidIncrement = 100;
//	public static int defaultAuctionTime = 60;
//	public static long maxStartingBid = 10000;
//	public static long minIncrement = 1;
//	public static long maxIncrement = 100;
//	public static int maxTime = 60;
//	public static int minTime = 15;
//	public static int maxAuctionQueueLength = 2;
//	public static int minAuctionIntervalSecs = 10;
//	public static boolean allowBidOnOwn = false;
//	public static boolean useOldBidLogic = false;
//	public static boolean logAuctions = false;
//	public static boolean allowEarlyEnd = false;
	public static int decimalPlaces = 2;
	public static String decimalRegex = "^[0-9]{0,13}(\\.[0-9]{1," + decimalPlaces + "})?$";
//	public static boolean allowCreativeMode = false;
//	public static boolean allowDamagedItems = false;
//	private static File auctionLog = null;
	private static long lastAuctionDestroyTime = 0;
//	public static boolean allowMaxBids = true;
//	public static boolean allowGamemodeChange = false;
//	public static boolean allowWorldChange = false;
//	public static List<String> bannedItems = new ArrayList<String>();
//	public static double taxPerAuction = 0;
//	public static double taxPercentage = 0;
//	public static String taxDestinationUser = "";
	public static Location currentBidPlayerLocation;
	public static GameMode currentBidPlayerGamemode;
	public static Location currentAuctionOwnerLocation;
	public static GameMode currentAuctionOwnerGamemode;
//	public static int cancelPreventionSeconds = 15;
//	public static double cancelPreventionPercent = 50;
	
	// Config files info.
//	private static File configFile = null;
//	private static InputStream defConfigStream;
	public static FileConfiguration config = null;
//	private static File textConfigFile = null;
//	private static InputStream defTextConfigStream;
	public FileConfiguration textConfig = null;
//	private static YamlConfiguration defConfig = null;
//	private static YamlConfiguration defTextConfig = null;
	public static File dataFolder;
//	private static ConsoleCommandSender console;
	public static Server server;
	public static int queueTimer;
	public static ArrayList<Auction> auctionQueue = new ArrayList<Auction>(); 
	
	// TODO: Save these items when updated so we don't loose info when restarting.
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
		for(int i = 0; i < orphanDeathRow.size(); i++) {
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
    public static Permission perms = null;
    public static Chat chat = null;

	public void onEnable() {
		server = getServer();
//		console = server.getConsoleSender();
		dataFolder = getDataFolder();
		final Plugin plugin = this;

		setupEconomy();
        setupPermissions();
        setupChat();

		if (server.getPluginManager().getPlugin("WhatIsIt") == null) {
			log.log(Level.SEVERE, Messaging.chatPrepClean(textConfig.getString("no-whatisit")));
			server.getPluginManager().disablePlugin(this);
            return;
		}
        loadConfig();
		if (econ == null) {
			log.log(Level.SEVERE, Messaging.chatPrepClean(textConfig.getString("no-economy")));
			server.getPluginManager().disablePlugin(this);
            return;
		}
        
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void playerJoin(PlayerJoinEvent event) {
        	    iAuction.killOrphan(event.getPlayer());
            }
            @EventHandler
            public void onPlayerChangedWorld(PlayerChangedWorldEvent event){
            	if (getConfig().getBoolean("allow-world-change") || publicAuction == null) return;
            	
                // Get player objects
                final Player player = event.getPlayer();
                if (publicAuction.getOwner().equalsIgnoreCase(player.getName()) && !player.getLocation().getWorld().equals(currentAuctionOwnerLocation.getWorld())) {
                	// This is running as a timer because MultiInv is using HIGHEST priority and 
                	// there's no way to send a cancel to it, so we have to go after the fact and
                	// remove the user.
                	getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                		public void run() {
    	                	player.teleport(currentAuctionOwnerLocation, TeleportCause.PLUGIN);
                		}
                	}, 1L);
                	Messaging.sendMessage("worldchange-fail-auction-owner", player, publicAuction);
                } else if (publicAuction.getCurrentBid() != null && publicAuction.getCurrentBid().getBidder().equalsIgnoreCase(player.getName())
                		 && !player.getLocation().getWorld().equals(currentBidPlayerLocation.getWorld())
                		) {
                	// This is running as a timer because MultiInv is using HIGHEST priority and 
                	// there's no way to send a cancel to it, so we have to go after the fact and
                	// remove the user.
                	getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                		public void run() {
                        	player.teleport(currentBidPlayerLocation, TeleportCause.PLUGIN);
                		}
                	}, 1L);
                	Messaging.sendMessage("worldchange-fail-auction-bidder", player, publicAuction);
                }
            }
            @EventHandler
            public void onPlayerChangedWorld(PlayerGameModeChangeEvent event){
            	if (getConfig().getBoolean("allow-gamemode-change") || publicAuction == null) return;
            	
                // Get player objects
                Player player = event.getPlayer();
                
                if (publicAuction.getOwner().equalsIgnoreCase(player.getName())) {
                	event.setCancelled(true);
                	Messaging.sendMessage("gamemodechange-fail-auction-owner", player, publicAuction);
                } else if (publicAuction.getCurrentBid() != null && publicAuction.getCurrentBid().getBidder().equalsIgnoreCase(player.getName())) {
                	event.setCancelled(true);
                	Messaging.sendMessage("gamemodechange-fail-auction-bidder", player, publicAuction);
                }
            }
        }, this);
		
		queueTimer = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
		    public void run() {
		    	checkAuctionQueue();
		    }
		}, 20L, 20L);
		
		orphanLots = functions.loadArrayListAuctionLot("orphanLots.ser");
		voluntarilyDisabledUsers = functions.loadArrayListString("voluntarilyDisabledUsers.ser");
		suspendedUsers = functions.loadArrayListString("suspendedUsers.ser");

        // Load up the Plugin metrics
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
		Messaging.sendMessage("plugin-enabled", getServer().getConsoleSender(), null);
		
		//TODO: Load orphan lots from save file.
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
		
//	    logAuctions = getConfig().getBoolean("log-auctions");
	    
		decimalPlaces = Math.min(Math.max(getConfig().getInt("decimal-places"), 0), 5);
		getConfig().set("decimal-places", decimalPlaces);
		if (decimalPlaces < 1) {
			decimalRegex = "^[0-9]{1,13}$";
		} else if (decimalPlaces == 1) {
			decimalRegex = "^[0-9]{0,13}(\\.[0-9])?$";
		} else {
			decimalRegex = "^[0-9]{0,13}(\\.[0-9]{1," + decimalPlaces + "})?$";
		}
//	    defaultStartingBid = functions.getSafeMoney(config.getDouble("default-starting-bid"));
//		defaultBidIncrement = functions.getSafeMoney(config.getDouble("default-bid-increment"));
//		defaultAuctionTime = config.getInt("default-auction-time");
//		maxStartingBid = functions.getSafeMoney(config.getDouble("max-starting-bid"));
//		minIncrement = functions.getSafeMoney(config.getDouble("min-bid-increment"));
//		maxIncrement = functions.getSafeMoney(config.getDouble("max-bid-increment"));
//		maxTime = config.getInt("max-auction-time");
//		minTime = config.getInt("min-auction-time");
//		maxAuctionQueueLength = config.getInt("max-auction-queue-length");
//		minAuctionIntervalSecs = config.getInt("min-auction-interval-secs");
//		allowBidOnOwn = config.getBoolean("allow-bid-on-own-auction");
//		useOldBidLogic = config.getBoolean("use-old-bid-logic");
//		allowEarlyEnd = config.getBoolean("allow-early-end");
//		allowCreativeMode = config.getBoolean("allow-gamemode-creative");
//		allowDamagedItems = config.getBoolean("allow-damaged-items");
//		bannedItems = config.getStringList("banned-items");
//		taxPerAuction = config.getDouble("auction-start-tax");
//		taxPercentage = config.getDouble("auction-end-tax-percent");
//		allowMaxBids = config.getBoolean("allow-max-bids");
//		allowGamemodeChange = config.getBoolean("allow-gamemode-change");
//		allowWorldChange = config.getBoolean("allow-world-change");
//		taxDestinationUser = config.getString("deposit-tax-to-user");
//		cancelPreventionSeconds = config.getInt("cancel-prevention-seconds");
//		cancelPreventionPercent = config.getDouble("cancel-prevention-percent");
    }
    public FileConfiguration getTextConfig() {
        if (textConfig == null) {
            reloadTextConfig();
        }
        return textConfig;
    }

    public void reloadTextConfig() {
    	textConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(),"language.yml"));

        InputStream defConfigStream = getResource("language.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);

            textConfig.setDefaults(defConfig);
        }
    }
    public void saveTextConfig() {
    	File file = new File(this.getDataFolder(),"language.yml");
        try {
            getTextConfig().save(file);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save language config to " + file, ex);
        }
    }
    
	public void onDisable() { 
		getServer().getScheduler().cancelTask(queueTimer);
		Messaging.sendMessage("plugin-disabled", getServer().getConsoleSender(), null);
		
		//TODO: Save orphan lots from save file.
	}
	public void detachAuction(Auction auction) {
		publicAuction = null;
		lastAuctionDestroyTime = System.currentTimeMillis();
		checkAuctionQueue();
	}
	
    
    public void queueAuction(Auction auctionToQueue, Player player, Auction currentAuction) {
		String playerName = player.getName();

		if (currentAuction == null) {
			// Queuing because of interval not yet timed out.
			// Allow a queue of 1 to override if 0 for this condition.
	    	if (Math.max(getConfig().getInt("max-auction-queue-length"), 1) <= auctionQueue.size()) {
				Messaging.sendMessage("auction-queue-fail-full", player, currentAuction);
				return;
			}
		} else {
	    	if (getConfig().getInt("max-auction-queue-length") <= 0) {
				Messaging.sendMessage("auction-fail-auction-exists", player, currentAuction);
				return;
			}
			if (currentAuction.getOwner().equalsIgnoreCase(playerName)) {
				Messaging.sendMessage("auction-queue-fail-current-auction", player, currentAuction);
				return;
			}
			if (getConfig().getInt("max-auction-queue-length") <= auctionQueue.size()) {
				Messaging.sendMessage("auction-queue-fail-full", player, currentAuction);
				return;
			}
		}
		for(int i = 0; i < auctionQueue.size(); i++) {
			if (auctionQueue.get(i) != null) {
				Auction queuedAuction = auctionQueue.get(i);
				if (queuedAuction.getOwner().equalsIgnoreCase(playerName)) {
					Messaging.sendMessage("auction-queue-fail-in-queue", player, currentAuction);
					return;
				}
			}
		}
		if (auctionToQueue.isValid()) {
			auctionQueue.add(auctionToQueue);
			checkAuctionQueue();
			if (auctionQueue.contains(auctionToQueue)) {
				Messaging.sendMessage("auction-queue-enter", player, currentAuction);
			}
		}
    }
	private void checkAuctionQueue() {
		if (publicAuction != null) {
			return;
		}
		if (System.currentTimeMillis() - lastAuctionDestroyTime < getConfig().getInt("min-auction-interval-secs") * 1000) {
			return;
		}
		if (auctionQueue.size() == 0) {
			return;
		}
		Auction auction = auctionQueue.remove(0);
		if (auction == null) {
			return;
		}
		if (!auction.isValid()) {
			return;
		}
		if (auction.start()) {
			publicAuction = auction;
		}
	}
    
  
    
   
//    private static void broadcastMessage(String message) {
//    	Player[] onlinePlayers = server.getOnlinePlayers();
//    	
//    	for (Player player : onlinePlayers) {
//        	if (voluntarilyDisabledUsers.indexOf(player.getName()) == -1) {
//        		player.sendMessage(message);
//    		}
//    	}
//    	
//    	if (voluntarilyDisabledUsers.indexOf("*console*") == -1) {
//			console.sendMessage(message);
//		}
//    }

	private boolean setupEconomy() {
        if (server.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = server.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = server.getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return false;
        }
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = server.getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    
    //Scope handling
	public AuctionScope getScope(CommandSender sender) {
		// TODO Auto-generated method stub
		return null;
	}
	public ArrayList<AuctionScope> getAuctionScopes() {
		return auctionScopes;
	}
}

