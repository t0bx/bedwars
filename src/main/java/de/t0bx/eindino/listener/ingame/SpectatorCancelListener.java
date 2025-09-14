package de.t0bx.eindino.listener.ingame;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.spectator.SpectatorHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class SpectatorCancelListener implements Listener {

    private final SpectatorHandler spectatorHandler;

    public SpectatorCancelListener() {
        this.spectatorHandler = BedWarsPlugin.getInstance().getSpectatorHandler();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!this.spectatorHandler.isSpectator(player)) return;

        event.setCancelled(true);
        if (event.getItem() == null) return;

        if (event.getItem().getType() == Material.COMPASS) {
            BedWarsPlugin.getInstance().getNavigatorInventory().openInventory(player);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (this.spectatorHandler.isSpectator(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickupArrow(PlayerPickupArrowEvent event) {
        Player player = event.getPlayer();
        if (this.spectatorHandler.isSpectator(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (this.spectatorHandler.isSpectator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwapItems(PlayerSwapHandItemsEvent event) {
        if (this.spectatorHandler.isSpectator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
