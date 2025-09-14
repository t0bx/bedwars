package de.t0bx.eindino.inventory.inventories;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.inventory.InventoryProvider;
import de.t0bx.eindino.team.TeamData;
import de.t0bx.eindino.team.TeamHandler;
import de.t0bx.eindino.utils.ItemProvider;
import net.asyncproxy.nicksystem.NickSystem;
import net.asyncproxy.nicksystem.nick.NickManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class NavigatorInventory {

    private final TeamHandler teamHandler;
    private final InventoryProvider inventoryProvider;
    private final NickManager nickManager;

    public NavigatorInventory() {
        this.teamHandler = BedWarsPlugin.getInstance().getTeamHandler();
        this.inventoryProvider = BedWarsPlugin.getInstance().getInventoryProvider();
        this.nickManager = NickSystem.getInstance().getNickManager();
    }

    public void openInventory(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "navigator", 9*3, "<gray>» <red>Navigator <gray>«");

        inventory.clear();
        int index = 0;
        for (TeamData teamData : this.teamHandler.getRemainingTeams()) {
            for (Player players : teamData.getPlayersAlive()) {
                if (this.nickManager.isNickedPlayer(players)) {
                    inventory.setItem(index, ItemProvider.createPlayerSkull(this.nickManager.getNick(players))
                            .setName(extractColorFromComponent(teamData.getDisplayName()) + players.getName())
                            .setPersistentData("bedwars", "navigator", players.getName())
                            .build());
                } else {
                    inventory.setItem(index, ItemProvider.createPlayerSkull(players.getName())
                            .setName(extractColorFromComponent(teamData.getDisplayName()) + players.getName())
                            .setPersistentData("bedwars", "navigator", players.getName())
                            .build());
                }
                index++;
            }
        }

        player.openInventory(inventory);
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
