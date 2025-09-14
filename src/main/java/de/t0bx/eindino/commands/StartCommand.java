package de.t0bx.eindino.commands;

import de.eindino.server.api.command.AbstractCommandBase;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.game.GameHandler;
import de.t0bx.eindino.game.GameState;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class StartCommand extends AbstractCommandBase {

    private final GameHandler gameHandler;
    private final MiniMessage mm;
    private final String prefix;

    public StartCommand(JavaPlugin plugin, String commandName) {
        super(plugin, commandName);
        this.gameHandler = BedWarsPlugin.getInstance().getGameHandler();
        this.mm = MiniMessage.miniMessage();
        this.prefix = BedWarsPlugin.getInstance().getPrefix();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!player.hasPermission("bedwars.vip")) {
            player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du hast keine Rechte auf diesen Befehl!"));
            return true;
        }

        if (!player.hasPermission("bedwars.admin")) {
            if (this.gameHandler.getCurrentGameState() != GameState.LOBBY) {
                player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du kannst das Spiel nur in der Lobby verkürzen!"));
                return true;
            }

            if (this.gameHandler.getCountdown().get() <= 15) {
                player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du kannst das Spiel nicht mehr verkürzen."));
                return true;
            }

            if (!this.gameHandler.checkIfAbleToStart()) {
                player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es sind zu wenig Spieler in der Runde um das Spiel zu starten!"));
                return true;
            }

            this.gameHandler.getCountdown().set(15);
            player.sendMessage(this.mm.deserialize(this.prefix + "<green>Du hast das Spiel verkürzt!"));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.3F, 1.0F);
            return true;
        }

        if (this.gameHandler.getCurrentGameState() != GameState.LOBBY) {
            player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du kannst das Spiel nur in der Lobby verkürzen!"));
            return true;
        }

        if (this.gameHandler.getCountdown().get() <= 15) {
            player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du kannst das Spiel nicht mehr verkürzen."));
            return true;
        }

        this.gameHandler.getCountdown().set(15);
        player.sendMessage(this.mm.deserialize(this.prefix + "<green>Du hast das Spiel verkürzt!"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.3F, 1.0F);
        if (!this.gameHandler.checkIfAbleToStart()) {
            this.gameHandler.setForceStart(true);
            this.gameHandler.startCountdown();
            player.sendMessage(this.mm.deserialize(this.prefix + "<red><b>Das Spiel wurde mit der nicht benötigten Spieleranzahl gestartet!"));
            player.sendMessage(this.mm.deserialize(this.prefix + "<red><b>Dies ist nur möglich da du die benötigte Berechtigung hast!"));
            player.sendMessage(this.mm.deserialize(this.prefix + "<red><b>Unerwartete Fehler können auf treten!"));
        }
        return false;
    }
}
