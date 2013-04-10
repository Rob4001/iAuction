package couk.rob4001.iAuction;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import couk.rob4001.iAuction.gui.BasicInfoInterface;
import couk.rob4001.iAuction.gui.BasicNewInterface;
import couk.rob4001.iAuction.gui.BasicNewLayout;
import couk.rob4001.util.InventoryUtil;
import couk.rob4001.util.chat.ChatManager;

public class AuctionCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] a) {
		LinkedList<String> args = new LinkedList<String>(Arrays.asList(a));
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players may auction");
			return true;
		}

		Player player = (Player) sender;
		if (args.size() == 0) {
			Messaging.playerMessage(player, "help");
			return true;
		}

		String main = args.removeFirst();

		if (main.equalsIgnoreCase("start") || main.equalsIgnoreCase("s")) {
			ChatManager.addListener(player);
			if(!player.hasPermission("auction.start")){
				Messaging.playerMessage(player, "error.perm");
				return true;
			}
			if (iAuction.getInstance().hasAuction(player)) {
				Messaging.playerMessage(player, "start.hasauction");
				return true;
			}
			if (iAuction.getInstance().lots
					.containsKey(player.getName())) {
				Messaging.playerMessage(player, "start.collection");
				return true;
			}
			if (args.size() != 2) {
				Messaging.playerMessage(player, "start.wrongargs");
				return true;
			}
			int price,time;
			try{
			price = Integer.parseInt(args.getFirst());
			args.removeFirst();
			time = Integer.parseInt(args.getFirst());
			}catch(NumberFormatException e){
				Messaging.playerMessage(player, "start.wrongargs");
				return true;
			}
			if(time < iAuction.getInstance().getConfig().getInt("start.timelow")){
				Messaging.playerMessage(player, "start.timelow");
				return true;
			}
			if(time > iAuction.getInstance().getConfig().getInt("start.timehigh")){
				Messaging.playerMessage(player, "start.timehigh");
				return true;
			}
			if(price < iAuction.getInstance().getConfig().getInt("start.pricelow")&&!(-1 == iAuction.getInstance().getConfig().getInt("start.pricelow"))){
				Messaging.playerMessage(player, "start.pricelow");
				return true;
			}
			if(price > iAuction.getInstance().getConfig().getInt("start.pricehigh")&&!(-1 == iAuction.getInstance().getConfig().getInt("start.pricehigh"))){
				Messaging.playerMessage(player, "start.pricehigh");
				return true;
			}
			
			((Player) sender).openInventory(new BasicNewInterface(
					new BasicNewLayout(iAuction.getInstance().getConfig().getInt("start.rows")), price, time).getInventory());
			return true;
		}

		if (main.equalsIgnoreCase("list") || main.equalsIgnoreCase("l")) {
			ChatManager.addListener(player);
			Iterator<Auction> it = iAuction.auctionQueue.iterator();
			Integer i = 0;
			Messaging.playerMessage(player, "auction.listtitle");
			while (it.hasNext()) {
				i++;
				Auction au = it.next();
				Messaging.playerMessage(player, "auction.info",i.toString(), au.getOwner().getDisplayName(),au.getTime(),au.getBid());
				}
			return true;
		}

		if (main.equalsIgnoreCase("help") || main.equalsIgnoreCase("h")) {
			Messaging.playerMessage(player, "help");
			return true;
		}
		if (main.equalsIgnoreCase("listen")) {
			if(!player.hasPermission("auction.listen")){
				Messaging.playerMessage(player, "error.perm");
				return true;
			}
			iAuction.getInstance().listeners.add(player.getName());
			ChatManager.addListener(player);
			Messaging.playerMessage(player, "listen.on");
			return true;
		}
		if (main.equalsIgnoreCase("mute")) {
			if(!player.hasPermission("auction.mute")){
				Messaging.playerMessage(player, "error.perm");
				return true;
			}
			if (iAuction.getInstance().listeners.contains(player
					.getName())) {
				iAuction.getInstance().listeners
						.remove(player.getName());
			}
			ChatManager.removeListener(player);
			Messaging.playerMessage(player, "listen.off");
			return true;
		}

		if (main.equalsIgnoreCase("collect")) {
			if (iAuction.getInstance().lots
					.containsKey(player.getName())) {
				

				InventoryUtil.setupCollect(player).open();
			} else {
				Messaging.playerMessage(player, "error.nocollect");
			}
			return true;
		}
		// TODO: Add Custom Command support ??
		Auction auc = iAuction.getCurrent();
		if (auc != null) {
			if (main.equalsIgnoreCase("bid")) {
				if(!player.hasPermission("auction.bid")){
					Messaging.playerMessage(player, "error.perm");
					return true;
				}
				if (iAuction.getInstance().lots.containsKey(player
						.getName())) {
					Messaging.playerMessage(player, "bidding.collection");
					return true;
				}

				if (args.size() > 1) {
					auc.bid(player, args);
				} else {
					auc.bid(player, args);
				}
				ChatManager.addListener(player);
				return true;
			}
			if (main.equalsIgnoreCase("info")) {
				((Player) sender).openInventory(new BasicInfoInterface(auc
						.getInventory()).getInventory());
				ChatManager.addListener(player);
				return true;
			}

			if ((main.equalsIgnoreCase("end")) || (main.equalsIgnoreCase("e"))) {
				ChatManager.addListener(player);
				auc.end(player, args);
				return true;
			}
			if ((main.equalsIgnoreCase("cancel"))
					|| (main.equalsIgnoreCase("c"))) {
				ChatManager.addListener(player);
				auc.cancel(player, args);
				return true;
			}

		} else {
			Messaging.playerMessage(player, "error.invalidcommand");
		}

		return true;
	}

}
