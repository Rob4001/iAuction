package couk.rob4001.iauction.chat;

import java.util.ArrayList;

import couk.rob4001.iauction.iAuction;
import couk.rob4001.utility.functions;

public class ChatManager {

	static ArrayList<String> listeners = new ArrayList<String>();
	private static iAuction plugin;

	public ChatManager(iAuction plugin) {
		ChatManager.plugin = plugin;
		listeners = functions.loadArrayListString("listeners.ser");
	}


	public static void broadcast(String msg) {
		for (String p : listeners) {
			plugin.getServer().getPlayer(p).sendMessage(msg);
		}
	}

	public static void addListener(String p) {
		if (!listeners.contains(p)) {
			listeners.add(p);
		}
	}

	public static void removeListener(String p) {
		if(listeners.contains(p)){
			listeners.remove(p);
		}
	}


	public static void stop() {
		functions.saveObject(listeners, "listeners.ser");
	}

}
