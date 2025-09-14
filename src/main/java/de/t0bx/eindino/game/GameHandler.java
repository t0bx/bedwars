package de.t0bx.eindino.game;

import de.eindino.server.api.ServerAPI;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.map.MapHandler;
import de.t0bx.eindino.player.PlayerHandler;
import de.t0bx.eindino.team.TeamData;
import de.t0bx.eindino.team.TeamHandler;
import de.t0bx.eindino.utils.ItemProvider;
import de.t0bx.eindino.vote.VotingHandler;
import de.t0bx.sentienceEntity.SentienceEntity;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.modules.bridge.BridgeServiceHelper;
import lombok.Getter;
import lombok.Setter;
import net.asyncproxy.nicksystem.NickSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class GameHandler {

    @Setter
    private GameState currentGameState;

    private final String playType;

    @Setter
    private String currentMap;

    @Setter
    private boolean isGoldActive;

    @Setter
    private boolean forceStart;

    private final PlayerHandler playerHandler;
    private BukkitTask task;
    private final AtomicInteger countdown;
    private final MiniMessage mm;
    private final String prefix;
    private final MapHandler mapHandler;
    private final TeamHandler teamHandler;
    private final VotingHandler votingHandler;

    private BukkitTask gameTask;
    private BukkitTask bronzeTask;
    private BukkitTask ironTask;
    private BukkitTask goldTask;

    private final Map<Block, Long> timeBlocks;
    private final List<ArmorStand> armorStands;

    private final BridgeServiceHelper bridgeServiceHelper;

    private final String gameId;

    private final List<UUID> wasInSpectator;

    /**
     * Initializes a new instance of the GameHandler class with the specified play type.
     *
     * @param playType The type of the game being played. It cannot be null.
     */
    public GameHandler(@NotNull String playType) {
        this.currentGameState = GameState.LOBBY;
        this.playType = playType;
        this.countdown = new AtomicInteger(30);
        this.playerHandler = BedWarsPlugin.getInstance().getPlayerHandler();
        this.mm = MiniMessage.miniMessage();
        this.prefix = BedWarsPlugin.getInstance().getPrefix();
        this.mapHandler = BedWarsPlugin.getInstance().getMapHandler();
        this.teamHandler = BedWarsPlugin.getInstance().getTeamHandler();
        this.votingHandler = BedWarsPlugin.getInstance().getVotingHandler();
        this.timeBlocks = new ConcurrentHashMap<>();
        this.armorStands = new ArrayList<>();
        this.bridgeServiceHelper = InjectionLayer.ext().instance(BridgeServiceHelper.class);
        this.setForceStart(false);
        this.setGoldActive(false);
        this.gameId = this.generateGameId();
        this.wasInSpectator = new ArrayList<>();
    }

    /**
     * Starts the countdown sequence for the game. This method manages a timed countdown,
     * providing messages and actions at specific intervals to prepare players for the game start.
     * It includes the following behaviors:
     *
     * - Resets any existing countdown task and initializes the countdown timer to 30 seconds.
     * - Cancels the countdown and terminates if certain conditions (such as the readiness of players) are not met.
     * - Broadcasts messages and plays sounds to players at specific intervals (e.g., 30, 20, 15, 10 seconds).
     * - Ends voting for the game map and other settings, notifying players of the selected options.
     * - Handles the final seconds (1–9) with additional notifications.
     * - Distributes unassigned players to teams and starts the game when the countdown reaches 0.
     * - Updates player levels to reflect the remaining countdown duration.
     *
     * The countdown executes periodically every second and interacts with multiple components,
     * including player teams, voting results, and game map setup.
     */
    public void startCountdown() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }

        this.countdown.set(30);
        this.task = Bukkit.getScheduler().runTaskTimer(BedWarsPlugin.getInstance(), () -> {
            int currentCountdown = this.countdown.getAndDecrement();
            if (!this.isForceStart()) {
                if (!this.checkIfAbleToStart()) {
                    this.killCountdown();
                    return;
                }
            }

            if (currentCountdown <= 0) {
                List<Player> notInTeams = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (this.teamHandler.getPlayerTeam(player) == null) {
                        notInTeams.add(player);
                    }
                    player.setLevel(0);
                    player.getInventory().clear();
                    player.sendMessage(this.mm.deserialize(this.prefix + "<green>Das Spiel startet jetzt!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f);
                }
                this.teamHandler.distributePlayers(notInTeams);
                this.task.cancel();
                this.startGame();
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setLevel(currentCountdown);
            }

            if (currentCountdown == 20 || currentCountdown == 30) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Das Spiel startet in <green>" + currentCountdown + " <gray>Sekunden!"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                }
            }

            if (currentCountdown == 15 || currentCountdown == 25) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Die Voting-Phase endet in <green>" + (currentCountdown - 10) + " <gray>Sekunden!"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                }
            }

            if (currentCountdown == 10) {
                this.setCurrentMap(this.votingHandler.getVotedMap());
                this.setGoldActive(this.votingHandler.getVotedGold());

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.getInventory().setItem(4, null);
                    player.sendMessage(this.mm.deserialize(this.prefix + "Die Voting-Phase ist jetzt beendet!"));
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.6f, 1.0f);
                    player.showTitle(Title.title(this.mm.deserialize("<gray>Es wird auf der Map"), this.mm.deserialize("<green>" + this.getCurrentMap() + " <gray>gespielt.")));
                    player.sendMessage(this.mm.deserialize(this.prefix + " "));
                    player.sendMessage(this.mm.deserialize(this.prefix + "Map <dark_gray>» <green>" + this.getCurrentMap() + "<gray>."));
                    player.sendMessage(this.mm.deserialize(this.prefix + "Gold <dark_gray>» <green>" + (this.isGoldActive() ? "<green>Aktiviert" : "<red>Deaktiviert")));
                    player.sendMessage(this.mm.deserialize(this.prefix + " "));
                }
                this.mapHandler.setupMapForGame(this.mapHandler.getMap(this.getCurrentMap()), this.teamHandler);
            }

            if (currentCountdown > 1 && currentCountdown < 10) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Das Spiel startet in <green>" + currentCountdown + " <gray>Sekunden!"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                }
            }

            if (currentCountdown == 1) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Das Spiel startet in <green>" + currentCountdown + " <gray>Sekunde!"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                }
            }
        }, 0L, 20L);
    }

    /**
     * Starts the game, initializing all necessary game states, tasks, and configurations.
     *
     * Preparation includes setting up game modes, teleporting players, initiating resource spawning systems,
     * and handling the state of teams and players.
     *
     * Responsibilities:
     * - Changes the game state to "In Game".
     * - Disables the active tab list feature via the server API.
     * - Configures the nickname system for game mode.
     * - Updates the bed destruction and player state for all teams.
     * - Initiates various resources spawners (copper, iron, gold if active) for the game duration.
     * - Constructs in-game scoreboards for each player.
     * - Ensures parkour mode handling for players is reset for in-game activities.
     * - Establishes countdown timers to manage game timing events such as the auto-end condition.
     * - Clears and removes entities associated with pre-game spawning (e.g., holograms).
     *
     * In the course of game timing:
     * - Sends action bar notifications to players indicating remaining time and other relevant data.
     * - Ends the game when the timer reaches zero and triggers appropriate cleanup.
     */
    public void startGame() {
        this.setCurrentGameState(GameState.IN_GAME);
        ServerAPI.getInstance().setActiveTabList(false);
        NickSystem.getInstance().getNickManager().setGame(true);
        this.bridgeServiceHelper.changeToIngame();
        for (TeamData teams : this.teamHandler.getAllTeams()) {
            if (teams.getPlayerCount() <= 0) {
                teams.getBedLocation()[0].getBlock().setType(Material.AIR);
                teams.getBedLocation()[1].getBlock().setType(Material.AIR);
                continue;
            }

            teams.setBedDestroyed(false);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.teamHandler.getPlayerTeam(player) == null) continue;

            this.playerHandler.addGamesPlayed(player.getUniqueId(), 1);
            this.teamHandler.getPlayerTeam(player).getPlayersAlive().add(player);
            player.teleport(this.teamHandler.getPlayerTeam(player).getSpawnLocation());
            SentienceEntity.getApi().getHologramManager().showAllHolograms(player);
            BedWarsPlugin.getInstance().getScoreboardBuilder().buildInGameScoreboard(player);
            BedWarsPlugin.getInstance().getParkourManager().removePlayerFromParkour(player, true);
        }

        this.countdown.set(1800);
        this.startCopperTask();
        this.startIronTask();
        this.startGameTask();
        if (this.isGoldActive()) {
            this.startGoldTask();
        }
        Location location = BedWarsPlugin.getInstance().getSpawnManager().getSpawn();
        if (location != null) {
            for (UUID uuid : BedWarsPlugin.getInstance().getTop5Hologram()) {
                Entity entity = location.getWorld().getEntity(uuid);
                if (entity != null) {
                    entity.remove();
                }
            }
        }
        this.task = Bukkit.getScheduler().runTaskTimer(BedWarsPlugin.getInstance(), () -> {
            if (Bukkit.isStopping()) {
                this.task.cancel();
                this.gameTask.cancel();
                this.bronzeTask.cancel();
                this.ironTask.cancel();
                if (this.goldTask != null) {
                    this.goldTask.cancel();
                }
                return;
            }

            int currentCountdown = this.countdown.getAndDecrement();
            if (currentCountdown == 0) {
                this.task.cancel();
                this.endGame(null);
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendActionBar(this.mm.deserialize("<dark_gray>» <gray>Das Spiel endet in <green>" + this.convertGameCountdown(currentCountdown) + " <dark_gray>| <gray>GameId: <green>" + this.gameId + " <dark_gray>«"));
            }
        }, 0L, 20L);
    }

    /**
     * Ends the current game session, performs all necessary cleanup, and initiates server shutdown.
     * This method notifies all players, resets their state (inventory, health, etc.), and performs team-related and game state-related operations.
     * If a winning team is provided, rewards are distributed and a winning announcement is made.
     * The method schedules a countdown timer to shut down the server after a specified time.
     *
     * @param teamData the team that won the game, or null if there is no winner
     */
    public void endGame(@Nullable TeamData teamData) {
        this.setCurrentGameState(GameState.END);
        this.task.cancel();
        this.bronzeTask.cancel();
        this.ironTask.cancel();
        if (this.goldTask != null) {
            this.goldTask.cancel();
        }

        BedWarsPlugin.getInstance().spawnHolograms();
        ServerAPI.getInstance().setActiveTabList(true);
        BedWarsPlugin.getInstance().getSpectatorHandler().removeAll();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(BedWarsPlugin.getInstance().getSpawnManager().getSpawn());
            player.getInventory().clear();
            player.setHealth(20);
            player.setFoodLevel(20);
            if (NickSystem.getInstance().getNickManager().isNickedPlayer(player)) {
                NickSystem.getInstance().getNickManager().unnickPlayer(player);
            }
        }

        if (teamData != null) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                players.playSound(players.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                players.showTitle(Title.title(this.mm.deserialize("<gray>Team " + this.mm.serialize(teamData.getDisplayName())), this.mm.deserialize("<green>hat das Spiel gewonnen!")));
            }

            int nuggets = new Random().nextInt(150, 300);
            for (Player players : teamData.getPlayers()) {
                this.playerHandler.addWins(players.getUniqueId(), 1);
                ServerAPI.getInstance().getNuggetManager().addNuggets(players.getUniqueId(), nuggets);
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.teamHandler.getPlayerTeam(player) != null) {
                this.teamHandler.removePlayerFromTeam(player);
                ServerAPI.getInstance().getTabListManager().updateTab(player);
            }
        }

        AtomicInteger rest = new AtomicInteger(10);
        Bukkit.getScheduler().runTaskTimer(BedWarsPlugin.getInstance(), (runnable) -> {
            int currentCountdown = rest.getAndDecrement();
            if (currentCountdown <= 0) {
                runnable.cancel();
                Bukkit.getServer().shutdown();
                return;
            }

            if (currentCountdown == 1) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(this.mm.deserialize(this.prefix + "Der Server schließt in <green>" + currentCountdown + " <gray>Sekunde!"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                }
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(this.mm.deserialize(this.prefix + "Der Server schließt in <green>" + currentCountdown + " <gray>Sekunden!"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            }
        }, 0L, 20L);
    }

    /**
     * Initiates and manages the game task which recurrently processes specific in-game logic.
     *
     * If there is an existing game task running, it will be canceled before a new task is started.
     * The new task executes at regular intervals to manage blocks whose type needs to be updated
     * based on a timestamp. Specifically, it checks and removes blocks whose timestamps have expired,
     * replacing their material type with air.
     *
     * The method is typically used as part of the game lifecycle to handle time-sensitive logic
     * related to blocks in the game.
     */
    private void startGameTask() {
        if (this.gameTask != null) {
            this.gameTask.cancel();
        }

        this.gameTask = Bukkit.getScheduler().runTaskTimer(BedWarsPlugin.getInstance(), () -> {
            Iterator<Map.Entry<Block, Long>> iterator = this.timeBlocks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Block, Long> entry = iterator.next();
                if (entry.getValue() <= System.currentTimeMillis()) {
                    entry.getKey().setType(Material.AIR);
                    iterator.remove();
                }
            }
        }, 0L, 20L);
    }

    /**
     * Initiates the copper resource spawning task for the game. This method handles
     * scheduling a repeating task that spawns copper ingots at predefined spawner
     * locations within the game map. If an existing bronze task is active, it is
     * canceled before starting the new task.
     *
     * This method:
     * - Retrieves all spawner locations for the "bronze" type from the current game map.
     * - Adjusts the Y-coordinate of each spawner location slightly to ensure proper
     *   item drop placement.
     * - Schedules a repeating task using the Bukkit scheduler, which periodically
     *   spawns {@code Material.COPPER_INGOT} items at the specified locations.
     *
     * The task runs with an initial delay of 0 ticks and repeats every 15 ticks.
     *
     * Precondition:
     * - The map associated with the current game session must have valid "bronze"
     *   spawner locations defined.
     *
     * Postcondition:
     * - A new scheduled task for spawning copper ingots is running. The previous
     *   task, if any, is terminated.
     */
    private void startCopperTask() {
        ItemStack item = new ItemStack(Material.COPPER_INGOT);
        if (this.bronzeTask != null) {
            this.bronzeTask.cancel();
        }

        List<Location> locations = new ArrayList<>(this.mapHandler.getMap(this.getCurrentMap()).getSpawners("bronze"));
        locations.forEach(location -> location.add(0, 0.2, 0));

        this.bronzeTask = Bukkit.getScheduler().runTaskTimer(BedWarsPlugin.getInstance(), () -> {
            for (Location location : locations) {
                location.getWorld().dropItem(location, item);
            }
        }, 0L, 15L);
    }

    /**
     * Initializes and starts the iron task for the game. The iron task periodically drops iron ingots
     * at specified spawn locations within the game map.
     *
     * The method performs the following sequence of actions:
     * - Cancels any existing iron task if one is active.
     * - Retrieves the list of iron spawner locations from the current map.
     * - Adjusts the Y-coordinate of the spawn locations by increasing it by 0.2 units.
     * - Schedules a repeating task that spawns iron ingots at these locations every 300 ticks.
     *
     * The task utilizes `BukkitScheduler` to handle scheduled execution.
     *
     * Note:
     * - The current map is determined using the `getCurrentMap` method.
     * - The iron spawner locations are retrieved using the `getSpawners("iron")` method
     *   from the map data of the current map.
     */
    private void startIronTask() {
        ItemStack item = new ItemStack(Material.IRON_INGOT);
        if (this.ironTask != null) {
            this.ironTask.cancel();
        }

        List<Location> locations = new ArrayList<>(this.mapHandler.getMap(this.getCurrentMap()).getSpawners("iron"));
        locations.forEach(location -> location.add(0, 0.2, 0));

        this.ironTask = Bukkit.getScheduler().runTaskTimer(BedWarsPlugin.getInstance(), () -> {
            for (Location location : locations) {
                location.getWorld().dropItem(location, item);
            }
        }, 300L, 300L);
    }

    /**
     * Starts the gold task, managing the spawning of gold ingots and updating related holograms
     * at designated spawner locations.
     *
     * This method performs the following:
     * 1. Cancels any existing gold task if active.
     * 2. Retrieves all "gold" spawner locations from the current map and adjusts their coordinates for spawning and holograms.
     * 3. Spawns holograms (armor stands) at each spawner location to display countdown information.
     * 4. Sets up a periodic task that:
     *    - Decrements a countdown timer.
     *    - Drops gold ingots at the spawner locations when the countdown reaches zero, then resets the timer.
     *    - Updates the holograms' text to reflect the current countdown state.
     *
     * Internally, it uses an {@link AtomicInteger} to manage the countdown and leverages the server's task scheduler
     * to execute actions at a fixed interval of 20 ticks (1 second). This helps manage server-side timing for gold spawning
     * events and updates the display of hologram entities tied to the respective spawn locations.
     */
    private void startGoldTask() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        if (this.goldTask != null) {
            this.goldTask.cancel();
        }

        List<Location> locations = this.mapHandler.getMap(this.getCurrentMap()).getSpawners("gold");
        locations.forEach(location -> location.add(0, 0.2, 0));

        for (Location location : locations) {
            Location armorStandLocation = location.clone().add(0, 1, 0);
            this.armorStands.add(this.spawnGoldHologram(armorStandLocation));
        }

        AtomicInteger goldCountdown = new AtomicInteger(40);
        this.goldTask = Bukkit.getScheduler().runTaskTimer(BedWarsPlugin.getInstance(), () -> {
            int currentGold = goldCountdown.getAndDecrement();

            if (currentGold <= 0) {
                for (Location location : locations) {
                    location.getWorld().dropItem(location, item);
                }
                goldCountdown.set(40);
            }

            Component customName = this.mm.deserialize("<#ffaa00>Gold <dark_gray>» <gray>" + convertGameCountdown(currentGold));
            for (ArmorStand armorStand : this.armorStands) {
                armorStand.customName(customName);
            }
        }, 0L, 20L);
    }

    /**
     * Checks whether the game is ready to start based on the play type and the number of online players.
     * It evaluates different conditions for specific play types and ensures the minimum or exact number
     * of players required are online.
     *
     * @return true if the game meets the conditions required for the specific play type to start, otherwise false
     */
    public synchronized boolean checkIfAbleToStart() {
        if (this.getPlayType().equalsIgnoreCase("2x1") ||
                this.getPlayType().equalsIgnoreCase("4x1")) {
            return Bukkit.getOnlinePlayers().size() >= 2;
        }

        if (this.getPlayType().equalsIgnoreCase("2x2")) {
            return Bukkit.getOnlinePlayers().size() == 4;
        }

        if (this.getPlayType().equalsIgnoreCase("8x1") ||
                this.getPlayType().equalsIgnoreCase("4x2")) {
            return Bukkit.getOnlinePlayers().size() >= 4;
        }

        if (this.getPlayType().equalsIgnoreCase("4x4")) {
            return Bukkit.getOnlinePlayers().size() >= 8;
        }

        return false;
    }

    /**
     * Determines the number of additional players required to meet the player count
     * required to start the game based on the current play type. The result depends
     * on the play type and the number of online players.
     *
     * @return the number of players still needed to start the game. Returns 1 for
     *         play types "2x1" and "4x1". For "8x1", "4x2", and "2x2", returns
     *         4 minus the number of online players. For "4x4", returns 8 minus the
     *         number of online players. Returns 0 if the play type does not match
     *         any of the predefined types.
     */
    public synchronized int getRestPlayersNeeded() {
        if (this.getPlayType().equalsIgnoreCase("2x1") ||
                this.getPlayType().equalsIgnoreCase("4x1")) {
            return 1;
        }

        if (this.getPlayType().equalsIgnoreCase("8x1") ||
                this.getPlayType().equalsIgnoreCase("4x2") ||
                this.getPlayType().equalsIgnoreCase("2x2")) {
            return 4 - Bukkit.getOnlinePlayers().size();
        }

        if (this.getPlayType().equalsIgnoreCase("4x4")) {
            return 8 - Bukkit.getOnlinePlayers().size();
        }

        return 0;
    }

    /**
     * Cancels the current countdown task and resets relevant state and player attributes.
     * This method is typically invoked when a player leaves the game during the lobby phase,
     * rendering the countdown invalid.
     *
     * The method performs the following actions:
     * - Cancels the active countdown task and sets it to null.
     * - Resets the level for all online players to 0.
     * - Provides players with the appropriate lobby items.
     * - Broadcasts messages to inform players about the countdown cancellation and the
     *   number of players required to begin the game.
     * - Plays specific sounds for all players to signify the countdown cancellation.
     *
     * Preconditions:
     * - This method assumes it is called during the lobby phase, prior to the game start.
     * - The `task` field should reference the active countdown timer task.
     *
     * Postconditions:
     * - The countdown task is terminated, and the game state returns to its initial lobby state.
     * - All online players receive lobby-specific items.
     *
     * This method integrates with:
     * - `givePlayerLobbyItems(Player)` to equip players with lobby items.
     * - `getRestPlayersNeeded()` to calculate the number of players still needed to begin the game.
     */
    public void killCountdown() {
        this.task.cancel();
        this.task = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setLevel(0);
            this.givePlayerLobbyItems(player);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1.0f, 1.0f);
            player.sendMessage(this.mm.deserialize(this.prefix + "<red>Der Countdown wurde abgebrochen da ein Spieler das Spiel verlassen hat!"));
            if (this.getRestPlayersNeeded() == 1) {
                Bukkit.broadcast(this.mm.deserialize(this.prefix + "Es wird noch <green>1 <gray>weiterer Spieler benötigt damit das Spiel starten kann."));
            } else {
                Bukkit.broadcast(this.mm.deserialize(this.prefix + "Es werden noch <green>" + this.getRestPlayersNeeded() + " <gray>weitere Spieler benötigt damit das Spiel starten kann."));
            }
        }
    }

    /**
     * Converts a total number of seconds into a formatted string representing minutes and seconds.
     * The format of the returned string is "MM:SS", where both minutes and seconds are always padded
     * with zeros if they are single digits.
     *
     * @param totalSeconds the total number of seconds to be converted
     * @return a formatted string representing the provided time in "MM:SS" format
     */
    private String convertGameCountdown(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Provides the player with specific items to be used in the game lobby.
     * Clears the player's current inventory and sets predefined items in specific slots.
     *
     * @param player The player to whom the lobby items will be given.
     */
    private void givePlayerLobbyItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(0, new ItemProvider(Material.RED_BED).setName("<gray>» <red>Teamauswahl").build());
        player.getInventory().setItem(4, new ItemProvider(Material.PAPER).setName("<gray>» <green>Voting").build());
        player.getInventory().setItem(8, new ItemProvider(Material.SLIME_BALL).setName("<gray>» <red>Spiel Verlassen").build());
    }

    /**
     * Spawns a holographic armor stand to represent a gold spawner at the specified location.
     * The armor stand is configured to be invisible, non-collidable, invulnerable, and display a custom name.
     *
     * @param location The location where the holographic armor stand should be spawned.
     * @return The spawned ArmorStand object configured as a hologram.
     */
    private ArmorStand spawnGoldHologram(Location location) {
        World world = location.getWorld();
        ArmorStand armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.setCollidable(false);
        armorStand.setInvulnerable(true);
        armorStand.setCustomNameVisible(true);
        armorStand.setSmall(true);
        armorStand.setMarker(true);
        return armorStand;
    }

    /**
     * Generates a unique game ID consisting of a random sequence of uppercase
     * letters and digits. The generated ID will always have a fixed length of 5 characters.
     *
     * @return A randomly generated string representing the unique game ID.
     */
    private String generateGameId() {
        final int length = 5;
        final String vaildKeys = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789";
        final StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final int index = (int) (vaildKeys.length() * Math.random());
            stringBuilder.append(vaildKeys.charAt(index));
        }
        return stringBuilder.toString();
    }
}
