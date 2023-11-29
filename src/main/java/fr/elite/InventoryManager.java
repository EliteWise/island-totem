package fr.elite;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class InventoryManager implements InventoryProvider {

    public static final SmartInventory INVENTORY = SmartInventory.builder()
            .id("totem-inv")
            .provider(new InventoryManager())
            .size(3, 9)
            .title("Totem d’île")
            .build();

    private final Random random = new Random();

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));

        List<String> lores = Arrays.asList("Ligne 1", "Ligne 2", "Ligne 3");

        contents.set(1, 1, ClickableItem.of(addItemLores(Material.GOLDEN_HOE, lores),
                e -> player.sendMessage(ChatColor.GOLD + "Niveau: X/20")));

        contents.set(1, 2, ClickableItem.of(addItemLores(Material.COMPASS, lores),
                e -> player.sendMessage(ChatColor.GOLD + "Niveau: X/20")));

        contents.set(1, 4, ClickableItem.of(addItemLores(Material.EMERALD, lores),
                e -> player.sendMessage(ChatColor.GOLD + "Nombres de points disponibles: X/20")));

        contents.set(1, 6, ClickableItem.of(addItemLores(Material.GOLDEN_PICKAXE, lores),
                e -> player.sendMessage(ChatColor.GOLD + "Niveau: X/20")));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    public ItemStack addItemLores(Material material, List<String> loreList) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> formattedLoreList = new ArrayList<>();
        for(String lore : loreList) {
            formattedLoreList.add(lore);
            formattedLoreList.add(" ");
        }
        formattedLoreList.remove(formattedLoreList.size() - 1);
        itemMeta.setLore(formattedLoreList);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}