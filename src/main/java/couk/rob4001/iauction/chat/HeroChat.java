package couk.rob4001.iauction.chat;

import org.bukkit.entity.Player;

import com.dthielke.herochat.Channel;
import com.dthielke.herochat.Chatter;
import com.dthielke.herochat.Herochat;

import couk.rob4001.iauction.iAuction;

public class HeroChat implements Chat<HeroChat> {

	private Channel c;


	@Override
	public void broadcast(String Msg) {
		c.announce(Msg);
	}

	@Override
	public HeroChat setup() {
		iAuction pl = ChatManager.getPlugin();
		pl.getConfig().addDefault("herochat.channel","trade");
		pl.saveConfig();
		com.dthielke.herochat.ChannelManager cm = Herochat.getChannelManager();
		this.c = cm.getChannel(pl.getConfig().getString("herochat.channel"));
		return this;
	}

	@Override
	public void addListener(Player p) {
		Chatter chatter = Herochat.getChatterManager().getChatter(p);
        if (chatter == null) return ;
        
            chatter.addChannel(c, true, true);
    
	}

	@Override
	public void removeListener(Player p) {
		Chatter chatter = Herochat.getChatterManager().getChatter(p);
        if (chatter == null) return ;
        
            chatter.removeChannel(c, true, true);
    
		
	}

}
