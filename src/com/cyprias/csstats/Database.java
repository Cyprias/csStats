package com.cyprias.csstats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import com.cyprias.csstats.Database.adminShopStats;
import com.cyprias.csstats.Database.itemStats;

public class Database {
	static int col_id = 1;
	static int col_buy = 2;
	static int col_shop_owner = 3;
	static int col_shop_user = 4;
	static int col_item_id = 5;
	static int col_item_durability = 6;
	static int col_amount = 7;
	static int col_price = 8;
	static int col_sec = 9;

	public String warpTable = "warpTable";
	static String shopTable = "css_shops";
	
	
	static Logger log = Logger.getLogger("Minecraft");

	private csStats plugin;

	public Database(csStats plugin) {
		this.plugin = plugin;

		// In case the mysql columns ever change, I keep them in the config
		// file.
		plugin.config.loadColumns();

		setupShopsTable();
	}

	/**/
	public static double mean(double[] p) {
		double sum = 0; // sum of all the elements
		for (int i = 0; i < p.length; i++) {
			sum += p[i];
		}
		return sum / p.length;
	}// end method mean

	public static double median(double[] m) {
		int middle = m.length / 2;
		if (m.length % 2 == 1) {
			return m[middle];
		} else {
			return (m[middle - 1] + m[middle]) / 2.0;
		}
	}

	public static double mode(double[] prices) {
		double maxValue = 0, maxCount = 0;

		for (int i = 0; i < prices.length; ++i) {
			int count = 0;
			for (int j = 0; j < prices.length; ++j) {
				if (prices[j] == prices[i])
					++count;
			}
			if (count > maxCount) {
				maxCount = count;
				maxValue = prices[i];
			}
		}

		return maxValue;
	}

	public static double Round(double Rval, int Rpl) {
		double p = (double) Math.pow(10, Rpl);
		Rval = Rval * p;
		double tmp = Math.round(Rval);
		return (double) tmp / p;
	}

	public static class adminShopStats {
		int id = 0;
		int durability = 0;
		int transactions = 0;
		double totalPrice = 0;
		double totalAmount = 0;

		public adminShopStats(int id2, int Idurability, int i, double price, int amount) {
			id = id2;
			durability = Idurability;
			transactions = i;
			totalPrice = price;
			totalAmount = amount;
		}
	}

	public static class itemStats {
		int total;
		double totalPrice;
		double totalAmount;
		double avgPrice;
		double mean;
		double median;
		double mode;
		double amean;
		double amedian;
		double amode;
	}

	public ArrayList adminShopTrans(CommandSender sender, long maxAge, boolean buys) {
		Config.mysqlInfo mysqlInfo = plugin.config.getMysqlInfo();
		String SQL = "select * from " + mysqlInfo.table + " where shop_owner = 'Admin Shop'";

		if (buys == true) {
			SQL += " and buy = '0'";
		} else {
			SQL += " and buy = '1'";
		}

		if (maxAge != 0)
			SQL += " AND sec >= ?";

		SQL = SQL + " ORDER BY sec DESC";
		SQL = SQL + " LIMIT 10000";

		// HashMap<Integer, adminShopStats> items = new HashMap<Integer,
		// adminShopStats>(); //= new HashMap<Integer, itemStats>();

		ArrayList<adminShopStats> items2 = new ArrayList(); // = null;

		try {
			Connection con = DriverManager.getConnection(mysqlInfo.URL, mysqlInfo.username, mysqlInfo.password);
			PreparedStatement statement = con.prepareStatement(SQL);

			if (maxAge != 0)
				statement.setInt(1, (int) maxAge);

			// statement.setInt(1, itemID);

			ResultSet result = statement.executeQuery();

			while (result.next()) {
				// int id = result.getInt(col_id);
				// boolean buy = result.getBoolean(col_buy);
				// String shop_owner = result.getString(col_shop_owner);
				// String shop_user = result.getString(col_shop_user);
				int item_id = result.getInt(col_item_id);
				int item_durability = result.getInt(col_item_durability);
				int amount = result.getInt(col_amount);
				double price = result.getLong(col_price);
				// long sec = result.getLong(col_sec);

				// sender.sendMessage(id + ", " + amount + ", " + price);

				if (items2 != null && items2.size() > 0) {
					for (int i = 0; i < items2.size(); i++) { // i indexes each
																// element
																// successively.
						if (items2.get(i).id == item_id) {
							items2.get(i).transactions = items2.get(i).transactions + 1;
							items2.get(i).totalAmount = items2.get(i).totalAmount + amount;
							items2.get(i).totalPrice = items2.get(i).totalPrice + price;
							break;

						} else if (i == (items2.size() - 1)) {
							// sender.sendMessage(String.format("Creating %S, i: %s, size: %s",
							// item_id, i, items2.size()));

							items2.add(items2.size(), new adminShopStats(item_id, item_durability, items2.size(), price, amount));

						}
					}

				} else {

					items2.add(0, new adminShopStats(item_id, item_durability, 1, price, amount));
				}
				// items.put(items.size(), item);
				// items.put(id, item);
			}

			// sender.sendMessage("r1: " + r1);

			result.close();
			statement.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return items2;
	}

	public double tellItemPrice(CommandSender sender, int itemID) {
		Config.mysqlInfo mysqlInfo = plugin.config.getMysqlInfo();

		/*
		 * String SQL = "select * from " + mysqlInfo.table; //SQL =
		 * SQL+" WHERE buy = 1"; SQL = SQL+" WHERE item_id ='" +
		 * String.valueOf(itemID).toString() + "'"; SQL =
		 * SQL+" AND shop_owner != 'Admin Shop'"; SQL =
		 * SQL+" AND shop_user != 'Admin Shop'";
		 * 
		 * SQL = SQL+" ORDER BY sec DESC"; SQL = SQL+" LIMIT 1000";
		 */

		String SQL = "select * from " + mysqlInfo.table;
		// SQL = SQL+" WHERE buy = 1";
		SQL = SQL + " WHERE item_id =?";
		SQL = SQL + " AND shop_owner != 'Admin Shop'";
		SQL = SQL + " AND shop_user != 'Admin Shop'";

		SQL = SQL + " ORDER BY sec DESC";
		SQL = SQL + " LIMIT 1000";

		double totalPrice = 0;
		double totalAmount = 0;

		int i = 0;

		// double[] prices = new double[]; // new double[20];
		List<Double> prices = new ArrayList();
		List<Integer> amounts = new ArrayList();

		try {
			Connection con = DriverManager.getConnection(mysqlInfo.URL, mysqlInfo.username, mysqlInfo.password);
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setInt(1, itemID);

			ResultSet result = statement.executeQuery();

			while (result.next()) {

				// result.getInt(1);

				int id = result.getInt(col_id);
				boolean buy = result.getBoolean(col_buy);
				String shop_owner = result.getString(col_shop_owner);
				String shop_user = result.getString(col_shop_user);
				int item_id = result.getInt(col_item_id);
				int item_durability = result.getInt(col_item_durability);
				int amount = result.getInt(col_amount);
				double price = result.getLong(col_price);
				long sec = result.getLong(col_sec);

				long age = plugin.getUnixTime() - sec;
				double aPrice = price / amount;

				/*
				 * log.info("id: " + id + ", buy: " + buy + ", shop_owner: " +
				 * shop_owner + ", shop_user: " + shop_user + ", item_id: " +
				 * item_id + // ", item_durability: " + item_durability +
				 * ", amount: " + amount + ", price: " + price + ", aPrice" +
				 * aPrice + //", sec: " + sec + ", age: " +
				 * plugin.secondsToString(age) );
				 */

				totalPrice = totalPrice + price;
				totalAmount = totalAmount + amount;

				// prices[i] = aPrice;

				prices.add(aPrice);
				amounts.add(amount);

				i = i + 1;

			}
			result.close();
			statement.close();
			con.close();
			sender.sendMessage("---------------------");

			sender.sendMessage("i: " + i);
			sender.sendMessage("totalPrice: " + totalPrice);
			sender.sendMessage("totalAmount: " + totalAmount);

			// for (double v : prices) {
			// log.info("V: " + v);
			// }

			double avgPrice = totalPrice / totalAmount;
			sender.sendMessage("avgPrice: " + Round(avgPrice, 2));

			if (prices.size() > 0) {
				double[] dPrices = new double[prices.size()];

				for (int i1 = 0; i1 < prices.size(); i1++)
					dPrices[i1] = prices.get(i1);

				sender.sendMessage("mean: " + Round(mean(dPrices), 2));
				sender.sendMessage("median: " + Round(median(dPrices), 2));
				sender.sendMessage("mode: " + Round(mode(dPrices), 2));
			}

			if (amounts.size() > 0) {
				double[] dAmounts = new double[amounts.size()];

				for (int i1 = 0; i1 < amounts.size(); i1++)
					dAmounts[i1] = amounts.get(i1);

				sender.sendMessage("Amount mean: " + Round(mean(dAmounts), 2));
				sender.sendMessage("Amount median: " + Round(median(dAmounts), 2));
				sender.sendMessage("Amount mode: " + Round(mode(dAmounts), 2));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return totalPrice / totalAmount;

	}

	public itemStats getItemStats(int itemID, int dur, long maxAge) {
		itemStats myReturn = new itemStats();
		Config.mysqlInfo mysqlInfo = plugin.config.getMysqlInfo();

		// log.info("maxAge: " + maxAge);

		String SQL = "select * from " + mysqlInfo.table;
		// SQL = SQL+" WHERE buy = 1";
		SQL = SQL + " WHERE item_id = ?";
		SQL = SQL + " AND item_durability = ?";
		SQL = SQL + " AND shop_owner != 'Admin Shop'";
		SQL = SQL + " AND shop_user != 'Admin Shop'";

		SQL = SQL + " AND shop_user != 'Admin Shop'";

		if (maxAge != 0)
			SQL = SQL + " AND sec >= ?";

		SQL = SQL + " ORDER BY sec DESC";
		SQL = SQL + " LIMIT 1000";

		double totalPrice = 0;
		double totalAmount = 0;

		int i = 0;
		List<Double> prices = new ArrayList<Double>();
		List<Integer> amounts = new ArrayList<Integer>();

		try {
			Connection con = DriverManager.getConnection(mysqlInfo.URL, mysqlInfo.username, mysqlInfo.password);
			PreparedStatement statement = con.prepareStatement(SQL);

			statement.setInt(1, itemID);
			statement.setInt(2, dur);

			if (maxAge != 0)
				statement.setInt(3, (int) maxAge);

			ResultSet result = statement.executeQuery();

			while (result.next()) {

				// result.getInt(1);

				// int id = result.getInt(col_id);
				// boolean buy = result.getBoolean(col_buy);
				// String shop_owner = result.getString(col_shop_owner);
				// String shop_user = result.getString(col_shop_user);
				// int item_id = result.getInt(col_item_id);
				// int item_durability = result.getInt(col_item_durability);
				int amount = result.getInt(col_amount);
				double price = result.getLong(col_price);
				// long sec = result.getLong(col_sec);

				// long rAge = plugin.getUnixTime() - sec;
				double aPrice = price / amount;

				/*
				 * log.info("id: " + id + ", buy: " + buy + ", shop_owner: " +
				 * shop_owner + ", shop_user: " + shop_user + ", item_id: " +
				 * item_id + // ", item_durability: " + item_durability +
				 * ", amount: " + amount + ", price: " + price + ", aPrice" +
				 * aPrice + //", sec: " + sec + ", age: " +
				 * plugin.secondsToString(age) );
				 */

				totalPrice += aPrice;
				totalAmount += amount;

				// prices[i] = aPrice;

				prices.add(aPrice);
				amounts.add(amount);

				i = i + 1;

			}
			result.close();
			statement.close();
			con.close();

			myReturn.total = i;

			// myReturn.totalPrice = totalPrice;

			myReturn.totalAmount = totalAmount;

			// for (double v : prices) {
			// log.info("V: " + v);
			// }

			double avgPrice = totalPrice / i;
			// sender.sendMessage("avgPrice: " + Round(avgPrice,2));

			myReturn.avgPrice = avgPrice;

			myReturn.mean = 0;
			myReturn.median = 0;
			myReturn.mode = 0;

			if (prices.size() > 0) {
				double[] dPrices = new double[prices.size()];

				for (int i1 = 0; i1 < prices.size(); i1++)
					dPrices[i1] = prices.get(i1);

				// myReturn.mean = mean(dPrices);
				myReturn.median = median(dPrices);
				myReturn.mode = mode(dPrices);
			}
			if (amounts.size() > 0) {
				double[] dAmounts = new double[amounts.size()];

				for (int i1 = 0; i1 < amounts.size(); i1++)
					dAmounts[i1] = amounts.get(i1);

				myReturn.amean = mean(dAmounts);
				myReturn.amedian = median(dAmounts);
				myReturn.amode = mode(dAmounts);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return myReturn;
	}

	public void connectToDB() {

		Config.mysqlInfo mysqlInfo = plugin.config.getMysqlInfo();

		String SQL = "select * from " + mysqlInfo.table.toString() + " ORDER BY sec DESC LIMIT 30";

		try {
			Connection con = DriverManager.getConnection(mysqlInfo.URL, mysqlInfo.username, mysqlInfo.password);
			PreparedStatement statement = con.prepareStatement(SQL);
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				// result.getInt(1);

				int id = result.getInt(col_id);
				boolean buy = result.getBoolean(col_buy);
				String shop_owner = result.getString(col_shop_owner);
				String shop_user = result.getString(col_shop_user);
				int item_id = result.getInt(col_item_id);
				int item_durability = result.getInt(col_item_durability);
				int amount = result.getInt(col_amount);
				long price = result.getLong(col_price);
				long sec = result.getLong(col_sec);

				long age = plugin.getUnixTime() - sec;

				log.info("id: " + id + ", buy: " + buy + ", shop_owner: " + shop_owner + ", shop_user: " + shop_user + ", item_id: " + item_id
					+ ", item_durability: " + item_durability + ", amount: " + amount + ", price: " + price + ", sec: " + sec + ", age: "
					+ plugin.secondsToString(age));

			}

			result.close();
			statement.close();
			con.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	

	public void setupShopsTable() {
		Config.mysqlInfo mysqlInfo = plugin.config.getMysqlInfo();

		try {
			Class.forName("com.mysql.jdbc.Driver");

			Connection con = DriverManager.getConnection(mysqlInfo.URL, mysqlInfo.username, mysqlInfo.password);
			PreparedStatement statement = con.prepareStatement("show tables like '%" + shopTable + "%'");

			ResultSet result = statement.executeQuery();

			result.last();
			if (result.getRow() == 0) {

				String SQL = resourceToString("Create-Table-mysql.sql");
				SQL = String.format(SQL, shopTable);

				try {
					statement = con.prepareStatement(SQL);
					int result2 = statement.executeUpdate();

					plugin.info("Created " + shopTable + " table. " + result2);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			result.close();
			statement.close();
			con.close();

		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static String resourceToString(String name) {

		InputStream input = csStats.class.getResourceAsStream("/" + name);
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];

		if (input != null) {
			try {
				int n;
				Reader reader = new BufferedReader(new InputStreamReader(input));
				while ((n = reader.read(buffer)) != -1)
					writer.write(buffer, 0, n);
			} catch (IOException e) {
				try {
					input.close();
				} catch (IOException ex) {
				}
				return null;
			} finally {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		} else {
			return null;
		}

		String text = writer.toString().trim();
		text = text.replace("\r\n", " ").replace("\n", " ");
		return text.trim();
	}
}
