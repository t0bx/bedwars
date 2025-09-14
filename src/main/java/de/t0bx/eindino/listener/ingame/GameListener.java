package de.t0bx.eindino.listener.ingame;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.game.GameHandler;
import de.t0bx.eindino.game.GameState;
import de.t0bx.eindino.map.MapHandler;
import de.t0bx.eindino.player.PlayerHandler;
import de.t0bx.eindino.team.TeamData;
import de.t0bx.eindino.team.TeamHandler;
import de.t0bx.eindino.utils.ItemProvider;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameListener implements Listener {

    private final Queue<Block> placedBlocks;
    private final GameHandler gameHandler;
    private final PlayerHandler playerHandler;
    private final TeamHandler teamHandler;
    private final MapHandler mapHandler;

    private final Map<Location, TeamData> alarmBlocks;

    public GameListener() {
        this.placedBlocks = new ConcurrentLinkedQueue<>();
        this.gameHandler = BedWarsPlugin.getInstance().getGameHandler();
        this.playerHandler = BedWarsPlugin.getInstance().getPlayerHandler();
        this.teamHandler = BedWarsPlugin.getInstance().getTeamHandler();
        this.mapHandler = BedWarsPlugin.getInstance().getMapHandler();
        this.alarmBlocks = new ConcurrentHashMap<>();
    }

    private boolean isGame() {
        return this.gameHandler.getCurrentGameState() == GameState.IN_GAME;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isGame()) {
            Player player = event.getPlayer();
            if (BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) {
                event.setCancelled(true);
                return;
            }

            Block block = event.getBlock();
            if (isTeamSpawn(block.getLocation()) ||
                    isSpawner(block.getLocation()) ||
                    isShop(block.getLocation())) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(false);
            if (block.getType().name().endsWith("_WOOL")) {
                this.gameHandler.getTimeBlocks().put(block, System.currentTimeMillis() + 5000);
                return;
            }

            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getItemMeta() != null) {
                if (itemStack.getItemMeta().customName() != null) {
                    if (itemStack.getItemMeta().customName().equals(MiniMessage.miniMessage().deserialize("<green>Alarmblock"))) {
                        this.alarmBlocks.put(block.getLocation(), this.teamHandler.getPlayerTeam(player));
                    }
                }
            }

            this.placedBlocks.add(block);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isGame()) {
            Player player = event.getPlayer();
            if (BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) {
                event.setCancelled(true);
                return;
            }

            Block block = event.getBlock();
            if (block.getType().name().endsWith("_BED")) return;

            if (!this.placedBlocks.contains(block)) {
                event.setCancelled(true);
                return;
            }

            if (this.alarmBlocks.containsKey(block.getLocation())) {
                event.setCancelled(true);
                TeamData teamData = this.alarmBlocks.remove(block.getLocation());
                this.placedBlocks.remove(block);
                block.setType(Material.AIR);
                block.getLocation().getWorld().dropItemNaturally(block.getLocation(), new ItemProvider(this.getPlayerTeamBlockConcrete(player)).setName("<green>Baublock").build());
                if (teamData == null) return;
                for (Player players : teamData.getPlayersAlive()) {
                    players.showTitle(Title.title(MiniMessage.miniMessage().deserialize("<red>Alarmblock"), MiniMessage.miniMessage().deserialize("<red>ausgelöst!")));
                }
                return;
            }

            if (block.getType().name().endsWith("_CONCRETE")) {
                event.setCancelled(true);
                block.setType(Material.AIR);
                block.getLocation().getWorld().dropItemNaturally(block.getLocation(), new ItemProvider(this.getPlayerTeamBlockConcrete(player)).setName("<green>Baublock").build());
            }
            event.setCancelled(false);
            this.placedBlocks.remove(block);
        }
    }

    @EventHandler
    public void onPlayerPlaceArmorStand(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (event.getItem() == null) return;

        Player player = event.getPlayer();
        if (BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) return;

        if (event.getItem().getType() == Material.ARMOR_STAND) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplosionEvent(EntityExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (this.gameHandler.getCurrentGameState() == GameState.IN_GAME) {
            if (event.getEntity() instanceof Player player) {
                if (BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        if (BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        boolean hasPlaceableItem = itemInHand.getType().isBlock();

        if (!player.isSneaking() || !hasPlaceableItem) {
            event.setCancelled(true);
        }

        if (player.isSneaking() && !hasPlaceableItem) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSpawnChangeEvent(PlayerSetSpawnEvent event) {
        if (event.getCause() == PlayerSetSpawnEvent.Cause.BED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (isGame()) {
            if (event.getEntity() instanceof Player victim && event.getDamager() instanceof Player damager) {
                if (!BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(damager) &&
                    !BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(victim)) {
                    this.playerHandler.getFightMap().put(victim, damager);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (isGame()) {
            Player player = event.getPlayer();

            if (!BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) {
                if (player.getLocation().getY() <= 0) {
                    player.damage(20);
                }
            } else {
                if (player.getLocation().getY() <= 0) {
                    player.teleport(this.mapHandler.getMap(this.gameHandler.getCurrentMap()).getSpectatorLocation());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;

        Player player = event.getPlayer();

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType() == Material.BLAZE_ROD) {
            switch (event.getAction()) {
                case RIGHT_CLICK_AIR:
                case RIGHT_CLICK_BLOCK:
                    break;
                default:
                    return;
            }

            if (itemStack.getAmount() > 1) {
                itemStack.setAmount(itemStack.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            Location location = player.getLocation();
            int platformX = location.getBlockX();
            int platformY = location.getBlockY() - 4;
            int platformZ = location.getBlockZ();

            List<Block> blocks = this.fillCircle(location.getWorld(), platformX, platformY, platformZ, 4, Material.SLIME_BLOCK);
            for (Block block : blocks) {
                this.gameHandler.getTimeBlocks().put(block, System.currentTimeMillis() + 5000);
            }
        }

        if (itemStack.getType() == Material.ARMOR_STAND) {
            BedWarsPlugin.getInstance().getShopInventory().openInventory(player);
        }
    }

    private List<Block> fillCircle(World world, int centerX, int centerY, int centerZ, int radius, Material material) {
        List<Block> blocksAffected = new ArrayList<>();
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                int dx = x - centerX;
                int dz = z - centerZ;

                if (dx * dx + dz * dz <= radius * radius) {
                    Block block = world.getBlockAt(x, centerY, z);
                    if (block.getType() != Material.AIR) continue;
                    block.setType(material);
                    blocksAffected.add(block);
                }
            }
        }
        return blocksAffected;
    }

    private boolean isTeamSpawn(Location location) {
        for (TeamData teams : this.teamHandler.getAllTeams()) {
            Location spawn = teams.getSpawnLocation();

            int dx = Math.abs(spawn.getBlockX() - location.getBlockX());
            int dz = Math.abs(spawn.getBlockZ() - location.getBlockZ());

            if (dx <= 3 && dz <= 3) {
                int locY = location.getBlockY();
                int spawnY = spawn.getBlockY();

                if (locY >= spawnY && locY <= spawnY + 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSpawner(Location location) {
        for (Map.Entry<String, List<Location>> entry : this.mapHandler.getMap(this.gameHandler.getCurrentMap()).getSpawners().entrySet()) {
            List<Location> spawnerList = entry.getValue();

            for (Location spawnerLoc : spawnerList) {
                int dx = Math.abs(spawnerLoc.getBlockX() - location.getBlockX());
                int dz = Math.abs(spawnerLoc.getBlockZ() - location.getBlockZ());

                if (dx <= 2 && dz <= 2) {
                    int locY = location.getBlockY();
                    int spawnY = spawnerLoc.getBlockY();

                    if (locY >= spawnY && locY <= spawnY + 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isShop(Location location) {
        for (Location locations : this.mapHandler.getMap(this.gameHandler.getCurrentMap()).getShops()) {
            int dx = Math.abs(locations.getBlockX() - location.getBlockX());
            int dz = Math.abs(locations.getBlockZ() - location.getBlockZ());

            if (dx <= 2 && dz <= 2) {
                int locY = location.getBlockY();
                int spawnY = locations.getBlockY();

                if (locY >= spawnY && locY <= spawnY + 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private Material getPlayerTeamBlockConcrete(Player player) {
        return switch (this.teamHandler.getPlayerTeam(player).getName().toLowerCase()) {
            case "red" -> Material.RED_CONCRETE;
            case "green" -> Material.GREEN_CONCRETE;
            case "blue" -> Material.BLUE_CONCRETE;
            case "yellow" -> Material.YELLOW_CONCRETE;
            case "orange" -> Material.ORANGE_CONCRETE;
            case "purple" -> Material.PURPLE_CONCRETE;
            case "pink" -> Material.PINK_CONCRETE;
            case "white" -> Material.WHITE_CONCRETE;
            case "black" -> Material.BLACK_CONCRETE;
            case "gray", "grey" -> Material.GRAY_CONCRETE;
            case "aqua" -> Material.LIGHT_BLUE_CONCRETE;
            case "lime" -> Material.LIME_CONCRETE;
            default -> Material.BLACK_CONCRETE;
        };
    }
}
