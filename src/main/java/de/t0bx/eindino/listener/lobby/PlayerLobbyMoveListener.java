package de.t0bx.eindino.listener.lobby;

import de.t0bx.eindino.game.GameHandler;
import de.t0bx.eindino.game.GameState;
import de.t0bx.eindino.manager.ParkourManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerLobbyMoveListener implements Listener {

    private final GameHandler gameHandler;
    private final ParkourManager parkourManager;

    public PlayerLobbyMoveListener(GameHandler gameHandler, ParkourManager parkourManager) {
        this.gameHandler = gameHandler;
        this.parkourManager = parkourManager;
    }

    private boolean isLobby() {
        return this.gameHandler.getCurrentGameState() == GameState.LOBBY;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (this.isLobby()) {
            this.parkourManager.onMove(event.getPlayer());
        }
    }
}
