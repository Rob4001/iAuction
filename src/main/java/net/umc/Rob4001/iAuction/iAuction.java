package net.umc.Rob4001.iAuction;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

//import com.ensifera.animosity.craftirc.CraftIRC;
//import com.ensifera.animosity.craftirc.EndPoint;
//import com.ensifera.animosity.craftirc.RelayedMessage;
import com.herocraftonline.dthielke.herochat.HeroChat;


public class iAuction extends JavaPlugin implements Listener{
	public class CoolRunnable implements Runnable {
	    
	    private iAuction plugin;

	    public CoolRunnable(iAuction plugin){
		this.plugin = plugin;
	    }

	@Override
	public void run() {
	    plugin.inCooldown=false;

	}
	

    }

	private Auction auction;
	private boolean circEnabled;
	//private CraftIRC craftIRC;
	private com.herocraftonline.dthielke.herochat.channels.Channel c;
	private boolean hcEnabled;
	private int task;
	private Logger log = Logger.getLogger("Minecraft");
	public ChatColor auxcolour;
	public ChatColor fieldcolour;
	public ChatColor valuecolour;
	public ChatColor errorcolour;
	public ChatColor titlecolour;
	public ChatColor helpcolour;
	public ChatColor helpvaluecolour;
	private com.feildmaster.channelchat.channel.Channel cc;
	private boolean ccEnabled;
	private boolean inCooldown;
	private String tag;
	private Economy economy;
	
	public void onDisable() {
		System.out.println("[iAuction] Disabled!");
	}

	public void onEnable() {
		getCommand("auction").setExecutor(new AuctionCommand(this));
		
		setupConfig();
		setupColours();
		if (!setupEconomy()) {
			log.warning("[iAuction] Economy Failure! Make sure you have an economy and Vault in your plugins folder ");
			this.setEnabled(false);
			return;
		}
		if (getConfig().getBoolean("herochat.enable"))
			enableHeroChat();
		if (getConfig().getBoolean("channelchat.enable"))
			enableChannelChat();
		if (getConfig().getBoolean("craftirc.enable"))
			//enableCraftIRC();
		this.getServer().getPluginManager().registerEvents(this,this);
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
		log.info("[iAuction] Enabled!");
		
	}

	private void enableChannelChat() {
	    String ccChannelName = getConfig().getString("channelchat.channel");
		Plugin p = getServer().getPluginManager().getPlugin("ChannelChat");
		if (p != null) {
			if (!p.isEnabled())
				getServer().getPluginManager().enablePlugin(p);
			com.feildmaster.channelchat.channel.ChannelManager cm = com.feildmaster.channelchat.channel.ChannelManager.getManager();

			this.cc = cm.getChannel(ccChannelName);
			if ((this.cc.getName().equalsIgnoreCase(ccChannelName))
					|| (this.cc.getAlias().equalsIgnoreCase(ccChannelName))) {
				System.out
						.println("[iAuction]ChannelChat system has enabled properly!");
				this.ccEnabled = true;
				return;
			}
			System.out
					.println("[iAuction]Your channel spesified does not exist");
		} else {
			System.out
					.println("[iAuction] ChannelChat system is enabled but could not be loaded!");
		}
		this.ccEnabled = false;
	    
	}

	private void setupColours() {
	   titlecolour = ChatColor.getByChar(getConfig().getString("colour.title"));
	   fieldcolour = ChatColor.getByChar(getConfig().getString("colour.field"));
	   auxcolour = ChatColor.getByChar(getConfig().getString("colour.aux"));
	   valuecolour = ChatColor.getByChar(getConfig().getString("colour.value"));
	   errorcolour = ChatColor.getByChar(getConfig().getString("colour.error"));
	   helpcolour = ChatColor.getByChar(getConfig().getString("colour.help"));
	   helpvaluecolour = ChatColor.getByChar(getConfig().getString("colour.helpvalue"));
	   tag = getConfig().getString("tag");
	}

	private void setupConfig() {
		FileConfiguration c = getConfig();

		c.addDefault("start.maxtime", Integer.valueOf(60));
		c.addDefault("start.mintime", Integer.valueOf(0));
		c.addDefault("start.maxbid", Integer.valueOf(0));
		c.addDefault("bidding.maxbid", Integer.valueOf(0));
		c.addDefault("bidding.minbid", Integer.valueOf(5));
		c.addDefault("bidding.minincrement", Integer.valueOf(0));
		c.addDefault("herochat.enable", Boolean.valueOf(false));
		c.addDefault("herochat.channel", "trade");
		c.addDefault("channelchat.enable", Boolean.valueOf(false));
		c.addDefault("channelchat.channel", "trade");
		c.addDefault("craftirc.enable", Boolean.valueOf(false));
		c.addDefault("craftirc.tag", "");
		c.addDefault("antisnipe.enabled", Boolean.valueOf(false));
		c.addDefault("antisnipe.value", Integer.valueOf(5));
		c.addDefault("antisnipe.endtime", Integer.valueOf(0));
		c.addDefault("decimalcurrency", Boolean.valueOf(true));
		c.addDefault("colour.title", "a");
		c.addDefault("colour.field", "9");
		c.addDefault("colour.value", "b");
		c.addDefault("colour.error", "c");
		c.addDefault("colour.aux", "e");
		c.addDefault("colour.help", "b");
		c.addDefault("colour.helpvalue", "e");
		c.addDefault("tag", "[iAuction]");
		c.addDefault("allowincreative",Boolean.valueOf(true));

		c.options().copyDefaults(true);

		saveConfig();
	}

	public void enableHeroChat() {
		String hcChannelName = getConfig().getString("herochat.channel");
		Plugin p = getServer().getPluginManager().getPlugin("HeroChat");
		if (p != null) {
			if (!p.isEnabled())
				getServer().getPluginManager().enablePlugin(p);
			com.herocraftonline.dthielke.herochat.channels.ChannelManager cm = ((HeroChat) p).getChannelManager();

			this.c = cm.getChannel(hcChannelName);
			if ((this.c.getName().equalsIgnoreCase(hcChannelName))
					|| (this.c.getNick().equalsIgnoreCase(hcChannelName))) {
				System.out
						.println(tag + " Herochat system has enabled properly!");
				this.hcEnabled = true;
				return;
			}
			System.out
					.println(tag + " Your channel spesified does not exist");
		} else {
			System.out
					.println(tag + " HeroChat system is enabled but could not be loaded!");
		}
		this.hcEnabled = false;
	}

//	public void enableCraftIRC() {
//		Plugin p = getServer().getPluginManager().getPlugin("CraftIRC");
//		if (p == null) {
//			System.out.println(tag + " WARNING! CraftIRC not detected!");
//			this.circEnabled = false;
//		} else {
//			try {
//				System.out
//						.println(tag + " CraftIRC found, enabling support...");
//				this.craftIRC = ((CraftIRC) p);
//			} catch (ClassCastException ex) {
//				ex.printStackTrace();
//				System.out
//						.println("[SEVERE]"+ tag + " Unable to link to CraftIRC!");
//				this.circEnabled = false;
//			}
//		}
//	}
	
	private Boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

	public Economy getEco() {
		return this.economy;
	}

	public void New(ItemStack l, Player o, int t, float b) {
	    if(!inCooldown){
		this.auction = new Auction(this, l, o, t, b);
		this.task = getServer().getScheduler().scheduleAsyncRepeatingTask(this,
				this.auction, 0L, 20L);
	    }else{
		warn(o, "Cooldown Period Active!");
	    }
	}

	public Auction getAuc() {
		return this.auction;
	}

	public void resetAuc() {
		this.auction = null;
		getServer().getScheduler().cancelTask(this.task);
		this.inCooldown = true;
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new CoolRunnable(this) , getConfig().getInt("auction.cooldown") * 20L);
	}

	public void warn(Player player, String msg) {
		player.sendMessage(ChatColor.RED + "[iAuction] " + msg);
	}

	public void broadcast(String msg) {
		if (this.hcEnabled) {
			this.c.sendMessage(tag, msg, this.c.getMsgFormat(),
					this.c.getPlayers(), false, true);
		} else if(this.ccEnabled) {
		    this.cc.sendMessage(tag +  msg);
		}else{
			getServer().broadcastMessage("[iAuction] " + msg);
		}
		if (this.circEnabled) {
			//RelayedMessage rm = this.craftIRC.newMsgToTag(this,tag, "");
			//rm.setField("message", msg);
		}
	}
	@EventHandler
	public void quitChecker(PlayerQuitEvent e){
	    if (!(this.auction == null)){
		if(this.auction.owner == e.getPlayer()){
		    this.auction.stop();
		}
	    }
	}

}