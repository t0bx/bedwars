package de.t0bx.eindino.scoreboard;

import de.eindino.server.api.scoreboard.PlayerScore;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.player.BedwarsPlayer;
import de.t0bx.eindino.player.PlayerHandler;
import de.t0bx.eindino.team.TeamData;
import de.t0bx.eindino.team.TeamHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class ScoreboardBuilder {

    private final TeamHandler teamHandler;
    private final PlayerHandler playerHandler;

    /**
     * Constructs a ScoreboardBuilder instance and initializes its dependencies.
     *
     * @param teamHandler the TeamHandler object used to manage team-related operations
     */
    public ScoreboardBuilder(TeamHandler teamHandler) {
        this.teamHandler = teamHandler;
        this.playerHandler = BedWarsPlugin.getInstance().getPlayerHandler();
    }

    /**
     * Builds and displays the lobby scoreboard for the specified player.
     * The scoreboard includes detailed statistics such as kills, games played, and win rate.
     * Additionally, it adds aesthetic elements like gradients and a promotional link.
     *
     * @param player the player for whom the lobby scoreboard will be built
     */
    public void buildLobbyScoreboard(Player player) {
        if (player.hasMetadata("score")) {
            PlayerScore score = (PlayerScore) player.getMetadata("score").getFirst().value();
            BedwarsPlayer bedwarsPlayer = this.playerHandler.getBedwarsPlayer(player.getUniqueId());

            score.createSidebar("<gradient:#54fc54:#38ae38><b>einDino</b></gradient>.<gradient:#ffffff:#b0b0b0><b>net</b></gradient> <dark_gray>| <gradient:#00aaaa:#55ffff><b>BedWars</b></gradient>");

            score.setScore("<dark_gray>――――――――――――", 11);
            score.setScore("", 10);
            score.setScore("<green>» <white><b>Kills<gray>:", 9);
            score.setScore("   <green>" + bedwarsPlayer.getKills(), 8);
            score.setScore("", 7);
            score.setScore("<green>» <white><b>Spiele<gray>:", 6);
            score.setScore("   <green>" + bedwarsPlayer.getGamesPlayed(), 5);
            score.setScore("", 4);
            score.setScore("<green>» <white><b>Siegesquote<gray>:", 3);
            score.setScore("   <green>" + this.calculateWinRate(bedwarsPlayer.getWins(), bedwarsPlayer.getGamesPlayed()) + "%", 2);
            score.setScore("", 1);

            String shopText = "shop.einDino.net";
            int totalWidth = 32;
            int padding = (totalWidth - shopText.length()) / 2;
            String centeredShopText = " ".repeat(padding) + "<gradient:#54fc54:#42d742><b>shop.einDino</b></gradient>.<gradient:#ffffff:#b0b0b0><b>net</b></gradient>";

            score.setScore(centeredShopText, 0);
        }
    }

    /**
     * Calculates the win rate percentage based on the number of games won and games played.
     * If no games have been played, the method returns 0 to prevent division by zero.
     *
     * @param gamesWon the number of games won by a player
     * @param gamesPlayed the total number of games played by a player
     * @return the win rate as a percentage (integer value) or 0 if no games were played
     */
    public int calculateWinRate(int gamesWon, int gamesPlayed) {
        if (gamesPlayed == 0) {
            return 0;
        }
        return (gamesWon * 100) / gamesPlayed;
    }

    /**
     * Builds and updates the in-game scoreboard for the specified player.
     * The scoreboard represents current game information including team statuses, map name,
     * gold status, and additional information such as a shop link.
     *
     * @param player the player for whom the in-game scoreboard is being built or updated
     */
    public void buildInGameScoreboard(Player player) {
        if (player.hasMetadata("score")) {
            PlayerScore score = (PlayerScore) player.getMetadata("score").getFirst().value();
            score.createSidebar("<gradient:#54fc54:#38ae38><b>einDino</b></gradient>.<gradient:#ffffff:#b0b0b0><b>net</b></gradient> <dark_gray>| <gradient:#00aaaa:#55ffff><b>BedWars</b></gradient>");

            int index = 4;
            for (TeamData teamData : this.teamHandler.getAllTeams()) {
                teamData.setScore(index);
                if (teamData.isBedDestroyed()) {
                    if (!teamData.getPlayersAlive().isEmpty()) {
                        score.setScore("<yellow>❤ " + MiniMessage.miniMessage().serialize(teamData.getDisplayName()) + " <gray>» <yellow>" + teamData.getPlayersAlive().size() + " Spieler Übrig", index);
                    } else {
                        score.setScore("<gray>❤ " + MiniMessage.miniMessage().serialize(teamData.getDisplayName()), index);
                    }
                } else {
                    score.setScore("<red>❤ " + MiniMessage.miniMessage().serialize(teamData.getDisplayName()), index);
                }
                index++;
            }

            score.setScore("", index + 4);
            score.setScore("<green>» <white><b>Map<gray>:", index + 3);
            score.setScore("   <green>" + BedWarsPlugin.getInstance().getGameHandler().getCurrentMap(), index + 2);
            score.setScore("", index + 1);

            score.setScore("", 3);
            score.setScore("<green>» <white><b>Gold<gray>:", 2);
            score.setScore("    " + (BedWarsPlugin.getInstance().getGameHandler().isGoldActive() ? "<green>✔ <gray>| <green>Aktiviert" : "<red>✘ <gray>| <red>Deaktiviert"), 1);

            String shopText = "shop.einDino.net";
            int totalWidth = 32;
            int padding = (totalWidth - shopText.length()) / 2;
            String centeredShopText = " ".repeat(padding) + "<gradient:#54fc54:#42d742><b>shop.einDino</b></gradient>.<gradient:#ffffff:#b0b0b0><b>net</b></gradient>";

            score.setScore(centeredShopText, 0);
        }
    }

    /**
     * Builds and configures the end-game scoreboard for the specified player.
     *
     * @param player the player for whom the end-game scoreboard is built. This player
     *               must have metadata "score" available to properly set up the scoreboard.
     */
    public void buildEndGameScoreboard(Player player) {
        if (player.hasMetadata("score")) {
            PlayerScore score = (PlayerScore) player.getMetadata("score").getFirst().value();
            score.createSidebar("<gradient:#54fc54:#38ae38><b>einDino</b></gradient>.<gradient:#ffffff:#b0b0b0><b>net</b></gradient> <dark_gray>| <gradient:#00aaaa:#55ffff><b>BedWars</b></gradient>");
            String shopText = "shop.einDino.net";
            int totalWidth = 32;
            int padding = (totalWidth - shopText.length()) / 2;
            String centeredShopText = " ".repeat(padding) + "<gradient:#54fc54:#42d742><b>shop.einDino</b></gradient>.<gradient:#ffffff:#b0b0b0><b>net</b></gradient>";

            score.setScore(centeredShopText, 0);
        }
    }
}
