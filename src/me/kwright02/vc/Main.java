package me.kwright02.vc;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.kwright02.vc.guilds.Guild;
import me.kwright02.vc.guilds.GuildManager;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {

	static {
		ConfigurationSerialization.registerClass(Guild.class);
	}

	@Override
	public void onEnable() {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "Loading VelClaims"));
		Bukkit.getPluginManager().registerEvents(this, this);
		getConfig().addDefault("guilds", new HashMap<String, Object>());
		getConfig().options().copyDefaults();
		GuildManager.populateGuildArray(this);


		ItemStack goldTotemBase = new ItemStack(Material.AMETHYST_SHARD, 1);
		ItemMeta goldTotemBaseItemMeta = goldTotemBase.getItemMeta();
		goldTotemBaseItemMeta.setCustomModelData(3);
		goldTotemBase.setItemMeta(goldTotemBaseItemMeta);
		Bukkit.addRecipe(new SmithingRecipe(new NamespacedKey(this, "custom_totem"), new ItemStack(Material.TOTEM_OF_UNDYING, 1), new RecipeChoice.ExactChoice(goldTotemBase), new RecipeChoice.MaterialChoice(Material.EMERALD)));
	}

	@Override
	public void onDisable() {
		GuildManager.storeGuilds(this);
	}


	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent e) {
		if(e.getFrom().getChunk() == e.getTo().getChunk()) return;
		boolean fromClaimed = GuildManager.isLandClaimed(e.getFrom());
		boolean toClaimed = GuildManager.isLandClaimed(e.getTo());

		if(fromClaimed) {
			Guild g = GuildManager.getLandOwner(e.getFrom());
			e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&5You are leaving the claim of '" + g.getName() + "'"));
		}
		if(!fromClaimed && toClaimed) {
			e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&5You are leaving the wild"));
		}
		if(toClaimed) {
			Guild g = GuildManager.getLandOwner(e.getTo());
			if(!g.isBordersOpen() && !g.getPlayers().contains(Bukkit.getOfflinePlayer(e.getPlayer().getUniqueId()))) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&c'" + g.getName() + "' currently has it's borders closed. You may NOT enter."));
				return;
			}
			e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6You are entering the claim of '" + g.getName() + "'"));
		} 
		if(fromClaimed && !toClaimed) {
			e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&2You are entering the wild"));
		}
	}

	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {

		ArrayList<EntityType> blacklist = new ArrayList<>();
		blacklist.add(EntityType.SHEEP);
		blacklist.add(EntityType.COW);
		blacklist.add(EntityType.CHICKEN);
		blacklist.add(EntityType.RABBIT);
		blacklist.add(EntityType.GOAT);
		blacklist.add(EntityType.LLAMA);
		blacklist.add(EntityType.VILLAGER);
		blacklist.add(EntityType.CAT);
		blacklist.add(EntityType.WOLF);
		blacklist.add(EntityType.PARROT);
		blacklist.add(EntityType.SNOWMAN);
		blacklist.add(EntityType.HORSE);
		blacklist.add(EntityType.DONKEY);
		blacklist.add(EntityType.MINECART);
		blacklist.add(EntityType.MINECART_CHEST);
		blacklist.add(EntityType.MINECART_FURNACE);
		blacklist.add(EntityType.MINECART_HOPPER);
		blacklist.add(EntityType.MINECART_TNT);
		blacklist.add(EntityType.ARMOR_STAND);
		blacklist.add(EntityType.WANDERING_TRADER);
		blacklist.add(EntityType.AXOLOTL);
		blacklist.add(EntityType.COD);
		blacklist.add(EntityType.TRADER_LLAMA);
		blacklist.add(EntityType.TROPICAL_FISH);
		blacklist.add(EntityType.DOLPHIN);
		blacklist.add(EntityType.PANDA);
		blacklist.add(EntityType.PAINTING);
		blacklist.add(EntityType.FOX);
		blacklist.add(EntityType.ITEM_FRAME);
		blacklist.add(EntityType.LEASH_HITCH);
		blacklist.add(EntityType.MULE);
		blacklist.add(EntityType.TURTLE);
		blacklist.add(EntityType.MUSHROOM_COW);
		blacklist.add(EntityType.OCELOT);

		if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player v = (Player) e.getEntity();
			Player d = (Player) e.getDamager();
			if(GuildManager.isInGuild(v) && GuildManager.isInGuild(d)) {
				if(GuildManager.getGuild(v).getPlayers().contains(d)) {
					e.setCancelled(true);
				}
			}
		} else if(e.getDamager() instanceof Player) {
			Player p = (Player) e.getDamager();
			if(GuildManager.isLandClaimed(e.getEntity().getLocation())) {
				if(GuildManager.getLandOwner(e.getEntity().getLocation()) != null && GuildManager.getLandOwner(e.getEntity().getLocation()) != GuildManager.getGuild(p)) {
					if(blacklist.contains(e.getEntity().getType())) {
						e.setCancelled(true);
						error(p, "&cYou cannot attack peaceful mobs in another guild's claim");
					}
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent e) {
		Player p = (Player) e.getPlayer();
		if(GuildManager.isLandClaimed(e.getBlock().getLocation())) {
			if(!GuildManager.isInGuild(p) || GuildManager.getLandOwner(e.getBlock().getLocation()) != GuildManager.getGuild(p)) {
				e.setCancelled(true);
				error(p, "&cYou cannot destroy land that is not yours");
				return;
			}
		}
	}

	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent e) {
		Player p = (Player) e.getPlayer();
		if(GuildManager.isLandClaimed(e.getBlock().getLocation())) {
			if(!GuildManager.isInGuild(p) || GuildManager.getLandOwner(e.getBlock().getLocation()) != GuildManager.getGuild(p)) {
				e.setCancelled(true);
				error(p, "&cYou cannot build on land that is not yours");
				return;
			}
		}
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent e) {

		ArrayList<Material> blacklist = new ArrayList<>();
		blacklist.add(Material.CHEST);
		blacklist.add(Material.FURNACE);
		blacklist.add(Material.TRAPPED_CHEST);
		blacklist.add(Material.CHEST_MINECART);
		blacklist.add(Material.FURNACE_MINECART);
		blacklist.add(Material.HOPPER_MINECART);
		blacklist.add(Material.FURNACE_MINECART);
		blacklist.add(Material.ANVIL);
		blacklist.add(Material.CHIPPED_ANVIL);
		blacklist.add(Material.DAMAGED_ANVIL);
		blacklist.add(Material.BARREL);
		blacklist.add(Material.HOPPER);
		blacklist.add(Material.DISPENSER);
		blacklist.add(Material.DROPPER);
		blacklist.add(Material.ITEM_FRAME);
		blacklist.add(Material.LEVER);
		blacklist.add(Material.ACACIA_BUTTON);
		blacklist.add(Material.OAK_BUTTON);
		blacklist.add(Material.DARK_OAK_BUTTON);
		blacklist.add(Material.JUNGLE_BUTTON);
		blacklist.add(Material.BIRCH_BUTTON);
		blacklist.add(Material.CRIMSON_BUTTON);
		blacklist.add(Material.POLISHED_BLACKSTONE_BUTTON);
		blacklist.add(Material.SPRUCE_BUTTON);
		blacklist.add(Material.STONE_BUTTON);
		blacklist.add(Material.WARPED_BUTTON);
		blacklist.add(Material.ACACIA_PRESSURE_PLATE);
		blacklist.add(Material.OAK_PRESSURE_PLATE);
		blacklist.add(Material.DARK_OAK_PRESSURE_PLATE);
		blacklist.add(Material.JUNGLE_PRESSURE_PLATE);
		blacklist.add(Material.BIRCH_PRESSURE_PLATE);
		blacklist.add(Material.CRIMSON_PRESSURE_PLATE);
		blacklist.add(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
		blacklist.add(Material.SPRUCE_PRESSURE_PLATE);
		blacklist.add(Material.STONE_PRESSURE_PLATE);
		blacklist.add(Material.WARPED_PRESSURE_PLATE);
		blacklist.add(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
		blacklist.add(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);

		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			if(blacklist.contains(e.getClickedBlock().getType())) {
				if(GuildManager.isLandClaimed(e.getClickedBlock().getLocation())) {
					Guild landOwner = GuildManager.getLandOwner(e.getClickedBlock().getLocation());
					if(landOwner.getPlayers().contains(Bukkit.getOfflinePlayer(e.getPlayer().getUniqueId()))) return;
					e.setCancelled(true);
					error(e.getPlayer(), "&cYou cannot interact with blocks you do not own");
					return;
				}
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender,Command cmd,  String commandLabel, String[] args) {
		switch(commandLabel) {
		case "sethome":
			Player p = (Player) sender;
			switch(p.getWorld().getName()) {
			case "world":

				if(args.length == 0) {
					p.sendMessage("§cUsage: /sethome <1,2,3> - World specific");
					break;
				}
				switch(args[0]) {
				case "1":
					Location l = p.getLocation();
					getConfig().set(p.getUniqueId().toString() + "-world-home1", l);
					p.sendMessage("§2Home 1 was set for overworld. Homes are world-specific, so use /dimensions to access them");
					break;
				case "2":
					if(!p.hasPermission("velraven.gui.homes2")) {
						p.sendMessage("§cGive us your money bitch");
						break;
					}
					Location ll = p.getLocation();
					getConfig().set(p.getUniqueId().toString() + "-world-home2", ll);
					p.sendMessage("§2Home 2 was set for overworld. Homes are world-specific, so use /dimensions to access them");
					break;
				case "3":
					if(!p.hasPermission("velraven.gui.homes3")) {
						p.sendMessage("§cGive us your money bitch");
						break;
					}
					Location lll = p.getLocation();
					getConfig().set(p.getUniqueId().toString() + "-world-home3", lll);
					p.sendMessage("§2Home 3 was set for overworld. Homes are world-specific, so use /dimensions to access them");
					break;
				}

				break;
			}
			break;


		case "g":
		case "guild":
			if(!check(sender, commandLabel)) return true;
			if(args.length == 0) {
				error((Player) sender, "&cUsage: /g <create|delete|members|info>");
				break;
			}
			switch(args[0]) {
			case "c":
			case "create":
				if(!check(sender, commandLabel)) return true;
				if(GuildManager.isInGuild((Player) sender)) {
					error((Player) sender, "&cYou are already in a guild");
					break;
				}
				if(!GuildManager.isNameAvaliable(args[1])) {
					error((Player) sender, "&cThat guild name is already taken");
					break;
				} else {
					GuildManager.createGuild(args[1], (Player) sender);
				}
				break;
			}
			break;
		case "vc":
		case "velclaims":
		case "velc":
		case "vclaims":
			if(!check(sender, commandLabel)) return true;
			if(args.length == 0) {
				error((Player) sender, "&cUsage: /vc <claim|unclaim|unclaimall|info>");
				break;
			}
			switch(args[0]) {
			case "claim":
				if(!GuildManager.isInGuild(Bukkit.getOfflinePlayer(((Player) sender).getUniqueId()))) {
					error((Player) sender, "&cYou must be in a guild to claim land");
					break;
				} else {
					Location l = ((Player) sender).getLocation();
					Guild g = GuildManager.getGuild((Player) sender);
					if(GuildManager.isLandClaimed(l)) {
						error((Player) sender, "&cThe chunk your in is already claimed");
						break;
					} else if(g.getCore() == null && g.getClaimCount() == 1) {
						error((Player) sender, "&cYou cannot claim more chunks before placing your core");
						break;
					} else {
						if(g.getClaimCount() == g.getClaimLimit()) {
							error((Player) sender, "&cYou already hit your limit of (" + g.getClaimLimit() + ") chunks you can claim!");
							break;
						}
						g.addClaim(l);
						g.setClaimCount(g.getClaimCount() + 1);
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2This chunk is now claimed for your guild and you have " + g.getClaimCount() + "/" + g.getClaimLimit() + " chunks claimed"));
					}
				}
				break;
			case "unclaim":
				if(!GuildManager.isInGuild(Bukkit.getOfflinePlayer(((Player) sender).getUniqueId()))) {
					error((Player) sender, "&cYou must be in a guild to unclaim land");
					break;
				} else {
					Location l = ((Player) sender).getLocation();
					Guild g = GuildManager.getGuild((Player) sender);
					if(!GuildManager.isLandClaimed(l)) {
						error((Player) sender, "&cThe chunk your in is not claimed");
						break;
					} else if(g.isLandClaimed(g.getCore().getLocation())) {
						error((Player) sender, "&cYou cannot unclaim your core chunk before removing the core");
						break;
					} else {
						for(Location x : g.getClaims()) {
							if(x.getChunk() == l.getChunk()) {
								ArrayList<Location> xx = g.getClaims();
								xx.remove(x);
								g.setClaims(xx);
							}
						}
						g.setClaimCount(g.getClaimCount() - 1);
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4This chunk is now unclaimed from your guild and you have " + g.getClaimCount() + "/" + g.getClaimLimit() + " chunks claimed"));
					}
				}
				break;
			case "members":
				if(!GuildManager.isInGuild(Bukkit.getOfflinePlayer(((Player) sender).getUniqueId()))) {
					error((Player) sender, "&cYou must be in a guild to list members");
					break;
				} else {
					Guild g = GuildManager.getGuild((Player) sender);
					for(int i = 0; i < g.getPlayers().size(); i++) {
						OfflinePlayer member = g.getPlayers().get(i);
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2" + i + ": " + member.getName()));
					}
				}
				break;
			case "closeborders":
			case "cb":
			case "closeb":
			case "cborders":
				if(!GuildManager.isInGuild(Bukkit.getOfflinePlayer(((Player) sender).getUniqueId()))) {
					error((Player) sender, "&cYou must be in a guild to close borders");
					break;
				}
				if(GuildManager.getGuild((Player) sender).getOwner().getUniqueId() != ((Player) sender).getUniqueId()) {
					error((Player) sender, "&cYou must be the guild owner to close borders");
					break;
				} else {
					Guild g = GuildManager.getGuild((Player) sender);
					g.setBordersOpen(false);
					error((Player) sender, "&4Your borders are now closed. This means only guild members can enter your land");
				}
				break;
			case "openborders":
			case "ob":
			case "openb":
			case "oborders":
				if(!GuildManager.isInGuild(Bukkit.getOfflinePlayer(((Player) sender).getUniqueId()))) {
					error((Player) sender, "&cYou must be in a guild to close borders");
					break;
				}
				if(GuildManager.getGuild((Player) sender).getOwner().getUniqueId() != ((Player) sender).getUniqueId()) {
					error((Player) sender, "&cYou must be the guild owner to close borders");
					break;
				} else {
					Guild g = GuildManager.getGuild((Player) sender);
					g.setBordersOpen(true);
					error((Player) sender, "&4Your borders are now open. This means anyone can enter your land");
				}
				break;
			}
			break;

		}
		return true;
	}

	public boolean check(CommandSender sender, String command) {
		if(!sender.hasPermission("valraven.claims." + command)) {
			error((Player) sender, "&cYou do not have permission for the command '" + command + "'");
			return false;
		}
		return true;
	}

	public static void error(Player p, String message) {
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "ꑚ " + message));
	}

}
