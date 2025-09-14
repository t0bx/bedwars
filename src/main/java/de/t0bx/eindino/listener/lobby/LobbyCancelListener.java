package de.t0bx.eindino.listener.lobby;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.game.GameHandler;
import de.t0bx.eindino.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class LobbyCancelListener implements Listener {

    private final GameHandler gameHandler;

    public LobbyCancelListener() {
        this.gameHandler = BedWarsPlugin.getInstance().getGameHandler();
    }

    private boolean isLobby() {
        return this.gameHandler.getCurrentGameState() == GameState.LOBBY || this.gameHandler.getCurrentGameState() == GameState.END;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isLobby()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isLobby()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (isLobby()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (isLobby()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (isLobby()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (isLobby()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isLobby()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
        if (isLobby()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBurning(EntityCombustEvent event) {
        if (isLobby()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerDropItem(PlayerDropItemEvent event) {
        if (isLobby()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (isLobby()) {
            Player player = event.getPlayer();
            if (player.getLocation().getY() <= 10) {
                player.teleport(BedWarsPlugin.getInstance().getSpawnManager().getSpawn());
            }
        }
    }
}
