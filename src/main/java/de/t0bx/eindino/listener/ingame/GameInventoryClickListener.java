package de.t0bx.eindino.listener.ingame;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.inventory.inventories.ShopInventory;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;

public class GameInventoryClickListener implements Listener {

    private final ShopInventory shopInventory;
    private final NamespacedKey shopKey;
    private final NamespacedKey navigatorKey;

    public GameInventoryClickListener() {
        this.shopInventory = BedWarsPlugin.getInstance().getShopInventory();
        this.shopKey = new NamespacedKey("bedwars", "shop");
        this.navigatorKey = new NamespacedKey("bedwars", "navigator");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getItemMeta() == null) return;
        if (event.getCurrentItem().getItemMeta().customName() == null) return;

        if (BedWarsPlugin.getInstance().getSpectatorHandler().isSpectator(player)) {
            event.setCancelled(true);
            if (event.getView().title().equals(MiniMessage.miniMessage().deserialize("<gray>» <red>Navigator <gray>«"))) {
                if (event.getCurrentItem().getPersistentDataContainer().has(this.navigatorKey)) {
                    String playerName = event.getCurrentItem().getPersistentDataContainer().get(this.navigatorKey, PersistentDataType.STRING);

                    if (playerName == null) return;
                    Player target = Bukkit.getPlayer(playerName);

                    if (target == null) return;
                    player.teleport(target.getLocation());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.0f);
                }
            }
            return;
        }

        if (event.getView().title().equals(MiniMessage.miniMessage().deserialize("<gray>» <green>Shop <gray>«"))) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 0 -> this.shopInventory.openInventory(player);
                case 1 -> this.shopInventory.openArmorInventory(player);
                case 2 -> this.shopInventory.openPickaxeInventory(player);
                case 3 -> this.shopInventory.openSwordInventory(player);
                case 4 -> this.shopInventory.openBowInventory(player);
                case 5 -> this.shopInventory.openFoodInventory(player);
                case 6 -> this.shopInventory.openChestInventory(player);
                case 7 -> this.shopInventory.openPotionInventory(player);
                case 8 -> this.shopInventory.openExtraInventory(player);
            }
            if (event.getSlot() <= 8) return;

            String itemData = event.getCurrentItem().getPersistentDataContainer().get(this.shopKey, PersistentDataType.STRING);
            if (itemData == null) return;

            String[] itemDataArray = itemData.split(":");
            Material neededItem = this.getMaterialFromItem(itemDataArray[0]);
            if (neededItem == null) return;
            int pricePerPurchase = Integer.parseInt(itemDataArray[1]);
            int itemAmountPerPurchase = event.getCurrentItem().getAmount();

            int availableCurrency = this.countItemsInInventory(player, neededItem);
            if (availableCurrency < pricePerPurchase) {
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_PLACE, 1.0f, 1.0f);
                return;
            }

            int maxAffordablePurchases = availableCurrency / pricePerPurchase;
            int purchasesToMake;

            if (event.isShiftClick()) {
                int targetTotalItems = 64;
                int purchasesForTargetAmount = (targetTotalItems + itemAmountPerPurchase - 1) / itemAmountPerPurchase;
                purchasesToMake = Math.min(maxAffordablePurchases, purchasesForTargetAmount);
            } else {
                purchasesToMake = 1;
            }


            int totalItemsToReceive = purchasesToMake * itemAmountPerPurchase;
            int totalCost = purchasesToMake * pricePerPurchase;

            this.removeItemsFromInventory(player, neededItem, totalCost);

            ItemStack purchasedItem = event.getCurrentItem().clone();
            purchasedItem.setAmount(totalItemsToReceive);

            ItemMeta meta = purchasedItem.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().remove(this.shopKey);
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore != null && !lore.isEmpty()) {
                        lore.clear();
                        meta.setLore(lore);
                    }
                }
                purchasedItem.setItemMeta(meta);
            }

            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(purchasedItem);

            if (!leftover.isEmpty()) {
                for (ItemStack item : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
        }
    }

    private int countItemsInInventory(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeItemsFromInventory(Player player, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    remaining -= itemAmount;
                    contents[i] = null;
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }

        player.getInventory().setContents(contents);
    }

    private Material getMaterialFromItem(String name) {
        return switch (name.toLowerCase()) {
            case "copper" -> Material.COPPER_INGOT;
            case "iron" -> Material.IRON_INGOT;
            case "gold" -> Material.GOLD_INGOT;
            default -> null;
        };
    }
}
