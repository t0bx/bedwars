package de.t0bx.eindino.commands;

import de.eindino.server.api.command.AbstractCommandBase;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.player.PlayerHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TrollCommand extends AbstractCommandBase {

    private final PlayerHandler playerHandler;
    private final MiniMessage mm;
    private final String prefix;

    public TrollCommand(JavaPlugin plugin, String commandName) {
        super(plugin, commandName);
        this.playerHandler = BedWarsPlugin.getInstance().getPlayerHandler();
        this.mm = MiniMessage.miniMessage();
        this.prefix = BedWarsPlugin.getInstance().getPrefix();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.mm.deserialize(this.prefix + "Only players can use this command"));
            return true;
        }

        if (!player.hasPermission("bedwars.admin")) {
            player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du hast keine Rechte auf diesen Befehl!"));
            return true;
        }

        if (!this.playerHandler.isStatsEnabled()) {
            player.sendMessage(this.mm.deserialize(this.prefix + "<red>Stats sind in dieser Runde bereits deaktiviert!"));
            return true;
        }

        this.playerHandler.setStatsEnabled(false);
        player.sendMessage(this.mm.deserialize(this.prefix + "<red>Stats sind in dieser Runde nun deaktiviert!"));
        for (Player players : Bukkit.getOnlinePlayers()) {
            players.sendMessage(this.mm.deserialize(this.prefix + " "));
            players.sendMessage(this.mm.deserialize(this.prefix + "<red>Stats wurden in dieser Runde deaktiviert!"));
            players.sendMessage(this.mm.deserialize(this.prefix + " "));
        }
        return false;
    }
}
