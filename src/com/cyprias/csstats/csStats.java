package com.cyprias.csstats;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.cyprias.csstats.Database.adminShopStats;
import com.cyprias.csstats.ItemDb.itemData;

public class csStats extends JavaPlugin {
	public static File folder = new File("plugins/csStats");
	public static String chatPrefix = "§f[§acss§f] ";
	public String name;
	public String version;
	public Config config;
	public Seller seller;
	public ItemDb iDB;
	public Warps warps;
	public Database database;
	public static Server server;

	public static Logger log = Logger.getLogger("Minecraft");

	private String stPluginEnabled = chatPrefix + "§f%s §7v§f%s §7is enabled.";

	public void onEnable() {
		server = getServer();
		name = this.getDescription().getName();
		version = this.getDescription().getVersion();

		// Load config and permissions
		config = new Config(this);
		database = new Database(this);
		iDB = new ItemDb(this);
		warps = new Warps(this);

		seller = new Seller(this);

		log.info(String.format(stPluginEnabled, name, version));
	}

	private String getItemStatsMsg(Database.itemStats stats, int stackCount) {
		int roundTo = 1;
		if (stats.total == 0)
			return "§7sales: §f" + stats.total;

		return "§7sales: §f" + stats.total
			+ "§7, items: §f"
			+ Database.Round(stats.totalAmount, 0)
			+
			// "§7, price: $§f" + Database.Round(stats.avgPrice * stackCount,
			// roundTo) + "/" + Database.Round(stats.median * stackCount,
			// roundTo) + "/" + Database.Round(stats.mode * stackCount, roundTo)
			"§7, avg: $§f" + Database.Round(stats.avgPrice * stackCount, roundTo) + "§7, median: $§f" + Database.Round(stats.median * stackCount, roundTo)
			+ "§7, mode: $§f" + Database.Round(stats.mode * stackCount, roundTo);
	}

	public static String encodeEnchantment(Map<Enchantment, Integer> map) {
		int integer = 0;
		for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
			integer = integer * 1000 + (entry.getKey().getId()) * 10 + entry.getValue();
		}
		return (integer != 0 ? Integer.toString(integer, 32) : null);
	}

	public List<statRequest> queuedRequests = new ArrayList<statRequest>();

	static class statRequest {
		CommandSender sender;
		int itemID;
		int dur;
		int stack;
	}

	public boolean hasPermission(CommandSender sender, String node) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player) sender;
		if (player.isOp()) {
			return true;
		}

		if (player.isPermissionSet(node)) {
			return player.hasPermission(node);
		}

		return player.hasPermission("csstats.*");
	}

	private boolean command_admin_buys(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		// database.adminShopBuys();
		int seconds = 0;

		ArrayList<Database.adminShopStats> all;
		if (args.length == 3) {
			seconds = Integer.parseInt(args[2]) * 60;
			all = database.adminShopBuys(sender, getUnixTime() - seconds);
		} else {
			all = database.adminShopBuys(sender, 0);
		}

		// sender.sendMessage(chatPrefix + "Lenth: " + all.size());

		for (adminShopStats k : all) { // d gets successively each value in ar.
			sender.sendMessage(chatPrefix
				+ String.format("§f%s §7trans: §f%s§7, amount: §f%s§7, value: §f$%s", iDB.getItemName(k.id, k.durability), k.transactions,
					Database.Round(k.totalAmount, 0), Database.Round(k.totalPrice, 0)));

		}

		return true;
	}

	public void command_seller(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length == 1) {
			sender.sendMessage(chatPrefix + "§7You need to include a player name.");
			return;
		}

		String targetName = args[1];

		ArrayList<Warps.warpInfo> sellerWarps = warps.getPlayerWarps(targetName);

		if (sellerWarps.size() == 0) {
			sender.sendMessage(chatPrefix + "§7That seller has no warps.");
			return;
		}

		sender.sendMessage(chatPrefix + String.format("§f%s §7has the following public warps...", targetName));

		for (int i = 0; i < sellerWarps.size(); i++) {
			sender.sendMessage("    "
				+ String.format("§f%s§7 in §f%s §7at §f%s %s %s", sellerWarps.get(i).name, sellerWarps.get(i).world, database.Round(sellerWarps.get(i).x, 0),
					database.Round(sellerWarps.get(i).y, 0), database.Round(sellerWarps.get(i).z, 0))

			);
		}

	}

	public ItemDb.itemData getItemDataFromInput(String id) {
		id = id.toLowerCase();

		int itemid = 0;
		String itemname = null;
		short metaData = 0;
		if (id.matches("^\\d+[:+',;.]\\d+$")) {
			itemid = Integer.parseInt(id.split("[:+',;.]")[0]);
			metaData = Short.parseShort(id.split("[:+',;.]")[1]);
		} else if (id.matches("^\\d+$")) {
			itemid = Integer.parseInt(id);
		} else if (id.matches("^[^:+',;.]+[:+',;.]\\d+$")) {
			itemname = id.split("[:+',;.]")[0].toLowerCase();
			metaData = Short.parseShort(id.split("[:+',;.]")[1]);
		} else {
			itemname = id.toLowerCase();
		}

		if (itemname == null && itemid > 0) {
			itemname = iDB.getItemName(itemid, metaData);
		}

		if (itemname != null) {
			ItemDb.itemData iD = iDB.getItemID(itemname);
			if (iD != null)
				return iD;

			// iD = new itemDB.itemData("item", string2, string3);

		}
		/*
		 * try { ItemDb.itemData iD = iDB.getItemID(input.toLowerCase()); if (iD
		 * != null) return iD; } catch (Exception e) {e.printStackTrace();}
		 * 
		 * try { String itemName; if (input.contains(":")) { String[] temp =
		 * input.split(":"); itemName =
		 * iDB.getItemName(Integer.parseInt(temp[0]),
		 * Integer.parseInt(temp[1])); return new ItemDb.itemData(itemName,
		 * temp[0], temp[1]); } else { itemName =
		 * iDB.getItemName(Integer.parseInt(input), 0); return new
		 * ItemDb.itemData(itemName, input, "0"); } } catch (Exception e)
		 * {e.printStackTrace();}
		 */
		return null;
	}

	public void command_sellers(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			sender.sendMessage("§7You need to be a player to use this command.");
			return;
		}

		int itemID = player.getItemInHand().getTypeId();
		int dur = player.getItemInHand().getDurability();
		String itemName = "NULL";

		/*
		 * if (args.length == 2) { ItemDb.itemData iD =
		 * iDB.getItemID(args[1].toLowerCase());
		 * 
		 * if (iD != null){ itemID = iD.itemID; dur = iD.itemDur; itemName =
		 * iD.itemName; }else{
		 * 
		 * if (args[1].contains(":")) { String[] temp = args[1].split(":");
		 * itemID = Integer.parseInt(temp[0]); dur = Integer.parseInt(temp[1]);
		 * } else { itemID = Integer.parseInt(args[1]); dur = 0; } itemName =
		 * iDB.getItemName(itemID, dur); } }
		 */

		if (args.length == 2) {
			ItemDb.itemData iD = getItemDataFromInput(args[1]);
			if (iD == null) {
				sender.sendMessage(chatPrefix + "Cannot find itemID for '" + args[1] + "', Try again.");
				return;
			}
			itemID = iD.itemID;
			dur = iD.itemDur;
			itemName = iD.itemName;
		}

		ArrayList<Seller.sellerInfo> recentSellers = seller.getRecentSellers(sender, itemID, dur);

		if (recentSellers.size() == 0) {
			sender.sendMessage(chatPrefix + String.format("§7No one has sold §f%s §7yet.", itemName));
			return;
		}

		sender.sendMessage(chatPrefix + String.format("§7The following users have sold §f%s§7.", itemName));

		for (int i = 0; i < recentSellers.size(); i++) {
			sender.sendMessage(chatPrefix
				+ String.format("§f%s§7: §f$%s §7per 64 §f%s §7ago.", recentSellers.get(i).shop_owner,
					Database.Round(recentSellers.get(i).pricePerUnit * 64, 2), secondsToString(recentSellers.get(i).age)));
		}
		// dPrices[i1] = prices.get(i1);

	}

	static class cmdRequest {
		CommandSender sender;
		Command cmd;
		String commandLabel;
		String[] args;
	}

	public ArrayList<cmdRequest> queuedCommands = new ArrayList<cmdRequest>();

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		cmdRequest newCmd = new cmdRequest();
		newCmd.sender = sender;
		newCmd.cmd = cmd;
		newCmd.commandLabel = commandLabel;
		newCmd.args = args;

		queuedCommands.add(newCmd);

		this.getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
			public void run() {

				for (int i = queuedCommands.size() - 1; i >= 0; i--) {
					// plugin.getServer().broadcastMessage(i + ": " +
					// usedCommands.get(i).playerName + " said " +
					// usedCommands.get(i).message);

					commandHandler(queuedCommands.get(i).sender, queuedCommands.get(i).cmd, queuedCommands.get(i).commandLabel, queuedCommands.get(i).args);

					queuedCommands.remove(i);
				}

			}
		}, 0L);
		return true;
	}

	/**/
	public boolean commandHandler(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = null;
		String senderName = "[SERVER]";
		if (sender instanceof Player) {
			player = (Player) sender;
			senderName = player.getDisplayName();
		}
		final String message = getFinalArg(args, 0);
		log.info(chatPrefix + senderName + " " + cmd.getName() + ": " + message.toString());

		if (cmd.getName().equalsIgnoreCase("css")) {
			if (args.length == 0) {

				// sender.sendMessage(chatPrefix + "/css test");
				sender.sendMessage(chatPrefix + "  §aChestShop Stats");
				sender.sendMessage(chatPrefix + "/css stats [itemID/Name] [stackSize]");
				sender.sendMessage(chatPrefix + "/css sellers [itemID] - Who sells the item in your hand.");
				sender.sendMessage(chatPrefix + "/css seller <player> - Public warps for that player.");

				if (hasPermission(sender, "css.admin"))
					sender.sendMessage(chatPrefix + "/css admin");

				return true;

			}
			if (args[0].equalsIgnoreCase("test") && hasPermission(sender, "csstats.test")) {
				int itemID = player.getItemInHand().getTypeId();
				int dur = player.getItemInHand().getDurability();
				int stack = 1;

				// sender.sendMessage("Enchants: " +
				// encodeEnchantment(player.getItemInHand().getEnchantments()));

				// public static String getEnchantment(ItemStack item){
				// / return encodeEnchantment(item.getEnchantments());
				// }

				if (args.length == 1) {
					stack = player.getItemInHand().getAmount();
				}

				if (args.length == 2) {
					if (args[1].contains(":")) {
						String[] temp = args[1].split(":");
						itemID = Integer.parseInt(temp[0]);
						dur = Integer.parseInt(temp[1]);
					} else {
						itemID = Integer.parseInt(args[1]);
						dur = 0;
					}
				}

				if (args.length == 3) {
					stack = Integer.parseInt(args[2]);
				}

				sender.sendMessage(chatPrefix + String.format("§7item: §f%s§7, stack: §f%s", iDB.getItemName(itemID, dur), stack));

				Database.itemStats stats = database.getItemStats(itemID, dur, getUnixTime() - (60 * 60 * 24));
				if (stats.total > 0)
					sender.sendMessage("1d " + getItemStatsMsg(stats, stack));

				stats = database.getItemStats(itemID, dur, getUnixTime() - (60 * 60 * 24 * 3));
				if (stats.total > 0)
					sender.sendMessage("3d " + getItemStatsMsg(stats, stack));

				stats = database.getItemStats(itemID, dur, getUnixTime() - (60 * 60 * 24 * 7));
				if (stats.total > 0)
					sender.sendMessage("1w " + getItemStatsMsg(stats, stack));

				stats = database.getItemStats(itemID, dur, getUnixTime() - (60 * 60 * 24 * 30));
				if (stats.total > 0)
					sender.sendMessage("1m " + getItemStatsMsg(stats, stack));

				stats = database.getItemStats(itemID, dur, 0);
				if (stats.total > 0)
					sender.sendMessage("all " + getItemStatsMsg(stats, stack));

				return true;
			} else if (args[0].equalsIgnoreCase("db") && hasPermission(sender, "csstats.test")) {
				database.connectToDB();
				return true;
			} else if (args[0].equalsIgnoreCase("price") && hasPermission(sender, "csstats.price")) {
				if (args.length == 1) {
					sender.sendMessage("§7You need to include a itemID. /css price 4");
					return true;
				}
				//
				// log.info("item: " + itemID,dur );

				database.tellItemPrice(sender, Integer.parseInt(args[1]));

				return true;
			} else if (args[0].equalsIgnoreCase("reload") && hasPermission(sender, "csstats.reload")) {
				config.reloadOurConfig();
				sender.sendMessage(chatPrefix + " plugin reloaded.");
				return true;
			} else if (args[0].equalsIgnoreCase("admin") && hasPermission(sender, "csstats.admin")) {
				if (args.length == 1) {
					sender.sendMessage("/css admin buys [lastMinutes] - Stats on admin shop buys.");
					return true;
				}

				if (args[1].equalsIgnoreCase("buys")) {
					command_admin_buys(sender, cmd, commandLabel, args);
					return true;
				}

			} else if (args[0].equalsIgnoreCase("sellers")) {
				command_sellers(sender, cmd, commandLabel, args);
				return true;
			} else if (args[0].equalsIgnoreCase("seller")) {
				command_seller(sender, cmd, commandLabel, args);
				return true;
			} else if (args[0].equalsIgnoreCase("stats")) {
				statRequest newReq = new statRequest();

				int itemID = player.getItemInHand().getTypeId();
				int dur = player.getItemInHand().getDurability();
				int stack =  player.getItemInHand().getAmount();


				if (args.length >= 2) {

					if (args[1].equalsIgnoreCase("help")) {
						sender.sendMessage(chatPrefix + "/css stats [stack] [itemID]");
						return true;
					}

					// ItemDb.itemData iD =
					// iDB.getItemID(args[1].toLowerCase());
					ItemDb.itemData iD = getItemDataFromInput(args[1]);

					if (iD != null) {
						itemID = iD.itemID;
						dur = iD.itemDur;
						stack = 1;
					}else{
						sender.sendMessage(chatPrefix + "Cannot find ID for '"+args[1]+"', try again.");
						return true;
					}

				}

				if (args.length >= 3) {
					/*
					 * ItemDb.itemData iD =
					 * iDB.getItemID(args[2].toLowerCase());
					 * 
					 * if (iD != null){ itemID = iD.itemID; dur = iD.itemDur;
					 * stack = 1; }else{
					 * 
					 * if (args[2].contains(":")) { String[] temp =
					 * args[1].split(":"); itemID = Integer.parseInt(temp[0]);
					 * dur = Integer.parseInt(temp[1]); } else { itemID =
					 * Integer.parseInt(args[2]); } }
					 */

					if (args[2].matches("^\\d+$")) {
						stack = Integer.parseInt(args[2]);
					}
					
					/*
					ItemDb.itemData iD = getItemDataFromInput(args[2]);
					if (iD == null) {
						sender.sendMessage(chatPrefix + "Cannot find itemID for '" + args[2] + "', Try again.");
						return true;
					}
					itemID = iD.itemID;
					dur = iD.itemDur;
*/
				}

				sender.sendMessage(chatPrefix + String.format("§7item: §f%s§7, stack: §f%s", iDB.getItemName(itemID, dur), stack));
				// }
				Database.itemStats stats;

				stats = database.getItemStats(itemID, dur, getUnixTime() - (60 * 60 * 24));
				// if (stats.total > 0)
				sender.sendMessage("1d " + getItemStatsMsg(stats, stack));

				stats = database.getItemStats(itemID, dur, getUnixTime() - (60 * 60 * 24 * 3));
				// if (stats.total > 0)
				sender.sendMessage("3d " + getItemStatsMsg(stats, stack));

				stats = database.getItemStats(itemID, dur, getUnixTime() - (60 * 60 * 24 * 7));
				// if (stats.total > 0)
				sender.sendMessage("1w " + getItemStatsMsg(stats, stack));

				stats = database.getItemStats(itemID, dur, getUnixTime() - (60 * 60 * 24 * 30));
				// if (stats.total > 0)
				sender.sendMessage("1m " + getItemStatsMsg(stats, stack));

				stats = database.getItemStats(itemID, dur, 0);
				// if (stats.total > 0)
				sender.sendMessage("all " + getItemStatsMsg(stats, stack));

				/*
				 * newReq.sender = sender; newReq.itemID = itemID; newReq.dur =
				 * dur; newReq.stack = stack;
				 * 
				 * queuedRequests.add(newReq);
				 * 
				 * this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
				 * new Runnable() { public void run() { Database.itemStats
				 * stats;
				 * 
				 * for (int i = queuedRequests.size() - 1; i >= 0; i--) { //
				 * plugin.getServer().broadcastMessage(i + ": " + //
				 * usedCommands.get(i).playerName + " said " + //
				 * usedCommands.get(i).message);
				 * 
				 * try { //
				 * plugin.database.commandUsed(queuedRequests.get(i).player, //
				 * queuedRequests.get(i).message);
				 * 
				 * // if //
				 * (!Integer.valueOf(queuedRequests.get(i).dur).equals(0)){ //
				 * queuedRequests.get(i).sender.sendMessage(String.format(
				 * "§7item: §f%s§7:§f%s§7, stack: §f%s", //
				 * queuedRequests.get(i).itemID, // queuedRequests.get(i).dur,
				 * // queuedRequests.get(i).stack)); // }else{
				 * queuedRequests.get
				 * (i).sender.sendMessage(String.format("item: %s, stack: %s",
				 * iDB.getItemName(queuedRequests.get(i).itemID,
				 * queuedRequests.get(i).dur), queuedRequests.get(i).stack)); //
				 * }
				 * 
				 * stats = database.getItemStats(queuedRequests.get(i).itemID,
				 * queuedRequests.get(i).dur, getUnixTime() - (60 * 60 * 24));
				 * // if (stats.total > 0)
				 * queuedRequests.get(i).sender.sendMessage("1d " +
				 * getItemStatsMsg(stats, queuedRequests.get(i).stack));
				 * 
				 * stats = database.getItemStats(queuedRequests.get(i).itemID,
				 * queuedRequests.get(i).dur, getUnixTime() - (60 * 60 * 24 *
				 * 3)); // if (stats.total > 0)
				 * queuedRequests.get(i).sender.sendMessage("3d " +
				 * getItemStatsMsg(stats, queuedRequests.get(i).stack));
				 * 
				 * stats = database.getItemStats(queuedRequests.get(i).itemID,
				 * queuedRequests.get(i).dur, getUnixTime() - (60 * 60 * 24 *
				 * 7)); // if (stats.total > 0)
				 * queuedRequests.get(i).sender.sendMessage("1w " +
				 * getItemStatsMsg(stats, queuedRequests.get(i).stack));
				 * 
				 * stats = database.getItemStats(queuedRequests.get(i).itemID,
				 * queuedRequests.get(i).dur, getUnixTime() - (60 * 60 * 24 *
				 * 30)); // if (stats.total > 0)
				 * queuedRequests.get(i).sender.sendMessage("1m " +
				 * getItemStatsMsg(stats, queuedRequests.get(i).stack));
				 * 
				 * stats = database.getItemStats(queuedRequests.get(i).itemID,
				 * queuedRequests.get(i).dur, 0); // if (stats.total > 0)
				 * queuedRequests.get(i).sender.sendMessage("all " +
				 * getItemStatsMsg(stats, queuedRequests.get(i).stack));
				 * 
				 * } catch (Exception e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); }
				 * 
				 * queuedRequests.remove(i); }
				 * 
				 * } }, 0L);
				 */
				return true;

			}
		}

		return false;

	}

	public static String getFinalArg(final String[] args, final int start) {
		final StringBuilder bldr = new StringBuilder();
		for (int i = start; i < args.length; i++) {
			if (i != start) {
				bldr.append(" ");
			}
			bldr.append(args[i]);
		}
		return bldr.toString();
	}

	public long getUnixTime() {
		return System.currentTimeMillis() / 1000L;
	}

	public String secondsToString(long totalSeconds) {

		long days = totalSeconds / 86400;
		long remainder = totalSeconds % 86400;

		long hours = remainder / 3600;
		remainder = totalSeconds % 3600;
		long minutes = remainder / 60;
		long seconds = remainder % 60;

		if (days > 0)
			return days + "d" + hours + "h" + minutes + "m" + seconds + "s";
		else if (hours > 0)
			return hours + "h" + minutes + "m" + seconds + "s";
		else if (minutes > 0)
			return minutes + "m" + seconds + "s";
		else
			return seconds + "s";
	}
}
