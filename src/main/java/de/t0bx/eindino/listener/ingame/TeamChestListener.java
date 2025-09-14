package de.t0bx.eindino.listener.ingame;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.spectator.SpectatorHandler;
import de.t0bx.eindino.team.TeamData;
import de.t0bx.eindino.team.TeamHandler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeamChestListener implements Listener {

    private final TeamHandler teamHandler;
    private final SpectatorHandler spectatorHandler;
    private final Map<TeamData, Inventory> teamChests = new HashMap<>();
    private final Map<UUID, EnderChest> enderChests = new ConcurrentHashMap<>();

    public TeamChestListener() {
        this.teamHandler = BedWarsPlugin.getInstance().getTeamHandler();
        this.spectatorHandler = BedWarsPlugin.getInstance().getSpectatorHandler();
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (this.spectatorHandler.isSpectator(player)) return;

        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.ENDER_CHEST) return;
        event.setCancelled(true);

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        TeamData teamData = this.teamHandler.getPlayerTeam(player);
        if (teamData == null) return;

        Inventory teamChest = this.teamChests.computeIfAbsent(teamData, _ -> Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize("<green>Team-Kiste")));
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
        player.openInventory(teamChest);
        if (event.getClickedBlock() instanceof EnderChest enderChest) {
            enderChest.open();
            this.enderChests.put(player.getUniqueId(), enderChest);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(MiniMessage.miniMessage().deserialize("<green>Team-Kiste"))) {
            Player player = (Player) event.getPlayer();
            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, 1.0f, 1.0f);
            EnderChest enderChest = this.enderChests.remove(player.getUniqueId());
            if (enderChest == null) return;
            enderChest.close();
        }
    }
}
