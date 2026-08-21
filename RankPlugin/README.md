# RankPlugin

Paper 1.20.4 + LuckPerms plugin for:

`/rank <Player_Name> the <Rank_Name>`

Ranks: `member`, `vip`, `admin`, `owner`.

- Admin can change Member/VIP ranks.
- Admin cannot assign or modify Admin/Owner.
- Owner can change all four ranks.
- The target must be online.
- The plugin executes LuckPerms `parent set`, so the player's existing parent groups are replaced by the selected rank.

## Install
Copy `RankPlugin-1.0.1.jar` into `plugins/` and restart the server.

Then give permissions with LuckPerms:

`lp group admin permission set rankplugin.use true`
`lp group admin permission set rankplugin.admin true`
`lp group owner permission set rankplugin.use true`
`lp group owner permission set rankplugin.owner true`

Your owner already has `*`, so the explicit owner permissions are optional.
