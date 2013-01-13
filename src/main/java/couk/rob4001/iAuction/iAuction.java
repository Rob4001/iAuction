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

	public static ArrayBlockingQueue<Auction> auctionQueue ;
	public HashMap<String, ArrayList<CardboardBox>> lots = new HashMap<String, ArrayList<CardboardBox>>();
	public ArrayList<String> listeners = new ArrayList<String>();
	private static iAuction instance;
	public static boolean wiienabled = false;
	private Economy economy;
	private YamlConfiguration langconfig;

	@SuppressWarnings("unchecked")
	public void onEnable() {
		instance = this;
		setupLanguages();
		setupConfig();
		auctionQueue = new ArrayBlockingQueue<Auction>(
				getConfig().getInt("start.queuesize"), true);
		new ChatManager(this);
		if (!setupEconomy()) {
			getLogger().log(Level.SEVERE, Messaging.get("error.economy"));
			this.setEnabled(false);
			return;
		}
		
		if (getServer().getPluginManager().getPlugin("WhatIsIt") != null) {
			wiienabled = true;
			getLogger().info("[iAuction] WhatIsIt Integration Enabled");
		}

		this.getCommand("auction").setExecutor(new AuctionCommand());

		this.getServer().getPluginManager()
				.registerEvents(new GUIListener(), this);
		this.getServer().getPluginManager()
		.registerEvents(this, this);
		
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
				lots = (HashMap<String, ArrayList<CardboardBox>>) ois.readObject();
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
				listeners = (ArrayList<String>) ois.readObject();
				ois.close();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		if(!auctionQueue.isEmpty()){
			auctionQueue.peek().start();
		}
		
		for(String name :listeners){
			if(Bukkit.getPlayer(name) != null){
				ChatManager.addListener(Bukkit.getPlayer(name));
				getLogger().log(Level.INFO,name);
			}
		}
		
		

	}

	public void onDisable() {
		if (iAuction.getCurrent() != null){
		 iAuction.getCurrent().stop();
		}
	        try {
	        	FileOutputStream fos = new FileOutputStream(new File(
						this.getDataFolder(), "lots.auction"));
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(lots);
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
				oos.writeObject(listeners);
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        ChatManager.removeChats();
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		if(listeners.contains(e.getPlayer().getDisplayName())){
			ChatManager.addListener(e.getPlayer());
		}
		if(lots.keySet().contains(e.getPlayer().getDisplayName())){
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

	// Language Support
	public YamlConfiguration getLangConfig() {
		return langconfig;
	}

	private void setupLanguages() {
		InputStream langStream = getResource("defaultLang.yml");
		langconfig = YamlConfiguration.loadConfiguration(new File(
				getDataFolder(), "lang.yml"));
		if (langStream != null) {
			YamlConfiguration defaultLang = YamlConfiguration
					.loadConfiguration(langStream);
			if (defaultLang != null) {
				langconfig.setDefaults(defaultLang);
			}
		}



		langconfig.options().copyDefaults(true);

		try {
			langconfig.save(new File(getDataFolder(), "lang.yml"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Messaging.plugin = this;
		Messaging.tag = this.getLangConfig().getString("tag");

	}
	public void setupConfig(){
		InputStream confStream = getResource("defaultConf.yml");
		
		if (confStream != null) {
			YamlConfiguration defaultConf = YamlConfiguration
					.loadConfiguration(confStream);
			if (defaultConf != null) {
				getConfig().setDefaults(defaultConf);
			}
		}
getConfig().options().copyDefaults(true);
saveConfig();
	}

	public void saveLang() {
		try {
			langconfig.save(new File(getDataFolder(), "lang.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public boolean hasAuction(Player player){
		for(Auction a : auctionQueue){
			if (a.getOwner() == player){
				return true;
			}
		}
		return false;
	}

}
