package me.kwright02.vc.guilds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

public class Guild implements ConfigurationSerializable{
	
	private String name;
	private OfflinePlayer owner;
	private ArrayList<OfflinePlayer> players;
	private ArrayList<Location> claims;
	private int claimLimit = 9;
	private int claimCount = 0;
	private Block core;
	private boolean alive;
	private boolean bordersOpen;
	
	public Guild(String name, OfflinePlayer owner, ArrayList<OfflinePlayer> players, ArrayList<Location> claims, int claimLimit, int claimCount, Block core, boolean alive, boolean bordersOpen) {
		this.setName(name);
		this.setOwner(owner);
		this.setCore(core);
		this.setAlive(alive);
		this.setPlayers(players);
		this.setClaims(claims);
		this.setClaimLimit(claimLimit);
		this.setClaimCount(claimCount);
	}
	
	public Guild(String name, OfflinePlayer owner) {
		this(name, owner, null, new ArrayList<Location>(), 9, 0, null, false, true);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OfflinePlayer getOwner() {
		return owner;
	}

	public void setOwner(OfflinePlayer owner) {
		this.owner = owner;
	}

	public Block getCore() {
		return core;
	}

	public void setCore(Block core) {
		this.core = core;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public ArrayList<OfflinePlayer> getPlayers() {
		return players;
	}

	public void setPlayers(ArrayList<OfflinePlayer> players) {
		this.players = players;
	}

	public ArrayList<Location> getClaims() {
		return claims;
	}

	public void setClaims(ArrayList<Location> claims) {
		this.claims = claims;
	}
	
	public void addClaim(Location claim) {
		this.claims.add(claim);
	}
	
	public boolean isLandClaimed(Location l) {
		try {
			if(this.claims == null || claims.isEmpty()) return false;
			for(Location x : claims) {
				if(x.getChunk() == l.getChunk()) {
					return true;
				}
			}
		} catch(NullPointerException e) {
			return false;
		}
		return false;
	}

	public int getClaimLimit() {
		return claimLimit;
	}

	public void setClaimLimit(int claimLimit) {
		this.claimLimit = claimLimit;
	}

	public int getClaimCount() {
		return claimCount;
	}

	public void setClaimCount(int claimCount) {
		this.claimCount = claimCount;
	}
	
	public boolean isBordersOpen() {
		return bordersOpen;
	}

	public void setBordersOpen(boolean bordersOpen) {
		this.bordersOpen = bordersOpen;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> vals = new HashMap<>();
		vals.put("name", this.name);
		vals.put("owner", this.owner.getUniqueId().toString());
		ArrayList<String> pls = new ArrayList<>();
		for(OfflinePlayer x : players) {
			pls.add(x.getUniqueId().toString());
		}
		vals.put("players", pls);
		vals.put("claims", this.claims);
		vals.put("claimLimit", this.claimLimit);
		vals.put("claimCount", this.claimCount);
		vals.put("core", this.core);
		vals.put("alive", this.alive);
		vals.put("bordersOpen", this.bordersOpen);
		return vals;
	}
	
	@SuppressWarnings("unchecked")
	public static Guild deserialize(Map<String, Object> deserialize) {
		ArrayList<OfflinePlayer> pls = new ArrayList<>();
		for(String x : (ArrayList<String>) deserialize.get("players")) {
			pls.add(Bukkit.getOfflinePlayer(UUID.fromString(x)));
		}
        return new Guild((String) deserialize.get("name"), Bukkit.getOfflinePlayer(UUID.fromString((String) deserialize.get("owner"))), 
        		pls, (ArrayList<Location>) deserialize.get("claims"),
        		NumberConversions.toInt(deserialize.get("claimLimit")), 
        		NumberConversions.toInt(deserialize.get("claimCount")),
        		(Block) deserialize.get("core"), (Boolean) deserialize.get("alive"), (Boolean) deserialize.get("bordersOpen"));
    }

	

}
