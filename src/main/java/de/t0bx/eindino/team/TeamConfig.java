package de.t0bx.eindino.team;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public class TeamConfig {

    private Location spawnLocation;
    private Location[] bedLocations;

    /**
     * Default constructor for the TeamConfig class.
     * Initializes the bedLocations array with a fixed size of 2, representing
     * the top and bottom bed locations for the team. By default, the array
     * elements will be set to null.
     */
    public TeamConfig() {
        this.bedLocations = new Location[2];
    }

    /**
     * Constructs a TeamConfig with a specified spawn location and two bed locations.
     *
     * @param spawnLocation the spawn location for the team
     * @param topBed the location of the top part of the team's bed
     * @param bottomBed the location of the bottom part of the team's bed
     */
    public TeamConfig(Location spawnLocation, Location topBed, Location bottomBed) {
        this.spawnLocation = spawnLocation;
        this.bedLocations = new Location[]{topBed, bottomBed};
    }

    /**
     * Sets the top bed location for the team.
     * If the bed locations array is null, it initializes the array with a fixed size of 2.
     *
     * @param location the location to be set as the top bed
     */
    public void setBedTop(Location location) {
        if (bedLocations == null) {
            bedLocations = new Location[2];
        }
        bedLocations[0] = location;
    }

    /**
     * Sets the bottom bed location for the team.
     * Initializes the bedLocations array if it is null.
     *
     * @param location the Location representing the bottom bed position
     */
    public void setBedBottom(Location location) {
        if (bedLocations == null) {
            bedLocations = new Location[2];
        }
        bedLocations[1] = location;
    }

    /**
     * Retrieves the top bed location for the team.
     *
     * @return the top bed location if it exists, otherwise null
     */
    public Location getBedTop() {
        return bedLocations != null && bedLocations.length > 0 ? bedLocations[0] : null;
    }

    /**
     * Retrieves the bottom bed location for the team configuration.
     * If the bed location array is not initialized or does not contain
     * the required index for the bottom bed location, it returns null.
     *
     * @return the Location of the bottom bed configuration if available, otherwise null
     */
    public Location getBedBottom() {
        return bedLocations != null && bedLocations.length > 1 ? bedLocations[1] : null;
    }

    /**
     * Checks if the team has a spawn location configured.
     *
     * @return true if a spawn location is set; false otherwise
     */
    public boolean hasSpawn() {
        return spawnLocation != null;
    }

    /**
     * Checks if both bed locations have been set for the team.
     *
     * @return true if both top and bottom bed locations are not null, false otherwise.
     */
    public boolean hasBeds() {
        return bedLocations != null && bedLocations[0] != null && bedLocations[1] != null;
    }

    /**
     * Determines if the configuration is complete, meaning it has both a spawn location
     * and valid bed locations.
     *
     * @return true if the configuration has a spawn location and two valid bed locations, false otherwise
     */
    public boolean isComplete() {
        return hasSpawn() && hasBeds();
    }

    /**
     * Creates a deep copy of the current TeamConfig object, including its spawn location
     * and bed locations if they are set.
     *
     * @return a new instance of TeamConfig with identical properties to the original
     */
    public TeamConfig copy() {
        TeamConfig copy = new TeamConfig();
        copy.setSpawnLocation(this.spawnLocation != null ? this.spawnLocation.clone() : null);

        if (this.bedLocations != null) {
            copy.bedLocations = new Location[2];
            copy.bedLocations[0] = this.bedLocations[0] != null ? this.bedLocations[0].clone() : null;
            copy.bedLocations[1] = this.bedLocations[1] != null ? this.bedLocations[1].clone() : null;
        }

        return copy;
    }

    /**
     * Returns a string representation of the {@code TeamConfig} instance.
     * The string includes the current state of the spawn location
     * and the existence of the bed top and bottom locations.
     *
     * @return a string describing the spawn, bed top, and bed bottom states
     */
    @Override
    public String toString() {
        return String.format("TeamConfig{spawn=%s, bedTop=%s, bedBottom=%s}",
                hasSpawn(), getBedTop() != null, getBedBottom() != null);
    }
}
