package me.kwright02.vc.guilds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

public class GuildManager {
	
	public static ArrayList<Guild> guilds = new ArrayList<>();
	
	public static Guild createGuild(String name, Player owner) {
		Guild g = new Guild(name, owner);
		guilds.add(g);
		ArrayList<OfflinePlayer> players = g.getPlayers();
		if(players == null)
			players = new ArrayList<OfflinePlayer>();
		players.add(Bukkit.getOfflinePlayer(owner.getUniqueId()));
		g.setPlayers(players);
		owner.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3A new guild was created for you named '" + name + "'"));
		return g;
	}
	
	public static Guild getGuild(String name) {
		for(Guild g : guilds) {
			if(g.getName() == name) {
				return g;
			}
		}
		return null;
	}
	
	public static Guild getGuild(OfflinePlayer player) {
		for(Guild g : guilds) {
			if(g.getPlayers().contains(player)) {
				return g;
			}
		}
		return null;
	}
	
	public static boolean isInGuild(OfflinePlayer p) {
		if(guilds.isEmpty()) {
			Bukkit.broadcastMessage("Guilds empty");
			return false;
		}
		for(Guild g : guilds) {
			for(OfflinePlayer x : g.getPlayers()) {
//				Bukkit.broadcastMessage(g.getPlayers().toString());
				if(x.getUniqueId() == p.getUniqueId()) {
					return true;
				}
			}
		}
		Bukkit.broadcastMessage("No guild members match " + p.getName());
		return false;
	}
	
	public static boolean isNameAvaliable(String guildname) {
		for(Guild g: guilds) {
			Bukkit.broadcastMessage("Testing I: " + guildname + " against X: " + g.getName());
			if(g.getName().equalsIgnoreCase(guildname)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isLandClaimed(Location l) {
		for(Guild g : guilds) {
			if(g.isLandClaimed(l)) {
				return true;
			}
		}
		return false;
	}
	
	public static Guild getLandOwner(Location l) {
		for(Guild g : guilds) {
			if(g.isLandClaimed(l)) {
				return g;
			}
		}
		return null;
	}
	
	public static void storeGuilds(Plugin p) {
		List<Map<String, Object>> gs = new ArrayList<>();
		for(Guild g: guilds) {
			gs.add(g.serialize());
		}
		p.getConfig().set("guilds", gs);
		p.saveConfig();
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<Guild> populateGuildArray(Plugin p) {
		guilds = (ArrayList<Guild>) p.getConfig().getMapList("guilds")
        .stream()
        .map(serializedGuild -> Guild.deserialize((Map<String, Object>) serializedGuild))
        .collect(Collectors.toList());
		Bukkit.broadcastMessage("Guilds list is " + guilds.size());
		return guilds;
	}

}
