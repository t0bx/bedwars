package de.t0bx.eindino.commands;

import de.eindino.server.api.command.AbstractCommandBase;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.config.SpawnManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SetSpawnCommand extends AbstractCommandBase {

    private final SpawnManager spawnManager;
    private final String prefix;
    private final MiniMessage mm;

    public SetSpawnCommand(JavaPlugin plugin, String commandName) {
        super(plugin, commandName);
        this.spawnManager = BedWarsPlugin.getInstance().getSpawnManager();
        this.prefix = BedWarsPlugin.getInstance().getPrefix();
        this.mm = MiniMessage.miniMessage();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.mm.deserialize(this.prefix + "<red>Only players can use this command!"));
            return true;
        }

        if (!player.hasPermission("bedwars.admin")) {
            player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du hast keine Rechte auf diesen Befehl!"));
            return true;
        }

        this.spawnManager.setSpawn(player.getLocation());
        player.sendMessage(this.mm.deserialize(this.prefix + "<green>Du hast erfolgreich den Spawn gesetzt!"));
        return false;
    }
}
