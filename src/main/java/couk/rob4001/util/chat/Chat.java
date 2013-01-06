package couk.rob4001.util.chat;

import org.bukkit.entity.Player;

public interface Chat<T> {

	public void broadcast(String Msg);

	public T setup();
	
	public void addListener(Player p);
	public void removeListener(Player p);

}
