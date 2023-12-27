package fr.elite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.http.WebSocket;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class Main extends JavaPlugin implements WebSocket.Listener, @NotNull Listener {

    private NamespacedKey preventDupliPluginKey;
    private NamespacedKey levelPluginKey;

    HashMap<Character, Material> totemMaterials = new HashMap<>() {{
        put('E', Material.EMERALD_BLOCK);
        put('P', Material.DIAMOND_PICKAXE);
        put('H', Material.DIAMOND_HOE);
        put('R', Material.AIR);
    }};
    String[] totemCraftPattern = {"EPE", "HRH", "EPE"};
    String textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDkzNmJiMWNjNGFiNmVjY2U2NWI2NDI5ODM5NGZhZmM1ZmUzZjc4NzZkN2M5NDFkMDVhOTI5NGZhMzkyYjdjIn19fQ==";

    Utils utils = new Utils();

    public static HashMap<Player, Integer> totemPoints = new HashMap<>();
    private File configFile;

    public final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public Map<String, Object> gsonMap = new HashMap<>();

    private Database database;
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        preventDupliPluginKey = new NamespacedKey(this, "island-totem");
        levelPluginKey = new NamespacedKey(this, "island-totem");

        try {
            // Ensure the plugin's data folder exists
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            database = new Database(getDataFolder().getAbsolutePath() + "/totem.db");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to database! " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }


        configFile = new File(getDataFolder(), "inventory-items.json");
        if (!configFile.exists()) saveResource(configFile.getName(), false);

        this.getServer().getPluginManager().registerEvents(this, this);
        utils.craftTotem(totemMaterials, totemCraftPattern, textureValue);
    }

    @Override
    public void onDisable() {
        try {
            database.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public Database getDatabase() {
        return database;
    }

    @EventHandler
    public void onPlayerPlaceTotem(BlockPlaceEvent e) {
        Block placedBlock = e.getBlockPlaced();
        Player player = e.getPlayer();
        ItemStack itemInHand = e.getItemInHand();
        if(placedBlock.getType() == Material.PLAYER_HEAD && itemInHand.getItemMeta().getDisplayName().contains("Totem d’île")) {
            Bukkit.getServer().getWorld(player.getWorld().getName()).playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 1, 1);
        }
    }

    @EventHandler
    public void onPlayerClickOnTotem(PlayerInteractEvent e) throws SQLException {
        Player player = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();
        if(e.getHand() == EquipmentSlot.HAND) return;
        ItemStack itemInHand = player.getItemInHand();

        if(clickedBlock.getType() == Material.PLAYER_HEAD) {
            if(itemInHand.getType() == Material.EMERALD) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
                //totemPoints.put(player, totemPoints.getOrDefault(player, 0) + 1);
                if(getDatabase().playerExists(player)) {
                    int totem_levels = getDatabase().getPlayerAttribute(player, "totem_levels");
                    getDatabase().updatePlayerAttribute(player, "totem_levels", totem_levels + 1);
                } else {
                    getDatabase().addPlayer(player);
                }
                player.sendMessage(ChatColor.GOLD + "[Totem d’île] " + ChatColor.YELLOW + "Votre émeraude a été converti en point pour ce totem.");
            } else {
                InventoryManager.INVENTORY.open(player);
            }
        }
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent e) throws SQLException {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        if(!Utils.isCrop(block) && !Utils.isOre(block)) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        int enchantLevel = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);

        int playerLevel = getDatabase().getPlayerAttribute(player, "ores_quantity_level"); // Replace with a dynamic detection of the block type with the right attribute

        // Chances init
        double chanceDouble = (playerLevel * playerLevel) / (4.1 + 2.5);
        double chanceQuadruple = chanceDouble / 3;

        // Cancel the original drop
        e.setDropItems(false);

        double randomValue = Math.random() * 100; // Between 0 and 100

        ItemStack addedDrop = block.getDrops().stream().findFirst().orElse(null);

        if(addedDrop != null) {
            addedDrop.setAmount(1);
            // Check to *4 drops first
            if (randomValue <= chanceQuadruple && !Utils.isBlockPersisted(block, preventDupliPluginKey)) {
                player.sendMessage("+4");
                addedDrop.setAmount(4);
            } else if (randomValue <= chanceDouble && !Utils.isBlockPersisted(block, preventDupliPluginKey)) {
                player.sendMessage("+2");
                addedDrop.setAmount(2);
            }

            Random random = new Random();
            int enchantLoot = Utils.fortuneEnchantSimulation(enchantLevel, random, block);
            if(enchantLoot != 0) {
                addedDrop.setAmount(addedDrop.getAmount() + enchantLoot);
            }
            player.getWorld().dropItemNaturally(block.getLocation(), addedDrop);
        }

    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();
        if(Utils.isCrop(block)) {
            Utils.persistBlock(block, preventDupliPluginKey, levelPluginKey, player);
        }
    }

    @EventHandler
    public void onPlantGrow(BlockGrowEvent e) {
        Block block = e.getBlock();
        BlockData blockData = block.getBlockData();

        if(Utils.isCrop(block)) {
            PersistentDataContainer customBlockData = new CustomBlockData(block, Bukkit.getPluginManager().getPlugin("island-totem"));
            String placedData = customBlockData.get(preventDupliPluginKey, PersistentDataType.STRING);
            if(placedData.equalsIgnoreCase("placed")) {
                customBlockData.remove(preventDupliPluginKey);
            }

            String levelData = customBlockData.get(levelPluginKey, PersistentDataType.STRING);
            int playerLevel = 0;


            if(levelData.matches(".*\\d.*")) {
                playerLevel = Integer.parseInt(levelData.split("|")[1]);
            }

            if(blockData instanceof Ageable) {
                Ageable ageable = (Ageable) blockData;
                int currentAge = ageable.getAge();
                int maxAge = ageable.getMaximumAge();
                double timeReduction = Utils.calculateTimeReduction(playerLevel);

                int newAge = Utils.calculateNewAge(currentAge, maxAge, timeReduction);
                if(newAge > currentAge) {
                    ageable.setAge(newAge);
                    block.setBlockData(ageable);
                }
            }
        }

    }

}
