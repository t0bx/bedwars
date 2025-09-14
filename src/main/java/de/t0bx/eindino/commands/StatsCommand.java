package de.t0bx.eindino.commands;

import de.eindino.server.api.command.AbstractCommandBase;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.player.BedwarsPlayer;
import de.t0bx.eindino.player.PlayerHandler;
import de.t0bx.eindino.utils.UUIDFetcher;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StatsCommand extends AbstractCommandBase {

    private final PlayerHandler playerHandler;
    private final MiniMessage mm;
    private final String prefix;

    public StatsCommand(JavaPlugin plugin, String commandName) {
        super(plugin, commandName);
        this.playerHandler = BedWarsPlugin.getInstance().getPlayerHandler();
        this.mm = MiniMessage.miniMessage();
        this.prefix = BedWarsPlugin.getInstance().getPrefix();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.mm.deserialize(this.prefix + "Only players can execute this command!"));
            return true;
        }

        if (args.length == 0) {
            this.playerHandler.getPlacement(player.getUniqueId()).thenAccept(placement -> {
                BedwarsPlayer bedwarsPlayer = this.playerHandler.getBedwarsPlayer(player.getUniqueId());
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    player.sendMessage(this.mm.deserialize(this.prefix + " "));
                    player.sendMessage(this.mm.deserialize(this.prefix + "Deine Platzierung » <green>#" + placement));
                    player.sendMessage(this.mm.deserialize(this.prefix + " "));
                    player.sendMessage(this.mm.deserialize(this.prefix + "Kills » <green>" + bedwarsPlayer.getKills()));
                    player.sendMessage(this.mm.deserialize(this.prefix + "Tode » <green>" + bedwarsPlayer.getDeaths()));
                    player.sendMessage(this.mm.deserialize(this.prefix + "K/D » <green>" + this.getRoundedKD(bedwarsPlayer.getKills(), bedwarsPlayer.getDeaths())));
                    player.sendMessage(this.mm.deserialize(this.prefix + "Gespielte Spiele » <green>" + bedwarsPlayer.getGamesPlayed()));
                    player.sendMessage(this.mm.deserialize(this.prefix + "Gewonnene Spiele » <green>" + bedwarsPlayer.getWins()));
                    player.sendMessage(this.mm.deserialize(this.prefix + "Sieges Quote » <green>" + this.calculateWinRate(bedwarsPlayer.getWins(), bedwarsPlayer.getGamesPlayed()) + "%"));
                    player.sendMessage(this.mm.deserialize(this.prefix + "Zerstörte Betten » <green>" + bedwarsPlayer.getBedsDestroyed()));
                    player.sendMessage(this.mm.deserialize(this.prefix + " "));
                });
            });
        } else {
            if (args.length != 1) {
                player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/stats <Spielername>"));
                return true;
            }

            String playerName = args[0];
            this.fetchAndSendStats(player, playerName);
        }
        return false;
    }

    public void fetchAndSendStats(Player player, String playerName) {
        UUIDFetcher.getUUIDAsync(playerName)
                .thenCompose(uuid -> {
                    if (uuid == null) {
                        sendPlayerNotFoundMessage(player, playerName);
                        return CompletableFuture.completedFuture(null);
                    }

                    return playerHandler.doesPlayerExist(uuid)
                            .thenApply(exists -> exists ? uuid : null);
                })
                .thenCompose(uuid -> {
                    if (uuid == null) return CompletableFuture.completedFuture(null);

                    if (!playerHandler.isLoaded(uuid)) {
                        playerHandler.loadPlayer(uuid);
                    }

                    BedwarsPlayer bedwarsPlayer = playerHandler.getBedwarsPlayer(uuid);
                    return playerHandler.getPlacement(uuid)
                            .thenApply(placement -> new PlayerStatsResult(uuid, bedwarsPlayer, placement));
                })
                .thenAccept(result -> {
                    if (result == null) return;

                    Bukkit.getScheduler().runTask(plugin, () -> sendPlayerStats(player, playerName, result));
                })
                .exceptionally(ex -> {
                    sendPlayerNotFoundMessage(player, playerName);
                    return null;
                });
    }

    private void sendPlayerNotFoundMessage(Player player, String playerName) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage(mm.deserialize(prefix + "<red>Der Spieler " + playerName + " existiert nicht!"));
        });
    }

    private void sendPlayerStats(Player player, String playerName, PlayerStatsResult result) {
        BedwarsPlayer bedwarsPlayer = result.bedwarsPlayer();
        int placement = result.placement();

        player.sendMessage(mm.deserialize(prefix + "Statistiken von » <green>" + playerName));
        player.sendMessage(mm.deserialize(prefix + " "));
        player.sendMessage(mm.deserialize(prefix + "Platzierung » <green>#" + placement));
        player.sendMessage(mm.deserialize(prefix + " "));
        player.sendMessage(mm.deserialize(prefix + "Kills » <green>" + bedwarsPlayer.getKills()));
        player.sendMessage(mm.deserialize(prefix + "Tode » <green>" + bedwarsPlayer.getDeaths()));
        player.sendMessage(mm.deserialize(prefix + "K/D » <green>" + getRoundedKD(bedwarsPlayer.getKills(), bedwarsPlayer.getDeaths())));
        player.sendMessage(mm.deserialize(prefix + "Gespielte Spiele » <green>" + bedwarsPlayer.getGamesPlayed()));
        player.sendMessage(mm.deserialize(prefix + "Gewonnene Spiele » <green>" + bedwarsPlayer.getWins()));
        player.sendMessage(mm.deserialize(prefix + "Sieges Quote » <green>" + calculateWinRate(bedwarsPlayer.getWins(), bedwarsPlayer.getGamesPlayed()) + "%"));
        player.sendMessage(mm.deserialize(prefix + "Zerstörte Betten » <green>" + bedwarsPlayer.getBedsDestroyed()));
        player.sendMessage(mm.deserialize(prefix + " "));
    }

    private record PlayerStatsResult(UUID uuid, BedwarsPlayer bedwarsPlayer, int placement) {}

    public double getRoundedKD(int kills, int deaths) {
        if (deaths == 0) return kills;
        double kd = (double) kills / deaths;
        return Math.round(kd * 100.0) / 100.0;
    }

    public int calculateWinRate(int gamesWon, int gamesPlayed) {
        if (gamesPlayed == 0) {
            return 0;
        }
        return (gamesWon * 100) / gamesPlayed;
    }
}
