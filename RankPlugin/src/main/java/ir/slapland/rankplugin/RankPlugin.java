package ir.slapland.rankplugin;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class RankPlugin extends JavaPlugin implements CommandExecutor, TabCompleter {
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
        Objects.requireNonNull(getCommand("rank")).setExecutor(this);
        Objects.requireNonNull(getCommand("rank")).setTabCompleter(this);
        getLogger().info("RankPlugin enabled. /rank <Player_Name> the <Rank_Name>");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("rank")) return false;

        if (!sender.hasPermission("rankplugin.rank")) {
            sender.sendMessage("§cYou do not have permission to use /rank.");
            return true;
        }
        if (args.length != 3 || !args[1].equalsIgnoreCase("the")) {
            sender.sendMessage("§eUsage: /rank <Player_Name> the <Rank_Name>");
            sender.sendMessage("§7Ranks: §fmember, vip, admin, owner");
            return true;
        }

        String targetName = args[0];
        String requestedRank = args[2].toLowerCase(Locale.ROOT);
        if (!RANKS.contains(requestedRank)) {
            sender.sendMessage("§cUnknown rank. §7Use: member, vip, admin, owner");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cPlayer not found: §f" + targetName);
            return true;
        }

        boolean owner = sender.isOp() || sender.hasPermission("rankplugin.owner");
        String actorPrimary = getPrimaryGroup(sender);
        if (!owner && actorPrimary.equalsIgnoreCase("owner")) owner = true;
        boolean admin = !owner && (sender.hasPermission("rankplugin.admin") || actorPrimary.equalsIgnoreCase("admin"));

        if (!owner && !admin) {
            sender.sendMessage("§cOnly Admin or Owner can use /rank.");
            return true;
        }

        if (admin && (requestedRank.equals("admin") || requestedRank.equals("owner"))) {
            sender.sendMessage("§cAdmin cannot assign Admin or Owner ranks.");
            return true;
        }

        // Admin/Owner may not modify an Owner target; Admin may not modify Admin target.
        luckPerms.getUserManager().loadUser(target.getUniqueId()).thenAccept(user -> {
            String targetPrimary = user.getPrimaryGroup();
            if (!owner && targetPrimary.equalsIgnoreCase("owner")) {
                sender.sendMessage("§cYou cannot change the Owner's rank.");
                return;
            }
            if (admin && targetPrimary.equalsIgnoreCase("admin")) {
                sender.sendMessage("§cAdmin cannot change another Admin's rank.");
                return;
            }
            setRank(sender, user, requestedRank);
        }).exceptionally(ex -> {
            Bukkit.getScheduler().runTask(this, () -> sender.sendMessage("§cCould not load that player's LuckPerms data."));
            getLogger().warning("Failed to load user " + target.getUniqueId() + ": " + ex.getMessage());
            return null;
        });
        return true;
    }

    private void setRank(CommandSender sender, User user, String rank) {
        var group = luckPerms.getGroupManager().getGroup(rank);
        if (group == null) {
            sender.sendMessage("§cLuckPerms group does not exist: §f" + rank);
            return;
        }

        // Remove only the four server ranks; other permission nodes/groups are preserved.
        user.data().clear(node -> node instanceof InheritanceNode inheritance
                && RANKS.contains(inheritance.getGroupName().toLowerCase(Locale.ROOT)));
        user.data().add(InheritanceNode.builder(rank).build());

        luckPerms.getUserManager().saveUser(user).thenRun(() ->
                Bukkit.getScheduler().runTask(this, () -> {
                    sender.sendMessage("§aRank changed successfully: §f" + user.getUsername()
                            + " §7→ §f" + rank);
                    if (user.getPlayer() != null) {
                        user.getPlayer().sendMessage("§aYour rank is now §f" + rank + "§a.");
                    }
                })
        ).exceptionally(ex -> {
            Bukkit.getScheduler().runTask(this, () -> sender.sendMessage("§cFailed to save the new rank."));
            getLogger().warning("Failed to save user " + user.getUniqueId() + ": " + ex.getMessage());
            return null;
        });
    }

    private String getPrimaryGroup(CommandSender sender) {
        if (!(sender instanceof org.bukkit.entity.Player player)) return "owner";
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        return user == null ? "default" : user.getPrimaryGroup();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("rank")) return Collections.emptyList();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream().map(p -> p.getName())
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(prefix)).sorted().collect(Collectors.toList());
        }
        if (args.length == 2) return List.of("the");
        if (args.length == 3) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            return RANKS.stream().filter(r -> r.startsWith(prefix)).sorted().collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
