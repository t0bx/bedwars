package de.t0bx.eindino.listener;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.game.GameHandler;
import de.t0bx.eindino.game.GameState;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerPreLoginListener implements Listener {

    private final GameHandler gameHandler;
    private final LuckPerms luckPermsProvider;

    public PlayerPreLoginListener() {
        this.gameHandler = BedWarsPlugin.getInstance().getGameHandler();
        this.luckPermsProvider = LuckPermsProvider.get();
    }

    @EventHandler
    public void onPlayerPreLogin(PlayerLoginEvent event) {
        if (this.gameHandler.getCurrentGameState() != GameState.LOBBY) return;
        Player player = event.getPlayer();

        int maxPlayer = BedWarsPlugin.getInstance().maxPlayerCount();
        int playerCount = Bukkit.getOnlinePlayers().size();

        if (playerCount < maxPlayer) return;

        User joiningUser = luckPermsProvider.getUserManager().getUser(player.getUniqueId());
        if (joiningUser == null) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, MiniMessage.miniMessage().deserialize("<red>Dein Rang konnte nicht geladen werden."));
            return;
        }

        Group joiningGroup = luckPermsProvider.getGroupManager().getGroup(joiningUser.getPrimaryGroup());
        if (joiningGroup == null || joiningGroup.getWeight().isEmpty()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, MiniMessage.miniMessage().deserialize("<red>Dein Rang konnte nicht gefunden werden."));
            return;
        }

        Player kickTarget = null;

        for (Player online : Bukkit.getOnlinePlayers()) {
            User onlineUser = luckPermsProvider.getUserManager().getUser(online.getUniqueId());
            if (onlineUser == null) continue;

            Group onlineGroup = luckPermsProvider.getGroupManager().getGroup(onlineUser.getPrimaryGroup());
            if (onlineGroup == null || onlineGroup.getWeight().isEmpty()) continue;

            int onlineWeight = onlineGroup.getWeight().getAsInt();
            if (onlineWeight > 115) {
                kickTarget = online;
                break;
            }
        }

        if (kickTarget != null) {
            kickTarget.kick(MiniMessage.miniMessage().deserialize("<red>Du wurdest gekickt, um Platz für einen Spieler mit höheren Rang zu machen."));
        } else {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, MiniMessage.miniMessage().deserialize("<red>Der Server ist voll. Es konnte kein Spieler mit niedrigeren Rang gekickt werden."));
        }
    }
}
