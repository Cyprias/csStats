package com.cyprias.csstats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config extends JavaPlugin {
	private csStats plugin;

	private static Configuration config;

	File mysqlFile = null;
	FileConfiguration mysql = new YamlConfiguration();

	
	
	public static double convenienceTax;
	public static boolean logTransactions;
	
	public Config(csStats plugin) {
		this.plugin = plugin;

		config = plugin.getConfig().getRoot();
		config.options().copyDefaults(true);
		// config.set("version", plugin.version);
		plugin.saveConfig();
		
		loadConfigOpts();
		
	}

	private void loadConfigOpts(){
		convenienceTax = config.getDouble("convenienceTax");
		logTransactions = config.getBoolean("logTransactions");
	}
	
	@SuppressWarnings("unused")
	private void debug(String msg) {
		csStats.log.info(csStats.chatPrefix + msg);
	}

	// http://forums.bukkit.org/threads/bukkits-yaml-configuration-tutorial.42770/
	
	public void reloadOurConfig(){
		plugin.reloadConfig();
		config = plugin.getConfig().getRoot();
		loadConfigOpts();
		
		reloadMysql();
	}
	
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadColumns() {
		if (mysqlFile == null) {
			reloadMysql();
		}

		if (!mysql.isSet("columns.id"))
			mysql.set("columns.id", Database.col_id);
		if (!mysql.isSet("columns.buy"))
			mysql.set("columns.buy", Database.col_buy);
		if (!mysql.isSet("columns.shop_owner"))
			mysql.set("columns.shop_owner", Database.col_shop_owner);
		if (!mysql.isSet("columns.shop_user"))
			mysql.set("columns.shop_user", Database.col_shop_user);
		if (!mysql.isSet("columns.item_id"))
			mysql.set("columns.item_id", Database.col_item_id);
		if (!mysql.isSet("columns.item_durability"))
			mysql.set("columns.item_durability", Database.col_item_durability);
		if (!mysql.isSet("columns.amount"))
			mysql.set("columns.amount", Database.col_amount);
		if (!mysql.isSet("columns.price"))
			mysql.set("columns.price", Database.col_price);
		if (!mysql.isSet("columns.sec"))
			mysql.set("columns.sec", Database.col_sec);
		
		
		
		Database.col_id = mysql.getInt("columns.id");
		Database.col_buy = mysql.getInt("columns.buy");
		Database.col_shop_owner = mysql.getInt("columns.shop_owner");
		Database.col_shop_user = mysql.getInt("columns.shop_user");
		Database.col_item_id = mysql.getInt("columns.item_id");
		Database.col_item_durability = mysql.getInt("columns.item_durability");
		Database.col_amount = mysql.getInt("columns.amount");
		Database.col_price = mysql.getInt("columns.price");
		Database.col_sec = mysql.getInt("columns.sec");

		try {
			mysql.save(mysqlFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static class mysqlInfo {
		String table;
		int port;
		String username;
		String hostname;
		String password;
		String database;
		String URL;
		String warpTable = "warpTable";
	}

	private void reloadMysql() {
		try {
			mysqlFile = new File(plugin.getDataFolder(), "mysql.yml");

			// Check if file exists in plugin dir, or create it.
			if (!mysqlFile.exists()) {
				mysqlFile.getParentFile().mkdirs();
				copy(plugin.getResource("mysql.yml"), mysqlFile);
			}

			
			
			mysql.load(mysqlFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public mysqlInfo getMysqlInfo() {
		if (mysqlFile == null) {
			reloadMysql();
		}

		mysqlInfo myReturner = new mysqlInfo();
		myReturner.username = mysql.getString("mysql.username");
		myReturner.password = mysql.getString("mysql.password");
		myReturner.URL = "jdbc:mysql://" + mysql.getString("mysql.hostname") + ":" + mysql.getInt("mysql.port") + "/" + mysql.getString("mysql.database");
		myReturner.database = mysql.getString("mysql.database");
		myReturner.table = mysql.getString("mysql.table");
		
		if (mysql.isSet("mysql.warpTable"))	
			myReturner.warpTable = mysql.getString("mysql.warpTable");
		
		
		
		return myReturner;

		/*
		 * try { mysql.save(mysqlFile); } catch (IOException e) {
		 * e.printStackTrace(); }
		 */
	}

}
