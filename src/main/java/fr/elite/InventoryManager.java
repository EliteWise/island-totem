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

import java.io.IOException;
import java.util.*;

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

        List<Integer> skillsPoints = Arrays.asList(10, 20, 30, 40);
        List<Integer> levels = Arrays.asList(1, 2, 3);
        int pointsIndex = 0;
        int levelsIndex = 0;

        int skillPoint = -1;
        int level = -1;

        try {
            List<InventoryItem> inventoryItem = Utils.deserializeJsonFile("inventory-items.json");
            for(InventoryItem it : inventoryItem) {
                if(it.getLores().stream().anyMatch(lore -> lore.contains("{points}")) && pointsIndex < skillsPoints.size()) {
                    skillPoint = skillsPoints.get(pointsIndex);
                    pointsIndex++;
                }
                if(it.getLores().stream().anyMatch(lore -> lore.contains("{level}")) && levelsIndex < levels.size()) {
                    level = levels.get(levelsIndex);
                    levelsIndex++;
                }
                contents.set(it.getPosition().getX(), it.getPosition().getY(), ClickableItem.of(addItemLores(Material.getMaterial(it.getMaterial()), it.getLores(), skillPoint, level),
                        e -> player.sendMessage(ChatColor.GOLD + "[Totem d’île] " + ChatColor.valueOf(it.getColor()) + it.getMessage())));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    public ItemStack addItemLores(Material material, List<String> loreList, int skillPoint, int level) throws IOException {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> formattedLoreList = new ArrayList<>();
        List<InventoryItem> inventoryItem = Utils.deserializeJsonFile("inventory-items.json");
        int index = 0;
        for(String lore : loreList) {
            if(skillPoint != -1) {
                lore = lore.replace("{points}", ChatColor.GOLD + String.valueOf(skillPoint));
            }
            if(level != -1) {
                lore = lore.replace("{level}", ChatColor.GOLD + String.valueOf(level));
            }
            formattedLoreList.add(ChatColor.valueOf(inventoryItem.get(index).getColor()) + lore);
            formattedLoreList.add(" ");
            index++;
        }
        itemMeta.setLore(formattedLoreList);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}