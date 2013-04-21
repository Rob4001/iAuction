package couk.rob4001.util.chat;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.feildmaster.channelchat.channel.Channel;
import com.feildmaster.channelchat.channel.ChannelManager;

public class ChannelChat implements Chat<ChannelChat> {

	private Channel c;

	@Override
	public void broadcast(String msg) {
		this.c.sendMessage(msg);

	}

	@Override
	public ChannelChat setup() {
		JavaPlugin pl = ChatManager.getPlugin();

		pl.getConfig().addDefault("channelchat.channel", "trade");
		pl.getConfig().addDefault("channelchat.enable", "true");

		ChannelManager cm = ChannelManager.getManager();
		this.c = cm.getChannel(pl.getConfig().getString("channelchat.channel"));

		return this;

	}

	@Override
	public void addListener(Player p) {
		this.c.addMember(p);
	}

	@Override
	public void removeListener(Player p) {
		this.c.delMember(p);
	}

}
