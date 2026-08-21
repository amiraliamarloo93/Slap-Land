package com.slapland.rankplugin;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
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
    private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        try {
            luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException ex) {
            getLogger().severe("LuckPerms was not found. RankPlugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Do not call JavaPlugin#getCommand here. Its return type changed in newer
        // Paper APIs, which can cause a NoSuchMethodError when the plugin is run on 1.20.4.
        // Because this plugin implements CommandExecutor, Bukkit uses the plugin itself
        // as the command executor for /rank declared in plugin.yml.
        getLogger().info("RankPlugin 1.0.2 enabled for Paper 1.20.4. /rank <player> the <rank>");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("rank")) return false;
        if (!(sender instanceof Player actor)) {
            sender.sendMessage(ChatColor.RED + "This command must be used in-game.");
            return true;
        }

        String actorRank = getPrimaryGroup(actor);
        boolean owner = actorRank.equals("owner") || actor.hasPermission("*") || actor.isOp();
        boolean admin = owner || actorRank.equals("admin");

        if (!admin) {
            actor.sendMessage(ChatColor.RED + "Only Admin or Owner can use /rank.");
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

        String targetRank = getPrimaryGroup(target);
        if (!owner && (rank.equals("admin") || rank.equals("owner")
                || targetRank.equals("admin") || targetRank.equals("owner"))) {
            actor.sendMessage(ChatColor.RED + "Admin cannot change Admin/Owner ranks.");
            return true;
        }

        boolean dispatched = Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                "lp user " + target.getName() + " parent set " + rank
        );

        if (!dispatched) {
            actor.sendMessage(ChatColor.RED + "Could not execute the LuckPerms command.");
            return true;
        }

        actor.sendMessage(ChatColor.GREEN + "Rank of " + target.getName() + " changed to " + rank + ".");
        target.sendMessage(ChatColor.GREEN + "Your rank is now " + rank + ".");
        return true;
    }

    private String getPrimaryGroup(Player player) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "default";
        return user.getPrimaryGroup().toLowerCase(Locale.ROOT);
    }
}
