package de.t0bx.eindino.listener.lobby;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.inventory.inventories.MapVotingInventory;
import de.t0bx.eindino.inventory.inventories.TeamSelectionInventory;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import eu.cloudnetservice.modules.bridge.player.executor.ServerSelectorType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerLobbyInteractListener implements Listener {

    private final MiniMessage mm;
    private final TeamSelectionInventory teamSelectionInventory;
    private final MapVotingInventory mapVotingInventory;
    private final PlayerManager playerManager;

    public PlayerLobbyInteractListener() {
        this.mm = MiniMessage.miniMessage();
        this.teamSelectionInventory = new TeamSelectionInventory();
        this.mapVotingInventory = BedWarsPlugin.getInstance().getMapVotingInventory();
        this.playerManager = BedWarsPlugin.getInstance().getPlayerManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getItem() == null) return;

        if (event.getItem().getType() == Material.AIR) return;

        if (event.getItem().getItemMeta() == null) return;

        if (event.getHand() != EquipmentSlot.HAND) return;

        if (event.getItem().getItemMeta().customName() == null) return;

        if (event.getItem().getItemMeta().customName().equals(this.mm.deserialize("<gray>» <red>Teamauswahl"))) {
            event.setCancelled(true);
            this.teamSelectionInventory.openInventory(player);
        }

        if (event.getItem().getItemMeta().customName().equals(this.mm.deserialize("<gray>» <green>Voting"))) {
            event.setCancelled(true);
            this.mapVotingInventory.openInventory(player);
        }

        if (event.getItem().getItemMeta().customName().equals(this.mm.deserialize("<gray>» <red>Spiel Verlassen"))) {
            event.setCancelled(true);
            this.playerManager.playerExecutor(player.getUniqueId()).connectToTask("Lobby", ServerSelectorType.RANDOM);
        }
    }
}
