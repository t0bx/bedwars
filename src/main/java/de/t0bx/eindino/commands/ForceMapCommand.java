package de.t0bx.eindino.commands;

import de.eindino.server.api.command.AbstractCommandBase;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.map.MapHandler;
import de.t0bx.eindino.vote.VotingHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class ForceMapCommand extends AbstractCommandBase {

    private final String prefix;
    private final MiniMessage mm;
    private final VotingHandler votingHandler;
    private final MapHandler mapHandler;

    public ForceMapCommand(JavaPlugin plugin, String commandName) {
        super(plugin, commandName);
        this.prefix = BedWarsPlugin.getInstance().getPrefix();
        this.votingHandler = BedWarsPlugin.getInstance().getVotingHandler();
        this.mm = MiniMessage.miniMessage();
        this.mapHandler = BedWarsPlugin.getInstance().getMapHandler();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.mm.deserialize(this.prefix + "<red>Only players can use this command!"));
            return true;
        }

        if (!player.hasPermission("bedwars.vip")) {
            player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du hast keine Rechte auf diesen Befehl."));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/forcemap <Mapname>"));
            return true;
        }

        if (this.votingHandler.isForceMap()) {
            player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es wird bereits schon für eine Map gevotet."));
            return true;
        }

        String mapName = args[0];
        if (!this.mapHandler.hasMap(mapName)) {
            player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es existiert keine Map mit dem Namen " + mapName));
            return true;
        }

        this.votingHandler.forceMap(mapName);
        player.sendMessage(this.mm.deserialize(this.prefix + "Es wird nun auf der Map <green>" + mapName + " gespielt!"));
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return this.mapHandler.getMapNames();
        }

        return Collections.emptyList();
    }
}
