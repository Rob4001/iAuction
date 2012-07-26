package couk.rob4001.iauction;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import couk.rob4001.iauction.chat.ChatManager;

public class Messaging {
	public static iAuction plugin;
	public static String tag;

	public static String get(String path, String... arg) {
		String msg;
		if(plugin.getLangConfig().contains(path)){
		msg = plugin.getLangConfig().getString(path);
		}else{
			return "Missing Language config : " + path;
		}
		
		msg = tag  +msg;

		for (int i = 0; i < arg.length; i++) {
			msg = msg.replace("{" + i + "}", arg[i]);
		}
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		return msg;
	}
	
	public static void playerMessage(Player player,String path,String... arg){
		String msg = get(path,arg);
		player.sendMessage(msg.replace(tag, ""));
	}
	
	public static void broadcast(String path,String... arg){
		String msg = get(path,arg);
		for(String part:msg.split("\n")){
			ChatManager.broadcast(part);
		}
	}
}
