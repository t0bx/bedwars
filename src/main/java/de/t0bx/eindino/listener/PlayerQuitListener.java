package de.t0bx.eindino.listener;

import de.eindino.server.api.scoreboard.PlayerScore;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.game.GameHandler;
import de.t0bx.eindino.game.GameState;
import de.t0bx.eindino.team.TeamData;
import de.t0bx.eindino.team.TeamHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final GameHandler gameHandler;
    private final String prefix;
    private final MiniMessage mm;
    private final TeamHandler teamHandler;

    public PlayerQuitListener() {
        this.gameHandler = BedWarsPlugin.getInstance().getGameHandler();
        this.prefix = BedWarsPlugin.getInstance().getPrefix();
        this.mm = MiniMessage.miniMessage();
        this.teamHandler = BedWarsPlugin.getInstance().getTeamHandler();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
        Player player = event.getPlayer();
        player.setLevel(0);
        if (BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) {
            BedWarsPlugin.getInstance().getSpectatorHandler().removeSpectator(player);
            return;
        }

        if (this.gameHandler.getCurrentGameState() == GameState.LOBBY) {
            TeamData team = this.teamHandler.getPlayerTeam(player);
            if (team != null) {
                Bukkit.broadcast(this.mm.deserialize(this.prefix + "Der Spieler " + this.teamHandler.extractFirstTag(team.getDisplayName()) + player.getName() + " <gray>hat das Spiel verlassen."));
                this.teamHandler.removePlayerFromTeam(player);
                return;
            }

            Bukkit.broadcast(this.mm.deserialize(this.prefix + "Der Spieler <green>" + player.getName() + " <gray>hat das Spiel verlassen."));
            BedWarsPlugin.getInstance().getParkourManager().removePlayerFromParkour(player, true);
            return;
        }

        if (this.gameHandler.getCurrentGameState() == GameState.IN_GAME) {
            TeamData teamData = this.teamHandler.getPlayerTeam(player);
            Bukkit.broadcast(this.mm.deserialize(this.prefix + "Der Spieler " + this.teamHandler.extractFirstTag(teamData.getDisplayName()) + player.getName() + " <gray>ist ausgeschieden."));

            teamData.getPlayersAlive().remove(player);
            if (teamData.getPlayersAlive().isEmpty()) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    players.sendMessage(this.mm.deserialize(this.prefix + "Das Team " + this.mm.serialize(teamData.getDisplayName()) + " <gray>ist <dark_red><u>ausgeschieden."));
                }

                if (this.teamHandler.getRemainingTeams().size() == 1) {
                    TeamData winner = this.teamHandler.getRemainingTeams().getFirst();
                    this.gameHandler.endGame(winner);
                }
            }
            BedWarsPlugin.getInstance().getTeamHandler().removePlayerFromTeam(player);
            this.updateScoreboard(teamData);
        }
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

}
