package de.t0bx.eindino.spectator;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.game.GameHandler;
import de.t0bx.eindino.map.MapHandler;
import de.t0bx.eindino.team.TeamHandler;
import de.t0bx.eindino.utils.ItemProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpectatorHandler {

    private final List<UUID> spectators;
    private final GameHandler gameHandler;
    private final MapHandler mapHandler;
    private final TeamHandler teamHandler;

    /**
     * Constructs a new instance of the SpectatorHandler class.
     * This class is responsible for managing game spectators in the BedWars plugin.
     * It initializes the required handlers and a thread-safe list to track spectators.
     */
    public SpectatorHandler() {
        this.spectators = new CopyOnWriteArrayList<>();
        this.gameHandler = BedWarsPlugin.getInstance().getGameHandler();
        this.mapHandler = BedWarsPlugin.getInstance().getMapHandler();
        this.teamHandler = BedWarsPlugin.getInstance().getTeamHandler();
    }

    /**
     * Checks whether the specified player is a spectator.
     *
     * @param player the player to check
     * @return true if the player is a spectator, false otherwise
     */
    public boolean isSpectator(Player player) {
        return this.spectators.contains(player.getUniqueId());
    }

    /**
     * Adds a player to the spectators list and configures their settings for spectator mode.
     * The player's inventory is cleared, they are assigned to the spectator team, teleported
     * to the spectator location of the current map, and given the ability to fly. Additionally,
     * the player is hidden from all other players and vice versa.
     *
     * @param player the player to be added as a spectator
     */
    public void addSpectator(Player player) {
        player.getInventory().clear();
        this.spectators.add(player.getUniqueId());

        if (this.teamHandler.getPlayerTeam(player) != null) {
            this.teamHandler.removePlayerFromTeam(player);
        }

        this.teamHandler.addPlayerToTeam("999spectator", player);
        this.teamHandler.updateScoreboardForAllPlayers();

        player.teleport(this.mapHandler.getMap(this.gameHandler.getCurrentMap()).getSpectatorLocation());

        player.getInventory().setItem(0, new ItemProvider(Material.COMPASS).setName("<gray>» <red>Navigator").build());
        player.setAllowFlight(true);
        player.setFlying(true);

        for (Player players : Bukkit.getOnlinePlayers()) {
            players.hidePlayer(BedWarsPlugin.getInstance(), player);
        }

        for (UUID uuids : this.spectators) {
            Player players = Bukkit.getPlayer(uuids);
            if (players == null) continue;

            player.hidePlayer(BedWarsPlugin.getInstance(), players);
        }
    }

    /**
     * Removes a player from the list of spectators and resets their game state
     * (such as flight ability and visibility to other players).
     *
     * @param player The player to be removed from the spectator list.
     */
    public void removeSpectator(Player player) {
        player.getInventory().clear();
        player.setAllowFlight(false);
        player.setFlying(false);
        this.spectators.remove(player.getUniqueId());

        for (Player players : Bukkit.getOnlinePlayers()) {
            players.showPlayer(BedWarsPlugin.getInstance(), player);
        }
    }

    /**
     * Removes all spectators currently managed by this handler.
     *
     * Iterates through the list of spectators and performs the following steps:
     * 1. Adds each spectator's unique identifier (UUID) to the `wasInSpectator` list in the GameHandler.
     * 2. Attempts to retrieve the Player object associated with each UUID. If the Player object is null
     *    (e.g., the player is offline), it skips to the next spectator.
     * 3. For players that are found, calls the `removeSpectator` method to perform cleanup and remove them
     *    from spectator mode.
     */
    public void removeAll() {
        for (UUID uuid : this.spectators) {
            this.gameHandler.getWasInSpectator().add(uuid);
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            this.removeSpectator(player);
        }
    }
}
