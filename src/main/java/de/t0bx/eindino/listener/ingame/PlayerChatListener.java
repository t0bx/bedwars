package de.t0bx.eindino.listener.ingame;

import de.eindino.server.api.ServerAPI;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.game.GameHandler;
import de.t0bx.eindino.game.GameState;
import de.t0bx.eindino.spectator.SpectatorHandler;
import de.t0bx.eindino.team.TeamData;
import de.t0bx.eindino.team.TeamHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerChatListener implements Listener {

    private final GameHandler gameHandler;
    private final TeamHandler teamHandler;
    private final SpectatorHandler spectatorHandler;
    private final List<UUID> wroteGG;

    public PlayerChatListener() {
        this.gameHandler = BedWarsPlugin.getInstance().getGameHandler();
        this.teamHandler = BedWarsPlugin.getInstance().getTeamHandler();
        this.spectatorHandler = BedWarsPlugin.getInstance().getSpectatorHandler();
        this.wroteGG = new ArrayList<>();
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (this.gameHandler.getCurrentGameState() == GameState.IN_GAME) {
            if (this.spectatorHandler.isSpectator(player)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(BedWarsPlugin.getInstance().getPrefix() + "<red>Du kannst als Zuschauer nicht im Chat schreiben."));
                event.setCancelled(true);
                return;
            }

            TeamData teamData = this.teamHandler.getPlayerTeam(player);

            if (teamData == null) return;

            String message = event.getMessage();
            boolean isGlobal = false;

            if (message.toLowerCase().startsWith("@") || message.toLowerCase().startsWith("@a") || message.toLowerCase().startsWith("@all")) {
                isGlobal = true;
                message = message.replaceFirst("(?i)^(@all |@a |@ )", "");
            }

            if (this.gameHandler.getPlayType().endsWith("1")) {
                isGlobal = true;
            }

            String teamColor = extractColorFromComponent(teamData.getDisplayName());
            String playerName = teamColor + player.getName();
            String prefix = isGlobal ? "<dark_gray>[<gray>@<dark_gray>] <gray>" : "";
            Component formattedMessage = MiniMessage.miniMessage().deserialize(prefix + playerName + " <gray>» <white>" + message);

            event.setCancelled(true);

            if (isGlobal) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendMessage(formattedMessage);
                }
            } else {
                for (Player teammate : teamData.getPlayers()) {
                    teammate.sendMessage(formattedMessage);
                }
            }
            return;
        }

        if (this.gameHandler.getCurrentGameState() == GameState.END) {
            if (this.gameHandler.getWasInSpectator().contains(player.getUniqueId())) return;

            if (this.wroteGG.contains(player.getUniqueId())) return;

            if (event.getMessage().toLowerCase().contains("gg") ||
                    event.getMessage().toLowerCase().contains("goodgame") ||
                    event.getMessage().toLowerCase().contains("good game")) {
                this.wroteGG.add(player.getUniqueId());
                ServerAPI.getInstance().getNuggetManager().addNuggets(player.getUniqueId(), 15);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f);
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
}
