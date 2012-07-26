package couk.rob4001.iauction.chat;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import couk.rob4001.iauction.iAuction;

public class ChatManager {

	private static ArrayList<Chat<?>> chats = new ArrayList<Chat<?>>();
	private static iAuction plugin;

	public ChatManager(iAuction plugin) {
		ChatManager.plugin = plugin;
		
		boolean found = false;

		if (plugin.getServer().getPluginManager().getPlugin("Herochat") != null) {
			chats.add(new HeroChat().setup());
			found = true;
			plugin.getLogger().info("[iAuction] Herochat Integration Enabled");
		}
		if (plugin.getServer().getPluginManager().getPlugin("ChannelChat") != null) {
			chats.add(new ChannelChat().setup());
			found = true;
			plugin.getLogger().info("[iAuction] ChannelChat Integration Enabled");
		}
		if (plugin.getServer().getPluginManager().getPlugin("iChat")!= null){
			chats.add(new NonChannelChat().setup());
			found = true;
			plugin.getLogger().info("[iAuction] Non-ChannelChat Integration Enabled");
		}
		if (!found){
			chats.add(new NonChannelChat().setup());
			plugin.getLogger().info("[iAuction] No Chat plugin found: Enabling Non-ChannelCHat ");
		}
	}

	public static iAuction getPlugin() {
		return plugin;
	}

	public static void broadcast(String msg) {
		for (Chat<?> chat : chats) {
			chat.broadcast(msg);
		}
	}
	
	public static void addListener(Player p ){
		for (Chat<?> chat : chats) {
			chat.addListener(p);
		}
	}
	public static void removeListener(Player p ){
		for (Chat<?> chat : chats) {
			chat.removeListener(p);
		}
	}

}
