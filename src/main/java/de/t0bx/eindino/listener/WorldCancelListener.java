package de.t0bx.eindino.listener;

import de.t0bx.eindino.BedWarsPlugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

public class WorldCancelListener implements Listener {

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onRecipeDiscover(PlayerRecipeDiscoverEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        event.getPlayer().getAdvancementProgress(event.getAdvancement()).revokeCriteria(event.getAdvancement().getCriteria().iterator().next());
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockFertilize(BlockFertilizeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHarvestBlock(PlayerHarvestBlockEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) {
                event.setCancelled(true);
            }
        }

        if (event.getEntity() instanceof Player player) {
            if (BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerCrafting(CraftItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();

        if (clickedBlock.getType().name().endsWith("_SIGN") ||
                clickedBlock.getType().name().endsWith("_DOOR") ||
                clickedBlock.getType().name().endsWith("_TRAPDOOR") ||
                clickedBlock.getType().name().endsWith("_GATE") ||
                clickedBlock.getType().name().equalsIgnoreCase("ARMOR_STAND") ||
                clickedBlock.getType().name().endsWith("_FRAME")) {
            event.setCancelled(true);
        }

        if (clickedBlock.getType().name().endsWith("_BED")) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            boolean hasPlaceableItem = itemInHand.getType().isBlock();

            if (!player.isSneaking() || !hasPlaceableItem) {
                event.setCancelled(true);
            }

            if (player.isSneaking() && !hasPlaceableItem) {
                event.setCancelled(true);
            }
        }
    }
}
