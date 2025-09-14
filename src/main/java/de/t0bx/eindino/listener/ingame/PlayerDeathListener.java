package de.t0bx.eindino.listener.ingame;

import de.eindino.server.api.scoreboard.PlayerScore;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.game.GameHandler;
import de.t0bx.eindino.game.GameState;
import de.t0bx.eindino.player.PlayerHandler;
import de.t0bx.eindino.team.TeamData;
import de.t0bx.eindino.team.TeamHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final GameHandler gameHandler;
    private final TeamHandler teamHandler;
    private final MiniMessage mm;
    private final String prefix;
    private final PlayerHandler playerHandler;

    public PlayerDeathListener() {
        this.playerHandler = BedWarsPlugin.getInstance().getPlayerHandler();
        this.gameHandler = BedWarsPlugin.getInstance().getGameHandler();
        this.teamHandler = BedWarsPlugin.getInstance().getTeamHandler();
        this.mm = MiniMessage.miniMessage();
        this.prefix = BedWarsPlugin.getInstance().getPrefix();
    }

    public boolean isGame() {
        return this.gameHandler.getCurrentGameState() == GameState.IN_GAME;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (isGame()) {
            Player player = event.getPlayer();
            event.getDrops().clear();
            TeamData teamData = this.teamHandler.getPlayerTeam(player);
            if (teamData.isBedDestroyed()) {
                if (this.playerHandler.getFightMap().containsKey(player)) {
                    Player attacker = this.playerHandler.getFightMap().remove(player);
                    this.playerHandler.addKills(attacker.getUniqueId(), 1);
                    this.playerHandler.addDeaths(player.getUniqueId(), 1);
                }
                teamData.getPlayersAlive().remove(player);
                Bukkit.getScheduler().runTaskLater(BedWarsPlugin.getInstance(), () -> {
                    player.spigot().respawn();

                    if (!BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) {
                        BedWarsPlugin.getInstance().getSpectatorHandler().addSpectator(player);
                    }
                }, 1L);
                event.deathMessage(this.mm.deserialize(this.prefix + "Der Spieler " + this.extractColorFromComponent(teamData.getDisplayName()) + player.getName() + " <gray>ist ausgeschieden."));
                if (teamData.getPlayersAlive().isEmpty()) {
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        players.sendMessage(this.mm.deserialize(this.prefix + "Das Team " + this.mm.serialize(teamData.getDisplayName()) + " <gray>ist <dark_red><u>ausgeschieden."));
                    }

                    Bukkit.getScheduler().runTaskLater(BedWarsPlugin.getInstance(), () -> {
                        player.spigot().respawn();

                        if (this.teamHandler.getRemainingTeams().size() == 1) {
                            TeamData winner = this.teamHandler.getRemainingTeams().getFirst();
                            this.gameHandler.endGame(winner);
                        } else {
                            if (!BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) {
                                BedWarsPlugin.getInstance().getSpectatorHandler().addSpectator(player);
                            }
                        }
                    }, 1L);
                }
                this.updateScoreboard(teamData);
            } else {
                if (this.playerHandler.getFightMap().containsKey(player)) {
                    Player attacker = this.playerHandler.getFightMap().remove(player);
                    TeamData attackerTeam = this.teamHandler.getPlayerTeam(attacker);
                    this.playerHandler.addKills(attacker.getUniqueId(), 1);
                    this.playerHandler.addDeaths(player.getUniqueId(), 1);
                    if (attackerTeam == null) return;
                    event.deathMessage(this.mm.deserialize(this.prefix + "Der Spieler <green>" + this.extractColorFromComponent(teamData.getDisplayName()) + player.getName() + " <gray>wurde von <green>" + this.extractColorFromComponent(attackerTeam.getDisplayName()) + attacker.getName() + " <gray>getötet."));
                } else {
                    event.deathMessage(this.mm.deserialize(this.prefix + "Der Spieler <green>" + this.extractColorFromComponent(teamData.getDisplayName()) + player.getName() + " <gray>ist gestorben."));
                    this.playerHandler.addDeaths(player.getUniqueId(), 1);
                }
            }

            if (!teamData.isBedDestroyed()) {
                Bukkit.getScheduler().runTaskLater(BedWarsPlugin.getInstance(), () -> {
                    player.spigot().respawn();
                    player.teleport(this.teamHandler.getPlayerTeam(player).getSpawnLocation());
                }, 1L);
            }
        }
    }

    private String extractColorFromComponent(Component component) {
        if (component == null) return null;

        String text = MiniMessage.miniMessage().serialize(component);
        int start = text.indexOf('<');
        int end = text.indexOf('>', start + 1);

        if (start != -1 && end != -1) {
            return text.substring(start, end + 1);
        }
        return null;
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
