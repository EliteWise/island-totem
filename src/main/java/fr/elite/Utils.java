package fr.elite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeff_media.customblockdata.CustomBlockData;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.block.Block;
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
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class Utils {

    public void craftTotem(HashMap<Character, Material> materials, String[] pattern, String textureValue) {
        if(materials.isEmpty() && pattern.length != 3 && textureValue.isEmpty()) return;

        ItemStack output = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta outputMeta = (SkullMeta) output.getItemMeta();
        outputMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Totem d’île");
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

        NamespacedKey nsKey = new NamespacedKey(Bukkit.getPluginManager().getPlugin("island-totem"), "totem-mk");
        ShapedRecipe recipe = new ShapedRecipe(nsKey, output);

        recipe.shape(pattern[0], pattern[1], pattern[2]);

        for(char key : materials.keySet()) {
            ItemStack ingredient = new ItemStack(materials.get(key));

            recipe.setIngredient(key, ingredient.getType());
        }

        Bukkit.getServer().addRecipe(recipe);
    }

    public static List<InventoryItem> deserializeJsonFile(String filePath) throws IOException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new IOException("Cannot find resource: " + filePath);
        }
        return objectMapper.readValue(inputStream, new TypeReference<List<InventoryItem>>() {});
    }

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

    public static void persistBlock(Block block, NamespacedKey namespacedKey) {
        if(isCrop(block)) {
            // Add to Persistent Container //

            PersistentDataContainer customBlockData = new CustomBlockData(block, Bukkit.getPluginManager().getPlugin("island-totem"));

            if (customBlockData.has(namespacedKey, PersistentDataType.STRING)) {
                String storedValue = customBlockData.get(namespacedKey, PersistentDataType.STRING);
                Bukkit.broadcastMessage("Bloc déjà cassé ! &a" + storedValue);
            } else {
                customBlockData.set(namespacedKey, PersistentDataType.STRING, "test");
            }

        }
    }

    public static boolean isBlockPersisted(Block block, NamespacedKey namespacedKey) {
        if(isCrop(block)) {
            PersistentDataContainer customBlockData = new CustomBlockData(block, Bukkit.getPluginManager().getPlugin("island-totem"));
            if(customBlockData.has(namespacedKey, PersistentDataType.STRING)) {
                return true;
            }
        }
        return false;
    }

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

    public static double calculateTimeReduction(int level) {
        return (level * level) / 4.1 + 2.5;
    }

    public static int calculateNewAge(int currentAge, int maxAge, double timeReduction) {
        // Calculation of the new age of culture according to the time reduction
        int ageIncrement = (int) Math.round(timeReduction / 100 * maxAge);
        int newAge = currentAge + ageIncrement;
        return Math.min(newAge, maxAge);
    }
}
