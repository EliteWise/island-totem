package fr.elite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeff_media.customblockdata.CustomBlockData;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.codehaus.plexus.util.Base64;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class Utils {

    /**
     * Crafts a custom totem with specified materials, pattern, and texture.
     * @param materials A hashmap of characters and materials representing the recipe shape.
     * @param pattern An array of strings representing the crafting shape.
     * @param textureValue The base64 encoded texture value for the totem's appearance.
     */
    public void craftTotem(HashMap<Character, Material> materials, String[] pattern, String textureValue) {
        if(materials.isEmpty() && pattern.length != 3 && textureValue.isEmpty()) return;

        ItemStack output = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta outputMeta = (SkullMeta) output.getItemMeta();
        outputMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + Constants.TOTEM_INV_TITLE);
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", textureValue));
        Field profileField = null;
        try {
            profileField = outputMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(outputMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        output.setItemMeta(outputMeta);

        NamespacedKey nsKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin(Constants.PLUGIN_NAME), "totem-mk");
        ShapedRecipe recipe = new ShapedRecipe(nsKey, output);

        recipe.shape(pattern[0], pattern[1], pattern[2]);

        for(char key : materials.keySet()) {
            ItemStack ingredient = new ItemStack(materials.get(key));

            recipe.setIngredient(key, ingredient.getType());
        }

        Bukkit.getServer().addRecipe(recipe);
    }

    /**
     * Deserializes a JSON file into a list of InventoryItem objects.
     * @param filePath The path to the JSON file.
     * @return A list of InventoryItem objects.
     * @throws IOException If an I/O error occurs.
     * @throws JsonProcessingException If the JSON is not formatted correctly.
     */
    public static List<InventoryItem> deserializeJsonFile(String filePath) throws IOException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new IOException("Cannot find resource: " + filePath);
        }
        return objectMapper.readValue(inputStream, new TypeReference<List<InventoryItem>>() {});
    }

    /**
     * Simulates the effect of the Fortune enchantment on mining.
     * @param enchantLevel The level of the Fortune enchantment.
     * @param random A Random object for generating random numbers.
     * @param brokenBlock The block that was broken.
     * @return The number of items dropped due to the Fortune enchantment.
     */
    public static int fortuneEnchantSimulation(int enchantLevel, Random random, Block brokenBlock) {
        if (enchantLevel > 0 && isOre(brokenBlock)) {
            // Create a list of weights for loots
            List<Integer> weightedDrops = new ArrayList<>();
            weightedDrops.add(1);
            weightedDrops.add(1);

            for (int i = 2; i <= enchantLevel + 1; i++) {
                weightedDrops.add(i); // Weight of 1 for each additional number of loots
            }

            // Choose a number of loots at random according to weights
            int randomIndex = random.nextInt(weightedDrops.size());
            return weightedDrops.get(randomIndex);
        }
        return 0; // No Fortune enchantment applied
    }

    /**
     * Persists custom data to a block, marking it with a player's name and level.
     * @param block The block to persist data to.
     * @param preventDupliKey A NamespacedKey to prevent duplication.
     * @param levelKey A NamespacedKey representing the player's level.
     * @param player The player associated with the block.
     */
    public static void persistBlock(Block block, NamespacedKey preventDupliKey, NamespacedKey levelKey, Player player) throws SQLException {
        if(isCrop(block)) {
            // Add to Persistent Container //
            PersistentDataContainer customBlockData = new CustomBlockData(block, Bukkit.getPluginManager().getPlugin(Constants.PLUGIN_NAME));
            customBlockData.set(preventDupliKey, PersistentDataType.STRING, Constants.PERSISTANT_DATA_PREVENT_DUPLI);
            customBlockData.set(levelKey, PersistentDataType.STRING, player.getName()
                    + "|" + Main.getInstance().getDatabase().getPlayerAttribute(player, "crops_quantity_level")
                    + "|" + Main.getInstance().getDatabase().getPlayerAttribute(player, "crops_speed_level")
                    + "|" + Main.getInstance().getDatabase().getPlayerAttribute(player, "ores_quantity_level"));

        }
    }

    /**
     * Checks if a block has been persisted with a specific NamespacedKey.
     * @param block The block to check.
     * @param namespacedKey The NamespacedKey to look for.
     * @return True if the block has the specified data, false otherwise.
     */
    public static boolean isBlockPersisted(Block block, NamespacedKey namespacedKey) {
        if(isCrop(block)) {
            PersistentDataContainer customBlockData = new CustomBlockData(block, Bukkit.getPluginManager().getPlugin(Constants.PLUGIN_NAME));
            String data = customBlockData.get(namespacedKey, PersistentDataType.STRING);
            if(data != null && data.equals(Constants.PERSISTANT_DATA_PREVENT_DUPLI)) {
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Determines if a given block is an ore block.
     * @param block The block to check.
     * @return True if the block is an ore, false otherwise.
     */
    public static boolean isOre(Block block) {
        Material type = block.getType();
        switch (type) {
            case COAL_ORE:
            case IRON_ORE:
            case GOLD_ORE:
            case DIAMOND_ORE:
            case LAPIS_ORE:
            case REDSTONE_ORE:
            case EMERALD_ORE:
            case NETHER_QUARTZ_ORE:
            case NETHER_GOLD_ORE:
            case COPPER_ORE:
                // Deepslate //
            case DEEPSLATE_COAL_ORE:
            case DEEPSLATE_IRON_ORE:
            case DEEPSLATE_GOLD_ORE:
            case DEEPSLATE_DIAMOND_ORE:
            case DEEPSLATE_LAPIS_ORE:
            case DEEPSLATE_REDSTONE_ORE:
            case DEEPSLATE_EMERALD_ORE:
            case DEEPSLATE_COPPER_ORE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Determines if a given block is a crop.
     * @param block The block to check.
     * @return True if the block is a crop, false otherwise.
     */
    public static boolean isCrop(Block block) {
        Material type = block.getType();
        switch (type) {
            case WHEAT:
            case POTATOES:
            case CARROTS:
            case BEETROOTS:
            case MELON_STEM:
            case PUMPKIN_STEM:
            case NETHER_WART:
            case SUGAR_CANE:
            case CACTUS:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case COCOA:
            case BAMBOO:
            case CHORUS_PLANT:
            case CHORUS_FLOWER:
            case SWEET_BERRY_BUSH:
                return true;
            default:
                return false;
        }
    }

    /**
     * Calculates the time reduction for the crops to grow based on the player's level.
     * @param level The player's level.
     * @return The amount of time reduction.
     */
    public static double calculateTimeReduction(int level) {
        return (level * level) / 4.1 + 2.5;
    }

    /**
     * Calculates the new age of a crop based on the current age, maximum age, and time reduction.
     * @param currentAge The current age of the crop.
     * @param maxAge The maximum age the crop can reach.
     * @param timeReduction The reduction in time from the player's level.
     * @return The new age of the crop.
     */
    public static int calculateNewAge(int currentAge, int maxAge, double timeReduction) {
        // Calculation of the new age of culture according to the time reduction
        int ageIncrement = (int) Math.round(timeReduction / 100 * maxAge);
        int newAge = currentAge + ageIncrement;
        return Math.min(newAge, maxAge);
    }

}
