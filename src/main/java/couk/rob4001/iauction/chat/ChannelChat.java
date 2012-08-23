package couk.rob4001.iauction.chat;

import org.bukkit.entity.Player;

import com.feildmaster.channelchat.channel.Channel;
import com.feildmaster.channelchat.channel.ChannelManager;

import couk.rob4001.iauction.iAuction;

public class ChannelChat implements Chat<ChannelChat> {

	private Channel c;

	@Override
	public void broadcast(String msg) {
		this.c.sendMessage(msg);

	}

	@Override
	public ChannelChat setup() {
		iAuction pl = ChatManager.getPlugin();

		pl.getConfig().addDefault("channelchat.channel", "trade");

		ChannelManager cm = ChannelManager.getManager();
		this.c = cm.getChannel(pl.getConfig().getString("channelchat.channel"));

		return this;

	}

	@Override
	public void addListener(Player p) {
		c.addMember(p);
	}

	@Override
	public void removeListener(Player p) {
		c.delMember(p);
	}

}
