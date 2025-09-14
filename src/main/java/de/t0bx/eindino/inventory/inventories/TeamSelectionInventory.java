package de.t0bx.eindino.inventory.inventories;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.inventory.InventoryProvider;
import de.t0bx.eindino.team.TeamData;
import de.t0bx.eindino.team.TeamHandler;
import de.t0bx.eindino.utils.ItemProvider;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class TeamSelectionInventory {

    private final TeamHandler teamHandler;
    private final InventoryProvider inventoryProvider;

    public TeamSelectionInventory() {
        this.teamHandler = BedWarsPlugin.getInstance().getTeamHandler();
        this.inventoryProvider = BedWarsPlugin.getInstance().getInventoryProvider();
    }

    public void openInventory(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "team_selection", 9, "<gray>» <red>Teamauswahl <gray>«");
        inventory.clear();

        int[] slots = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        int slotIndex = 0;

        for (TeamData teams : this.teamHandler.getAllTeams()) {
            ItemProvider itemProvider = new ItemProvider(this.getTeamMaterial(teams.getName()))
                    .setName(MiniMessage.miniMessage().serialize(teams.getDisplayName()) + " <gray>- " + teams.getPlayerCount() + "/" + teams.getMaxPlayers())
                    .setPersistentData("bedwars", "teams", teams.getName());
            String colorCode = this.getTeamColor(teams.getName());
            teams.getPlayers().forEach(players -> {
                itemProvider.addLore(colorCode + players.getName());
            });

            inventory.setItem(slots[slotIndex], itemProvider.build());
            slotIndex++;
        }

        player.openInventory(inventory);
    }

    private Material getTeamMaterial(String teamName) {
        return switch (teamName.toLowerCase()) {
            case "red" -> Material.RED_BED;
            case "blue" -> Material.BLUE_BED;
            case "green" -> Material.GREEN_BED;
            case "yellow" -> Material.YELLOW_BED;
            case "orange" -> Material.ORANGE_BED;
            case "purple" -> Material.PURPLE_BED;
            case "pink" -> Material.PINK_BED;
            case "white" -> Material.WHITE_BED;
            case "black" -> Material.BLACK_BED;
            case "gray", "grey" -> Material.GRAY_BED;
            case "aqua" -> Material.LIGHT_BLUE_BED;
            case "lime" -> Material.LIME_BED;
            default -> Material.WHITE_BED;
        };
    }

    private String getTeamColor(String teamName) {
        return switch (teamName.toLowerCase()) {
            case "red" -> "§c";
            case "blue" -> "§9";
            case "green" -> "§2";
            case "yellow" -> "§e";
            case "orange" -> "§6";
            case "purple" -> "§5";
            case "pink" -> "§d";
            case "white" -> "§f";
            case "black" -> "§0";
            case "gray", "grey" -> "§7";
            case "aqua" -> "§b";
            case "lime" -> "§a";
            default -> "§f";
        };
    }
}
