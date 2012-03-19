package com.cyprias.csstats;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.cyprias.csstats.Database.adminShopStats;

public class Warps {
	private csStats plugin;
	static Logger log = Logger.getLogger("Minecraft");
	
	private  File file;
	public Warps(csStats plugin) {
		this.plugin = plugin;
	}
	
	public static class warpInfo {
		int id;
		String name;
		String creator;
		String world;
		double x;
		double y;
		double z;
		boolean isPublic;
		public warpInfo(int long1, String string, String string2, String string3, long long2, long long3, long long4, boolean boolean1) {
			// TODO Auto-generated constructor stub
			id = long1;
			name = string;
			creator = string2;
			world = string3;
			x = long2;
			y = long3;
			z = long4;
			isPublic = boolean1;
		}
	}
	
	public ArrayList getPlayerWarps(String playerName){
		//log.info("getPlayerWarps name: " + playerName);
		
		ArrayList pWarps = new ArrayList(); // = null;
		
		Config.mysqlInfo mysqlInfo  = plugin.config.getMysqlInfo();
		String SQL = "select * from " + mysqlInfo.warpTable +
		" WHERE creator = ?" + 
		" AND publicAll = '1'";
		
		Connection con;
		PreparedStatement statement;
		try {
			con = DriverManager.getConnection(mysqlInfo.URL, mysqlInfo.username, mysqlInfo.password);
			statement = con.prepareStatement(SQL);
			statement.setString(1, playerName);
			ResultSet result = statement.executeQuery();
			
			
			
			while (result.next()) {
				
				//result.getLong(col_price)
				//result.getString(col_shop_owner)
				log.info("getPlayerWarps: " + result.getString(1));
				
				pWarps.add(pWarps.size(), new warpInfo(
					result.getInt(1), //id
					result.getString(2), //name
					result.getString(3), //creator
					result.getString(4), //world
					result.getLong(5), //x
					result.getLong(6), //y
					result.getLong(7),//z
					result.getBoolean(10) //public
				));
				
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return pWarps;
	}
	
	
}
