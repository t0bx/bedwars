package de.t0bx.eindino.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class TeamHandler {

    private final Map<String, TeamData> teams;

    /**
     * Initializes a new instance of the TeamHandler class.
     * This constructor sets up an empty collection to manage teams.
     */
    public TeamHandler() {
        this.teams = new HashMap<>();
    }

    /**
     * Creates a new team with the specified properties and adds it to the internal collection.
     * If the provided team name exceeds 16 characters, it is truncated to the first 16.
     *
     * @param name the unique name identifier for the team
     * @param displayName the display name of the team as a Component
     * @param color the color associated with the team
     * @param maxPlayers the maximum number of players allowed in the team
     * @return a new instance of TeamData representing the created team
     */
    public TeamData createTeam(String name, Component displayName, NamedTextColor color, int maxPlayers) {
        String teamId = name.length() > 16 ? name.substring(0, 16) : name;

        TeamData team = new TeamData(name, displayName, color, maxPlayers);
        teams.put(teamId, team);

        return team;
    }

    /**
     * Adds a player to the specified team and updates relevant scoreboards.
     * If the team does not exist or the addition of the player fails, the method will return false.
     * If the player is already in a different team, they will be removed from that team before being added to the new one.
     *
     * @param teamName the name of the team to which the player should be added
     * @param player the player to add to the team
     * @return true if the player was successfully added to the team, false otherwise
     */
    public boolean addPlayerToTeam(String teamName, Player player) {
        TeamData teamData = this.teams.get(teamName);

        if (teamData == null) {
            return false;
        }

        removePlayerFromTeam(player);

        boolean success = teamData.addPlayer(player);
        if (!success) {
            return false;
        }

        updateScoreboardsForTeam(teamName);

        return true;
    }

    /**
     * Updates the scoreboards for all online players by synchronizing with the team data
     * associated with the specified team name.
     *
     * @param teamName the name of the team whose scoreboard entries should be updated.
     *                 If the team doesn't exist, no updates will be made.
     */
    private void updateScoreboardsForTeam(String teamName) {
        TeamData teamData = this.teams.get(teamName);
        if (teamData == null) return;

        String teamId = teamName.length() > 16 ? teamName.substring(0, 16) : teamName;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Scoreboard playerScoreboard = onlinePlayer.getScoreboard();

            Team scoreboardTeam = playerScoreboard.getTeam(teamId);

            if (scoreboardTeam != null) {
                Set<String> currentEntries = new HashSet<>(scoreboardTeam.getEntries());
                for (String entry : currentEntries) {
                    scoreboardTeam.removeEntry(entry);
                }

                for (Player teamPlayer : teamData.getPlayers()) {
                    scoreboardTeam.addEntry(teamPlayer.getName());
                }
            } else {
                scoreboardTeam = playerScoreboard.registerNewTeam(teamId);
                scoreboardTeam.displayName(teamData.getDisplayName());
                scoreboardTeam.color(teamData.getColor());
                scoreboardTeam.setAllowFriendlyFire(false);
                scoreboardTeam.setCanSeeFriendlyInvisibles(true);
                scoreboardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

                String colorTag = extractFirstTag(teamData.getDisplayName());
                if (colorTag == null) colorTag = "";

                String plainTeamName = MiniMessage.miniMessage().serialize(teamData.getDisplayName());

                Component prefix;
                if (!teamName.equalsIgnoreCase("999spectator")) {
                    prefix = MiniMessage.miniMessage().deserialize(colorTag + plainTeamName + " <gray>| " + colorTag);
                } else {
                    prefix = MiniMessage.miniMessage().deserialize(colorTag);
                }

                scoreboardTeam.prefix(prefix);

                for (Player teamPlayer : teamData.getPlayers()) {
                    scoreboardTeam.addEntry(teamPlayer.getName());
                }
            }
        }
    }

    /**
     * Extracts the first tag (enclosed in angle brackets) from the text representation
     * of the provided {@link Component}.
     *
     * @param component the component from which the tag will be extracted; if null, the method will return null
     * @return a string representing the first tag found in the serialized component text,
     * or null if no tags are found or the input component is null
     */
    public String extractFirstTag(Component component) {
        if (component == null) return null;

        String text = MiniMessage.miniMessage().serialize(component);

        int start = text.indexOf('<');
        int end = text.indexOf('>', start + 1);

        if (start != -1 && end != -1) {
            return text.substring(start, end + 1);
        }

        return null;
    }


    /**
     * Removes a player from their current team and updates the scoreboard accordingly.
     * If the player is found in a team, they will be removed from both the team
     * and the related scoreboard. If a team becomes empty after removal, the team
     * is unregistered from the scoreboard.
     *
     * @param player the player to remove from the team
     * @return true if the player was successfully removed from a team, false otherwise
     */
    public boolean removePlayerFromTeam(Player player) {
        for (TeamData teamData : this.teams.values()) {
            if (teamData.containsPlayer(player)) {
                teamData.removePlayer(player);

                String teamId = teamData.getName().length() > 16 ?
                        teamData.getName().substring(0, 16) : teamData.getName();

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    Scoreboard playerScoreboard = onlinePlayer.getScoreboard();
                    Team scoreboardTeam = playerScoreboard.getTeam(teamId);
                    if (scoreboardTeam != null) {
                        scoreboardTeam.removeEntry(player.getName());

                        if (scoreboardTeam.getEntries().isEmpty()) {
                            scoreboardTeam.unregister();
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the team that the specified player belongs to, if any.
     *
     * @param player the player whose team is to be retrieved
     * @return the team the player belongs to, or null if the player is not in any team
     */
    public TeamData getPlayerTeam(Player player) {
        for (TeamData team : this.teams.values()) {
            if (team.containsPlayer(player)) {
                return team;
            }
        }
        return null;
    }

    /**
     * Retrieves the TeamData object associated with the given team name.
     *
     * @param teamName the name of the team to retrieve
     * @return the TeamData object for the specified team name, or null if the team does not exist
     */
    public TeamData getTeam(String teamName) {
        return this.teams.get(teamName);
    }

    /**
     * Retrieves a list of all teams currently managed by this handler, excluding the "999spectator" team.
     *
     * @return a list of {@code TeamData} objects representing the teams, excluding the spectator team.
     */
    public List<TeamData> getAllTeams() {
        return this.teams.values().stream().filter(t -> !t.getName().equalsIgnoreCase("999spectator")).toList();
    }

    /**
     * Retrieves a list of teams that still have at least one alive player.
     *
     * @return a list of remaining teams, where each team has non-empty playersAlive.
     */
    public List<TeamData> getRemainingTeams() {
        return this.teams.values().stream().filter(t -> !t.getPlayersAlive().isEmpty()).toList();
    }

    /**
     * Distributes a list of players across all available teams.
     * First clears each player from all teams, then assigns players
     * to teams in a shuffled order while respecting team capacity limits.
     * Updates the scoreboard for all players after redistribution.
     *
     * @param players the list of players to be distributed across teams
     */
    public void distributePlayers(List<Player> players) {
        for (Player player : players) {
            removePlayerFromAllTeamsQuietly(player);
        }

        List<TeamData> teamList = getAllTeams();
        if (teamList.isEmpty()) {
            return;
        }

        List<Player> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);

        int teamIndex = 0;
        for (Player player : shuffledPlayers) {
            while (teamIndex < teamList.size()) {
                TeamData team = teamList.get(teamIndex);
                if (team.addPlayer(player)) {
                    break;
                }
                teamIndex++;
            }

            if (teamIndex >= teamList.size()) {
                teamIndex = 0;
            }
        }

        updateScoreboardForAllPlayers();
    }

    /**
     * Updates the player's scoreboard by reassigning them to their current team.
     * If the player is part of a team, they will be removed and re-added to the team,
     * ensuring the scoreboard is updated accordingly.
     *
     * @param player the player whose scoreboard needs to be updated
     */
    public void updatePlayerScoreboard(Player player) {
        TeamData playerTeam = getPlayerTeam(player);
        if (playerTeam != null) {
            String teamName = playerTeam.getName();
            playerTeam.removePlayer(player);
            addPlayerToTeam(teamName, player);
        }
    }

    /**
     * Updates the scoreboards for all players in the game.
     * <p>
     * This method iterates through all registered teams in the `teams` map and updates the scoreboards
     * for each team using the `updateScoreboardsForTeam` method. It ensures that the current state of
     * all teams is properly reflected in the players' scoreboards.
     * <p>
     * The `updateScoreboardsForTeam` method handles the specifics of updating each team's scoreboard,
     * including setting entries, team attributes, and formatting.
     */
    public void updateScoreboardForAllPlayers() {
        for (String teamName : this.teams.keySet()) {
            updateScoreboardsForTeam(teamName);
        }
    }

    /**
     * Deletes a team by its name. First, it removes all players from the team,
     * then removes the team from the internal team storage.
     *
     * @param teamName the name of the team to be deleted
     * @return true if the team was successfully deleted, false if the team
     *         with the given name does not exist
     */
    public boolean deleteTeam(String teamName) {
        TeamData team = this.teams.get(teamName);
        if (team == null) {
            return false;
        }

        for (Player player : new ArrayList<>(team.getPlayers())) {
            removePlayerFromTeam(player);
        }

        teams.remove(teamName);
        return true;
    }

    /**
     * Deletes all teams managed by this instance.
     *
     * This method iterates through all the teams currently stored and calls
     * the {@link #deleteTeam(String)} method for each team, ensuring all associated
     * players and resources are properly removed.
     *
     * It utilizes a temporary list to avoid modification issues while iterating over
     * the team's keys.
     */
    public void deleteAllTeams() {
        for (String teamName : new ArrayList<>(this.teams.keySet())) {
            deleteTeam(teamName);
        }
    }

    /**
     * Removes the specified player from all teams without any additional processing or
     * scoreboard handling.
     *
     * @param player the player to remove from all teams
     */
    private void removePlayerFromAllTeamsQuietly(Player player) {
        for (TeamData teamData : this.teams.values()) {
            teamData.removePlayer(player);
        }
    }
}