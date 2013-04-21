package couk.rob4001.iAuction;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import couk.rob4001.util.chat.ChatManager;

public class Messaging {
	public static iAuction plugin;
	public static String tag;

	public static String get(String path, String... arg) {
		String msg;
		if (iAuction.getInstance().getLangConfig().contains(path)) {
			msg = iAuction.getInstance().getLangConfig().getString(path);
		} else {
			iAuction.getInstance().getLangConfig().set(path, "");
			// TODO:remove this
			iAuction.getInstance().saveLang();
			return "Missing Language config : " + path;
		}
		// TODO: dont display message if blank

		msg = tag + msg;

		for (int i = 0; i < arg.length; i++) {
			msg = msg.replace("{" + i + "}", arg[i]);
		}
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		return msg;
	}

	public static void playerMessage(Player player, String path, String... arg) {
		String msg = get(path, arg);

		for (String part : msg.split("\\{n\\}")) {
			player.sendMessage(part.replace(tag, ""));
		}
	}

	public static void broadcast(String path, String... arg) {
		String msg = get(path, arg);
		for (String part : msg.split("\\{n\\}")) {
			ChatManager.broadcast(part);
		}
	}
}
