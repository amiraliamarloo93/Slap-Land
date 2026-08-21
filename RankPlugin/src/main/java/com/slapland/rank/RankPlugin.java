package com.slapland.rank;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.track.Track;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class RankPlugin extends JavaPlugin implements CommandExecutor {
    private LuckPerms luckPerms;
    private String trackName;

    @Override public void onEnable() {
        saveDefaultConfig();
        trackName = getConfig().getString("track", "ranks");
        luckPerms = LuckPermsProvider.get();
        getCommand("rank").setExecutor(this);
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 3 || !args[1].equalsIgnoreCase("the")) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /rank <player> the <rank>");
            return true;
        }
        if (!sender.hasPermission("rankplugin.rank")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player is not online: " + args[0]);
            return true;
        }
        String rank = args[2].toLowerCase(Locale.ROOT);
        Track track = luckPerms.getTrackManager().getTrack(trackName).orElse(null);
        if (track == null) {
            sender.sendMessage(ChatColor.RED + "LuckPerms track not found: " + trackName);
            return true;
        }
        Group group = luckPerms.getGroupManager().getGroup(rank);
        if (group == null) {
            sender.sendMessage(ChatColor.RED + "Rank not found: " + rank);
            return true;
        }
        // /rank is intentionally restricted to the ranks configured below.
        String current = luckPerms.getUserManager().getUser(target.getUniqueId()).getPrimaryGroup();
        if (isProtected(current) || isProtected(rank)) {
            sender.sendMessage(ChatColor.RED + "Admin/Owner ranks cannot be changed with /rank.");
            return true;
        }
        User user = luckPerms.getUserManager().getUser(target.getUniqueId());
        if (user == null) {
            sender.sendMessage(ChatColor.RED + "LuckPerms user is not loaded.");
            return true;
        }
        user.data().clearNodes(node -> node.getKey().equals("group." + current));
        user.setPrimaryGroup(rank);
        luckPerms.getUserManager().saveUser(user);
        sender.sendMessage(ChatColor.GREEN + "Rank of " + target.getName() + " changed to " + rank + ".");
        target.sendMessage(ChatColor.GREEN + "Your rank is now " + rank + ".");
        return true;
    }

    private boolean isProtected(String rank) {
        return rank.equalsIgnoreCase("admin") || rank.equalsIgnoreCase("owner");
    }
}
