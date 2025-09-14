package de.t0bx.eindino.player;

import de.eindino.server.api.database.IMySQLManager;
import de.t0bx.eindino.BedWarsPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerHandler {

    private final Map<UUID, BedwarsPlayer> loadedPlayers;
    private final IMySQLManager mySQLManager;

    @Getter
    private final Map<Player, Player> fightMap;

    @Setter
    @Getter
    private boolean statsEnabled;

    /**
     * The PlayerHandler class is responsible for managing player data within the BedWars plugin.
     * It initializes necessary data structures and dependencies for handling player stats and interactions.
     * This includes loading players, managing player statistics, and interacting with the MySQLManager
     * to persist player data.
     *
     * Constructs a new instance of the PlayerHandler class, initializing the internal player data storage
     * and establishing a connection with the MySQLManager for database operations.
     */
    public PlayerHandler() {
        this.loadedPlayers = new ConcurrentHashMap<>();
        this.mySQLManager = BedWarsPlugin.getInstance().getMySQLManager();
        this.fightMap = new ConcurrentHashMap<>();
        this.setStatsEnabled(true);
    }

    /**
     * Loads a player's data from the database and caches it for further use.
     * If the player's data is already cached, this method will do nothing.
     *
     * @param uuid The unique identifier of the player whose data should be loaded.
     */
    public void loadPlayer(UUID uuid) {
        if (this.loadedPlayers.containsKey(uuid)) return;

        String sql = "SELECT * FROM bedwars_players WHERE uuid = ?";
        this.mySQLManager.query(sql, resultSet -> {
            try {
                return this.loadedPlayers.put(uuid, new BedwarsPlayer(
                        uuid,
                        resultSet.getInt("kills"),
                        resultSet.getInt("deaths"),
                        resultSet.getInt("wins"),
                        resultSet.getInt("gamesPlayed"),
                        resultSet.getInt("bedsDestroyed")
                ));
            } catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        }, uuid.toString());
    }

    /**
     * Adds the specified number of kills to the player's total kills count.
     * Updates both the in-memory data for the player and persists the change
     * to the database asynchronously. If the player is not loaded or statistics
     * are disabled, the operation is skipped.
     *
     * @param uuid The unique identifier of the player whose kills are to be incremented.
     * @param kills The number of kills to add to the player's total kills count.
     */
    public void addKills(UUID uuid, int kills) {
        if (!this.loadedPlayers.containsKey(uuid)) return;

        if (!this.statsEnabled) return;
        BedwarsPlayer bedwarsPlayer = this.loadedPlayers.get(uuid);
        bedwarsPlayer.setKills(bedwarsPlayer.getKills() + kills);

        String sql = "UPDATE bedwars_players SET kills = kills + ? WHERE uuid = ?";
        this.mySQLManager.asyncUpdate(sql, kills, uuid.toString());
    }

    /**
     * Adds the specified number of deaths to the player associated with the given UUID.
     * If the player is not loaded or statistics are not enabled, the operation is skipped.
     * The updated death count is also synchronized with the database.
     *
     * @param uuid The UUID of the player whose death count is to be incremented.
     * @param deaths The number of deaths to add to the player's total deaths.
     */
    public void addDeaths(UUID uuid, int deaths) {
        if (!this.loadedPlayers.containsKey(uuid)) return;

        if (!this.statsEnabled) return;
        BedwarsPlayer bedwarsPlayer = this.loadedPlayers.get(uuid);
        bedwarsPlayer.setDeaths(bedwarsPlayer.getDeaths() + deaths);

        String sql = "UPDATE bedwars_players SET deaths = deaths + ? WHERE uuid = ?";
        this.mySQLManager.asyncUpdate(sql, deaths, uuid.toString());
    }

    /**
     * Adds the specified number of wins to the player associated with the given UUID.
     * If the player is not loaded or stats are not enabled, the operation is skipped.
     * The player's updated win count is also synchronized with the database.
     *
     * @param uuid The UUID of the player whose wins are to be incremented.
     * @param wins The number of wins to add to the player's total wins.
     */
    public void addWins(UUID uuid, int wins) {
        if (!this.loadedPlayers.containsKey(uuid)) return;

        if (!this.statsEnabled) return;
        BedwarsPlayer bedwarsPlayer = this.loadedPlayers.get(uuid);
        bedwarsPlayer.setWins(bedwarsPlayer.getWins() + wins);

        String sql = "UPDATE bedwars_players SET wins = wins + ? WHERE uuid = ?";
        this.mySQLManager.asyncUpdate(sql, wins, uuid.toString());
    }

    /**
     * Updates the number of games played for a specific player identified by their UUID.
     * Adds the specified number of games to the player's current record in both the in-memory data structure
     * and the database if the player data is loaded and statistics are enabled.
     *
     * @param uuid the unique identifier of the player whose games played are being updated
     * @param gamesPlayed the number of games to add to the player's record
     */
    public void addGamesPlayed(UUID uuid, int gamesPlayed) {
        if (!this.loadedPlayers.containsKey(uuid)) return;

        if (!this.statsEnabled) return;
        BedwarsPlayer bedwarsPlayer = this.loadedPlayers.get(uuid);
        bedwarsPlayer.setGamesPlayed(bedwarsPlayer.getGamesPlayed() + gamesPlayed);

        String sql = "UPDATE bedwars_players SET gamesPlayed = gamesPlayed + ? WHERE uuid = ?";
        this.mySQLManager.asyncUpdate(sql, gamesPlayed, uuid.toString());
    }

    /**
     * Updates the number of beds destroyed for a specific player and persists the change to the database.
     * If the player is not loaded or statistics are disabled, the method does nothing.
     *
     * @param uuid The unique identifier of the player whose beds destroyed count is being updated.
     * @param bedsDestroyed The number of additional beds destroyed to be added to the player's current count.
     */
    public void addBedsDestroyed(UUID uuid, int bedsDestroyed) {
        if (!this.loadedPlayers.containsKey(uuid)) return;

        if (!this.statsEnabled) return;
        BedwarsPlayer bedwarsPlayer = this.loadedPlayers.get(uuid);
        bedwarsPlayer.setBedsDestroyed(bedwarsPlayer.getBedsDestroyed() + bedsDestroyed);

        String sql = "UPDATE bedwars_players SET bedsDestroyed = bedsDestroyed + ? WHERE uuid = ?";
        this.mySQLManager.asyncUpdate(sql, bedsDestroyed, uuid.toString());
    }

    /**
     * Retrieves the placement of a player in the leaderboard based on their total wins
     * compared to other players' win counts.
     *
     * @param uuid the unique identifier (UUID) of the player whose placement is being retrieved
     * @return a CompletableFuture that, when completed, supplies the player's placement as an integer;
     *         returns -1 if the player's data could not be found or an error occurs
     */
    public CompletableFuture<Integer> getPlacement(UUID uuid) {
        String sql = "SELECT COUNT(*) + 1 AS placement FROM bedwars_players WHERE wins > (SELECT wins FROM bedwars_players WHERE uuid = ?)";

        return this.mySQLManager.queryAsync(sql, resultSet -> {
            try {
                return resultSet.getInt("placement");
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }, uuid.toString()).thenApply(list -> list.isEmpty() ? -1 : list.getFirst());
    }

    /**
     * Checks asynchronously whether a player with the given UUID exists in the database.
     *
     * @param uuid the UUID of the player to check for existence
     * @return a CompletableFuture that resolves to {@code true} if the player exists in the database,
     *         otherwise {@code false}
     */
    public CompletableFuture<Boolean> doesPlayerExist(UUID uuid) {
        final String query = "SELECT uuid FROM bedwars_players WHERE uuid = ?";

        return this.mySQLManager.queryAsync(query, resultSet -> {
            try {
                return resultSet.getString("uuid");
            } catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        },uuid.toString()).thenApply(result -> {
            if (!result.isEmpty()) {
                String dbUUID = result.getFirst();
                return dbUUID.equals(uuid.toString());
            }
            return false;
        });
    }

    /**
     * Checks if a player with the given UUID is currently loaded in the system.
     *
     * @param uuid the unique identifier of the player to check
     * @return true if the player is loaded, otherwise false
     */
    public boolean isLoaded(UUID uuid) {
        return this.loadedPlayers.containsKey(uuid);
    }

    /**
     * Retrieves the BedwarsPlayer object associated with the provided UUID.
     *
     * @param uuid the UUID of the player whose BedwarsPlayer object is to be retrieved
     * @return the BedwarsPlayer object associated with the given UUID, or null if the player is not loaded
     */
    public BedwarsPlayer getBedwarsPlayer(UUID uuid) {
        return this.loadedPlayers.get(uuid);
    }

    /**
     * Creates a default Bedwars player entry in memory and the database.
     * The player is initialized with all statistical values set to 0.
     * This method is intended to be used for players who do not yet exist in the database.
     *
     * @param uuid the unique identifier of the player to create a default profile for
     */
    public void createDefaultPlayer(UUID uuid) {
        this.loadedPlayers.put(uuid, new BedwarsPlayer(uuid, 0, 0, 0, 0, 0));

        String sql = "INSERT INTO bedwars_players(uuid) VALUES (?)";
        this.mySQLManager.asyncUpdate(sql, uuid.toString());
    }

    /**
     * Asynchronously retrieves the top 5 Bedwars players based on the number of wins.
     * The players are retrieved from the database and returned as a list of {@code BedwarsPlayer} objects.
     * Players with data that cannot be parsed are excluded from the result.
     *
     * @return a {@code CompletableFuture} containing a {@code List} of the top 5 {@code BedwarsPlayer} objects,
     *         sorted in descending order of wins.
     */
    public CompletableFuture<List<BedwarsPlayer>> getTop5() {
        String sql = "SELECT * FROM bedwars_players ORDER by wins DESC LIMIT 5";

        return this.mySQLManager.queryAsync(sql, resultSet -> {
           try {
               UUID uuid = UUID.fromString(resultSet.getString("uuid"));
               int kills = resultSet.getInt("kills");
               int deaths = resultSet.getInt("deaths");
               int wins = resultSet.getInt("wins");
               int gamesPlayed = resultSet.getInt("gamesPlayed");
               int bedsDestroyed = resultSet.getInt("bedsDestroyed");

               return new BedwarsPlayer(
                               uuid,
                               kills,
                               deaths,
                               wins,
                               gamesPlayed,
                               bedsDestroyed
                       );
           } catch (SQLException exception) {
               exception.printStackTrace();
               return null;
           }
        }).thenApply(list -> list.stream().filter(Objects::nonNull).toList());
    }
}
