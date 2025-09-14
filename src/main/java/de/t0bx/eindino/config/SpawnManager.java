package de.t0bx.eindino.config;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.utils.JsonDocument;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;

public class SpawnManager {

    private final File file;
    private JsonDocument jsonDocument;

    /**
     * Constructs a new instance of SpawnManager.
     * This constructor initializes a file instance pointing to "spawn.json"
     * located in the data folder of the BedWarsPlugin.
     */
    public SpawnManager() {
        this.file = new File(BedWarsPlugin.getInstance().getDataFolder(), "spawn.json");
    }

    /**
     * Sets the spawn location and saves it to a JSON file.
     *
     * @param location the {@link Location} object representing the new spawn point.
     *                 The location object must contain coordinates (x, y, z),
     *                 orientation (yaw, pitch), and a reference to the world name.
     */
    public void setSpawn(Location location) {
        try {
            this.jsonDocument = JsonDocument.loadDocument(this.file);

            if (this.jsonDocument == null) {
                this.jsonDocument = new JsonDocument();
            }

            this.jsonDocument.setNumber("x", location.getX());
            this.jsonDocument.setNumber("y", location.getY());
            this.jsonDocument.setNumber("z", location.getZ());
            this.jsonDocument.setNumber("yaw", location.getYaw());
            this.jsonDocument.setNumber("pitch", location.getPitch());
            this.jsonDocument.setString("world", location.getWorld().getName());
            this.jsonDocument.save(this.file);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Retrieves the spawn location from the stored JSON document.
     * If the JSON document is not available or an error occurs, this method will return null.
     *
     * @return the Location representing the spawn point, or null if the JSON document is unavailable or invalid.
     */
    public Location getSpawn() {
        this.jsonDocument = JsonDocument.loadDocument(this.file);
        if (this.jsonDocument == null) return null;

        return new Location(
                Bukkit.getWorld(this.jsonDocument.get("world").getAsString()),
                this.jsonDocument.get("x").getAsDouble(),
                this.jsonDocument.get("y").getAsDouble(),
                this.jsonDocument.get("z").getAsDouble(),
                this.jsonDocument.get("yaw").getAsFloat(),
                this.jsonDocument.get("pitch").getAsFloat()
        );
    }
}
