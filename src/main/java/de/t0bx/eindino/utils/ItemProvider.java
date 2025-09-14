package de.t0bx.eindino.utils;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class ItemProvider {

    private final Map<String, HeadData> headCache = new HashMap<>();

    private ItemStack itemStack;


    /**
     * Constructs an ItemProvider instance with an ItemStack created from the given Material.
     *
     * @param material The material used to create the ItemStack. Cannot be null.
     */
    public ItemProvider(Material material) {
        this.itemStack = new ItemStack(material);
    }


    /**
     * Constructs an ItemProvider with the given ItemStack.
     * The provided ItemStack is cloned to avoid modifications to the original object.
     *
     * @param itemStack the ItemStack to initialize the ItemProvider; must not be null
     */
    public ItemProvider(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }


    /**
     * Constructs a new ItemProvider with the specified material type and amount.
     *
     * @param material the type of material to be used in the ItemStack; must not be null
     * @param amount the quantity of the ItemStack; must be a positive integer
     */
    public ItemProvider(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
    }


    /**
     * Creates a custom player skull item with the specified texture URL.
     *
     * @param skullTexture the URL of the texture to apply to the player skull.
     *                     It should be a valid texture URL compatible with Minecraft.
     * @return an {@link ItemProvider} instance representing the customized player skull.
     */
    public static ItemProvider createCustomSkull(String skullTexture) {
        ItemProvider builder = new ItemProvider(Material.PLAYER_HEAD);
        return builder.setSkullTexture(skullTexture);
    }

    /**
     * Creates a player head (skull) item with the specified player's skin.
     *
     * @param playerName The name of the player whose head skin should be applied. Must not be null.
     * @return An ItemProvider instance representing the player head with the specified player's skin.
     */
    public static ItemProvider createPlayerSkull(String playerName) {
        ItemProvider builder = new ItemProvider(Material.PLAYER_HEAD);
        return builder.setSkullOwner(playerName);
    }

    /**
     * Sets the custom display name for the item represented by the current instance.
     * The name is deserialized using MiniMessage for rich text formatting.
     *
     * @param name The custom name to be set for the item. Cannot be null.
     * @return The current ItemProvider instance for method chaining.
     */
    public ItemProvider setName(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.customName(MiniMessage.miniMessage().deserialize(name));
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Sets the lore (descriptive text) for the item using an array of strings,
     * converting it to a list format.
     *
     * @param lore the lore to be set for the item, where each string in the array
     *             represents a line of the lore
     * @return the current ItemProvider instance for method chaining
     */
    public ItemProvider setLore(String... lore) {
        return setLore(Arrays.asList(lore));
    }

    /**
     * Sets the lore (custom text descriptions) of the item represented by this {@code ItemProvider}.
     * The lore is displayed in the item's tooltip in the inventory and can include multiple lines.
     *
     * @param lore the list of strings to set as the lore of the item;
     *             each string represents a line in the lore tooltip
     * @return this {@code ItemProvider} instance for method chaining
     */
    public ItemProvider setLore(List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Adds the specified lines to the lore of the item represented by this {@code ItemProvider}.
     *
     * The lore is additional text displayed on the item in the game, often used for customization
     * or additional item descriptions. If the item already has lore, the provided lines will be
     * appended to the existing lore. If the item has no lore, a new lore list is created and the
     * lines are added to it.
     *
     * @param lines the lines of text to add to the item's lore; must not be null
     * @return the current {@code ItemProvider} instance with the updated lore
     */
    public ItemProvider addLore(String... lines) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.addAll(Arrays.asList(lines));
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Sets the amount of items in the item stack.
     *
     * @param amount the amount to set; must be a positive integer and not exceed the maximum stack size allowed
     * @return the current ItemProvider instance for method chaining
     */
    public ItemProvider setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Sets whether the item represented by this ItemProvider instance should be unbreakable.
     * If the `unbreakable` parameter is true, the item will not lose durability when used.
     *
     * @param unbreakable a boolean indicating whether the item should be unbreakable;
     *                    true to make the item unbreakable, false to make it breakable.
     * @return the current instance of ItemProvider, allowing for method chaining.
     */
    public ItemProvider setUnbreakable(boolean unbreakable) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Adds a specific enchantment to the item with the specified level.
     * If the enchantment already exists, it will be overwritten.
     *
     * @param enchantment The enchantment to be added to the item. Must not be null.
     * @param level The level of the enchantment to be applied. Must be a valid level for the type of enchantment.
     * @return An instance of {@code ItemProvider} with the specified enchantment applied, allowing method chaining.
     */
    public ItemProvider addEnchantment(Enchantment enchantment, int level) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Adds multiple enchantments to the item represented by this {@code ItemProvider}.
     * Each entry in the provided map represents an enchantment and its corresponding level.
     * If an enchantment already exists, it will be updated to the specified level.
     *
     * @param enchantments A map where keys are {@link Enchantment} objects representing the enchantments to add,
     *                     and values are {@code Integer} objects representing the levels for the corresponding enchantments.
     * @return The current {@code ItemProvider} instance, allowing for method chaining.
     */
    public ItemProvider addEnchantments(Map<Enchantment, Integer> enchantments) {
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            addEnchantment(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Adds a stored enchantment to the item if it is an enchanted book. The method updates the
     * metadata of the item and applies the specified enchantment with the provided level.
     *
     * @param enchantment The enchantment to be added to the item. Must not be null.
     * @param level The level of the enchantment to apply. Must be a positive integer.
     * @return The current instance of {@code ItemProvider} for method chaining.
     */
    public ItemProvider addStoredEnchantment(Enchantment enchantment, int level) {
        if (itemStack.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemStack.getItemMeta();
            if (meta != null) {
                meta.addStoredEnchant(enchantment, level, true);
                itemStack.setItemMeta(meta);
            }
        }
        return this;
    }

    /**
     * Adds the specified item flags to the item represented by this `ItemProvider`.
     * ItemFlags can be used to hide various attributes of the item (e.g., enchantments, attributes)
     * from being displayed in the item's tooltip in a player's inventory.
     *
     * If the item does not have a valid metadata object (`ItemMeta`), this method performs no operation.
     *
     * @param flags The item flags to be added to the item. Each flag denotes a specific attribute to hide.
     * @return The current `ItemProvider` instance for method chaining.
     */
    public ItemProvider addItemFlags(ItemFlag... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Removes the specified item flags from the associated ItemStack.
     * Item flags control which properties of the item are hidden in the item's tooltip.
     *
     * @param flags the item flags that should be removed from the item. Can accept multiple {@link ItemFlag} arguments.
     * @return the current {@code ItemProvider} instance for method chaining after the operation.
     */
    public ItemProvider removeItemFlags(ItemFlag... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.removeItemFlags(flags);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Sets the durability of the item to the specified value.
     * A non-negative durability value will be applied to the internal item stack.
     *
     * @param durability the durability value to set; must be greater than or equal to 0
     * @return the current instance of {@code ItemProvider} for method chaining
     */
    public ItemProvider setDurability(int durability) {
        if (durability >= 0) {
            itemStack.setDurability((short) durability);
        }
        return this;
    }

    /**
     * Sets the color for leather armor if the underlying item is a leather armor piece.
     * This method modifies the {@link LeatherArmorMeta} of the item
     * to apply the given color.
     *
     * @param color The {@link Color} to apply to the leather armor. Must not be null.
     * @return The current {@code ItemProvider} instance, allowing for method chaining.
     */
    public ItemProvider setLeatherArmorColor(Color color) {
        if (itemStack.getItemMeta() instanceof LeatherArmorMeta) {
            LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
            meta.setColor(color);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Sets the texture of a player head item to the specified URL.
     *
     * @param textureUrl The URL of the texture to be applied to the player head.
     *                   This should be a valid URL pointing to the skin texture.
     * @return The {@code ItemProvider} instance to allow method chaining.
     */
    public ItemProvider setSkullTexture(String textureUrl) {
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            try {
                SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                if (skullMeta != null) {
                    PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
                    PlayerTextures textures = profile.getTextures();

                    // Entfernen von 'minecraft:' oder 'minecraft://' am Anfang, falls vorhanden
                    if (textureUrl.startsWith("minecraft:")) {
                        textureUrl = textureUrl.substring("minecraft:".length());
                    }

                    URL url = new URL(textureUrl);
                    textures.setSkin(url);
                    profile.setTextures(textures);

                    skullMeta.setOwnerProfile(profile);
                    itemStack.setItemMeta(skullMeta);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this;
    }


    /**
     * Sets the skull owner of the current player head item to a specified player name.
     * This method updates the Player Head's metadata asynchronously to include the player's profile information.
     * If the item is not of type PLAYER_HEAD, the method does nothing.
     *
     * @param playerName The name of the player whose profile should be set as the skull's owner.
     *                   Cannot be null or empty.
     * @return The current instance of the ItemProvider for method chaining.
     */
    public ItemProvider setSkullOwner(String playerName) {
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            if (skullMeta != null) {
                com.destroystokyo.paper.profile.PlayerProfile profile = Bukkit.createProfile(playerName);
                Bukkit.getServer().getScheduler().runTaskAsynchronously(
                        Bukkit.getPluginManager().getPlugins()[0],
                        () -> {
                            profile.complete();
                        }
                );
                skullMeta.setOwnerProfile(profile);
                itemStack.setItemMeta(skullMeta);
            }
        }
        return this;
    }

    /**
     * Sets the potion type for the item if the item's metadata supports potion data.
     *
     * @param potionType the base potion type to set
     * @param extended   whether the potion should be extended
     * @param upgraded   whether the potion should be upgraded
     * @return the current instance of the ItemProvider for method chaining
     */
    public ItemProvider setPotionType(PotionType potionType, boolean extended, boolean upgraded) {
        if (itemStack.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
            meta.setBasePotionType(potionType);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Adds a custom potion effect to the item if it has potion metadata.
     * If the item does not support potion effects, no changes are made.
     * The custom potion effect is set to overwrite any existing effect of the same type.
     *
     * @param effect The {@link PotionEffect} to be added. This defines the type, duration, amplifier,
     *               and other properties of the potion effect. Cannot be null.
     * @return The current {@link ItemProvider} instance, allowing for method chaining.
     */
    public ItemProvider addCustomPotionEffect(PotionEffect effect) {
        if (itemStack.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
            meta.addCustomEffect(effect, true);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Adds a custom potion effect to the current item with the specified parameters.
     *
     * @param type The type of potion effect to add (e.g., SPEED, POISON).
     * @param duration The duration of the potion effect in ticks.
     * @param amplifier The level of the potion effect (e.g., 0 for level I, 1 for level II).
     * @param ambient Whether the potion effect is considered ambient.
     * @param particles Whether the potion effect shows visual particles to players.
     * @return The current ItemProvider instance with the custom potion effect applied.
     */
    public ItemProvider addCustomPotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles) {
        return addCustomPotionEffect(new PotionEffect(type, duration, amplifier, ambient, particles));
    }

    /**
     * Sets the color of the potion for the current ItemStack if the item has potion metadata.
     *
     * @param color the Color to set for the potion; cannot be null
     * @return the current ItemProvider instance for method chaining
     */
    public ItemProvider setPotionColor(Color color) {
        if (itemStack.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
            meta.setColor(color);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Adds an {@link AttributeModifier} to a specified {@link Attribute} for the item represented by this {@code ItemProvider}.
     * If the item has metadata, the attribute modifier is applied and the modified metadata is saved back to the item.
     *
     * @param attribute the {@link Attribute} to which the modifier should be applied
     * @param modifier the {@link AttributeModifier} to apply to the specified attribute
     * @return the current instance of {@link ItemProvider}, allowing for method chaining
     */
    public ItemProvider addAttributeModifier(Attribute attribute, AttributeModifier modifier) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addAttributeModifier(attribute, modifier);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Sets a persistent data entry in the item's metadata.
     * This data is stored using a specific plugin namespace and key and can be used
     * to attach custom data to the item that persists across server restarts.
     *
     * @param pluginName The name of the plugin to use as the namespace for the data. Must not be null.
     * @param key The key under which the data will be stored. Must not be null.
     * @param value The value to store under the specified key. Must not be null.
     * @return The updated ItemProvider instance, allowing for method chaining.
     */
    public ItemProvider setPersistentData(String pluginName, String key, String value) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            NamespacedKey namespacedKey = new NamespacedKey(pluginName.toLowerCase(), key);
            meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Sets an integer value in the persistent data container of the item.
     *
     * @param pluginName the name of the plugin using the persistent data container, typically used as a namespace
     * @param key the key under which the integer value will be stored in the persistent data container
     * @param value the integer value to be stored in the persistent data container
     * @return the current instance of ItemProvider, allowing for method chaining
     */
    public ItemProvider setPersistentDataInt(String pluginName, String key, int value) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            NamespacedKey namespacedKey = new NamespacedKey(pluginName.toLowerCase(), key);
            meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, value);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Sets the custom model data for the item represented by this ItemProvider.
     * Custom model data is an optional property that can be used in resource packs
     * to define alternative appearances for items.
     *
     * @param customModelData The custom model data to set for the item. This should be a non-negative integer.
     * @return This ItemProvider instance, with the custom model data set, allowing for method chaining.
     */
    public ItemProvider setCustomModelData(int customModelData) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            itemStack.setItemMeta(meta);
        }
        return this;
    }


    /**
     * Creates and returns a copy of this ItemProvider instance.
     *
     * @return a new ItemProvider object that is a clone of this instance.
     */
    public ItemProvider clone() {
        return new ItemProvider(itemStack.clone());
    }

    /**
     * Applies modifications to the {@link ItemMeta} of the underlying {@link ItemStack} using the specified {@link Consumer}.
     * If the {@link ItemMeta} of the {@link ItemStack} is non-null, the provided {@link Consumer} is applied,
     * and the modified meta is set back to the {@link ItemStack}.
     *
     * @param metaConsumer the {@link Consumer} that modifies the {@link ItemMeta}; cannot be null
     * @return the current instance of {@link ItemProvider} for method chaining
     */
    public ItemProvider meta(Consumer<ItemMeta> metaConsumer) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            metaConsumer.accept(meta);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Sets the metadata of the item as a book with a specified title and author,
     * if the current item meta is an instance of {@code BookMeta}.
     *
     * @param title  the title of the book to set; can be null or empty
     * @param author the author of the book to set; can be null or empty
     * @return the {@code ItemProvider} instance for method chaining
     */
    public ItemProvider setBookMeta(String title, String author) {
        if (itemStack.getItemMeta() instanceof BookMeta) {
            BookMeta meta = (BookMeta) itemStack.getItemMeta();
            meta.setTitle(title);
            meta.setAuthor(author);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Adds pages to a book item represented by this ItemProvider instance.
     * This method modifies the item's metadata only if it is of type BookMeta.
     *
     * @param pages the content to be added as pages to the book. Each provided Component
     *              represents a page. Components should not be null to ensure proper functionality.
     * @return the current ItemProvider instance for method chaining
     */
    public ItemProvider addBookPages(Component... pages) {
        if (itemStack.getItemMeta() instanceof BookMeta) {
            BookMeta meta = (BookMeta) itemStack.getItemMeta();
            meta.addPages(pages);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Constructs and returns a copy of the current ItemStack instance.
     *
     * @return A cloned instance of the ItemStack represented by this ItemProvider.
     */
    public ItemStack build() {
        return itemStack.clone();
    }

    /**
     * Converts this ItemProvider instance into an ItemStack object.
     * The returned ItemStack is built based on the configuration of the ItemProvider.
     *
     * @return a new ItemStack instance that represents the item described by this ItemProvider
     */
    public ItemStack toItemStack() {
        return build();
    }

    @Getter
    private static class HeadData {
        private final PlayerProfile profile;
        private final PlayerTextures textures;

        public HeadData(PlayerProfile profile, PlayerTextures textures) {
            this.profile = profile;
            this.textures = textures;
        }
    }
}
