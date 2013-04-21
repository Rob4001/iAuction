package couk.rob4001.util.chat;

import java.util.ArrayList;

import org.bukkit.entity.Player;

public class NonChannelChat implements Chat<NonChannelChat> {

	ArrayList<Player> listeners = new ArrayList<Player>();

	@Override
	public void broadcast(String Msg) {
		for (Player p : this.listeners) {
			p.sendMessage(Msg);
		}

	}

	@Override
	public NonChannelChat setup() {
		return this;
	}

	@Override
	public void addListener(Player p) {
		if (!this.listeners.contains(p)) {
			this.listeners.add(p);
		}
	}

	@Override
	public void removeListener(Player p) {

		this.listeners.remove(p);

	}

}
