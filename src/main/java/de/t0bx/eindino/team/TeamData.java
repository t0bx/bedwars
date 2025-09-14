package de.t0bx.eindino.team;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TeamData {

    private final String name;
    private Component displayName;
    private NamedTextColor color;
    private int maxPlayers;
    private final List<Player> players;
    private final List<Player> playersAlive;
    private final Map<String, Object> customData;
    private boolean friendlyFire;
    private int score;
    private Location[] bedLocation;
    private Location spawnLocation;
    private boolean isBedDestroyed;

    public TeamData(String name, Component displayName, NamedTextColor color, int maxPlayers) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
        this.maxPlayers = maxPlayers;
        this.players = new ArrayList<>();
        this.playersAlive = new ArrayList<>();
        this.friendlyFire = false;
        this.score = 0;
        this.customData = new HashMap<>();
        this.setBedDestroyed(true);
    }

    /**
     * Adds a player to this team (only data storage, no scoreboard handling)
     * Scoreboard handling is done in TeamHandler
     * @param player the player to add
     * @return true if successful, false if team is full
     */
    public boolean addPlayer(Player player) {
        if (this.players.size() >= maxPlayers) {
            return false;
        }

        removePlayer(player);

        this.players.add(player);

        player.customName(MiniMessage.miniMessage().deserialize(this.color + player.getName()));

        return true;
    }

    /**
     * Removes a player from this team (only data storage, no scoreboard handling)
     * Scoreboard handling is done in TeamHandler
     * @param player the player to remove
     * @return true if player was in team, false otherwise
     */
    public boolean removePlayer(Player player) {
        if (!this.players.contains(player)) {
            return false;
        }

        players.remove(player);

        player.customName(Component.text(player.getName()));

        return true;
    }

    /**
     * Checks if a player is in this team
     * @param player the player to check
     * @return true if player is in team
     */
    public boolean containsPlayer(Player player) {
        return this.players.contains(player);
    }

    /**
     * Adds score to this team
     * @param score score to add
     */
    public void addScore(int score) {
        this.score += score;
    }

    /**
     * Sets custom data for this team
     * @param key the key
     * @param value the value
     */
    public void setCustomData(String key, Object value) {
        this.customData.put(key, value);
    }

    /**
     * Gets custom data for this team
     * @param key the key
     * @return the value or null if not found
     */
    public Object getCustomData(String key) {
        return this.customData.get(key);
    }

    /**
     * Gets the chat color of this team
     * @return the color
     */
    public NamedTextColor getChatColor() {
        return color;
    }

    /**
     * Sets the chat color of this team and updates all player names
     * @param color the new color
     */
    public void setChatColor(NamedTextColor color) {
        this.color = color;

        for (Player player : players) {
            player.customName(MiniMessage.miniMessage().deserialize(color + player.getName()));
        }
    }

    /**
     * Gets a copy of the players list
     * @return list of players in this team
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Gets the number of players in this team
     * @return player count
     */
    public int getPlayerCount() {
        if (this.players != null) {
            return this.players.size();
        }
        return 0;
    }

    /**
     * Checks if this team is full
     * @return true if team has reached maximum player count
     */
    public boolean isFull() {
        return players.size() >= maxPlayers;
    }
}