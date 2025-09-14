package de.t0bx.eindino.listener;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.config.SpawnManager;
import de.t0bx.eindino.game.GameHandler;
import de.t0bx.eindino.game.GameState;
import de.t0bx.eindino.player.PlayerHandler;
import de.t0bx.eindino.scoreboard.ScoreboardBuilder;
import de.t0bx.eindino.utils.ItemProvider;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final SpawnManager spawnManager;
    private final PlayerHandler playerHandler;
    private final ScoreboardBuilder scoreboardBuilder;
    private final GameHandler gameHandler;
    private final String prefix;
    private final MiniMessage mm;

    public PlayerJoinListener() {
        this.spawnManager = BedWarsPlugin.getInstance().getSpawnManager();
        this.playerHandler = BedWarsPlugin.getInstance().getPlayerHandler();
        this.scoreboardBuilder = BedWarsPlugin.getInstance().getScoreboardBuilder();
        this.gameHandler = BedWarsPlugin.getInstance().getGameHandler();
        this.prefix = BedWarsPlugin.getInstance().getPrefix();
        this.mm = MiniMessage.miniMessage();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setLevel(0);

        if (!this.playerHandler.isLoaded(player.getUniqueId())) {
            this.playerHandler.doesPlayerExist(player.getUniqueId()).thenAccept(exists -> {
               if (exists) {
                   this.playerHandler.loadPlayer(player.getUniqueId());
               } else {
                   this.playerHandler.createDefaultPlayer(player.getUniqueId());
               }
            });
        }

        if (this.gameHandler.getCurrentGameState() == GameState.LOBBY) {
            this.givePlayerLobbyItems(player);
            Bukkit.getScheduler().runTaskLater(BedWarsPlugin.getInstance(), () -> {
                Location spawn = this.spawnManager.getSpawn();
                if (spawn != null) {
                    player.teleport(spawn);
                }
            }, 5L);

            Bukkit.getScheduler().runTaskLater(BedWarsPlugin.getInstance(), () -> {
                this.scoreboardBuilder.buildLobbyScoreboard(player);
                BedWarsPlugin.getInstance().getTeamHandler().updateScoreboardForAllPlayers();

                if (player.customName() != null) {
                    Bukkit.broadcast(this.mm.deserialize(this.prefix + "Der Spieler " + this.mm.serialize(player.customName()) + " <gray>hat das Spiel betreten."));
                } else {
                    Bukkit.broadcast(this.mm.deserialize(this.prefix + "Der Spieler <green>" + player.customName() + " <gray>hat das Spiel betreten."));
                }
            }, 20L);

            if (this.gameHandler.checkIfAbleToStart()) {
                if (this.gameHandler.getTask() == null) {
                    this.gameHandler.startCountdown();
                }
            } else {
                if (this.gameHandler.getRestPlayersNeeded() == 1) {
                    Bukkit.broadcast(this.mm.deserialize(this.prefix + "Es wird noch <green>1 <gray>weiterer Spieler benötigt damit das Spiel starten kann."));
                } else {
                    Bukkit.broadcast(this.mm.deserialize(this.prefix + "Es werden noch <green>" + this.gameHandler.getRestPlayersNeeded() + " <gray>weitere Spieler benötigt damit das Spiel starten kann."));
                }
            }
        } else if (this.gameHandler.getCurrentGameState() == GameState.IN_GAME) {
            Bukkit.getScheduler().runTaskLater(BedWarsPlugin.getInstance(), () -> {
                BedWarsPlugin.getInstance().getSpectatorHandler().addSpectator(player);
            }, 5L);

            Bukkit.getScheduler().runTaskLater(BedWarsPlugin.getInstance(), () -> {
                this.scoreboardBuilder.buildInGameScoreboard(player);
            }, 20L);
        } else {
            Bukkit.getScheduler().runTaskLater(BedWarsPlugin.getInstance(), () -> {
                Location spawn = this.spawnManager.getSpawn();
                if (spawn != null) {
                    player.teleport(spawn);
                }
            }, 5L);
        }
    }

    private void givePlayerLobbyItems(Player player) {
        player.getInventory().setItem(0, new ItemProvider(Material.RED_BED).setName("<gray>» <red>Teamauswahl").build());
        player.getInventory().setItem(4, new ItemProvider(Material.PAPER).setName("<gray>» <green>Voting").build());
        player.getInventory().setItem(8, new ItemProvider(Material.SLIME_BALL).setName("<gray>» <red>Spiel Verlassen").build());
    }
}
