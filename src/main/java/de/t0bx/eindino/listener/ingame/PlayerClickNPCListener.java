package de.t0bx.eindino.listener.ingame;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.inventory.inventories.ShopInventory;
import de.t0bx.sentienceEntity.events.PlayerClickNPCEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerClickNPCListener implements Listener {

    private final ShopInventory shopInventory;

    public PlayerClickNPCListener() {
        this.shopInventory = BedWarsPlugin.getInstance().getShopInventory();
    }

    @EventHandler
    public void onNPCClick(PlayerClickNPCEvent event) {
        Player player = event.getPlayer();

        if (BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) {
            return;
        }

        if (event.getNpcName().startsWith("shop_")) {
            this.shopInventory.openInventory(player);
        }
    }
}
