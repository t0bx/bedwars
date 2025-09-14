package de.t0bx.eindino.map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.team.TeamConfig;
import de.t0bx.eindino.team.TeamData;
import de.t0bx.eindino.team.TeamHandler;
import de.t0bx.eindino.utils.JsonDocument;
import de.t0bx.sentienceEntity.SentienceEntity;
import de.t0bx.sentienceEntity.hologram.HologramManager;
import de.t0bx.sentienceEntity.npc.NPCsHandler;
import de.t0bx.sentienceEntity.npc.SentienceNPC;
import de.t0bx.sentienceEntity.utils.SkinFetcher;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Entity;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MapHandler {

    private final Map<String, MapData> loadedMaps;
    @Getter
    private final File mapsFolder;
    private final NPCsHandler npCsHandler;
    private final HologramManager hologramManager;
    private final List<String> npcNames;
    private String skinValue;
    private String skinSignature;

    /**
     * Constructs a new instance of MapHandler. Initializes the folder for storing map data files,
     * loads existing map data, and sets up required managers and dependencies.
     *
     * @param pluginDataFolder the root directory for the plugin's data, used to store and manage map files
     */
    public MapHandler(File pluginDataFolder) {
        this.loadedMaps = new HashMap<>();
        this.mapsFolder = new File(pluginDataFolder, "maps");

        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }
        this.loadAllMaps();
        this.npcNames = new ArrayList<>();
        this.npCsHandler = SentienceEntity.getInstance().getNpcshandler();
        this.hologramManager = SentienceEntity.getInstance().getHologramManager();

        SentienceEntity.getInstance().getSkinFetcher().fetchSkin("einDino_", (skinValue, skinSignature) -> {
            this.skinValue = skinValue;
            this.skinSignature = skinSignature;
        });
    }

    /**
     * Creates a new map with the specified name and play type, stores it in the loaded maps, and returns the created map.
     *
     * @param mapName the name of the map to be created
     * @param playType the type of gameplay associated with the map
     * @return the {@code MapData} object representing the newly created map
     */
    public MapData createMap(String mapName, String playType) {
        MapData mapData = new MapData(mapName, playType);
        loadedMaps.put(mapName, mapData);
        return mapData;
    }

    /**
     * Saves the provided map data to a JSON file on the filesystem.
     *
     * The method converts the {@link MapData} object into a structured JSON format,
     * including details about the map name, play type, teams, shops, spawners, and
     * the spectator location, if available. It then writes the JSON data into a
     * file named after the map in the designated maps folder.
     *
     * @param mapData the {@link MapData} object containing the map's configuration and details to be saved
     * @throws IOException if an I/O error occurs during writing to the file
     */
    public void saveMap(MapData mapData) throws IOException {
        JsonDocument document = new JsonDocument();

        document.setString("mapname", mapData.getMapName());
        document.setString("playType", mapData.getPlayType());

        JsonObject spectator = new JsonObject();
        if (mapData.getSpectatorLocation() != null) {
            spectator.addProperty("world", mapData.getSpectatorLocation().getWorld().getName());
            spectator.addProperty("x", mapData.getSpectatorLocation().getX());
            spectator.addProperty("y", mapData.getSpectatorLocation().getY());
            spectator.addProperty("z", mapData.getSpectatorLocation().getZ());
            spectator.addProperty("yaw", mapData.getSpectatorLocation().getYaw());
            spectator.addProperty("pitch", mapData.getSpectatorLocation().getPitch());
        }
        document.getJsonObject().add("spectator", spectator);

        JsonObject teamsObject = new JsonObject();
        for (Map.Entry<String, TeamConfig> entry : mapData.getTeams().entrySet()) {
            JsonObject teamObject = new JsonObject();
            TeamConfig teamConfig = entry.getValue();

            if (teamConfig.getSpawnLocation() != null) {
                teamObject.add("spawn", locationToJson(teamConfig.getSpawnLocation()));
            }

            if (teamConfig.getBedLocations() != null && teamConfig.getBedLocations().length >= 2) {
                JsonObject bedObject = new JsonObject();
                bedObject.add("top", locationToJson(teamConfig.getBedLocations()[0]));
                bedObject.add("bottom", locationToJson(teamConfig.getBedLocations()[1]));
                teamObject.add("bed", bedObject);
            }

            teamsObject.add(entry.getKey(), teamObject);
        }
        document.getJsonObject().add("teams", teamsObject);

        JsonObject shopsObject = new JsonObject();
        for (int i = 0; i < mapData.getShops().size(); i++) {
            shopsObject.add(String.valueOf(i + 1), locationToJson(mapData.getShops().get(i)));
        }
        document.getJsonObject().add("shops", shopsObject);

        JsonObject spawnersObject = new JsonObject();
        for (Map.Entry<String, List<Location>> entry : mapData.getSpawners().entrySet()) {
            JsonObject spawnerTypeObject = new JsonObject();
            List<Location> locations = entry.getValue();

            for (int i = 0; i < locations.size(); i++) {
                spawnerTypeObject.add(String.valueOf(i + 1), locationToJson(locations.get(i)));
            }

            spawnersObject.add(entry.getKey(), spawnerTypeObject);
        }
        document.getJsonObject().add("spawners", spawnersObject);

        File mapFile = new File(mapsFolder, mapData.getMapName() + ".json");
        document.save(mapFile);
    }

    /**
     * Removes a map by its name from the loaded maps and deletes its corresponding
     * file from the file system.
     *
     * @param mapName the name of the map to be removed
     */
    public void removeMap(String mapName) {
        if (!this.loadedMaps.containsKey(mapName)) return;

        MapData mapData = this.loadedMaps.remove(mapName);
        File file = new File(this.mapsFolder, mapData.getMapName() + ".json");
        file.delete();
    }

    /**
     * Loads all maps from the specified folder and adds them to the collection of loaded maps.
     * Each map is represented by a JSON file in the folder. If the file is valid and can be loaded
     * successfully, the map data is added to the collection.
     *
     * @return the number of maps successfully loaded.
     */
    public int loadAllMaps() {
        loadedMaps.clear();

        File[] files = mapsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return 0;
        }

        int loaded = 0;
        for (File file : files) {
            try {
                MapData mapData = loadMap(file);
                if (mapData != null) {
                    loadedMaps.put(mapData.getMapName(), mapData);
                    loaded++;
                }
            } catch (Exception e) {
                System.err.println("Fehler beim Laden der Map " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return loaded;
    }

    /**
     * Loads map data from the specified JSON configuration file.
     * The method parses the JSON structure to construct a MapData object,
     * including details such as map name, play type, spectator location,
     * teams configurations, shop locations, and spawner configurations.
     *
     * @param file the JSON configuration file containing map details
     * @return a MapData object constructed from the parsed file,
     *         or null if the file is invalid or cannot be loaded
     */
    private MapData loadMap(File file) {
        JsonDocument document = JsonDocument.loadDocument(file);
        if (document == null || document.getJsonObject() == null) {
            return null;
        }

        JsonObject json = document.getJsonObject();

        String mapName = json.has("mapname") ? json.get("mapname").getAsString() :
                file.getName().replace(".json", "");
        String playType = json.has("playType") ? json.get("playType").getAsString() : "2x1";

        MapData mapData = new MapData(mapName, playType);

        if (json.has("spectator") && json.get("spectator").isJsonObject()) {
            JsonObject spectatorObject = json.get("spectator").getAsJsonObject();

            mapData.setSpectatorLocation(new Location(
                    Bukkit.getWorld(spectatorObject.get("world").getAsString()),
                    spectatorObject.get("x").getAsDouble(),
                    spectatorObject.get("y").getAsDouble(),
                    spectatorObject.get("z").getAsDouble(),
                    spectatorObject.get("yaw").getAsFloat(),
                    spectatorObject.get("pitch").getAsFloat()
            ));
        }

        if (json.has("teams") && json.get("teams").isJsonObject()) {
            JsonObject teamsObject = json.getAsJsonObject("teams");

            for (Map.Entry<String, JsonElement> teamEntry : teamsObject.entrySet()) {
                String teamName = teamEntry.getKey();
                JsonObject teamObject = teamEntry.getValue().getAsJsonObject();

                TeamConfig teamConfig = new TeamConfig();

                if (teamObject.has("spawn")) {
                    teamConfig.setSpawnLocation(jsonToLocation(teamObject.getAsJsonObject("spawn")));
                }

                if (teamObject.has("bed") && teamObject.get("bed").isJsonObject()) {
                    JsonObject bedObject = teamObject.getAsJsonObject("bed");
                    Location[] bedLocations = new Location[2];

                    if (bedObject.has("top")) {
                        bedLocations[0] = jsonToLocation(bedObject.getAsJsonObject("top"));
                    }
                    if (bedObject.has("bottom")) {
                        bedLocations[1] = jsonToLocation(bedObject.getAsJsonObject("bottom"));
                    }

                    teamConfig.setBedLocations(bedLocations);
                }

                mapData.addTeam(teamName, teamConfig);
            }
        }

        if (json.has("shops") && json.get("shops").isJsonObject()) {
            JsonObject shopsObject = json.getAsJsonObject("shops");

            for (Map.Entry<String, JsonElement> shopEntry : shopsObject.entrySet()) {
                Location shopLocation = jsonToLocation(shopEntry.getValue().getAsJsonObject());
                mapData.addShop(shopLocation);
            }
        }

        if (json.has("spawners") && json.get("spawners").isJsonObject()) {
            JsonObject spawnersObject = json.getAsJsonObject("spawners");

            for (Map.Entry<String, JsonElement> spawnerTypeEntry : spawnersObject.entrySet()) {
                String spawnerType = spawnerTypeEntry.getKey();
                JsonObject spawnerLocationsObject = spawnerTypeEntry.getValue().getAsJsonObject();

                for (Map.Entry<String, JsonElement> locationEntry : spawnerLocationsObject.entrySet()) {
                    Location spawnerLocation = jsonToLocation(locationEntry.getValue().getAsJsonObject());
                    mapData.addSpawner(spawnerType, spawnerLocation);
                }
            }
        }

        return mapData;
    }

    /**
     * Converts the given Location object to its corresponding JSON representation.
     *
     * @param location the Location object to be converted to JSON
     * @return a JsonObject representing the Location, including its coordinates (x, y, z),
     *         orientation (yaw, pitch), and world name
     */
    private JsonObject locationToJson(Location location) {
        JsonObject locationObject = new JsonObject();
        locationObject.addProperty("x", location.getX());
        locationObject.addProperty("y", location.getY());
        locationObject.addProperty("z", location.getZ());
        locationObject.addProperty("yaw", location.getYaw());
        locationObject.addProperty("pitch", location.getPitch());
        locationObject.addProperty("world", location.getWorld().getName());
        return locationObject;
    }

    /**
     * Converts a JsonObject representation of a location into a Bukkit Location object.
     *
     * @param locationObject the JsonObject containing the location data, which must include the
     *                       fields "world" (String), "x" (double), "y" (double), "z" (double),
     *                       "yaw" (float), and "pitch" (float)
     * @return a Location object created from the data in the provided JsonObject
     * @throws IllegalArgumentException if the world specified in the JsonObject cannot be found
     */
    private Location jsonToLocation(JsonObject locationObject) {
        String worldName = locationObject.get("world").getAsString();
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            throw new IllegalArgumentException("Welt '" + worldName + "' wurde nicht gefunden!");
        }

        double x = locationObject.get("x").getAsDouble();
        double y = locationObject.get("y").getAsDouble();
        double z = locationObject.get("z").getAsDouble();
        float yaw = locationObject.get("yaw").getAsFloat();
        float pitch = locationObject.get("pitch").getAsFloat();

        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Prepares the specified map and its settings for a game session.
     * This includes modifying world settings, cleaning up entities,
     * configuring team data, and creating NPC shopkeepers with holograms.
     *
     * @param mapData an instance of {@code MapData} representing the map to be configured.
     *                Includes team configurations, shop locations, and other map-specific data.
     * @param teamHandler an instance of {@code TeamHandler} to manage team-related operations and store team-specific settings.
     */
    public void setupMapForGame(MapData mapData, TeamHandler teamHandler) {
        Location location = mapData.getShops().getFirst();
        World world = location.getWorld();
        if (world != null) {
            world.getEntities().forEach(Entity::remove);
            world.setDifficulty(Difficulty.EASY);
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
            world.setGameRule(GameRule.DO_MOB_LOOT, false);
        }

        for (Map.Entry<String, TeamConfig> entry : mapData.getTeams().entrySet()) {
            String teamName = entry.getKey();
            TeamConfig teamConfig = entry.getValue();

            TeamData team = teamHandler.getTeam(teamName);

            if (teamConfig.getSpawnLocation() != null) {
                team.setSpawnLocation(teamConfig.getSpawnLocation());
            }

            if (teamConfig.getBedLocations() != null) {
                team.setBedLocation(teamConfig.getBedLocations());
            }
        }

        int index = 0;
        for (Location entityLocation : mapData.getShops()) {
            this.npCsHandler.createNPC("shop_" + index, entityLocation, this.skinValue, this.skinSignature);
            String name = "shop_" + index;
            SentienceNPC npc = this.npCsHandler.getNPC(name);
            this.npCsHandler.updateLookAtPlayer(name);
            this.hologramManager.createHologram(name, npc.getLocation());
            this.hologramManager.addLine(name, "<gray>» <green>Shop <gray>«", false);
            index++;
        }
    }

    /**
     * Retrieves the map data associated with the specified map name.
     *
     * @param mapName the name of the map to retrieve
     * @return the {@code MapData} object associated with the given map name,
     *         or {@code null} if no map with the specified name exists
     */
    public MapData getMap(String mapName) {
        return loadedMaps.get(mapName);
    }

    /**
     * Retrieves all loaded maps managed by the handler.
     *
     * @return a collection containing all MapData instances currently loaded.
     */
    public Collection<MapData> getAllMaps() {
        return loadedMaps.values();
    }

    /**
     * Retrieves a list of all loaded map names.
     *
     * @return a list of strings containing the names of all currently loaded maps
     */
    public List<String> getMapNames() {
        return new ArrayList<>(loadedMaps.keySet());
    }

    /**
     * Checks if a map with the given name exists in the loaded maps.
     *
     * @param mapName the name of the map to check for existence
     * @return true if the map exists in the loaded maps, false otherwise
     */
    public boolean hasMap(String mapName) {
        return loadedMaps.containsKey(mapName);
    }
}
