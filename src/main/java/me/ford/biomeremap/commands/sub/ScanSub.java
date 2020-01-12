package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.commands.SubCommand;
import me.ford.biomeremap.largetasks.LargeScanTaskStarter;

public class ScanSub extends SubCommand {
	private static final String PERMS = "biomeremap.scan";
	private static final String USAGE = "/biomeremap scan <chunk | region> [<world> <x> <z>]";
	private final BiomeRemap br;
	private final List<String> worldNames = new ArrayList<>();
	private boolean scanning = false;
	private final Pattern layerPattern = Pattern.compile("--layer(\\d+)");

	public ScanSub(BiomeRemap plugin) {
		super("scan");
		br = plugin;
		for (World world : br.getServer().getWorlds()) {
			worldNames.add(world.getName());
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, List<String> opts) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], Arrays.asList(new String[] {"chunk", "region"}), list);
		} else if (args.length == 2) {
			return StringUtil.copyPartialMatches(args[1], worldNames, list);
		}
		return list;
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, List<String> opts) {
		if (scanning) {
			sender.sendMessage(br.getMessages().getScanInProgress());
			return true;
		}
		if (args.length < 1) {
			return false;
		}
		String regionOrChunk = args[0];
		if (!regionOrChunk.equalsIgnoreCase("region") && !regionOrChunk.equalsIgnoreCase("chunk")) {
			return false;
		}
		boolean region = regionOrChunk.equalsIgnoreCase("region");
		boolean ingame = sender instanceof Player;
		boolean debug = opts.contains("--debug");
		boolean useNMS = opts.contains("--nms");
		if (useNMS) {
			sender.sendMessage("Using NMS"); // TODO - message?
		}
		int layer = 0;
		for (String opt : opts) {
			Matcher matcher = layerPattern.matcher(opt);
			if (matcher.matches()) {
				try {
					layer = Integer.parseInt(matcher.group(1));
				} catch (NumberFormatException e) {
					sender.sendMessage("Could not parse number for layer from option: " + opt); // shouldn't happen because of the regex
					return true;
				}
				sender.sendMessage("Scanning at layer " + layer + " instead of 0"); // TODO - message?
				break;
			}
		}
		if (useNMS && layer > 63) {
			layer = 63; // higher = same
			sender.sendMessage("Biome for layers higher than 63 is the same as for layer 63");
		}
		World world;
		int x, z;

		if (!ingame && args.length < 4) {
			return false;
		}
		if (args.length < 4) { // has to be in game
			if (args.length > 1) { // either no arguments for current chunk/region or specify chunk/region
				return false;
			}
			Location loc = ((Player) sender).getLocation();
			world = loc.getWorld();
			x = loc.getChunk().getX();
			z = loc.getChunk().getZ();
		} else {
			world = br.getServer().getWorld(args[1]);
			try {
				x = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				sender.sendMessage(br.getMessages().errorNotInteger(args[2]));
				return true;
			}
			try {
				z = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				sender.sendMessage(br.getMessages().errorNotInteger(args[3]));
				return true;
			}
		}
		if (world == null) {
			sender.sendMessage(br.getMessages().errorWorldNotFound(args[1]));
			return true;
		}
		if (region) {
			sender.sendMessage(br.getMessages().getScanRegionStart(world.getName(), x, z));
		} else {
			sender.sendMessage(br.getMessages().getScanChunkStart(world.getName(), x, z));
		}
		new LargeScanTaskStarter(br, world, sender, x, layer, z, region, debug, () -> taskDone(), useNMS);
		scanning = true;
		return true;
	}
	
	public void taskDone() {
		scanning = false;
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission(PERMS) || (sender instanceof ConsoleCommandSender);
	}

	@Override
	public String getUsage(CommandSender sender) {
		return USAGE;
	}

}
