package de.t0bx.eindino.map;

import de.t0bx.eindino.team.TeamConfig;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.*;

@Getter
@Setter
public class MapData {

    private String mapName;
    private String playType;
    private Map<String, TeamConfig> teams;
    private List<Location> shops;
    private Map<String, List<Location>> spawners;
    private Location spectatorLocation;

    /**
     * Constructs a MapData object with the specified map name and play type.
     *
     * @param mapName the name of the map
     * @param playType the type of gameplay associated with the map
     */
    public MapData(String mapName, String playType) {
        this.mapName = mapName;
        this.playType = playType;
        this.teams = new HashMap<>();
        this.shops = new ArrayList<>();
        this.spawners = new HashMap<>();
    }

    /**
     * Adds a team to the map with the specified team name and configuration.
     *
     * @param teamName the name of the team to add
     * @param teamConfig the configuration of the team including spawn location and bed locations
     */
    public void addTeam(String teamName, TeamConfig teamConfig) {
        teams.put(teamName, teamConfig);
    }

    /**
     * Removes a team with the specified name from the collection of teams.
     *
     * @param teamName the name of the team to be removed
     */
    public void removeTeam(String teamName) {
        teams.remove(teamName);
    }

    /**
     * Retrieves the configuration for a specific team based on the team name.
     *
     * @param teamName the name of the team whose configuration is to be retrieved
     * @return the {@code TeamConfig} associated with the specified team name,
     *         or {@code null} if no configuration exists for that team
     */
    public TeamConfig getTeam(String teamName) {
        return teams.get(teamName);
    }

    /**
     * Checks if a team with the specified name exists in the map.
     *
     * @param teamName the name of the team to check for existence
     * @return true if the team exists; false otherwise
     */
    public boolean hasTeam(String teamName) {
        return teams.containsKey(teamName);
    }

    /**
     * Retrieves the set of team names registered in the map.
     *
     * @return a set containing the names of all teams. The set is derived from the keys of the internal team map.
     */
    public Set<String> getTeamNames() {
        return teams.keySet();
    }

    /**
     * Retrieves the number of teams currently stored in the map data.
     *
     * @return the total count of teams.
     */
    public int getTeamCount() {
        return teams.size();
    }

    /**
     * Adds a shop to the list of shops in the map.
     *
     * @param location the location of the shop to be added
     */
    public void addShop(Location location) {
        shops.add(location);
    }

    /**
     * Removes the shop at the specified location from the map.
     *
     * @param location the location of the shop to be removed
     */
    public void removeShop(Location location) {
        shops.remove(location);
    }

    /**
     * Removes a shop at the specified index from the list of shops.
     *
     * @param index The index of the shop to remove. Must be a valid index within the bounds of the list of shops.
     */
    public void removeShop(int index) {
        if (index >= 0 && index < shops.size()) {
            shops.remove(index);
        }
    }

    /**
     * Removes all shop locations from the current map data.
     * This method clears the internal collection that stores shop locations,
     * effectively resetting the shop information associated with the map.
     */
    public void clearShops() {
        shops.clear();
    }

    /**
     * Retrieves the total number of shops currently present in the map.
     *
     * @return the count of shops.
     */
    public int getShopCount() {
        return shops.size();
    }

    /**
     * Adds a spawner of the specified type to the collection of spawners at the given location.
     *
     * @param type the type of the spawner to add
     * @param location the location where the spawner should be added
     */
    public void addSpawner(String type, Location location) {
        spawners.computeIfAbsent(type, _ -> new ArrayList<>()).add(location);
    }

    /**
     * Removes a specific spawner of the given type from the specified location.
     * If no spawners of the given type remain after removal, the type is removed from the spawner list.
     *
     * @param type the type of the spawner to be removed
     * @param location the location of the spawner to be removed
     */
    public void removeSpawner(String type, Location location) {
        List<Location> typeSpawners = spawners.get(type);
        if (typeSpawners != null) {
            typeSpawners.remove(location);
            if (typeSpawners.isEmpty()) {
                spawners.remove(type);
            }
        }
    }

    /**
     * Removes a spawner of the specified type at the given index. If the
     * specified type has no more spawners after removal, it is also removed
     * from the spawner mapping.
     *
     * @param type the type of the spawner to be removed
     * @param index the index of the spawner within the list of the specified type to be removed
     */
    public void removeSpawner(String type, int index) {
        List<Location> typeSpawners = spawners.get(type);
        if (typeSpawners != null && index >= 0 && index < typeSpawners.size()) {
            typeSpawners.remove(index);
            if (typeSpawners.isEmpty()) {
                spawners.remove(type);
            }
        }
    }

    /**
     * Removes all spawners of the specified type from the spawner collection.
     *
     * @param type the type of spawners to be cleared
     */
    public void clearSpawners(String type) {
        spawners.remove(type);
    }

    /**
     * Removes all registered spawners from the map data.
     * This method clears the collection of spawners, leaving it empty.
     */
    public void clearAllSpawners() {
        spawners.clear();
    }

    /**
     * Retrieves a list of spawner locations associated with the specified spawner type.
     *
     * @param type the type of spawner whose locations are to be retrieved
     * @return a list of {@link Location} objects for the specified spawner type;
     *         if no spawners of the given type exist, an empty list is returned
     */
    public List<Location> getSpawners(String type) {
        return spawners.getOrDefault(type, new ArrayList<>());
    }

    /**
     * Retrieves the set of all spawner types currently defined in the map.
     *
     * @return a set containing the names of all spawner types.
     */
    public Set<String> getSpawnerTypes() {
        return spawners.keySet();
    }

    /**
     * Retrieves the count of spawners associated with the specified type.
     *
     * @param type the type of spawners to count, represented as a String
     * @return the number of spawners of the specified type; returns 0 if no spawners of the type are found
     */
    public int getSpawnerCount(String type) {
        List<Location> typeSpawners = spawners.get(type);
        return typeSpawners != null ? typeSpawners.size() : 0;
    }

    /**
     * Calculates the total number of spawners by summing the sizes
     * of all spawner lists within the spawners map.
     *
     * @return the total count of all spawners across all spawner types
     */
    public int getTotalSpawnerCount() {
        return spawners.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Checks if the specified spawner type exists and contains at least one location.
     *
     * @param type the type of spawner to check
     * @return {@code true} if the specified spawner type exists and has at least one location, {@code false} otherwise
     */
    public boolean hasSpawners(String type) {
        return spawners.containsKey(type) && !spawners.get(type).isEmpty();
    }

    /**
     * Determines whether the current map configuration is valid. A valid configuration
     * requires the map name and play type to be non-null and non-empty, and the teams
     * collection to have at least one entry.
     *
     * @return true if the map name and play type are non-null and non-empty, and the
     *         teams collection is not empty; false otherwise.
     */
    public boolean isValid() {
        return mapName != null && !mapName.isEmpty() &&
                playType != null && !playType.isEmpty() &&
                !teams.isEmpty();
    }

    /**
     * Creates a deep copy of the current MapData object, including all teams,
     * shops, and spawners. This method ensures that any modifications to the copy
     * will not affect the original MapData and vice versa.
     *
     * @return a new instance of MapData that is a deep copy of the original, with
     *         identical map name, play type, and all associated configurations.
     */
    public MapData copy() {
        MapData copy = new MapData(this.mapName, this.playType);

        for (Map.Entry<String, TeamConfig> entry : this.teams.entrySet()) {
            copy.addTeam(entry.getKey(), entry.getValue().copy());
        }

        copy.shops.addAll(this.shops);

        for (Map.Entry<String, List<Location>> entry : this.spawners.entrySet()) {
            copy.spawners.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        return copy;
    }

    /**
     * Returns a string representation of the MapData object, including its
     * name, play type, number of teams, number of shops, and total spawner count.
     *
     * @return a formatted string containing the details of the MapData instance.
     */
    @Override
    public String toString() {
        return String.format("MapData{name='%s', playType='%s', teams=%d, shops=%d, spawners=%d}",
                mapName, playType, teams.size(), shops.size(), getTotalSpawnerCount());
    }
}
