package couk.rob4001.iauction;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import couk.rob4001.iauction.chat.ChatManager;

public class AuctionScope implements Listener {

	public static ArrayList<Auction> auctionQueue = new ArrayList<Auction>();
	private iAuction plugin;
	private Auction currentAuction;
	private long lastAuctionDestroyTime;
	public MemoryConfiguration config;
	public Location currentBidPlayerLocation;
	public GameMode currentBidPlayerGamemode;
	public Location currentAuctionOwnerLocation;
	public GameMode currentAuctionOwnerGamemode;

	public AuctionScope(iAuction p, MemoryConfiguration c) {
		this.plugin = p;
		this.config = c;

		plugin.getServer().getScheduler()
				.scheduleSyncRepeatingTask(plugin, new Runnable() {
					public void run() {
						checkAuctionQueue();
					}
				}, 20L, 20L);
	}

	public Auction getCurrentAuction() {
		return currentAuction;
	}

	public ArrayList<Auction> getQueue() {
		return auctionQueue;
	}

	public String getName() {
		// TODO: Check this works
		return config.getName();
	}

	public void detachAuction(Auction auction) {
		currentAuction = null;
		lastAuctionDestroyTime = System.currentTimeMillis();
		checkAuctionQueue();
	}

	public void queueAuction(Auction auctionToQueue, Player player,
			Auction currentAuction) {
		String playerName = player.getName();
plugin.getLogger().info(getName() + " New auction Queued");
		if (currentAuction == null) {
			// Queuing because of interval not yet timed out.
			// Allow a queue of 1 to override if 0 for this condition.
			if (Math.max(config.getInt("max-auction-queue-length"), 1) <= auctionQueue
					.size()) {
				Messaging.sendMessage("auction-queue-fail-full", player,
						currentAuction);
				return;
			}
		} else {
			if (config.getInt("max-auction-queue-length") <= 0) {
				Messaging.sendMessage("auction-fail-auction-exists", player,
						currentAuction);
				return;
			}
			if (currentAuction.getOwner().equalsIgnoreCase(playerName)) {
				Messaging.sendMessage("auction-queue-fail-current-auction",
						player, currentAuction);
				return;
			}
			if (config.getInt("max-auction-queue-length") <= auctionQueue
					.size()) {
				Messaging.sendMessage("auction-queue-fail-full", player,
						currentAuction);
				return;
			}
		}
		for (int i = 0; i < auctionQueue.size(); i++) {
			if (auctionQueue.get(i) != null) {
				Auction queuedAuction = auctionQueue.get(i);
				if (queuedAuction.getOwner().equalsIgnoreCase(playerName)) {
					Messaging.sendMessage("auction-queue-fail-in-queue",
							player, currentAuction);
					return;
				}
			}
		}
		if (auctionToQueue.isValid()) {
			auctionQueue.add(auctionToQueue);
			checkAuctionQueue();
			if (auctionQueue.contains(auctionToQueue)) {
				Messaging.sendMessage("auction-queue-enter", player,
						currentAuction);
			}
		}
	}

	private void checkAuctionQueue() {
		if (currentAuction != null) {
			return;
		}
		if (System.currentTimeMillis() - lastAuctionDestroyTime < config
				.getInt("min-auction-interval-secs") * 1000) {
			return;
		}
		if (auctionQueue.size() == 0) {
			return;
		}
		Auction auction = auctionQueue.remove(0);
		if (auction == null) {
			return;
		}
		if (!auction.isValid()) {
			return;
		}
		if (auction.start()) {
			currentAuction = auction;
		}
	}

	// Event Handlers
	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		iAuction.killOrphan(event.getPlayer());
		ChatManager.addListener(event.getPlayer().getName());
		plugin.getLogger().info(event.getPlayer().getName() + " Has Joined Adding him as listener!");
	}

	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		if (plugin.getConfig().getBoolean("allow-world-change")
				|| currentAuction == null)
			return;

		// Get player objects
		final Player player = event.getPlayer();
		if (currentAuction.getOwner().equalsIgnoreCase(player.getName())
				&& !player.getLocation().getWorld()
						.equals(currentAuctionOwnerLocation.getWorld())) {
			// This is running as a timer because MultiInv is using HIGHEST
			// priority and
			// there's no way to send a cancel to it, so we have to go after the
			// fact and
			// remove the user.
			plugin.getServer().getScheduler()
					.scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							player.teleport(currentAuctionOwnerLocation,
									TeleportCause.PLUGIN);
						}
					}, 1L);
			Messaging.sendMessage("worldchange-fail-auction-owner", player,
					currentAuction);
		} else if (currentAuction.getCurrentBid() != null
				&& currentAuction.getCurrentBid().getBidder()
						.equalsIgnoreCase(player.getName())
				&& !player.getLocation().getWorld()
						.equals(currentBidPlayerLocation.getWorld())) {
			// This is running as a timer because MultiInv is using HIGHEST
			// priority and
			// there's no way to send a cancel to it, so we have to go after the
			// fact and
			// remove the user.
			plugin.getServer().getScheduler()
					.scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							player.teleport(currentBidPlayerLocation,
									TeleportCause.PLUGIN);
						}
					}, 1L);
			Messaging.sendMessage("worldchange-fail-auction-bidder", player,
					currentAuction);
		}
	}

	@EventHandler
	public void onPlayerChangedWorld(PlayerGameModeChangeEvent event) {
		if (plugin.getConfig().getBoolean("allow-gamemode-change")
				|| currentAuction == null)
			return;

		// Get player objects
		Player player = event.getPlayer();

		if (currentAuction.getOwner().equalsIgnoreCase(player.getName())) {
			event.setCancelled(true);
			Messaging.sendMessage("gamemodechange-fail-auction-owner", player,
					currentAuction);
		} else if (currentAuction.getCurrentBid() != null
				&& currentAuction.getCurrentBid().getBidder()
						.equalsIgnoreCase(player.getName())) {
			event.setCancelled(true);
			Messaging.sendMessage("gamemodechange-fail-auction-bidder", player,
					currentAuction);
		}
	}
}
