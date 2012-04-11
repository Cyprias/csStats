package com.cyprias.csstats;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class Seller  {
	private csStats plugin;
	static Logger log = Logger.getLogger("Minecraft");

	public Seller(csStats plugin) {
		this.plugin = plugin;
	}

	public class sellerInfo {
		String shop_owner;
		double pricePerUnit = 0;
		long age = 0;
		public sellerInfo(String shop_owner2, double d, long lastAge) {
			shop_owner = shop_owner2;
			pricePerUnit = d;
			age = lastAge;
		}
	}
	
	public ArrayList<sellerInfo> getRecentSellers(CommandSender sender, int itemID, int dur){
		Player player;
		if (sender instanceof Player) {
			player = (Player) sender;
		}else{
			return null;
		}
		
		//int itemID = player.getItemInHand().getTypeId();
		//int dur = player.getItemInHand().getDurability();
		
		Config.mysqlInfo mysqlInfo  = plugin.config.getMysqlInfo();//.config.getMysqlInfo();
		String SQL = "select * from " + mysqlInfo.table;
		SQL = SQL+" WHERE buy = 1";
		SQL = SQL+" AND item_id = ?";
		SQL = SQL+" AND item_durability = ?";
		SQL = SQL+" AND shop_owner != 'Admin Shop'";
		SQL = SQL+" ORDER BY sec DESC";
		SQL = SQL+" LIMIT 1000";
		
		Connection con;
		PreparedStatement statement;
		
		ArrayList<sellerInfo> sellers = new ArrayList(); // = null;
		
		
		
		try {
			con = DriverManager.getConnection(mysqlInfo.URL, mysqlInfo.username, mysqlInfo.password);
			statement = con.prepareStatement(SQL);
			statement.setInt(1, itemID);
			statement.setInt(2, dur);
			ResultSet result = statement.executeQuery();
			
			
			String shop_owner;
			int amount;
			double price;
			while (result.next()) {
				
				if (sellers.size() <= 10){
					
					shop_owner = result.getString(Database.col_shop_owner);
					amount = result.getInt(Database.col_amount);
					price = result.getLong(Database.col_price);
					long sec = result.getLong(Database.col_sec);
					long age = plugin.getUnixTime() - sec;
					
					if (sellers != null && sellers.size() > 0){
						for (int i = 0; i < sellers.size(); i++) {  // i indexes each element successively.
							if (sellers.get(i).shop_owner.equalsIgnoreCase(shop_owner)){
								
								
								
								break;
							}else if (i == (sellers.size()-1)){
								sellers.add(sellers.size(), 
									new sellerInfo(shop_owner, price/amount, age)
								);
							}
						}
					}else{
						sellers.add(sellers.size(), 
							new sellerInfo(shop_owner, price/amount, age)
						);
					}

				}
					
			}
		} catch (SQLException e) {e.printStackTrace();}
		
		return sellers;
	}
	
	public ArrayList<sellerInfo> getRecentBuyers(CommandSender sender, int itemID, int dur){
		Player player;
		if (sender instanceof Player) {
			player = (Player) sender;
		}else{
			return null;
		}
		
		//int itemID = player.getItemInHand().getTypeId();
		//int dur = player.getItemInHand().getDurability();
		
		Config.mysqlInfo mysqlInfo  = plugin.config.getMysqlInfo();//.config.getMysqlInfo();
		String SQL = "select * from " + mysqlInfo.table;
		SQL = SQL+" WHERE buy = 0";
		SQL = SQL+" AND item_id = ?";
		SQL = SQL+" AND item_durability = ?";
		SQL = SQL+" AND shop_owner != 'Admin Shop'";
		SQL = SQL+" ORDER BY sec DESC";
		SQL = SQL+" LIMIT 1000";
		
		Connection con;
		PreparedStatement statement;
		
		ArrayList<sellerInfo> players = new ArrayList(); // = null;
		
		
		
		try {
			con = DriverManager.getConnection(mysqlInfo.URL, mysqlInfo.username, mysqlInfo.password);
			statement = con.prepareStatement(SQL);
			statement.setInt(1, itemID);
			statement.setInt(2, dur);
			ResultSet result = statement.executeQuery();
			
			
			String shop_owner;
			int amount;
			double price;
			while (result.next()) {
				
				if (players.size() <= 10){
					
					shop_owner = result.getString(Database.col_shop_owner);
					amount = result.getInt(Database.col_amount);
					price = result.getLong(Database.col_price);
					long sec = result.getLong(Database.col_sec);
					long age = plugin.getUnixTime() - sec;
					
					if (players != null && players.size() > 0){
						for (int i = 0; i < players.size(); i++) {  // i indexes each element successively.
							if (players.get(i).shop_owner.equalsIgnoreCase(shop_owner)){
								
								
								
								break;
							}else if (i == (players.size()-1)){
								players.add(players.size(), 
									new sellerInfo(shop_owner, price/amount, age)
								);
							}
						}
					}else{
						players.add(players.size(), 
							new sellerInfo(shop_owner, price/amount, age)
						);
					}

				}
					
			}
		} catch (SQLException e) {e.printStackTrace();}
		
		return players;
	}
}
