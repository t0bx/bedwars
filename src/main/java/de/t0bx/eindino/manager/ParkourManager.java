package de.t0bx.eindino.manager;

import de.eindino.server.api.ServerAPI;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.player.BedwarsPlayer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class ParkourManager {
    private final Location JUMP_AND_RUN_POSITION = new Location(Bukkit.getWorld("world"), 70.5, 76, 53.524);

    private final Location JUMP_AND_RUN_POSITION_START = new Location(Bukkit.getWorld("world"), -17, 100, 54);

    private final Random random;

    @Getter
    private final List<Player> playersInParkour;

    private final Map<Player, Integer> playerCheckpoints;

    private final Map<Player, Location> playerLastBlock;

    private final Map<Player, Location> playerNextBlock;

    private final Map<Player, BlockType> playerBlockType;

    private final Map<Player, Entity> playerBlockDisplays;

    /**
     * Constructs a ParkourManager object.
     * Initializes the data structures to manage player state and parkour progress,
     * including randomization, player tracking, checkpoint handling, and block-related information.
     */
    public ParkourManager() {
        this.random = new Random();
        this.playersInParkour = new ArrayList<>();
        this.playerCheckpoints = new HashMap<>();
        this.playerLastBlock = new HashMap<>();
        this.playerNextBlock = new HashMap<>();
        this.playerBlockType = new HashMap<>();
        this.playerBlockDisplays = new HashMap<>();
    }

    /**
     * Handles the movement of a player in the parkour game. This method checks if the player
     * is in the start zone to enter the parkour or if the player is currently in parkour to
     * update the game progress, trigger actions based on checkpoints, and manage failure states.
     *
     * @param player the player whose movement is being handled in the parkour game
     */
    public void onMove(Player player) {
        if (this.isInStartZone(player) && !this.playersInParkour.contains(player)) {
            this.startParkour(player);
            return;
        }

        if (this.playersInParkour.contains(player)) {
            player.sendActionBar(Component.text("§6§lJump'n Run §8| §7Checkpoint: §a" + this.playerCheckpoints.get(player) + "§7 von §a50"));
            if (this.isNextBlock(player)) {
                this.playerCheckpoints.put(player, this.playerCheckpoints.get(player) + 1);
                this.playerLastBlock.get(player).getBlock().setType(Material.AIR);

                Location currentBlock = this.playerNextBlock.get(player);
                currentBlock.getBlock().setType(this.playerBlockType.get(player).block);

                this.playerLastBlock.put(player, currentBlock);

                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 2.5f);

                if (this.playerCheckpoints.get(player) == 50) {
                    player.sendMessage("§6§lJump'n Run §8| §a§lGlückwunsch, §7du hast den Parkour geschafft!");
                    player.sendMessage("§6§lJump'n Run §8| §7Deine Checkpoints waren: §a" + this.playerCheckpoints.get(player));
                    player.teleport(BedWarsPlugin.getInstance().getSpawnManager().getSpawn());

                    ServerAPI.getInstance().getNuggetManager().addNuggets(player.getUniqueId(), this.random.nextInt(25, 50));
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

                    this.playerLastBlock.get(player).getBlock().setType(Material.AIR);
                    this.playerNextBlock.get(player).getBlock().setType(Material.AIR);

                    if (this.playerBlockDisplays.containsKey(player)) {
                        this.playerBlockDisplays.get(player).remove();
                        this.playerBlockDisplays.remove(player);
                    }

                    this.playersInParkour.remove(player);
                    this.playerCheckpoints.remove(player);
                    this.playerLastBlock.remove(player);
                    this.playerNextBlock.remove(player);
                    this.playerBlockType.remove(player);
                    return;
                }

                this.spawnNextParkourBlock(player, this.playerLastBlock.get(player));
            } else if (player.getLocation().blockY() < this.playerNextBlock.get(player).blockY() - 5) {
                this.removePlayerFromParkour(player, false);
            }
        }
    }

    /**
     * Checks if the player is currently in the start zone of the parkour.
     *
     * @param player the player whose position needs to be verified
     * @return true if the player is in the start zone, false otherwise
     */
    private boolean isInStartZone(Player player) {
        int playerX = player.getLocation().getBlockX();
        int playerY = player.getLocation().getBlockY();
        int playerZ = player.getLocation().getBlockZ();

        int startX = JUMP_AND_RUN_POSITION.getBlockX();
        int startY = JUMP_AND_RUN_POSITION.getBlockY();
        int startZ = JUMP_AND_RUN_POSITION.getBlockZ();

        return playerX == startX
                && playerY >= startY
                && playerY <= startY + 2
                && playerZ == startZ;
    }

    /**
     * Checks if the player's current location matches the expected next block location in the parkour course.
     *
     * @param player the player whose position is being checked
     * @return true if the player's current location matches the next block's location in the parkour, false otherwise
     */
    private boolean isNextBlock(Player player) {
        return player.getLocation().blockX() == this.playerNextBlock.get(player).blockX()
                && (player.getLocation().blockY() - 1) == this.playerNextBlock.get(player).blockY()
                && player.getLocation().blockZ() == this.playerNextBlock.get(player).blockZ();
    }

    /**
     * Determines if the next block position is directly above the last block position.
     *
     * @param nextPos the location of the next block.
     * @param lastPos the location of the last block.
     * @return true if the next block is directly above the last block on the Y-axis by one unit and has the same X and Z coordinates, otherwise false.
     */
    private boolean isNextBlockUpperLastBlock(Location nextPos, Location lastPos) {
        return lastPos.blockX() == nextPos.blockX()
                && (lastPos.blockY() + 1) == nextPos.blockY()
                && lastPos.blockZ() == nextPos.blockZ();
    }

    /**
     * Starts the parkour for a given player by finding a suitable starting position,
     * initializing player-specific parkour data, and spawning the initial blocks needed.
     * If a valid starting position cannot be found immediately, the method retries.
     *
     * @param player the player who is starting the parkour
     */
    private void startParkour(Player player) {
        Location startPos = this.findRandomAirBlock();

        if (!startPos.getBlock().getType().isAir()) {
            this.startParkour(player);
            return;
        }

        this.playersInParkour.add(player);
        this.playerCheckpoints.put(player, 0);
        this.playerBlockType.put(player, BlockType.values()[this.random.nextInt(BlockType.values().length)]);

        Location blockPos = startPos.clone().add(0, -1, 0);

        Material blockType = this.playerBlockType.get(player).block;

        blockPos.getBlock().setType(blockType);

        player.teleport(startPos.clone().add(0.5, 2, 0.5));

        this.playerLastBlock.put(player, blockPos);
        this.spawnNextParkourBlock(player, blockPos);
    }

    /**
     * Attempts to find a random air block around a specified starting position within a given radius and height variation.
     * The method ensures that the selected block and the two blocks above it are all air blocks.
     * If no suitable location is found within a maximum number of attempts, a default location is returned.
     *
     * @return A {@code Location} object representing the position of the selected air block. If no suitable block is found,
     *         a fallback location above the starting position is returned.
     */
    private Location findRandomAirBlock() {
        World world = JUMP_AND_RUN_POSITION_START.getWorld();
        int baseX = JUMP_AND_RUN_POSITION_START.getBlockX();
        int baseY = JUMP_AND_RUN_POSITION_START.getBlockY();
        int baseZ = JUMP_AND_RUN_POSITION_START.getBlockZ();

        int radius = 75;
        int heightVariation = 30;

        int maxAttempts = 50;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = baseX + random.nextInt(-radius, radius + 1);
            int y = baseY + random.nextInt(-heightVariation/2, heightVariation/2 + 1);
            int z = baseZ + random.nextInt(-radius, radius + 1);

            Location loc = new Location(world, x, y, z);

            if (loc.getBlock().getType().isAir() &&
                    loc.clone().add(0, 1, 0).getBlock().getType().isAir() &&
                    loc.clone().add(0, 2, 0).getBlock().getType().isAir()) {
                return loc;
            }
        }

        return new Location(world, baseX, baseY + 10, baseZ);
    }

    /**
     * Spawns the next parkour block for a player. Determines a suitable
     * location around the player's last position for placing the next
     * block. Attempts to avoid invalid or obstructed locations and sets
     * the block type based on the player's configured block type.
     *
     * If a new block can't be placed after a number of attempts, a
     * fallback position is used.
     *
     * @param player The player for whom the next parkour block is being spawned.
     * @param lastPos The last position of the parkour block associated with the player.
     */
    private void spawnNextParkourBlock(Player player, Location lastPos) {
        Location basePos = lastPos.clone();

        int maxAttempts = 20;
        int attempts = 0;

        while (attempts < maxAttempts) {
            attempts++;

            int offsetX = this.random.nextInt(-3, 3);
            if (offsetX == 0) offsetX = this.random.nextInt(-3, 3) + 1;

            int offsetY = this.random.nextInt(0, 2);

            int offsetZ = this.random.nextInt(-3, 3);
            if (offsetZ == 0) offsetZ = this.random.nextInt(-3, 3) + 1;

            Location newBlockPos = basePos.clone().add(offsetX, offsetY, offsetZ);

            if (!newBlockPos.getBlock().getType().isAir()) {
                continue;
            }

            if (isNextBlockUpperLastBlock(newBlockPos, basePos)) {
                continue;
            }

            newBlockPos.getBlock().setType(this.playerBlockType.get(player).nextBlock);

            if (!this.playerBlockDisplays.containsKey(player)) {
                BlockDisplay entity = (BlockDisplay) player.getWorld().spawnEntity(newBlockPos, EntityType.BLOCK_DISPLAY);
                entity.setBlock(this.playerBlockType.get(player).nextBlock.createBlockData());
                entity.setGravity(false);
                entity.setGlowing(true);

                this.playerBlockDisplays.put(player, entity);
            } else {
                this.playerBlockDisplays.get(player).teleport(newBlockPos);
            }

            this.playerNextBlock.put(player, newBlockPos);

            return;
        }

        Location fallbackPos = basePos.clone().add(2, 1, 0);
        fallbackPos.getBlock().setType(this.playerBlockType.get(player).nextBlock);
        this.playerNextBlock.put(player, fallbackPos);
    }

    /**
     * Removes a player from the parkour course and performs necessary cleanup actions.
     * Optionally enforces a forced removal without sending any messages or effects.
     *
     * @param player The player to be removed from the parkour course.
     * @param force If true, the player will be removed without any notifications
     *              or effects; if false, the player will be notified about the
     *              failure and teleported to the spawn point.
     */
    public void removePlayerFromParkour(Player player, boolean force) {
        if (!playersInParkour.contains(player)) return;

        this.playerLastBlock.get(player).getBlock().setType(Material.AIR);
        this.playerNextBlock.get(player).getBlock().setType(Material.AIR);

        if (this.playerBlockDisplays.containsKey(player)) {
            this.playerBlockDisplays.get(player).remove();
            this.playerBlockDisplays.remove(player);
        }

        this.playersInParkour.remove(player);
        this.playerLastBlock.remove(player);
        this.playerNextBlock.remove(player);
        this.playerBlockType.remove(player);

        if (!player.isOnline()) return;

        if (!force) {
            player.sendMessage("§6§lJump'n Run §8| §cSchade, du hast den Parkour §nnicht§r§c geschafft.");
            player.sendMessage("§6§lJump'n Run §8| §7Deine Checkpoints waren: §a" + this.playerCheckpoints.get(player));
            player.teleport(BedWarsPlugin.getInstance().getSpawnManager().getSpawn());

            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1.0f);
        }

        this.playerCheckpoints.remove(player);
    }

    private enum BlockType {

        GREEN(Material.GREEN_TERRACOTTA, Material.GREEN_STAINED_GLASS),
        RED(Material.RED_TERRACOTTA, Material.RED_STAINED_GLASS),
        BLUE(Material.BLUE_TERRACOTTA, Material.BLUE_STAINED_GLASS),
        WHITE(Material.WHITE_TERRACOTTA, Material.WHITE_STAINED_GLASS),
        BLACK(Material.BLACK_TERRACOTTA, Material.BLACK_STAINED_GLASS),
        YELLOW(Material.YELLOW_TERRACOTTA, Material.YELLOW_STAINED_GLASS),
        ORANGE(Material.ORANGE_TERRACOTTA, Material.ORANGE_STAINED_GLASS),
        MAGENTA(Material.MAGENTA_TERRACOTTA, Material.MAGENTA_STAINED_GLASS),
        LIGHT_BLUE(Material.LIGHT_BLUE_TERRACOTTA, Material.LIGHT_BLUE_STAINED_GLASS),
        LIME(Material.LIME_TERRACOTTA, Material.LIME_STAINED_GLASS),
        GREY(Material.GRAY_TERRACOTTA, Material.GRAY_STAINED_GLASS),
        LIGHT_GREY(Material.LIGHT_GRAY_TERRACOTTA, Material.LIGHT_GRAY_STAINED_GLASS),
        CYAN(Material.CYAN_TERRACOTTA, Material.CYAN_STAINED_GLASS),
        PURPLE(Material.PURPLE_TERRACOTTA, Material.PURPLE_STAINED_GLASS),
        PINK(Material.PINK_TERRACOTTA, Material.PINK_STAINED_GLASS),
        BROWN(Material.BROWN_TERRACOTTA, Material.BROWN_STAINED_GLASS);

        private final Material block;

        private final Material nextBlock;

        BlockType(Material block, Material nextBlock) {
            this.block = block;
            this.nextBlock = nextBlock;
        }
    }

    /**
     * Checks if the given location is within a specified range of any player currently in the parkour.
     *
     * @param location the location to check for proximity to players in the parkour
     * @return true if the location is within 12 blocks of any player in the parkour, false otherwise
     */
    public boolean isWithinRange(Location location) {
        for (Player players : this.playersInParkour) {
            double distance = location.distance(players.getLocation());
            return distance <= 12.0;
        }

        return false;
    }
}
