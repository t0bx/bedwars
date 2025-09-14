package de.t0bx.eindino.inventory.inventories;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.inventory.InventoryProvider;
import de.t0bx.eindino.map.MapHandler;
import de.t0bx.eindino.utils.ItemProvider;
import de.t0bx.eindino.vote.VotingHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MapVotingInventory {

    private final MapHandler mapHandler;
    private final VotingHandler votingHandler;
    private final InventoryProvider inventoryProvider;

    public MapVotingInventory() {
        this.mapHandler = BedWarsPlugin.getInstance().getMapHandler();
        this.votingHandler = BedWarsPlugin.getInstance().getVotingHandler();
        this.inventoryProvider = BedWarsPlugin.getInstance().getInventoryProvider();
    }

    public void openInventory(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "voting", 9, "<gray>» <green>Voting <gray>«");

        inventory.setItem(3, new ItemProvider(Material.GOLD_INGOT).setName("<gray>» <green>Gold-Voting").build());
        inventory.setItem(5, new ItemProvider(Material.PAPER).setName("<gray>» <green>Map-Voting").build());

        player.openInventory(inventory);
    }

    public void openGoldVoting(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "gold_voting", 9, "<gray>» <green>Gold-Voting <gray>«");

        inventory.setItem(3, new ItemProvider(Material.EMERALD).setName("<gray>» <green>Für Gold stimmen").setLore("§7Stimmen: §a" +  + this.votingHandler.getVotesForGold(true)).build());
        inventory.setItem(5, new ItemProvider(Material.REDSTONE).setName("<gray>» <green>Gegen Gold stimmen").setLore("§7Stimmen: §a" + this.votingHandler.getVotesForGold(false)).build());

        player.openInventory(inventory);
    }

    public void openMapVoting(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "map_voting", 9, "<gray>» <green>Map-Voting <gray>«");

        int[] slots = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        int slotIndex = 0;

        for (String mapName : this.mapHandler.getMapNames()) {
            inventory.setItem(slots[slotIndex], new ItemProvider(Material.PAPER)
                    .setName("<green>" + mapName)
                    .setLore("§7Stimmen: §a" + this.votingHandler.getVotesFromMap(mapName))
                    .setPersistentData("bedwars", "votes", mapName)
                    .build()
            );
            slotIndex++;
        }

        player.openInventory(inventory);
    }
}
