package com.cyprias.csstats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ItemDb {
	private csStats plugin;
	static Logger log = Logger.getLogger("Minecraft");
	
	private  File file;
	public ItemDb(csStats plugin) {
		this.plugin = plugin;

		file = new File(plugin.getDataFolder(), "items.csv");
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			copy(plugin.getResource("items.csv"), file);
		}
		
		
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
	
	private class itemData {
		int id = 0;
		int dur = 0;
		public itemData(String string, String string2) {
			// TODO Auto-generated constructor stub
			id = Integer.parseInt(string);
			dur = Integer.parseInt(string2);
		}

	}
	
	HashMap<String,String> idToName = new HashMap<String,String>();
	
	public String getItemName(int itemID, int itemDur){
		return idToName.get(itemID+":"+itemDur);
	}
	
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
					
					sID = values[1]+":"+values[2];
					//if (values[2] != "0")
					//	sID = sID+":"+values[2];
					
					
					//log.info(sID + " = " + values[0]);
					
					
					
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
