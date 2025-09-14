package de.t0bx.eindino.utils;

import com.google.gson.*;
import de.t0bx.eindino.BedWarsPlugin;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class JsonDocument {

    @Getter
    @Setter
    private JsonObject jsonObject;

    private final Gson gson;

    /**
     * Constructs a new, empty {@code JsonDocument} instance.
     *
     * This constructor initializes the underlying {@code JsonObject} to an empty object
     * and configures the {@code Gson} instance with specific settings:
     * - Disables HTML escaping.
     * - Enables pretty printing of JSON data.
     */
    public JsonDocument() {
        this.jsonObject = new JsonObject();
        this.gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    }

    /**
     * Constructs a new {@code JsonDocument} instance using the provided {@code JsonObject}.
     * This constructor initializes the internal {@code jsonObject} field and configures
     * a Gson instance with specific settings such as disabling HTML escaping and enabling
     * pretty printing.
     *
     * @param jsonObject the {@code JsonObject} to be used as the underlying data structure
     *                   for this {@code JsonDocument}. Cannot be null.
     */
    public JsonDocument(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        this.gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    }

    /**
     * Loads a JSON document from the specified file.
     *
     * This method attempts to parse the provided file into a {@code JsonDocument} object.
     * In case of any exceptions (e.g., invalid JSON format or file read errors), it returns {@code null}.
     *
     * @param file the file from which to load the JSON document; must not be null
     * @return the loaded {@code JsonDocument} instance, or {@code null} if an error occurs during loading
     */
    public static JsonDocument loadDocument(File file) {
        try{
            return new JsonDocument((JsonObject) JsonParser.parseReader(new FileReader(file)));
        }catch(Exception e){
            return null;
        }
    }

    /**
     * Saves the contents of the JsonObject to the specified file. If the parent directory
     * of the file does not exist, it will be created. The content is saved in UTF-8 encoding.
     *
     * @param file the file to which the JsonObject should be saved; must not be null.
     * @throws IOException if an I/O error occurs during the file writing process.
     */
    public void save(File file) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(jsonObject, outputStreamWriter);
        }
    }

    /**
     * Retrieves the value associated with the specified key from the underlying JSON object.
     *
     * @param key The key whose associated value is to be returned. Must not be null.
     * @return The {@link JsonElement} associated with the specified key, or null if no value is mapped to the key.
     */
    public JsonElement get(String key) {
        return jsonObject.get(key);
    }

    /**
     * Adds a key-value pair to the internal JSON object.
     *
     * @param key the key to associate with the value in the JSON object; must not be null
     * @param value the value to associate with the key in the JSON object; must not be null
     */
    public void set(String key, JsonElement value) {
        jsonObject.add(key, value);
    }

    /**
     * Sets a key-value pair in the underlying JSON object where the value is a string.
     *
     * @param key   The key as a String. Must not be null.
     * @param value The value to associate with the key as a String. Must not be null.
     */
    public void setString(String key, String value) {
        jsonObject.addProperty(key, value);
    }

    /**
     * Sets a numeric value in the JSON object with the specified key.
     *
     * @param key   The key under which the numeric value will be stored. Cannot be null.
     * @param value The numeric value to be associated with the given key. Cannot be null.
     */
    public void setNumber(String key, Number value) {
        jsonObject.addProperty(key, value);
    }

    /**
     * Sets a boolean value in the underlying JSON object with the specified key.
     *
     * @param key the key under which the boolean value is to be stored; cannot be null
     * @param value the boolean value to be set
     */
    public void setBoolean(String key, boolean value) {
        jsonObject.addProperty(key, value);
    }

    /**
     * Removes the entry with the specified key from the underlying JSON object.
     *
     * @param key The key of the entry to be removed from the JSON object. Cannot be null.
     */
    public void remove(String key) {
        jsonObject.remove(key);
    }

    /**
     * Checks if the specified key is present in the underlying JSON object.
     *
     * @param key the key to check for existence in the JSON object; must not be null
     * @return true if the key exists in the JSON object, false otherwise
     */
    public boolean hasKey(String key) {
        return jsonObject.has(key);
    }

    /**
     * Retrieves the set of all keys present in the internal JSON object.
     *
     * @return a set of strings representing all the keys in the JSON object
     */
    public Set<String> getKeys() {
        return jsonObject.keySet();
    }

    /**
     * Updates the Json structure at the specified path with the given value.
     * The path is a dot-separated string that represents the hierarchy in the Json structure.
     * If an intermediate path element does not exist, it creates it as a JsonObject.
     *
     * @param path  the dot-separated path string that specifies where to apply the update in the Json structure
     * @param value the value to set at the specified path; it can be a String, Number, Boolean, JsonElement, or null
     */
    public void update(String path, Object value) {
        try {
            String[] pathParts = path.split("\\.");
            JsonElement current = jsonObject;

            for (int i = 0; i < pathParts.length - 1; i++) {
                String part = pathParts[i];

                if (current instanceof JsonObject currentObj) {
                    if (!currentObj.has(part) || !currentObj.get(part).isJsonObject()) {
                        currentObj.add(part, new JsonObject());
                    }

                    current = currentObj.get(part);
                } else {
                    return;
                }
            }

            String lastPart = pathParts[pathParts.length - 1];
            if (current instanceof JsonObject parentObj) {
                switch (value) {
                    case String s -> parentObj.addProperty(lastPart, s);
                    case Number number -> parentObj.addProperty(lastPart, number);
                    case Boolean b -> parentObj.addProperty(lastPart, b);
                    case JsonElement element -> parentObj.add(lastPart, element);
                    case null -> parentObj.add(lastPart, JsonNull.INSTANCE);
                    default -> parentObj.add(lastPart, gson.toJsonTree(value));
                }

            }

        } catch (Exception exception) {
            BedWarsPlugin.getInstance().getLogger().log(Level.WARNING, "Failed to update json document", exception);
        }
    }

    /**
     * Retrieves all entries within the underlying JSON object as a set of map entries.
     * Each entry represents a key-value pair where the key is a string and the value is a JsonElement.
     *
     * @return a set of map entries containing the key-value pairs of the JSON object
     */
    public Set<Map.Entry<String, JsonElement>> getEntries() {
        return jsonObject.entrySet();
    }

    /**
     * Converts the underlying JSON structure of this JsonDocument instance into its JSON string representation.
     *
     * @return A JSON string representing the JsonObject stored in this JsonDocument.
     */
    @Override
    public String toString() {
        return gson.toJson(jsonObject);
    }
}
