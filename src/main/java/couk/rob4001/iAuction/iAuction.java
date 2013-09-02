package couk.rob4001.iAuction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import couk.rob4001.iAuction.gui.GUIListener;
import couk.rob4001.util.cardboard.CardboardBox;
import couk.rob4001.util.chat.ChatManager;

public class iAuction extends JavaPlugin implements Listener {

	public static ArrayBlockingQueue<Auction> auctionQueue;
	public HashMap<String, ArrayList<CardboardBox>> lots = new HashMap<String, ArrayList<CardboardBox>>();
	public ArrayList<String> listeners = new ArrayList<String>();
	private static iAuction instance;
	public static boolean wiienabled = false;
	private Economy economy;
	private YamlConfiguration langconfig;

	@Override
	@SuppressWarnings("unchecked")
	public void onEnable() {
		instance = this;
		this.setupLanguages();
		this.setupConfig();
		auctionQueue = new ArrayBlockingQueue<Auction>(this.getConfig().getInt(
				"start.queuesize"), true);
		new ChatManager(this);
		if (!this.setupEconomy()) {
			this.getLogger().log(Level.SEVERE, Messaging.get("error.economy"));
			this.setEnabled(false);
			return;
		}

		if (this.getServer().getPluginManager().getPlugin("WhatIsIt") != null) {
			wiienabled = true;
			this.getLogger().info("[iAuction] WhatIsIt Integration Enabled");
		}

		this.getCommand("auction").setExecutor(new AuctionCommand());

		this.getServer().getPluginManager()
				.registerEvents(new GUIListener(), this);
		this.getServer().getPluginManager().registerEvents(this, this);

		try {
			BukkitMetricsLite metrics = new BukkitMetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}

		if (new File(this.getDataFolder(), "lots.auction").exists()) {
			try {
				FileInputStream fis = new FileInputStream(new File(
						this.getDataFolder(), "lots.auction"));
				ObjectInputStream ois = new ObjectInputStream(fis);
				this.lots = (HashMap<String, ArrayList<CardboardBox>>) ois
						.readObject();
				ois.close();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		if (new File(this.getDataFolder(), "listeners.auction").exists()) {
			try {
				FileInputStream fis = new FileInputStream(new File(
						this.getDataFolder(), "listeners.auction"));
				ObjectInputStream ois = new ObjectInputStream(fis);
				this.listeners = (ArrayList<String>) ois.readObject();
				ois.close();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		if (!auctionQueue.isEmpty()) {
			auctionQueue.peek().start();
		}

		for (String name : this.listeners) {
			if (Bukkit.getPlayer(name) != null) {
				ChatManager.addListener(Bukkit.getPlayer(name));
				this.getLogger().log(Level.INFO, name);
			}
		}

	}

	@Override
	public void onDisable() {
		if (iAuction.getCurrent() != null) {
			iAuction.getCurrent().stop();
		}
		try {
			FileOutputStream fos = new FileOutputStream(new File(
					this.getDataFolder(), "lots.auction"));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this.lots);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			FileOutputStream fos = new FileOutputStream(new File(
					this.getDataFolder(), "queue.auction"));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(auctionQueue);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			FileOutputStream fos = new FileOutputStream(new File(
					this.getDataFolder(), "listeners.auction"));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this.listeners);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ChatManager.removeChats();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if (this.getConfig().getBoolean("listendefault")&&!this.listeners.contains(e.getPlayer().getName())) {
			listeners.add(e.getPlayer().getName());
			ChatManager.addListener(e.getPlayer());
		}
		if (this.lots.keySet().contains(e.getPlayer().getName())) {
			Messaging.playerMessage(e.getPlayer(), "auction.collection");
		}
	}

	// Auction Queue
	public static void queue(Auction auction) {
		auctionQueue.add(auction);
		if (auctionQueue.peek().started == false) {
			auctionQueue.peek().start();
		}
	}

	public static Auction getCurrent() {
		return auctionQueue.peek();
	}

	public static iAuction getInstance() {
		return instance;
	}

	public static void removeCurrent() {
		auctionQueue.poll();
		if (auctionQueue.peek() != null) {
			auctionQueue.peek().start();
		}
	}

	// Economy Stuff
	private Boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = this.getServer()
				.getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			this.economy = economyProvider.getProvider();
		}

		return (this.economy != null);
	}

	public Economy getEco() {
		return this.economy;
	}

	// Language Support
	public YamlConfiguration getLangConfig() {
		return this.langconfig;
	}

	private void setupLanguages() {
		InputStream langStream = this.getResource("defaultLang.yml");
		this.langconfig = YamlConfiguration.loadConfiguration(new File(this
				.getDataFolder(), "lang.yml"));
		if (langStream != null) {
			YamlConfiguration defaultLang = YamlConfiguration
					.loadConfiguration(langStream);
			if (defaultLang != null) {
				this.langconfig.setDefaults(defaultLang);
			}
		}

		this.langconfig.options().copyDefaults(true);

		try {
			this.langconfig.save(new File(this.getDataFolder(), "lang.yml"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Messaging.plugin = this;
		Messaging.tag = this.getLangConfig().getString("tag");

	}

	public void setupConfig() {
		InputStream confStream = this.getResource("defaultConf.yml");

		if (confStream != null) {
			YamlConfiguration defaultConf = YamlConfiguration
					.loadConfiguration(confStream);
			if (defaultConf != null) {
				this.getConfig().setDefaults(defaultConf);
			}
		}
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

	public void saveLang() {
		try {
			this.langconfig.save(new File(this.getDataFolder(), "lang.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public boolean hasAuction(Player player) {
		for (Auction a : auctionQueue) {
			if (a.getOwner() == player)
				return true;
		}
		return false;
	}

	public static void chargedDeposit(Economy eco, OfflinePlayer owner,
			double bid) {

		String fee = iAuction.getInstance().getConfig().getString("bid.fee");
		if (!fee.equalsIgnoreCase("0")) {
			if (fee.contains("%")) {
				fee = fee.replace("%", "");
				int f = Integer.parseInt(fee);
				int remainder = 100 - f;
				bid = bid / 100;
				bid = bid * remainder;
				Messaging.playerMessage(owner.getPlayer(), "bidding.fee", fee);
			} else {
				bid = bid
						- iAuction.getInstance().getConfig().getInt("bid.fee");
				Messaging.playerMessage(
						owner.getPlayer(),
						"bidding.fee",
						eco.format(iAuction.getInstance().getConfig()
								.getInt("bid.fee")));
			}
		}
		eco.depositPlayer(owner.getName(), bid);

	}

	public static void charge(Economy eco, OfflinePlayer owner) {
		int fee = iAuction.getInstance().getConfig().getInt("start.fee");
		if (fee != 0) {
			Messaging.playerMessage(owner.getPlayer(), "start.fee",
					Integer.toString(fee));
			eco.withdrawPlayer(owner.getName(), fee);
		}

	}

}
