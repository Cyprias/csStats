package com.cyprias.csstats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.Material;

public class ItemDb {
	private csStats plugin;
	static Logger log = Logger.getLogger("Minecraft");
	
	private  File file;
	public ItemDb(csStats plugin) {
		this.plugin = plugin;

		file = new File(plugin.getDataFolder(), "items.csv");
		//if (!file.exists()) {
			file.getParentFile().mkdirs();
			copy(plugin.getResource("items.csv"), file);
		//}
		
		
		loadFile();
	}
	public void copy(InputStream in, File file) {
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
	
	public itemData getItemID(String itemName){
		if (nameToID.containsKey(itemName))
			return nameToID.get(itemName);
		return null;
	}
	
	
	
	public String getItemName(int itemID, int itemDur){
		if (idToName.containsKey(itemID+":"+itemDur))
			return idToName.get(itemID+":"+itemDur);
		
		return itemID+":"+itemDur;
	}
	
	static class itemData {
		String itemName = "null";
		int itemID;
		short itemDur;
		public itemData(String string, int itemid2, short metaData) {
			itemName = string;
			itemID = itemid2; //Integer.parseInt(itemid2);
			itemDur = metaData; //Short.parseShort(metaData);
		}
	}
	HashMap<String, itemData> nameToID = new HashMap<String, itemData>();
	HashMap<String,String> idToName = new HashMap<String,String>();
	
	
	private void loadFile(){
		try {
			BufferedReader r = new BufferedReader(new FileReader(file));
			
			String line;
			
			
			int l=0;
			String sID;
			while ((line = r.readLine()) != null){
				l=l+1;
				if (l > 3){
					//log.info(line.toString());
					String[] values = line.split(",");
					
					
					//if (values[2] != "0")
					//	sID = sID+":"+values[2];
					
					
					//log.info(sID + " = " + values[0]);
					
					//if (!nameToID.containsKey(values[0]))
						nameToID.put(values[0], new itemData(values[0], Integer.parseInt(values[1]), Short.parseShort(values[2])));
						
					sID = values[1]+":"+values[2];
					if (! idToName.containsKey(sID))
						idToName.put(sID, values[0]);
			
				}
			//	warps.put(values[0], new Location(Bukkit.getWorld(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3]), Double.parseDouble(values[4])));
				//log.info(values[0] + " + " + values[1]);
			
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//log.info("1 is " + idToName.get("1:0"));
		
		
		
	}
	
	
	
}
