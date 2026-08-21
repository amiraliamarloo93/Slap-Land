package com.slapland.rankplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Set;

public final class RankPlugin extends JavaPlugin implements CommandExecutor {
    private static final Set<String> RANKS = Set.of("member", "vip", "admin", "owner");

    @Override
    public void onEnable() {
        if (getCommand("rank") != null) getCommand("rank").setExecutor(this);
        getLogger().info("RankPlugin enabled. /rank <player> the <rank>");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("rank")) return false;
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command must be used in-game.");
            return true;
        }
        Player actor = (Player) sender;
        if (!actor.hasPermission("rankplugin.use")) {
            actor.sendMessage(ChatColor.RED + "You do not have permission to use /rank.");
            return true;
        }
        if (args.length != 3 || !args[1].equalsIgnoreCase("the")) {
            actor.sendMessage(ChatColor.YELLOW + "Usage: /rank <Player_Name> the <Rank_Name>");
            actor.sendMessage(ChatColor.GRAY + "Ranks: member, vip, admin, owner");
            return true;
        }
        String rank = args[2].toLowerCase(Locale.ROOT);
        if (!RANKS.contains(rank)) {
            actor.sendMessage(ChatColor.RED + "Unknown rank. Use: member, vip, admin, owner");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            actor.sendMessage(ChatColor.RED + "Player must be online: " + args[0]);
            return true;
        }

        boolean owner = actor.hasPermission("rankplugin.owner") || actor.hasPermission("*");
        boolean admin = owner || actor.hasPermission("rankplugin.admin");
        if (!admin) {
            actor.sendMessage(ChatColor.RED + "Only Admin or Owner can use /rank.");
            return true;
        }

        if (!owner) {
            String current = getPrimaryGroup(target);
            if (rank.equals("admin") || rank.equals("owner") || current.equals("admin") || current.equals("owner")) {
                actor.sendMessage(ChatColor.RED + "Admin cannot change Admin/Owner ranks.");
                return true;
            }
        }

        boolean dispatched = Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "lp user " + target.getName() + " parent set " + rank);
        if (!dispatched) {
            actor.sendMessage(ChatColor.RED + "Could not execute LuckPerms command.");
            return true;
        }
        actor.sendMessage(ChatColor.GREEN + "Rank of " + target.getName() + " changed to " + rank + ".");
        target.sendMessage(ChatColor.GREEN + "Your rank is now " + rank + ".");
        return true;
    }

    private String getPrimaryGroup(Player player) {
        if (player.hasPermission("rankplugin.owner") || player.hasPermission("*")) return "owner";
        if (player.hasPermission("rankplugin.admin")) return "admin";
        return "member";
    }
}
