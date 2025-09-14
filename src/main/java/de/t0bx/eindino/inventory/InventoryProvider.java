package de.t0bx.eindino.inventory;

import de.t0bx.eindino.utils.ItemProvider;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryProvider {

    private final ConcurrentHashMap<UUID, Map<String, Inventory>> playerInventories;

    @Getter
    private static InventoryProvider instance;

    /**
     * Constructs a new instance of the InventoryProvider.
     *
     * This constructor initializes the internal data structure used to manage player inventories
     * and sets the static instance field to the current instance of the class.
     */
    public InventoryProvider() {
        instance = this;
        this.playerInventories = new ConcurrentHashMap<>();
    }

    /**
     * Retrieves or creates an inventory for a specific player and inventory name. If the inventory does not
     * already exist, it will be created with the specified size and title.
     *
     * @param player          The player for whom the inventory is being accessed or created.
     * @param inventoryName   The unique name of the inventory associated with the player.
     * @param inventorySize   The size of the inventory to be created if it does not exist.
     * @param inventoryTitle  The title of the inventory to be displayed, formatted with MiniMessage.
     * @return The existing or newly created inventory for the specified player and inventory name.
     */
    public Inventory getInventory(Player player, String inventoryName, int inventorySize, String inventoryTitle) {
        UUID playerUUID = player.getUniqueId();

        if (!playerInventories.containsKey(playerUUID)) {
            playerInventories.put(playerUUID, new HashMap<>());
        }

        Map<String, Inventory> inventories = playerInventories.get(playerUUID);

        if (!inventories.containsKey(inventoryName)) {
            Inventory newInventory = Bukkit.createInventory(player, inventorySize, MiniMessage.miniMessage().deserialize(inventoryTitle));
            inventories.put(inventoryName, newInventory);
        }

        return inventories.get(inventoryName);
    }

    /**
     * Opens an inventory for the specified player. If the inventory does not already exist,
     * it will be created and associated with the player.
     *
     * @param player         The player for whom the inventory is being opened.
     * @param inventoryName  The unique name identifying the inventory.
     * @param inventorySize  The size of the inventory to be created, if it does not already exist.
     * @param inventoryTitle The title to be displayed for the inventory.
     */
    public void openInventory(Player player, String inventoryName, int inventorySize, String inventoryTitle) {
        Inventory inventory = getInventory(player, inventoryName, inventorySize, inventoryTitle);
        player.openInventory(inventory);
    }

    /**
     * Removes the specified inventory associated with the given player.
     *
     * @param player The player whose inventory is to be removed.
     * @param inventoryName The name of the inventory to be removed.
     */
    public void removeInventory(Player player, String inventoryName) {
        UUID playerUUID = player.getUniqueId();
        if (playerInventories.containsKey(playerUUID)) {
            playerInventories.get(playerUUID).remove(inventoryName);
        }
    }

    /**
     * Removes all inventories associated with the specified player.
     *
     * @param player the player whose inventories are to be removed
     */
    public void removeAllInventories(Player player) {
        playerInventories.remove(player.getUniqueId());
    }

    /**
     * Sets a placeholder item in the specified slots of the given inventory.
     * The placeholder item is created using the specified material and has no display name.
     *
     * @param inventory the inventory where the placeholder items should be set
     * @param material  the material used to create the placeholder item
     * @param slots     the slots in the inventory where the placeholder item should be placed
     */
    public void setPlaceHolder(Inventory inventory, Material material, int... slots) {
        ItemStack item = new ItemProvider(material).setName(" ").build();
        for (int slot : slots) {
            inventory.setItem(slot, item);
        }
    }
}
