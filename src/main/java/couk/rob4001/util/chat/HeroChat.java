package couk.rob4001.util.chat;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.dthielke.herochat.Channel;
import com.dthielke.herochat.Chatter;
import com.dthielke.herochat.Herochat;

public class HeroChat implements Chat<HeroChat> {

	private Channel c;

	@Override
	public void broadcast(String Msg) {
		this.c.announce(Msg);
	}

	@Override
	public HeroChat setup() {
		JavaPlugin pl = ChatManager.getPlugin();
		pl.getConfig().addDefault("herochat.channel", "trade");
		pl.getConfig().addDefault("herochat.enable", "true");
		pl.saveConfig();
		com.dthielke.herochat.ChannelManager cm = Herochat.getChannelManager();
		this.c = cm.getChannel(pl.getConfig().getString("herochat.channel"));
		return this;
	}

	@Override
	public void addListener(Player p) {
		Chatter chatter = Herochat.getChatterManager().getChatter(p);
		if (chatter == null)
			return;

		chatter.addChannel(this.c, true, true);

	}

	@Override
	public void removeListener(Player p) {
		Chatter chatter = Herochat.getChatterManager().getChatter(p);
		if (chatter == null)
			return;

		chatter.removeChannel(this.c, true, true);

	}

}
