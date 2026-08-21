package com.slapland.rankplugin;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
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

        Command rank = getCommand("rank");
        if (rank == null) {
            getLogger().severe("Command 'rank' is missing from plugin.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        rank.setExecutor(this);
        rank.setTabCompleter(this);
        getLogger().info("RankPlugin 1.0.2 enabled for Paper 1.20.4 + LuckPerms.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("rank")) return false;
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return true;
        }
        if (args.length != 3 || !args[1].equalsIgnoreCase("the")) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /rank <Player_Name> the <Rank_Name>");
            return true;
        }

        User actorUser = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (actorUser == null) {
            player.sendMessage(ChatColor.RED + "Your LuckPerms user data is not loaded yet.");
            return true;
        }

        boolean owner = hasGroup(actorUser, "owner");
        boolean admin = hasGroup(actorUser, "admin");
        if (!owner && !admin) {
            player.sendMessage(ChatColor.RED + "Only Admin or Owner can use /rank.");
            return true;
        }

        String requestedRank = args[2].toLowerCase(Locale.ROOT);
        if (!owner && isProtectedRank(requestedRank)) {
            player.sendMessage(ChatColor.RED + "Admin cannot assign Admin or Owner. Only Owner can do that.");
            return true;
        }

        if (ranksTrack == null) {
            player.sendMessage(ChatColor.RED + "LuckPerms track 'ranks' is missing. Run: lp createtrack ranks");
            return true;
        }

        String exactRank = ranksTrack.getGroups().stream()
                .filter(g -> g.equalsIgnoreCase(requestedRank))
                .findFirst()
                .orElse(null);
        if (exactRank == null) {
            player.sendMessage(ChatColor.RED + "Rank not found on the 'ranks' track: " + requestedRank);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player must be online.");
            return true;
        }

        User targetUser = luckPerms.getUserManager().getUser(target.getUniqueId());
        if (targetUser == null) {
            player.sendMessage(ChatColor.RED + "LuckPerms user data is not loaded yet.");
            return true;
        }

        String currentRank = getHighestManagedRank(targetUser);
        if (!owner && isProtectedRank(currentRank)) {
            player.sendMessage(ChatColor.RED + "Admin cannot change an Admin or Owner. Only Owner can do that.");
            return true;
        }

        targetUser.data().clear(node -> isManagedRankNode(node));
        Group group = luckPerms.getGroupManager().getGroup(exactRank);
        if (group == null) {
            player.sendMessage(ChatColor.RED + "LuckPerms group does not exist: " + exactRank);
            return true;
        }
        targetUser.data().add(InheritanceNode.builder(group).build());
        targetUser.setPrimaryGroup(exactRank);

        luckPerms.getUserManager().saveUser(targetUser).thenRun(() -> Bukkit.getScheduler().runTask(this, () -> {
            player.sendMessage(ChatColor.GREEN + "Rank of " + target.getName() + " changed to " + exactRank + ".");
            target.sendMessage(ChatColor.GREEN + "Your rank has been changed to " + exactRank + ".");
        }));
        return true;
    }

    private boolean hasGroup(User user, String name) {
        if (user.getPrimaryGroup().equalsIgnoreCase(name)) return true;
        return user.getInheritedGroups(user.getQueryOptions()).stream()
                .anyMatch(g -> g.getName().equalsIgnoreCase(name));
    }

    private boolean isProtectedRank(String rank) {
        return rank != null && (rank.equalsIgnoreCase("admin") || rank.equalsIgnoreCase("owner"));
    }

    private boolean isManagedRankNode(Node node) {
        if (!(node instanceof InheritanceNode inheritance) || ranksTrack == null) return false;
        String group = inheritance.getGroupName();
        return ranksTrack.getGroups().stream().anyMatch(g -> g.equalsIgnoreCase(group));
    }

    private String getHighestManagedRank(User user) {
        if (ranksTrack == null) return null;
        String found = null;
        for (String group : ranksTrack.getGroups()) {
            if (user.getInheritedGroups(user.getQueryOptions()).stream()
                    .anyMatch(g -> g.getName().equalsIgnoreCase(group))) {
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
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) names.add(p.getName());
            }
            return names;
        }
        if (args.length == 2) return Collections.singletonList("the");
        if (args.length == 3 && ranksTrack != null) {
            boolean owner = sender instanceof Player p && isOwner(p);
            String prefix = args[2].toLowerCase(Locale.ROOT);
            return ranksTrack.getGroups().stream()
                    .filter(g -> !isProtectedRank(g) || owner)
                    .filter(g -> g.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .toList();
        }
        return Collections.emptyList();
    }

    private boolean isOwner(Player player) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        return user != null && hasGroup(user, "owner");
    }
}
