package de.t0bx.eindino.listener.ingame;

import de.eindino.server.api.ServerAPI;
import de.eindino.server.api.scoreboard.PlayerScore;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.player.PlayerHandler;
import de.t0bx.eindino.team.TeamData;
import de.t0bx.eindino.team.TeamHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Random;

public class PlayerBedBreakListener implements Listener {

    private final TeamHandler teamHandler;
    private final MiniMessage mm;
    private final String prefix;
    private final PlayerHandler playerHandler;

    public PlayerBedBreakListener() {
        this.teamHandler = BedWarsPlugin.getInstance().getTeamHandler();
        this.mm = MiniMessage.miniMessage();
        this.prefix = BedWarsPlugin.getInstance().getPrefix();
        this.playerHandler = BedWarsPlugin.getInstance().getPlayerHandler();
    }

    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!event.getBlock().getType().name().endsWith("_BED")) return;

        TeamData playerTeam = this.teamHandler.getPlayerTeam(player);
        if (playerTeam == null) {
            event.setCancelled(true);
            return;
        }

        Location location = event.getBlock().getLocation();

        if (this.isPlayerTeamBed(playerTeam, location)) {
            event.setCancelled(true);
            player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du kannst dein eigenes Bett nicht zerstören!"));
            return;
        }

        TeamData teamDestroyed = this.getBedFromTeam(location);
        if (teamDestroyed == null) return;
        if (teamDestroyed.isBedDestroyed()) return;

        event.setCancelled(false);
        teamDestroyed.setBedDestroyed(true);
        teamDestroyed.getBedLocation()[0].getBlock().setType(Material.AIR);
        teamDestroyed.getBedLocation()[1].getBlock().setType(Material.AIR);
        this.playerHandler.addBedsDestroyed(player.getUniqueId(), 1);
        int nuggets = new Random().nextInt(10, 20);
        ServerAPI.getInstance().getNuggetManager().addNuggets(player.getUniqueId(), nuggets);

        for (Player players : teamDestroyed.getPlayers()) {
            players.showTitle(Title.title(this.mm.deserialize("<red>Dein Bett wurde zerstört."), this.mm.deserialize("")));
        }

        for (Player players : Bukkit.getOnlinePlayers()) {
            players.playSound(players.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.5f, 1.0f);
            players.sendMessage(this.mm.deserialize(this.prefix + "Das Team " + this.mm.serialize(playerTeam.getDisplayName()) + " <gray>hat das Bett von Team " + this.mm.serialize(teamDestroyed.getDisplayName()) + " <gray>zerstört!"));
        }
        this.updateScoreboard(teamDestroyed);
    }


    private boolean isPlayerTeamBed(TeamData team, Location location) {
        if (team == null || location == null) return false;

        for (Location bedLocation : team.getBedLocation()) {
            if (bedLocation != null &&
                    bedLocation.getWorld().equals(location.getWorld()) &&
                    bedLocation.getBlockX() == location.getBlockX() &&
                    bedLocation.getBlockY() == location.getBlockY() &&
                    bedLocation.getBlockZ() == location.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    private void updateScoreboard(TeamData team) {
        String display = MiniMessage.miniMessage().serialize(team.getDisplayName());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("score")) {
                PlayerScore score = (PlayerScore) player.getMetadata("score").getFirst().value();
                if (score == null) continue;

                if (team.isBedDestroyed()) {
                    if (!team.getPlayersAlive().isEmpty()) {
                        score.setScore("<yellow>❤ " + display + " <gray>» <yellow>" + team.getPlayersAlive().size() + " Spieler Übrig", team.getScore());
                    } else {
                        score.setScore("<gray>❤ " + display, team.getScore());
                    }
                } else {
                    score.setScore("<red>❤ " + display, team.getScore());
                }
            }
        }
    }

    private TeamData getBedFromTeam(Location location) {
        for (TeamData teams : this.teamHandler.getAllTeams()) {
            Location bedLoc1 = teams.getBedLocation()[0];
            Location bedLoc2 = teams.getBedLocation()[1];

            if (bedLoc1.getWorld().equals(location.getWorld()) &&
                    bedLoc1.getBlockX() == location.getBlockX() &&
                    bedLoc1.getBlockY() == location.getBlockY() &&
                    bedLoc1.getBlockZ() == location.getBlockZ()) {
                return teams;
            }

            if (bedLoc2.getWorld().equals(location.getWorld()) &&
                    bedLoc2.getBlockX() == location.getBlockX() &&
                    bedLoc2.getBlockY() == location.getBlockY() &&
                    bedLoc2.getBlockZ() == location.getBlockZ()) {
                return teams;
            }
        }
        return null;
    }

}
