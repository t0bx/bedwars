package de.t0bx.eindino.config;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.utils.JsonDocument;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final File file;
    private JsonDocument jsonDocument;

    /**
     * Default constructor for the ConfigManager class.
     * Initializes the configuration file and loads its content into memory.
     * The configuration file is created in the plugin's data folder with the name `config.json`.
     * If the file does not exist or is invalid, a new configuration file is created with default values.
     */
    public ConfigManager() {
        this.file = new File(BedWarsPlugin.getInstance().getDataFolder(), "config.json");
        this.loadConfig();
    }

    /**
     * Loads the configuration file and initializes the JsonDocument object.
     * If the configuration file does not exist or is empty, a new JsonDocument object
     * is created with default configuration values, including a "prefix" and "playType".
     * The new configuration is then saved to the file system.
     *
     * This method also updates the plugin's prefix with the value from the loaded configuration.
     *
     * In case of IOException during file operations, the exception stack trace is printed.
     */
    private void loadConfig() {
        try {
            this.jsonDocument = JsonDocument.loadDocument(this.file);

            if (this.jsonDocument == null) {
                this.jsonDocument = new JsonDocument();

                this.jsonDocument.setString("prefix", "<gradient:#00aaaa:#55ffff>BedWars</gradient> <dark_gray>| <gray>");
                this.jsonDocument.setString("playType", "2x1");
                this.jsonDocument.save(this.file);
            }

            BedWarsPlugin.getInstance().setPrefix(this.jsonDocument.get("prefix").getAsString());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Retrieves the "playType" value from the JSON configuration file.
     *
     * The method reloads the internal JSON document from the associated file and then
     * retrieves the value associated with the "playType" key. If the file is not properly
     * loaded or the document is null, it returns {@code null}.
     *
     * @return the "playType" value as a String, or {@code null} if the JSON document is null
     *         or the "playType" key does not exist.
     */
    public String getPlayType() {
        this.jsonDocument = JsonDocument.loadDocument(this.file);
        if (this.jsonDocument == null) return null;

        return this.jsonDocument.get("playType").getAsString();
    }
}
