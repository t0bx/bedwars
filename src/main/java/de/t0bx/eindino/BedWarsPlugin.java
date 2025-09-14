package de.t0bx.eindino;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.eindino.server.api.ServerAPI;
import de.eindino.server.api.database.IMySQLManager;
import de.t0bx.eindino.commands.*;
import de.t0bx.eindino.config.ConfigManager;
import de.t0bx.eindino.config.SpawnManager;
import de.t0bx.eindino.game.GameHandler;
import de.t0bx.eindino.inventory.InventoryProvider;
import de.t0bx.eindino.inventory.inventories.MapVotingInventory;
import de.t0bx.eindino.inventory.inventories.NavigatorInventory;
import de.t0bx.eindino.inventory.inventories.ShopInventory;
import de.t0bx.eindino.listener.*;
import de.t0bx.eindino.listener.ingame.*;
import de.t0bx.eindino.listener.lobby.LobbyCancelListener;
import de.t0bx.eindino.listener.lobby.LobbyInventoryClickListener;
import de.t0bx.eindino.listener.lobby.PlayerLobbyInteractListener;
import de.t0bx.eindino.listener.lobby.PlayerLobbyMoveListener;
import de.t0bx.eindino.manager.ParkourManager;
import de.t0bx.eindino.map.MapData;
import de.t0bx.eindino.map.MapHandler;
import de.t0bx.eindino.player.BedwarsPlayer;
import de.t0bx.eindino.player.PlayerHandler;
import de.t0bx.eindino.scoreboard.ScoreboardBuilder;
import de.t0bx.eindino.spectator.SpectatorHandler;
import de.t0bx.eindino.team.TeamHandler;
import de.t0bx.eindino.utils.NameFetcher;
import de.t0bx.eindino.vote.VotingHandler;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.provider.CloudServiceProvider;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Getter
public final class BedWarsPlugin extends JavaPlugin {

    @Getter
    private static BedWarsPlugin instance;

    private CloudServiceProvider cloudServiceProvider;
    private PlayerManager playerManager;
    private IMySQLManager mySQLManager;

    @Setter
    private String prefix;

    private ConfigManager configManager;
    private List<String> teamNames;
    private SpawnManager spawnManager;
    private PlayerHandler playerHandler;

    private MapHandler mapHandler;
    private VotingHandler votingHandler;

    private TeamHandler teamHandler;
    private ScoreboardBuilder scoreboardBuilder;
    private GameHandler gameHandler;

    private SpectatorHandler spectatorHandler;

    private InventoryProvider inventoryProvider;
    private ShopInventory shopInventory;
    private MapVotingInventory mapVotingInventory;
    private NavigatorInventory navigatorInventory;

    private ParkourManager parkourManager;

    @Setter
    private List<UUID> top5Hologram;

    @Override
    public void onEnable() {
        instance = this;

        this.cloudServiceProvider = InjectionLayer.ext().instance(CloudServiceProvider.class);
        var serviceRegistry = InjectionLayer.boot().instance(ServiceRegistry.class);
        this.playerManager = serviceRegistry.defaultInstance(PlayerManager.class);

        this.mySQLManager = ServerAPI.getInstance().getMySQLManager();
        this.createDefaultSQLTables();
        this.configManager = new ConfigManager();
        this.spawnManager = new SpawnManager();

        this.playerHandler = new PlayerHandler();
        this.mapHandler = new MapHandler(this.getDataFolder());
        this.votingHandler = new VotingHandler();
        this.teamHandler = new TeamHandler();
        this.teamNames = this.initializePlayType(this.configManager.getPlayType());

        for (String teamName : this.teamNames) {
            this.createTeamsForPlayType(teamName, this.configManager.getPlayType());
        }
        this.teamHandler.createTeam("999spectator", MiniMessage.miniMessage().deserialize("<gray>"), NamedTextColor.GRAY, 50);

        this.scoreboardBuilder = new ScoreboardBuilder(this.teamHandler);
        this.gameHandler = new GameHandler(this.configManager.getPlayType());
        this.spectatorHandler = new SpectatorHandler();
        this.inventoryProvider = new InventoryProvider();
        this.shopInventory = new ShopInventory();
        this.mapVotingInventory = new MapVotingInventory();
        this.navigatorInventory = new NavigatorInventory();

        this.parkourManager = new ParkourManager();

        this.setupWorldRules();
        this.setupTop5Wall();
        this.initListener();
        this.initCommands();
        this.top5Hologram = this.spawnHolograms();

        this.getLogger().info("Bedwars enabled");
    }

    @Override
    public void onDisable() {
        this.teamHandler.deleteAllTeams();
    }

    private void initListener() {
        final PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(), this);
        pluginManager.registerEvents(new PlayerQuitListener(), this);
        pluginManager.registerEvents(new PlayerLobbyInteractListener(), this);
        pluginManager.registerEvents(new LobbyCancelListener(), this);
        pluginManager.registerEvents(new LobbyInventoryClickListener(), this);
        pluginManager.registerEvents(new PlayerPreLoginListener(), this);
        pluginManager.registerEvents(new WorldCancelListener(), this);
        pluginManager.registerEvents(new PlayerLobbyMoveListener(this.gameHandler, this.parkourManager), this);

        pluginManager.registerEvents(new PlayerNickListener(this.teamHandler), this);
        pluginManager.registerEvents(new PlayerUnNickListener(this.teamHandler), this);

        pluginManager.registerEvents(new PlayerClickNPCListener(), this);
        pluginManager.registerEvents(new GameInventoryClickListener(), this);
        pluginManager.registerEvents(new PlayerDeathListener(), this);
        pluginManager.registerEvents(new PlayerBedBreakListener(), this);
        pluginManager.registerEvents(new GameListener(), this);
        pluginManager.registerEvents(new SpectatorCancelListener(), this);
        pluginManager.registerEvents(new PlayerChatListener(), this);
        pluginManager.registerEvents(new TeamChestListener(), this);
    }

    private void initCommands() {
        new SetSpawnCommand(this, "setspawn");
        new MapCommand(this, "map");
        new StartCommand(this,  "start");
        new ForceMapCommand(this, "forcemap");
        new StatsCommand(this, "stats");
        new TrollCommand(this, "troll");
    }

    private void setupWorldRules() {
        World world = Bukkit.getWorld("world");
        if (world != null) {
            world.getEntities().forEach(Entity::remove);
            world.setDifficulty(Difficulty.PEACEFUL);
            world.setTime(6000);

            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(0);
            world.setThunderDuration(0);
            world.setClearWeatherDuration(Integer.MAX_VALUE);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);

            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_TILE_DROPS, false);
            world.setGameRule(GameRule.DO_MOB_LOOT, false);
            world.setGameRule(GameRule.FALL_DAMAGE, false);
        }

        for (MapData maps : this.mapHandler.getAllMaps()) {
            World mapWorld = maps.getShops().getFirst().getWorld();
            if (mapWorld != null) {
                mapWorld.getEntities().forEach(Entity::remove);
                mapWorld.setDifficulty(Difficulty.PEACEFUL);
                mapWorld.setTime(6000);

                mapWorld.setStorm(false);
                mapWorld.setThundering(false);
                mapWorld.setWeatherDuration(0);
                mapWorld.setThunderDuration(0);
                mapWorld.setClearWeatherDuration(Integer.MAX_VALUE);
                mapWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);

                mapWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
                mapWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                mapWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                mapWorld.setGameRule(GameRule.DO_MOB_LOOT, false);
            }
        }
    }

    private void createDefaultSQLTables() {
        String playerTable = """
                CREATE TABLE IF NOT EXISTS bedwars_players(
                uuid VARCHAR(36) PRIMARY KEY,
                kills INT(100) DEFAULT 0,
                deaths INT(100) DEFAULT 0,
                wins INT(100) DEFAULT 0,
                gamesPlayed INT(100) DEFAULT 0,
                bedsDestroyed INT(100) DEFAULT 0)
                """;

        this.mySQLManager.asyncUpdate(playerTable);
    }

    private List<String> initializePlayType(String playType) {
        return switch (playType.toLowerCase()) {
            case "2x1", "2x2" -> List.of("red", "blue");
            case "4x1", "4x2", "4x4" -> List.of("red", "blue", "yellow", "green");
            case "8x1", "8x2" -> List.of("red", "blue", "yellow", "green", "orange", "purple", "pink", "black");
            default -> List.of("red", "blue");
        };
    }

    public int maxPlayerCount() {
        String playType = this.configManager.getPlayType();
        String[] playTypeArray = playType.split("\\s*[xX]\\s*");

        return Integer.parseInt(playTypeArray[0]) * Integer.parseInt(playTypeArray[1]);
    }

    private void createTeamsForPlayType(String teamName, String playType) {
        this.getLogger().info("Creating team " + teamName);
        NamedTextColor color = getTeamColor(teamName);
        Component displayName = MiniMessage.miniMessage().deserialize(this.getTeamNameColored(teamName));
        String[] playTypeArray = playType.split("\\s*[xX]\\s*");

        this.teamHandler.createTeam(teamName, displayName, color, Integer.parseInt(playTypeArray[1]));
    }

    private NamedTextColor getTeamColor(String teamName) {
        return switch (teamName.toLowerCase()) {
            case "red" -> NamedTextColor.RED;
            case "blue" -> NamedTextColor.BLUE;
            case "green" -> NamedTextColor.GREEN;
            case "yellow" -> NamedTextColor.YELLOW;
            case "orange" -> NamedTextColor.GOLD;
            case "purple" -> NamedTextColor.DARK_PURPLE;
            case "pink" -> NamedTextColor.LIGHT_PURPLE;
            case "white" -> NamedTextColor.WHITE;
            case "black" -> NamedTextColor.BLACK;
            case "gray", "grey" -> NamedTextColor.GRAY;
            case "aqua" -> NamedTextColor.AQUA;
            case "lime" -> NamedTextColor.GREEN;
            default -> NamedTextColor.WHITE;
        };
    }

    private String getTeamNameColored(String teamName) {
        return switch (teamName.toLowerCase()) {
            case "red" -> "<red>Rot";
            case "blue" -> "<blue>Blau";
            case "green" -> "<green>Grün";
            case "yellow" -> "<yellow>Gelb";
            case "orange" -> "<#ffaa00>Orange";
            case "purple" -> "<#aa00aa>Lila";
            case "pink" -> "<#ff55ff>Pink";
            case "white" -> "<white>Weiß";
            case "black" -> "<black>Schwarz";
            case "gray", "grey" -> "<gray>Grau";
            case "aqua" -> "<aqua>Hellblau";
            case "lime" -> "<#55ff55>Hellgrün";
            default -> "<white>Weiß";
        };
    }

    private void setupTop5Wall() {
        PlayerProfile unknownProfile = Bukkit.createProfile(UUID.randomUUID());
        PlayerTextures textures = unknownProfile.getTextures();

        URL url = null;
        try {
            url = new URL("http://textures.minecraft.net/texture/d34e063cafb467a5c8de43ec78619399f369f4a52434da8017a983cdd92516a0");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        textures.setSkin(url);
        unknownProfile.setTextures(textures);
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            unknownProfile.complete();
        });

        World world = Bukkit.getWorld("world");
        final List<Location> top5HeadLocations = List.of(
                new Location(world, 81, 78, 43),
                new Location(world, 82, 78, 43),
                new Location(world, 83, 78, 43),
                new Location(world, 84, 78, 43),
                new Location(world, 85, 78, 43)
        );
        this.playerHandler.getTop5().thenAccept(top5 -> {
            for (int i = 0; i < top5HeadLocations.size(); i++) {
                int finalI = i;
                if (i < top5.size()) {
                    BedwarsPlayer player = top5.get(i);

                    NameFetcher.getNameAsync(player.getUuid()).thenAccept(name -> {
                        Bukkit.getScheduler().runTask(this, () -> {
                            Location headLocation = top5HeadLocations.get(finalI);
                            Location signLocation = headLocation.clone().add(0, -1, 0);

                            Block headBlock = headLocation.getBlock();
                            headBlock.setType(Material.PLAYER_WALL_HEAD);

                            Skull skull = (Skull) headBlock.getState();

                            Directional directional = (Directional) skull.getBlockData();
                            directional.setFacing(BlockFace.SOUTH);
                            skull.setBlockData(directional);

                            PlayerProfile profile = Bukkit.createProfile(player.getUuid());
                            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                                profile.complete();
                            });

                            skull.setOwnerProfile(profile);
                            skull.update();

                            Block signBlock = signLocation.getBlock();

                            if (signBlock.getState() instanceof Sign sign) {
                                sign.setLine(0, "§6#" + (finalI + 1));
                                sign.setLine(1, "§e" + name);
                                sign.setLine(2, "§7" + player.getWins() + " Wins");
                                sign.setLine(3, "§7" + this.getRoundedKD(player.getKills(), player.getDeaths()) + " K/D");
                                sign.update();
                            }
                        });
                    });
                } else {
                    Bukkit.getScheduler().runTask(this, () -> {
                        Location headLocation = top5HeadLocations.get(finalI);
                        Location signLocation = headLocation.clone().add(0, -1, 0);

                        Block headBlock = headLocation.getBlock();
                        headBlock.setType(Material.PLAYER_WALL_HEAD);

                        Skull skull = (Skull) headBlock.getState();

                        Directional directional = (Directional) skull.getBlockData();
                        directional.setFacing(BlockFace.SOUTH);
                        skull.setBlockData(directional);

                        skull.setOwnerProfile(unknownProfile);
                        skull.update();

                        Block signBlock = signLocation.getBlock();
                        if (signBlock.getState() instanceof Sign sign) {
                            sign.setLine(0, "§6#???");
                            sign.update();
                        }
                    });
                }
            }
        });
    }

    public List<UUID> spawnHolograms() {
        Location location = new Location(Bukkit.getWorld("world"), 83.570, 79, 43.302);
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setSmall(true);
        armorStand.setCustomNameVisible(true);
        armorStand.customName(MiniMessage.miniMessage().deserialize("<gray>» <#ffaa00>Top 5 <gray>«"));
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setMarker(true);
        armorStand.setCollidable(false);

        Location parkourLocation = new Location(Bukkit.getWorld("world"), 70.5, 78, 53.524);
        ArmorStand parkourArmorstand = (ArmorStand) parkourLocation.getWorld().spawnEntity(parkourLocation, EntityType.ARMOR_STAND);
        parkourArmorstand.setVisible(false);
        parkourArmorstand.setSmall(true);
        parkourArmorstand.setCustomNameVisible(true);
        parkourArmorstand.customName(MiniMessage.miniMessage().deserialize("<gray>» <aqua>Jump And Run <gray>«"));
        parkourArmorstand.setGravity(false);
        parkourArmorstand.setInvulnerable(true);
        parkourArmorstand.setMarker(true);
        parkourArmorstand.setCollidable(false);

        return List.of(armorStand.getUniqueId(), parkourArmorstand.getUniqueId());
    }

    public double getRoundedKD(int kills, int deaths) {
        if (deaths == 0) return kills;
        double kd = (double) kills / deaths;
        return Math.round(kd * 100.0) / 100.0;
    }
}
