package de.t0bx.eindino.inventory.inventories;

import de.t0bx.eindino.BedWarsPlugin;
import de.t0bx.eindino.inventory.InventoryProvider;
import de.t0bx.eindino.team.TeamHandler;
import de.t0bx.eindino.utils.ItemProvider;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionType;

public class ShopInventory {

    private final InventoryProvider inventoryProvider;
    private final TeamHandler teamHandler;

    public ShopInventory() {
        this.inventoryProvider = BedWarsPlugin.getInstance().getInventoryProvider();
        this.teamHandler = BedWarsPlugin.getInstance().getTeamHandler();
    }

    public void openInventory(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "shop_main", 9*2, "<gray>» <green>Shop <gray>«");

        this.setTopItems(player, inventory);

        inventory.setItem(10, new ItemProvider(this.getPlayerTeamBlockConcrete(player))
                .setPersistentData("bedwars", "shop", "copper:1")
                .setLore("§71 Kupfer")
                .setName("<green>Baublock")
                .setAmount(2)
                .build());

        inventory.setItem(11, new ItemProvider(this.getPlayerTeamBlockWool(player))
                .setPersistentData("bedwars", "shop", "copper:4")
                .setLore("§74 Kupfer", " ", "§eDieser Block verschwindet automatisch nach 5 Sekunden.")
                .setName("<green>Zeitblock")
                .build());

        inventory.setItem(12, new ItemProvider(Material.END_STONE)
                .setPersistentData("bedwars", "shop", "iron:3")
                .setLore("§83 Eisen")
                .setName("<green>Endgestein")
                .build());

        inventory.setItem(13, new ItemProvider(Material.IRON_BLOCK)
                .setPersistentData("bedwars", "shop", "iron:6")
                .setLore("§86 Eisen")
                .setName("<green>Eisenblock")
                .build());

        inventory.setItem(14, new ItemProvider(this.getPlayerTeamBlockGlass(player))
                .setPersistentData("bedwars", "shop", "copper:4")
                .setLore("§74 Kupfer")
                .setName("<green>Glas")
                .build());

        inventory.setItem(15, new ItemProvider(Material.GLOWSTONE)
                .setPersistentData("bedwars", "shop", "copper:8")
                .setLore("§78 Kupfer")
                .setName("<green>Glowstone")
                .setAmount(4)
                .build());

        player.openInventory(inventory);
    }

    public void openArmorInventory(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "shop_armor", 9*2, "<gray>» <green>Shop <gray>«");

        this.setTopItems(player, inventory);

        inventory.setItem(10, new ItemProvider(Material.LEATHER_HELMET)
                .setLeatherArmorColor(this.getPlayerTeamColorForArmor(player))
                .setPersistentData("bedwars", "shop", "copper:6")
                .setLore("§76 Kupfer")
                .setName("<green>Lederhelm")
                .build());

        inventory.setItem(11, new ItemProvider(Material.LEATHER_LEGGINGS)
                .setLeatherArmorColor(this.getPlayerTeamColorForArmor(player))
                .setPersistentData("bedwars", "shop", "copper:6")
                .setLore("§76 Kupfer")
                .setName("<green>Lederhose")
                .build());

        inventory.setItem(12, new ItemProvider(Material.LEATHER_BOOTS)
                .setLeatherArmorColor(this.getPlayerTeamColorForArmor(player))
                .setPersistentData("bedwars", "shop", "copper:6")
                .setLore("§76 Kupfer")
                .setName("<green>Lederschuhe")
                .build());

        inventory.setItem(13, new ItemProvider(Material.CHAINMAIL_CHESTPLATE)
                .setPersistentData("bedwars", "shop", "iron:3")
                .setLore("§83 Eisen")
                .setName("<green>Kettenrüstung")
                .build());

        inventory.setItem(14, new ItemProvider(Material.CHAINMAIL_CHESTPLATE)
                .setLeatherArmorColor(this.getPlayerTeamColorForArmor(player))
                .addEnchantment(Enchantment.PROTECTION, 1)
                .setPersistentData("bedwars", "shop", "iron:5")
                .setLore("§85 Eisen")
                .setName("<green>Kettenrüstung I")
                .build());

        inventory.setItem(15, new ItemProvider(Material.CHAINMAIL_CHESTPLATE)
                .setLeatherArmorColor(this.getPlayerTeamColorForArmor(player))
                .addEnchantment(Enchantment.PROTECTION, 2)
                .setPersistentData("bedwars", "shop", "iron:8")
                .setLore("§88 Eisen")
                .setName("<green>Kettenrüstung II")
                .build());

        inventory.setItem(16, new ItemProvider(Material.CHAINMAIL_CHESTPLATE)
                .setLeatherArmorColor(this.getPlayerTeamColorForArmor(player))
                .addEnchantment(Enchantment.PROTECTION, 3)
                .setPersistentData("bedwars", "shop", "gold:2")
                .setLore("§62 Gold")
                .setName("<green>Kettenrüstung III")
                .build());

        player.openInventory(inventory);
    }

    public void openPickaxeInventory(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "shop_axe", 9*2, "<gray>» <green>Shop <gray>«");

        this.setTopItems(player, inventory);

        inventory.setItem(12, new ItemProvider(Material.WOODEN_PICKAXE)
                .setPersistentData("bedwars", "shop", "copper:4")
                .setLore("§74 Kupfer")
                .setName("<green>Holzspitzhacke")
                .build());

        inventory.setItem(13, new ItemProvider(Material.STONE_PICKAXE)
                .setPersistentData("bedwars", "shop", "iron:3")
                .setLore("§83 Eisen")
                .setName("<green>Steinspitzhacke")
                .build());

        inventory.setItem(14, new ItemProvider(Material.IRON_PICKAXE)
                .setPersistentData("bedwars", "shop", "gold:1")
                .setLore("§61 Gold")
                .setName("<green>Eisenspitzhacke")
                .build());

        player.openInventory(inventory);
    }

    public void openSwordInventory(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "shop_sword", 9*2, "<gray>» <green>Shop <gray>«");

        this.setTopItems(player, inventory);

        inventory.setItem(11, new ItemProvider(Material.STICK)
                .setPersistentData("bedwars", "shop", "copper:3")
                .setLore("§73 Kupfer")
                .addEnchantment(Enchantment.KNOCKBACK, 1)
                .setName("<green>Knüppel")
                .build());

        inventory.setItem(12, new ItemProvider(Material.WOODEN_SWORD)
                .setPersistentData("bedwars", "shop", "iron:3")
                .setLore("§83 Eisen")
                .setName("<green>Holzschwert")
                .build());

        inventory.setItem(13, new ItemProvider(Material.STONE_SWORD)
                .setPersistentData("bedwars", "shop", "iron:6")
                .setLore("§86 Eisen")
                .setName("<green>Steinschwert")
                .build());

        inventory.setItem(14, new ItemProvider(Material.STONE_SWORD)
                .addEnchantment(Enchantment.SHARPNESS, 1)
                .setPersistentData("bedwars", "shop", "iron:10")
                .setLore("§810 Eisen")
                .setName("<green>Steinschwert II")
                .build());

        inventory.setItem(15, new ItemProvider(Material.IRON_SWORD)
                .addEnchantment(Enchantment.SHARPNESS, 1)
                .setPersistentData("bedwars", "shop", "gold:3")
                .setLore("§63 Gold")
                .setName("<green>Eisenschwert")
                .build());

        player.openInventory(inventory);
    }

    public void openBowInventory(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "shop_bow", 9*2, "<gray>» <green>Shop <gray>«");

        this.setTopItems(player, inventory);

        inventory.setItem(11, new ItemProvider(Material.ARROW)
                .setPersistentData("bedwars", "shop", "iron:2")
                .setLore("§82 Eisen")
                .setName("<green>Pfeile")
                .build());

        inventory.setItem(13, new ItemProvider(Material.BOW)
                .setPersistentData("bedwars", "shop", "gold:1")
                .setLore("§61 Gold")
                .setName("<green>Bogen I")
                .build());

        inventory.setItem(14, new ItemProvider(Material.BOW)
                .addEnchantment(Enchantment.POWER, 1)
                .setPersistentData("bedwars", "shop", "gold:3")
                .setLore("§63 Gold")
                .setName("<green>Bogen II")
                .build());

        inventory.setItem(15, new ItemProvider(Material.BOW)
                .addEnchantment(Enchantment.INFINITY, 1)
                .setPersistentData("bedwars", "shop", "gold:12")
                .setLore("§612 Gold")
                .setName("<green>Bogen III")
                .build());

        player.openInventory(inventory);
    }

    public void openFoodInventory(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "shop_food", 9*2, "<gray>» <green>Shop <gray>«");

        this.setTopItems(player, inventory);

        inventory.setItem(11, new ItemProvider(Material.BREAD)
                .setPersistentData("bedwars", "shop", "copper:5")
                .setLore("§75 Kupfer")
                .setName("<green>Brot")
                .setAmount(4)
                .build());

        inventory.setItem(13, new ItemProvider(Material.COOKED_PORKCHOP)
                .setPersistentData("bedwars", "shop", "iron:3")
                .setLore("§83 Eisen")
                .setName("<green>Schweinefleisch")
                .setAmount(4)
                .build());

        inventory.setItem(14, new ItemProvider(Material.COOKED_BEEF)
                .setPersistentData("bedwars", "shop", "iron:3")
                .setLore("§83 Eisen")
                .setName("<green>Steak")
                .setAmount(4)
                .build());

        inventory.setItem(15, new ItemProvider(Material.GOLDEN_APPLE)
                .setPersistentData("bedwars", "shop", "gold:1")
                .setLore("§61 Gold")
                .setName("<green>Goldapfel")
                .build());

        player.openInventory(inventory);
    }

    public void openChestInventory(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "shop_chest", 9*2, "<gray>» <green>Shop <gray>«");

        this.setTopItems(player, inventory);

        inventory.setItem(12, new ItemProvider(Material.CHEST)
                .setPersistentData("bedwars", "shop", "iron:4")
                .setLore("§84 Eisen")
                .setName("<green>Kiste")
                .build());

        inventory.setItem(14, new ItemProvider(Material.ENDER_CHEST)
                .setPersistentData("bedwars", "shop", "gold:1")
                .setLore("§61 Gold")
                .setName("<green>Team Kiste")
                .build());

        player.openInventory(inventory);
    }

    public void openPotionInventory(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "shop_potions", 9*2, "<gray>» <green>Shop <gray>«");

        this.setTopItems(player, inventory);

        inventory.setItem(11, new ItemProvider(Material.POTION)
                .setPotionType(PotionType.HEALING, false, false)
                .setPersistentData("bedwars", "shop", "iron:2")
                .setLore("§82 Eisen")
                .setName("<green>Heiltrank")
                .build());

        inventory.setItem(12, new ItemProvider(Material.SPLASH_POTION)
                .setPotionType(PotionType.HEALING, false, false)
                .setPersistentData("bedwars", "shop", "iron:4")
                .setLore("§84 Eisen")
                .setName("<green>Wurfheiltrank")
                .build());

        inventory.setItem(13, new ItemProvider(Material.POTION)
                .setPotionType(PotionType.REGENERATION, false, false)
                .setPersistentData("bedwars", "shop", "iron:5")
                .setLore("§85 Eisen")
                .setName("<green>Regenerationstrank")
                .build());

        inventory.setItem(14, new ItemProvider(Material.SPLASH_POTION)
                .setPotionType(PotionType.STRENGTH, false, false)
                .setPersistentData("bedwars", "shop", "gold:3")
                .setLore("§63 Gold")
                .setName("<green>Wurfstärketrank")
                .build());

        inventory.setItem(15, new ItemProvider(Material.POTION)
                .setPotionType(PotionType.INVISIBILITY, false, false)
                .setPersistentData("bedwars", "shop", "gold:6")
                .setLore("§66 Gold")
                .setName("<green>Unsichtbarkeitstrank")
                .build());

        player.openInventory(inventory);
    }

    public void openExtraInventory(Player player) {
        Inventory inventory = this.inventoryProvider.getInventory(player, "shop_extra", 9*2, "<gray>» <green>Shop <gray>«");

        this.setTopItems(player, inventory);

        inventory.setItem(11, new ItemProvider(Material.BLAZE_ROD)
                .setPersistentData("bedwars", "shop", "iron:10")
                .setLore("§810 Eisen")
                .setName("<green>Rettungsplatform")
                .build());

        inventory.setItem(12, new ItemProvider(Material.COBWEB)
                .setPersistentData("bedwars", "shop", "iron:8")
                .setLore("§88 Eisen")
                .setName("<green>Spinnennetz")
                .setAmount(2)
                .build());

        inventory.setItem(13, new ItemProvider(Material.ENDER_PEARL)
                .setPersistentData("bedwars", "shop", "gold:3")
                .setLore("§63 Gold")
                .setName("<green>Enderperle")
                .build());

        inventory.setItem(14, new ItemProvider(this.getPlayerTeamBlockConcrete(player))
                .setPersistentData("bedwars", "shop", "iron:5")
                .setLore("§85 Eisen")
                .setName("<green>Alarmblock")
                .build());

        /*inventory.setItem(15, new ItemProvider(this.getPlayerTeamBlockConcrete(player))
                .setPersistentData("bedwars", "shop", "gold:3")
                .setLore("§63 Gold")
                .setName("<green>Unsichtbarblock")
                .build());*/

        inventory.setItem(15, new ItemProvider(Material.ARMOR_STAND)
                .setPersistentData("bedwars", "shop", "gold:8")
                .setLore("§68 Gold")
                .setName("<green>Mobiler Shop")
                .build());

        player.openInventory(inventory);
    }

    private void setTopItems(Player player, Inventory inventory) {
        inventory.setItem(0, new ItemProvider(this.getPlayerTeamBlockConcrete(player)).setName("<gray>» <green>Blöcke").build());
        inventory.setItem(1, new ItemProvider(Material.CHAINMAIL_CHESTPLATE).setName("<gray>» <red>Rüstung").build());
        inventory.setItem(2, new ItemProvider(Material.IRON_PICKAXE).setName("<gray>» <green>Spitzhacken").build());
        inventory.setItem(3, new ItemProvider(Material.WOODEN_SWORD).setName("<gray>» <green>Nahkampf").build());
        inventory.setItem(4, new ItemProvider(Material.BOW).setName("<gray>» <green>Bögen").build());
        inventory.setItem(5, new ItemProvider(Material.COOKED_BEEF).setName("<gray>» <green>Essen").build());
        inventory.setItem(6, new ItemProvider(Material.ENDER_CHEST).setName("<gray>» <green>Kisten").build());
        inventory.setItem(7, new ItemProvider(Material.POTION).setName("<gray>» <green>Tränke").build());
        inventory.setItem(8, new ItemProvider(Material.EMERALD).setName("<gray>» <green>Extras").build());
    }

    private Material getPlayerTeamBlockConcrete(Player player) {
        return switch (this.teamHandler.getPlayerTeam(player).getName().toLowerCase()) {
            case "red" -> Material.RED_CONCRETE;
            case "green" -> Material.GREEN_CONCRETE;
            case "blue" -> Material.BLUE_CONCRETE;
            case "yellow" -> Material.YELLOW_CONCRETE;
            case "orange" -> Material.ORANGE_CONCRETE;
            case "purple" -> Material.PURPLE_CONCRETE;
            case "pink" -> Material.PINK_CONCRETE;
            case "white" -> Material.WHITE_CONCRETE;
            case "black" -> Material.BLACK_CONCRETE;
            case "gray", "grey" -> Material.GRAY_CONCRETE;
            case "aqua" -> Material.LIGHT_BLUE_CONCRETE;
            case "lime" -> Material.LIME_CONCRETE;
            default -> Material.BLACK_CONCRETE;
        };
    }

    private Material getPlayerTeamBlockWool(Player player) {
        return switch (this.teamHandler.getPlayerTeam(player).getName().toLowerCase()) {
            case "red" -> Material.RED_WOOL;
            case "green" -> Material.GREEN_WOOL;
            case "blue" -> Material.BLUE_WOOL;
            case "yellow" -> Material.YELLOW_WOOL;
            case "orange" -> Material.ORANGE_WOOL;
            case "purple" -> Material.PURPLE_WOOL;
            case "pink" -> Material.PINK_WOOL;
            case "white" -> Material.WHITE_WOOL;
            case "black" -> Material.BLACK_WOOL;
            case "gray", "grey" -> Material.GRAY_WOOL;
            case "aqua" -> Material.LIGHT_BLUE_WOOL;
            case "lime" -> Material.LIME_WOOL;
            default -> Material.BLACK_WOOL;
        };
    }

    private Material getPlayerTeamBlockGlass(Player player) {
        return switch (this.teamHandler.getPlayerTeam(player).getName().toLowerCase()) {
            case "red" -> Material.RED_STAINED_GLASS;
            case "green" -> Material.GREEN_STAINED_GLASS;
            case "blue" -> Material.BLUE_STAINED_GLASS;
            case "yellow" -> Material.YELLOW_STAINED_GLASS;
            case "orange" -> Material.ORANGE_STAINED_GLASS;
            case "purple" -> Material.PURPLE_STAINED_GLASS;
            case "pink" -> Material.PINK_STAINED_GLASS;
            case "white" -> Material.WHITE_STAINED_GLASS;
            case "black" -> Material.BLACK_STAINED_GLASS;
            case "gray", "grey" -> Material.GRAY_STAINED_GLASS;
            case "aqua" -> Material.LIGHT_BLUE_STAINED_GLASS;
            case "lime" -> Material.LIME_STAINED_GLASS;
            default -> Material.BLACK_STAINED_GLASS;
        };
    }

    private Color getPlayerTeamColorForArmor(Player player) {
        return switch (this.teamHandler.getPlayerTeam(player).getName().toLowerCase()) {
            case "red" -> Color.RED;
            case "green" -> Color.GREEN;
            case "blue" -> Color.BLUE;
            case "yellow" -> Color.YELLOW;
            case "orange" -> Color.ORANGE;
            case "purple" -> Color.PURPLE;
            case "pink" -> Color.fromRGB(255, 105, 180);
            case "white" -> Color.WHITE;
            case "black" -> Color.BLACK;
            case "gray", "grey" -> Color.GRAY;
            case "aqua" -> Color.AQUA;
            case "lime" -> Color.LIME;
            default -> Color.BLACK;
        };
    }
}