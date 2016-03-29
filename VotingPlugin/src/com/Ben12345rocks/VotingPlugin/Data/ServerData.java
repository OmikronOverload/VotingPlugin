package com.Ben12345rocks.VotingPlugin.Data;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.Ben12345rocks.VotingPlugin.Main;

public class ServerData {

	static ServerData instance = new ServerData();

	static Main plugin = Main.plugin;

	public static ServerData getInstance() {
		return instance;
	}

	FileConfiguration data;

	File dFile;

	private ServerData() {
	}

	public ServerData(Main plugin) {
		ServerData.plugin = plugin;
	}

	public FileConfiguration getData() {
		return data;
	}

	public void reloadData() {
		data = YamlConfiguration.loadConfiguration(dFile);
	}

	public void saveData() {
		try {
			data.save(dFile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger()
					.severe(ChatColor.RED + "Could not save ServerData.yml!");
		}
	}

	@SuppressWarnings("deprecation")
	public void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}

		dFile = new File(p.getDataFolder(), "ServerData.yml");

		boolean genFile = false;

		if (!dFile.exists()) {
			try {
				dFile.createNewFile();
				genFile = true;
			} catch (IOException e) {
				Bukkit.getServer()
						.getLogger()
						.severe(ChatColor.RED
								+ "Could not create ServerData.yml!");
			}
		}

		data = YamlConfiguration.loadConfiguration(dFile);
		data.options().header("DO NOT EDIT THIS FILE MANUALLY");
		if (genFile) {
			setPrevMonth(new Date().getMonth());
		}
		saveData();
	}

	public int getPrevMonth() {
		return getData().getInt("PrevMonth");
	}

	public void setPrevMonth(int value) {
		getData().set("PrevMonth", value);
		saveData();
	}

}
