package de.t0bx.eindino.listener;

import de.t0bx.eindino.team.TeamHandler;
import net.asyncproxy.nicksystem.event.PlayerUnNickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerUnNickListener implements Listener {

    private final TeamHandler teamHandler;

    public PlayerUnNickListener(TeamHandler teamHandler) {
        this.teamHandler = teamHandler;
    }

    @EventHandler
    public void onPlayerUnNick(PlayerUnNickEvent event) {
        Player player = event.getPlayer();
        this.teamHandler.updatePlayerScoreboard(player);
    }
}
