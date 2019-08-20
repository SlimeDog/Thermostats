package me.ford.biomeremap.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.mapping.BiomeMap;

public class Settings {
	private final BiomeRemap br;
	private final Map<String, BiomeMap> maps = new HashMap<>();
	private final Map<String, BiomeMap> worldMap = new HashMap<>();
	
	public Settings(BiomeRemap plugin) {
		br = plugin;
		reload();
	}
	
	public void reload() {
		maps.clear();
		worldMap.clear();
		ConfigurationSection mapsSection = br.getConfig().getConfigurationSection("biomemaps"); 
		for (String key : mapsSection.getKeys(false)) {
			ConfigurationSection curMapSection = mapsSection.getConfigurationSection(key);
			if (curMapSection == null) {
				br.getLogger().warning("Biomemap by the name of '" + key + "' is incomplete");
				continue;
			}
			maps.put(key, new BiomeMap(curMapSection));
		}
		for (BiomeMap map : maps.values()) {
			for (String worldName : map.getApplicableWorldNames()) {
				worldMap.put(worldName, map); // TODO - what happens with multiple mappings?
			}
		}
	}
	
	public String getVersion() {
		return br.getConfig().getString("version");
	}
	
	public boolean checkForUpdates() {
		return br.getConfig().getBoolean("check-for-updates");
	}
	
	public boolean enableMetrics() {
		return br.getConfig().getBoolean("enable-metrics");
	}
	
	public Set<String> getBiomeMapNames() {
		return maps.keySet();
	}
	
	public boolean debugAutoremap() {
		return br.getConfig().getBoolean("debug-autoremap", false);
	}
	
	public BiomeMap getBiomeMap(String name) {
		return maps.get(name);
	}
	
	public BiomeMap getApplicableBiomeMap(String worldName) {
		return worldMap.get(worldName);
	}

}
