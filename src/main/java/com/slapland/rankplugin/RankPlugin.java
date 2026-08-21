package com.slapland.rankplugin;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.track.Track;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class RankPlugin extends JavaPlugin implements CommandExecutor, TabCompleter {
    private LuckPerms luckPerms;
    private Track ranksTrack;

    @Override
    public void onEnable() {
        luckPerms = LuckPermsProvider.get();
        ranksTrack = luckPerms.getTrackManager().getTrack("ranks");

        if (ranksTrack == null) {
            getLogger().warning("LuckPerms track 'ranks' was not found. Create it with: lp createtrack ranks");
        }

        if (getCommand("rank") == null) {
            getLogger().severe("Command 'rank' is missing from plugin.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("rank").setExecutor(this);
        getCommand("rank").setTabCompleter(this);
        getLogger().info("RankPlugin enabled for Paper 1.20.4 + LuckPerms.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("rank")) return false;

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return true;
        }
        if (!player.hasPermission("rankplugin.rank")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use /rank.");
            return true;
        }
        if (args.length != 3 || !args[1].equalsIgnoreCase("the")) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /rank <Player_Name> the <Rank_Name>");
            return true;
        }

        String targetName = args[0];
        String requestedRank = args[2].toLowerCase(Locale.ROOT);
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player must be online.");
            return true;
        }

        if (isProtectedRank(requestedRank)) {
            player.sendMessage(ChatColor.RED + "You cannot assign the Admin or Owner rank with /rank.");
            return true;
        }

        User targetUser = luckPerms.getUserManager().getUser(target.getUniqueId());
        if (targetUser == null) {
            player.sendMessage(ChatColor.RED + "LuckPerms user data is not loaded yet.");
            return true;
        }

        String currentRank = getHighestManagedRank(targetUser);
        if (isProtectedRank(currentRank) && !isOwner(player)) {
            player.sendMessage(ChatColor.RED + "You cannot change an Admin or Owner with /rank.");
            return true;
        }

        if (ranksTrack == null) {
            player.sendMessage(ChatColor.RED + "LuckPerms track 'ranks' is missing.");
            return true;
        }
        if (ranksTrack.getGroups().stream().noneMatch(g -> g.equalsIgnoreCase(requestedRank))) {
            player.sendMessage(ChatColor.RED + "Rank not found on the 'ranks' track: " + requestedRank);
            return true;
        }

        String exactRank = ranksTrack.getGroups().stream()
                .filter(g -> g.equalsIgnoreCase(requestedRank))
                .findFirst().orElse(requestedRank);

        targetUser.data().clear(node -> node instanceof InheritanceNode);
        targetUser.data().add(InheritanceNode.builder(exactRank).build());
        luckPerms.getUserManager().saveUser(targetUser);

        player.sendMessage(ChatColor.GREEN + "Rank of " + target.getName() + " changed to " + exactRank + ".");
        target.sendMessage(ChatColor.GREEN + "Your rank has been changed to " + exactRank + ".");
        return true;
    }

    private boolean isOwner(Player player) {
        return player.hasPermission("rankplugin.owner") || player.hasPermission("*" );
    }

    private boolean isProtectedRank(String rank) {
        return rank.equalsIgnoreCase("admin") || rank.equalsIgnoreCase("owner");
    }

    private String getHighestManagedRank(User user) {
        List<String> groups = ranksTrack == null ? Collections.emptyList() : ranksTrack.getGroups();
        String found = null;
        for (String group : groups) {
            if (user.getInheritedGroups(user.getQueryOptions()).stream().anyMatch(g -> g.getName().equalsIgnoreCase(group))) {
                found = group;
            }
        }
        return found;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("rank")) return Collections.emptyList();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) names.add(p.getName());
            return names;
        }
        if (args.length == 2) return Collections.singletonList("the");
        if (args.length == 3 && ranksTrack != null) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            return ranksTrack.getGroups().stream()
                    .filter(g -> !isProtectedRank(g))
                    .filter(g -> g.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .toList();
        }
        return Collections.emptyList();
    }
}
