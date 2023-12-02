package fr.elite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
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
}
