package couk.rob4001.iauction.chat;

import java.util.ArrayList;

import org.bukkit.entity.Player;


public class NonChannelChat implements Chat<NonChannelChat>{
	
	ArrayList<Player> listeners = new ArrayList<Player>();

	@Override
	public void broadcast(String Msg) {
		for (Player p : listeners){
			p.sendMessage(Msg);
		}
		
	}

	@Override
	public NonChannelChat setup() {
		return this;
	}

	@Override
	public void addListener(Player p) {
		if(!listeners.contains(p)){
		listeners.add(p);
		}
	}

	@Override
	public void removeListener(Player p) {
		
		listeners.remove(p);
		
	}

}
