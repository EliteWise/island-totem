package fr.elite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.http.WebSocket;
import java.util.HashMap;
import java.util.Map;

public final class Main extends JavaPlugin implements WebSocket.Listener, @NotNull Listener {

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

    @Override
    public void onEnable() {
        configFile = new File(getDataFolder(), "inventory-items.json");
        if (!configFile.exists()) saveResource(configFile.getName(), false);

        this.getServer().getPluginManager().registerEvents(this, this);
        utils.craftTotem(totemMaterials, totemCraftPattern, textureValue);
    }

    @Override
    public void onDisable() {

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
    public void onPlayerClickOnTotem(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();
        if(e.getHand() == EquipmentSlot.HAND) return;
        ItemStack itemInHand = player.getItemInHand();

        if(clickedBlock.getType() == Material.PLAYER_HEAD) {
            if(itemInHand.getType() == Material.EMERALD) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
                totemPoints.put(player, totemPoints.getOrDefault(player, 0) + 1);
                player.sendMessage(ChatColor.GOLD + "[Totem d’île] " + ChatColor.YELLOW + "Votre émeraude a été converti en point pour ce totem.");
            } else {
                InventoryManager.INVENTORY.open(player);
            }
        }
    }
}
