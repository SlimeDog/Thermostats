package me.ford.biomeremap.settings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomConfigHandler {
	private FileConfiguration customConfig = null;
	private File customConfigFile = null;
	private final String fileName;
	private final JavaPlugin plugin;

	public CustomConfigHandler(JavaPlugin plugin, String fileName) {
		this.plugin = plugin;
		this.fileName = fileName;
	}

	public void reloadCustomConfig() {
		if (customConfigFile == null) {
			customConfigFile = new File(plugin.getDataFolder(), fileName);
		}
		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

		// Look for defaults in the jar
		Reader defConfigStream = null;
		InputStream resource = plugin.getResource(fileName);
		if (resource != null) {
			try {
				defConfigStream = new InputStreamReader(resource, "UTF8");
			} catch (UnsupportedEncodingException e) {
				plugin.getLogger().log(Level.SEVERE, "Unsupported encoding while loading resource " + fileName, e);
			}
		}
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			customConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getCustomConfig() {
		if (customConfig == null) {
			reloadCustomConfig();
		}
		return customConfig;
	}

	public void saveCustomConfig() {
		if (customConfig == null || customConfigFile == null) {
			return;
		}
		try {
			getCustomConfig().save(customConfigFile);
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
		}
	}

	public void saveDefaultConfig() {
		if (customConfigFile == null) {
			customConfigFile = new File(plugin.getDataFolder(), fileName);
		}
		if (!customConfigFile.exists()) {
			plugin.saveResource(fileName, false);
		}
	}

}
