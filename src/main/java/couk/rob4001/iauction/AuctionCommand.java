package couk.rob4001.iauction;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import couk.rob4001.iauction.chat.ChatManager;

public class AuctionCommand implements CommandExecutor {

	private final iAuction plugin;

	public AuctionCommand(iAuction pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
		LinkedList<String> args = new LinkedList<String>(Arrays.asList(a));

		if (!(sender instanceof Player)) {
			sender.sendMessage(Messaging.get("error.console"));
			return true;
		}
		Player player = (Player) sender;
		if (args.size() == 0) {
			help(player);
			return true;
		}
		String main = args.getFirst();
		int id = 1;

		if (main.equalsIgnoreCase("list") || main.equalsIgnoreCase("l")) {
			list(player);
			return true;
		}
		if (main.equalsIgnoreCase("help") || main.equalsIgnoreCase("h")) {
			help(player);
			return true;
		}
		if (main.equalsIgnoreCase("listen")) {
			ChatManager.addListener(player);
			player.sendMessage(Messaging.get("listen.on"));
			return true;
		}
		if (main.equalsIgnoreCase("mute")) {
			ChatManager.removeListener(player);
			player.sendMessage(Messaging.get("listen.off"));
			return true;
		}
		if ((main.equalsIgnoreCase("start")) || (main.equalsIgnoreCase("s"))) {
			if (player.hasPermission("auction.start")){
			auctionStart(player, args);
			}else{
				player.sendMessage(Messaging.get("error.perm"));
			}
			return true;
		}

		if (isInt(main)) {
			id = Integer.parseInt(main);
			args.removeFirst();
			main = args.getFirst();
		}
		Auction auc = this.plugin.getAuction(id);
		if (this.plugin.getAuction(id) != null) {

			if ((main.equalsIgnoreCase("end")) || (main.equalsIgnoreCase("e"))) {
				auc.end(player, args);
				return true;
			}
			if ((main.equalsIgnoreCase("cancel"))
					|| (main.equalsIgnoreCase("c"))) {
				auc.cancel(player, args);
				return true;
			}
			if ((main.equalsIgnoreCase("info")) || (main.equalsIgnoreCase("i"))) {
				auc.info(player, args);
				return true;
			}
			if ((main.equalsIgnoreCase("bid")) || (main.equalsIgnoreCase("b"))) {
				if (player.hasPermission("auction.bid")){
				auc.bid(player, args);
				}else{
					player.sendMessage(Messaging.get("error.perm"));
				}
				return true;
			}
		} else {
			player.sendMessage(Messaging.get("error.auctionid"));
			return true;
		}
		return true;

	}

	private void list(Player player) {
		
		List<Auction> auctions= plugin.getAuctions();
		if (auctions.isEmpty()){
			player.sendMessage(Messaging.get("list.noauctions"));
			return;
		}
		player.sendMessage(Messaging.get("list.title"));
		for (Auction a : auctions){
			ItemInfo ii = Items.itemByStack(a.getLot());
			player.sendMessage(Messaging.get("list.entry",a.getID().toString(), String.valueOf(ii
					.getName()), String.valueOf(ii.getId()), String.valueOf(ii
							.getSubTypeId()),String.valueOf(a.getLot()
					.getAmount()),String.valueOf(a.getBid())));
		}

		
	}

	private void help(Player player) {
		Messaging.playerMessage(player, "help.title");
		Messaging.playerMessage(player, "help.help");
		Messaging.playerMessage(player, "help.start1");
		Messaging.playerMessage(player, "help.start2");
		Messaging.playerMessage(player, "help.bid");
		Messaging.playerMessage(player, "help.end");
		Messaging.playerMessage(player, "help.cancel");
		Messaging.playerMessage(player, "help.info");
		Messaging.playerMessage(player, "help.list");
		Messaging.playerMessage(player, "help.listen");
	}

	private void auctionStart(Player player, LinkedList<String> args) {

		boolean hand = true;
		String iq = null;
		ItemStack is;
		int amount = -1;
		int time = plugin.getConfig().getInt("start.defaulttime");
		double price = plugin.getConfig().getDouble("start.defaultprice");

		for (int i = 0; i < args.size(); i++) {
			String arg = args.get(i);
			if (arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("hand")) {
				hand = true;
			}
			if (arg.equalsIgnoreCase("-i") || arg.equalsIgnoreCase("item")) {
				hand = false;
				iq = args.get(i + 1);
				i += 1;
			}
			if (arg.equalsIgnoreCase("-a") || arg.equalsIgnoreCase("amount")) {
				amount = Integer.parseInt(args.get(i + 1));
				i += 1;
			}
			if (arg.equalsIgnoreCase("-t") || arg.equalsIgnoreCase("time")) {
				time = Integer.parseInt(args.get(i + 1));
				i += 1;
			}
			if (arg.equalsIgnoreCase("-p") || arg.equalsIgnoreCase("price")) {
				price = Double.parseDouble(args.get(i + 1));
				i += 1;
			}
		}

		if (hand) {
			if(player.getItemInHand().getType() == Material.AIR){
				player.sendMessage(Messaging.get("start.noiteminhand"));
				return;
			}
			is = player.getItemInHand().clone();
		} else {
			is = Items.itemByString(iq).toStack();
		}

		if (amount != -1) {
			is.setAmount(amount);
		}
		if (!plugin.getConfig().getBoolean("eco.useDecimal")) {
			price = Math.round(price);
		}
		
		if (is == null){
			player.sendMessage(Messaging.get("start.noitemstack"));
			return;
		}
		
		if(!ItemManager.has(player, is)){
			player.sendMessage(Messaging.get("start.noitems"));
			return;
		}

		Auction auc = new Auction(plugin,is, price, player, time);
		if (!this.plugin.registerAuction(auc)) {
			player.sendMessage(Messaging.get("start.overmax"));
			return;
		}
		auc.start();
		

		

	}

	private boolean isInt(String main) {
		try {
			Integer.parseInt(main);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

}
