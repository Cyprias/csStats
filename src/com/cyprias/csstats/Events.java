package com.cyprias.csstats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.Acrobot.ChestShop.Chests.ChestObject;
import com.Acrobot.ChestShop.Chests.MinecraftChest;
import com.Acrobot.ChestShop.Items.Items;
import com.Acrobot.ChestShop.Utils.uSign;

public class Events implements Listener {
	private csStats plugin;

	public Events(csStats plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerBlockBreakEvent(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		Block block = event.getBlock();
		if (!(block.getState() instanceof Sign))
			return; // exiting due to the block not being a sign
		
		Block blockbelow = block.getRelative(0, -1, 0);
		if (!(blockbelow.getState() instanceof Chest))
			return; // exiting due to the block below the sign not being a chest
		
		Sign sign = (Sign) block.getState(); // exiting due to the sign being
		if (!uSign.isValid(sign))
			return;
		
		int X, Y, Z;
		X= block.getX();
		Y= block.getY();
		Z= block.getZ();

	
		//plugin.info("BlockBreakEvent: " + X + " " + Y + " " + Z);
		String world = block.getWorld().getName();
		String SQL = "DELETE FROM `%s` WHERE `x` = '%s' AND `y` = '%s' AND `z` = '%s' AND `world` = '%s';";
		
		Config.mysqlInfo mysqlInfo = plugin.config.getMysqlInfo();
		
		SQL = String.format(SQL, plugin.database.shopTable, X, Y, Z, world);
		

		//plugin.info("BlockBreakEvent SQL: " + SQL);
		
		try {
			Connection con = DriverManager.getConnection(mysqlInfo.URL, mysqlInfo.username, mysqlInfo.password);
			PreparedStatement statement = con.prepareStatement(SQL);
			statement.executeUpdate();
			statement.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onSignChangeEvent(SignChangeEvent event){
		if (event.isCancelled()) {
			return;
		}
		
		Block block = event.getBlock();
	    Player player = event.getPlayer();

		if (!(block.getState() instanceof Sign))
			return; // exiting due to the block not being a sign

		Block blockbelow = block.getRelative(0, -1, 0);
		if (!(blockbelow.getState() instanceof Chest))
			return; // exiting due to the block below the sign not being a chest

		if (plugin.hasPermission(player, "mcego.shop.register"))
			plugin.sendMessage(player, "§7Rightclick the shop sign to register it with ChestShopStats.");
		
	}
	
	public void checkForSign(Block block, Player player){
		
		
		if (!(block.getState() instanceof Sign))
			return; // exiting due to the block not being a sign
		Block blockbelow = block.getRelative(0, -1, 0);
		if (!(blockbelow.getState() instanceof Chest))
			return; // exiting due to the block below the sign not being a chest

		
		Sign sign = (Sign) block.getState(); // exiting due to the sign being
												// improperly formatted

		if (!uSign.isValid(sign))
			return;
		
		
		//Chest chest = (Chest) blockbelow.getState();

		String owner, amount, signMat = block.getWorld().getName();
		Location loc = block.getLocation();

		// try catch around following three lines removed because there should
		// be no problem getting data from the sign since it will be valid
		// (handler exits if it's not - see above)
		signMat = sign.getLine(3);
		owner = sign.getLine(0);
		amount = sign.getLine(1);


		ItemStack stack = Items.getItemStack(signMat);
		//short damageVal = stack.getDurability();
		//ChestObject chObj = new MinecraftChest(chest);

		foundShopSign(loc, owner, Integer.parseInt(amount), stack, player);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		Block block = event.getClickedBlock();

		if (action != Action.RIGHT_CLICK_BLOCK)
			return; // exiting handler due to it not being the right kind of
					// block interact


		checkForSign(block, event.getPlayer());
	}

	public int isShopInDB(String world, int X, int Y, int Z) {
		int id = 0;
		
		Config.mysqlInfo mysqlInfo = plugin.config.getMysqlInfo();

		String SQL = "select * from " + plugin.database.shopTable;
		SQL += " WHERE X = ?";
		SQL += " AND Y = ?";
		SQL += " AND Z = ?";
		SQL += " AND world = ?";
		SQL += " LIMIT 10";

		try {
			Connection con = DriverManager.getConnection(mysqlInfo.URL, mysqlInfo.username, mysqlInfo.password);
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setInt(1, X);
			statement.setInt(2, Y);
			statement.setInt(3, Z);
			statement.setString(4, world);
			
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				id = result.getInt(1);
				break;
			}
			result.close();
			statement.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return id;
	}

	
	
	public void foundShopSign(Location loc, String owner, int amount, ItemStack stack, Player player) {
		/*
		plugin.log.info("foundShopSign getWorld:" + loc.getWorld().getName());
		plugin.log.info("foundShopSign getBlockX:" + loc.getBlockX());
		plugin.log.info("foundShopSign getBlockY:" + loc.getBlockY());
		plugin.log.info("foundShopSign getBlockZ:" + loc.getBlockZ());
		plugin.log.info("foundShopSign owner:" + owner);
		plugin.log.info("foundShopSign amount:" + amount);
		plugin.log.info("foundShopSign getTypeId:" + stack.getTypeId());
		plugin.log.info("foundShopSign getDurability:" + stack.getDurability());
		plugin.log.info("foundShopSign getEnchantments:" + stack.getEnchantments().size());
		plugin.log.info("foundShopSign getUnixTime:" + plugin.getUnixTime());
*/
		
		Config.mysqlInfo mysqlInfo = plugin.config.getMysqlInfo();

		String SQL = "INSERT INTO `%s` (`id`, `owner`, `TypeId`, `Durability`, `amount`, `world`, `X`, `Y`, `Z`, `enchantments`, `unixtime`) VALUES "
			+ "(NULL, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');";



		

		

		int isInDB = isShopInDB(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		
		//plugin.log.info("isShopInDB: " + isInDB);
		
		if (isInDB > 0){
			SQL = "UPDATE `%s` SET " + 
			"`owner` = '%s', "+
			"`TypeId` = '%s', "+
			"`Durability` = '%s', "+
			"`amount` = '%s', "+
			"`world` = '%s', "+
			"`X` = '%s', "+
			"`Y` = '%s', "+
			"`Z` = '%s', "+
			"`enchantments` = '%s', "+
			"`unixtime` = '%s' "+
			"WHERE `id` ="+isInDB+";";
			
			
			
		}
		
		
		
		SQL = String.format(SQL, plugin.database.shopTable, owner, stack.getTypeId(), stack.getDurability(), amount, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(),
			loc.getBlockZ(), stack.getEnchantments().size(), plugin.getUnixTime());
		
		//plugin.log.info("foundShopSign SQL:" + SQL);
		
		
		
		
		try {
			Connection con = DriverManager.getConnection(mysqlInfo.URL, mysqlInfo.username, mysqlInfo.password);
			PreparedStatement statement = con.prepareStatement(SQL);
			statement.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if (isInDB == 0){
			if (plugin.hasPermission(player, "mcego.shop.register")) //don't want everyone seeing the msg while we're testing.
				plugin.sendMessage(player, "§7Shop registered.");
		}

		
		
		
		// plugin.getUnixTime()

		// plugin.log.info("foundShopSign owner:" + foundShopSign)

	}

}
