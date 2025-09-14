package de.t0bx.eindino.commands;

import de.eindino.server.api.command.AbstractCommandBase;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.map.MapData;
import de.t0bx.eindino.map.MapHandler;
import de.t0bx.eindino.team.TeamConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class MapCommand extends AbstractCommandBase {

    private final MapHandler mapHandler;
    private final String prefix;
    private final MiniMessage mm;

    public MapCommand(JavaPlugin plugin, String commandName) {
        super(plugin, commandName);
        this.mapHandler = BedWarsPlugin.getInstance().getMapHandler();
        this.prefix = BedWarsPlugin.getInstance().getPrefix();
        this.mm = MiniMessage.miniMessage();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.mm.deserialize(this.prefix + "Only players can use this command"));
            return true;
        }

        if (!player.hasPermission("bedwars.admin")) {
            player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du hast keine Rechte diesen Befehl auszuführen!"));
            return true;
        }

        if (args.length == 0) {
            this.sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length != 3) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map create <Map-Name> <PlayType>"));
                    return true;
                }

                String mapName = args[1];
                if (this.mapHandler.hasMap(mapName)) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es existiert bereits eine Map mit dem Namen " + mapName + "!"));
                    return true;
                }

                String playType = args[2];
                this.mapHandler.createMap(mapName, playType);
                player.sendMessage(this.mm.deserialize(this.prefix + "<green>Die Map " + mapName + " wurde erfolgreich erstellt!"));
            }

            case "remove" -> {
                if (args.length != 2) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map remove <Map-Name>"));
                    return true;
                }

                String mapName = args[1];
                if (!this.mapHandler.hasMap(mapName)) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es existiert keine Map mit dem Namen " + mapName + "!"));
                    return true;
                }

                this.mapHandler.removeMap(mapName);
                player.sendMessage(this.mm.deserialize(this.prefix + "<green>Die Map " + mapName + " wurde erfolgreich gelöscht!"));
            }

            case "addteam" -> {
                if (args.length != 3) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map addTeam <Map-Name> <Team>"));
                    return true;
                }

                String mapName = args[1];
                if (!this.mapHandler.hasMap(mapName)) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es existiert keine Map mit dem Namen " + mapName + "!"));
                    return true;
                }

                String teamName = args[2];
                if (this.mapHandler.getMap(mapName).hasTeam(teamName)) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es existiert bereits ein Team mit dem Namen " + teamName));
                    return true;
                }

                this.mapHandler.getMap(mapName).addTeam(teamName, new TeamConfig());
                player.sendMessage(this.mm.deserialize(this.prefix + "<green>Das Team " + teamName + " wurde erfolgreich erstellt!"));
            }

            case "setspawn" -> {
                if (args.length != 3) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map setspawn <Map-Name> <Team>"));
                    return true;
                }

                String mapName = args[1];
                if (!this.mapHandler.hasMap(mapName)) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es existiert keine Map mit dem Namen " + mapName + "!"));
                    return true;
                }

                String teamName = args[2];
                if (!this.mapHandler.getMap(mapName).hasTeam(teamName)) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es existiert kein Team mit dem Namen " + teamName));
                    return true;
                }

                this.mapHandler.getMap(mapName).getTeam(teamName).setSpawnLocation(player.getLocation());
                player.sendMessage(this.mm.deserialize(this.prefix + "<green>Du hast den Spawnpunkt für das Team " + teamName + " erfolgreich gesetzt!"));
            }

            case "setbed" -> {
                if (args.length != 3) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map setbed <Map-Name> <Team>"));
                    return true;
                }

                String mapName = args[1];
                if (!this.mapHandler.hasMap(mapName)) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es existiert keine Map mit dem Namen " + mapName + "!"));
                    return true;
                }

                String teamName = args[2];
                if (!this.mapHandler.getMap(mapName).hasTeam(teamName)) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es existiert kein Team mit dem Namen " + teamName));
                    return true;
                }

                Block block = player.getTargetBlockExact(20);
                if (block == null || !block.getType().toString().endsWith("_BED")) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du musst auf ein Bett schauen!"));
                    return true;
                }

                BlockData blockData = block.getBlockData();
                if (!(blockData instanceof Bed bed)) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Du musst auf ein Bett schauen!"));
                    return true;
                }

                BlockFace facing = bed.getFacing();
                boolean isHead = bed.getPart() == Bed.Part.HEAD;
                if (isHead) {
                    this.mapHandler.getMap(mapName).getTeam(teamName).setBedTop(block.getLocation());
                } else {
                    this.mapHandler.getMap(mapName).getTeam(teamName).setBedBottom(block.getLocation());
                }

                BlockFace direction = isHead ? facing.getOppositeFace() : facing;
                Block otherPart = block.getRelative(direction);

                if (isHead) {
                    this.mapHandler.getMap(mapName).getTeam(teamName).setBedBottom(otherPart.getLocation());
                } else {
                    this.mapHandler.getMap(mapName).getTeam(teamName).setBedTop(otherPart.getLocation());
                }

                player.sendMessage(this.mm.deserialize(this.prefix + "<green>Du hast das Bett für das Team " + teamName + " erfolgreich gesetzt!"));
            }

            case "setshop" -> {
                if (args.length != 2) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map setshop <Map-Name>"));
                    return true;
                }

                String mapName = args[1];
                if (!this.mapHandler.hasMap(mapName)) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es existiert keine Map mit dem Namen " + mapName + "!"));
                    return true;
                }

                this.mapHandler.getMap(mapName).addShop(player.getLocation());
                player.sendMessage(this.mm.deserialize(this.prefix + "<green>Du hast erfolgreich ein Shop gesetzt! Anzahl an Shops in der Maps: " +  this.mapHandler.getMap(mapName).getShops().size()));
            }

            case "setspawner" -> {
                if (args.length != 3) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map setspawner <Map-Name> <Spawner>"));
                    return true;
                }

                String mapName = args[1];
                if (!this.mapHandler.hasMap(mapName)) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es existiert keine Map mit dem Namen " + mapName + "!"));
                    return true;
                }

                String type = args[2];
                if (!type.equalsIgnoreCase("bronze") &&
                        !type.equalsIgnoreCase("iron") &&
                        !type.equalsIgnoreCase("gold")) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Ungültiger Spawner Typ!"));
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Folgende Sind erlaubt: Bronze, Iron, Gold"));
                    return true;
                }

                this.mapHandler.getMap(mapName).addSpawner(type.toLowerCase(), player.getLocation());
                player.sendMessage(this.mm.deserialize(this.prefix + "<green>Du hast erfolgreich ein neuen Spawner hinzugefügt!"));
            }

            case "setspectator" -> {
                if (args.length != 2) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map setspectator <Map-Name>"));
                    return true;
                }

                String mapName = args[1];
                if (!this.mapHandler.hasMap(mapName)) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es existiert keine Map mit dem Namen " + mapName + "!"));
                    return true;
                }

                this.mapHandler.getMap(mapName).setSpectatorLocation(player.getLocation());
                player.sendMessage(this.mm.deserialize(this.prefix + "<green>Du hast den Spectator Spawnpunkt gesetzt!"));
            }

            case "save" -> {
                if (args.length != 2) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map save <Map-Name>"));
                    return true;
                }

                String mapName = args[1];
                if (!this.mapHandler.hasMap(mapName)) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es existiert keine Map mit dem Namen " + mapName + "!"));
                    return true;
                }

                try {
                    this.mapHandler.saveMap(this.mapHandler.getMap(mapName));
                    player.sendMessage(this.mm.deserialize(this.prefix + "<green>Du hast erfolgreich die Map " + mapName + " die Map ist nun bereit zum spielen!"));
                } catch (IOException e) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "<red>Es ist ein Fehler aufgetreten die Map zu speichern!"));
                }
            }

            default -> this.sendHelp(player);
        }
        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map create <Map-Name> <PlayType>"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map remove <Map-Name>"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map addTeam <Map-Name> <Team>"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map setspawn <Map-Name> <Team>"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map setbed <Map-Name> <Team>"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map setshop <Map-Name>"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map setspawner <Map-Name> <Spawner>"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map setspectator <Map-Name>"));
        player.sendMessage(this.mm.deserialize(this.prefix + "Verwendung: <green>/map save <Map-Name>"));
    }
}