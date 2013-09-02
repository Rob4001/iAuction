package couk.rob4001.util.chat;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatManager {

	private static ArrayList<Chat<?>> chats = new ArrayList<Chat<?>>();
	private static JavaPlugin plugin;

	public ChatManager(JavaPlugin plugin) {
		ChatManager.plugin = plugin;

		boolean found = false;

		if (plugin.getServer().getPluginManager().getPlugin("Herochat") != null
				&& (plugin.getConfig().getBoolean("herochat.enable") || !plugin
						.getConfig().contains("herochat.enable"))) {
			chats.add(new HeroChat().setup());
			found = true;
			plugin.getLogger().info("[iAuction] Herochat Integration Enabled");
		}
		if (plugin.getServer().getPluginManager().getPlugin("ChannelChat") != null
				&& (plugin.getConfig().getBoolean("channelchat.enable") || !plugin
						.getConfig().contains("channelchat.enable"))) {
			chats.add(new ChannelChat().setup());
			found = true;
			plugin.getLogger().info(
					"[iAuction] ChannelChat Integration Enabled");
		}
		if (plugin.getServer().getPluginManager().getPlugin("iChat") != null) {
			chats.add(new NonChannelChat().setup());
			found = true;
			plugin.getLogger().info(
					"[iAuction] Non-ChannelChat Integration Enabled");
		}
		if (!found) {
			chats.add(new NonChannelChat().setup());
			plugin.getLogger()
					.info("[iAuction] No Chat plugin found: Enabling Non-ChannelCHat ");
		}
	}

	public static JavaPlugin getPlugin() {
		return plugin;
	}

	public static void broadcast(String msg) {
		for (Chat<?> chat : chats) {
			chat.broadcast(msg);
		}
	}

	public static void addListener(Player p) {
		
		for (Chat<?> chat : chats) {
			chat.addListener(p);
		}
	}

	public static void removeListener(Player p) {
		for (Chat<?> chat : chats) {
			chat.removeListener(p);
		}
	}

	public static void removeChats() {
		chats.clear();
	}

}
