package fr.elite;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

        List<Integer> skillsPoints = Arrays.asList(0, 0, 0);
        List<Integer> levels = Arrays.asList(0, 0, 0, 0);
        int pointsIndex = 0;
        int levelsIndex = 0;

        int skillPoint = -1;
        int level = -1;

        List<String> dbAttributes = Arrays.asList("crops_quantity_level", "crops_speed_level", "totem_levels", "ores_quantity_level");
        int attributesIndex = 0;
        Database db = Main.getInstance().getDatabase();

        try {
            List<InventoryItem> inventoryItem = Utils.deserializeJsonFile("inventory-items.json");
            for(InventoryItem it : inventoryItem) {
                if(it.getLores().stream().anyMatch(lore -> lore.contains("{points}")) && pointsIndex < skillsPoints.size()) {
                    String attr = dbAttributes.get(pointsIndex);
                    skillPoint = 1;
                    pointsIndex++;
                }
                if(it.getLores().stream().anyMatch(lore -> lore.contains("{level}")) && levelsIndex < levels.size()) {
                    String attr = dbAttributes.get(levelsIndex);
                    level = db.getPlayerAttribute(player, attr);
                    levelsIndex++;
                }
                int finalAttributesIndex = attributesIndex;
                contents.set(it.getPosition().getX(), it.getPosition().getY(), ClickableItem.of(addItemLores(Material.getMaterial(it.getMaterial()), it.getLores(), skillPoint, level),
                        e -> {
                            try {
                                if(db.playerExists(player)) {
                                    String attr = dbAttributes.get(finalAttributesIndex);
                                    if(db.getPlayerAttribute(player, "totem_levels") > 0) {
                                        if(dbAttributes.indexOf(attr) != 2) {
                                            db.updatePlayerAttribute(player, attr, db.getPlayerAttribute(player, attr) + 1);
                                            db.updatePlayerAttribute(player, "totem_levels", db.getPlayerAttribute(player, "totem_levels") - 1);
                                            player.sendMessage(ChatColor.GOLD + "[Totem d’île] " + ChatColor.valueOf(it.getColor()) + it.getMessage());
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.GOLD + "[Totem d’île] " + ChatColor.valueOf(it.getColor()) + "Vous n'avez pas de points totem.");
                                    }
                                } else {
                                    db.addPlayer(player);
                                }
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                        }));
                attributesIndex += 1;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    /**
     * Adds lore and formatting to an ItemStack based on given parameters.
     *
     * @param material The material of the item stack to create.
     * @param loreList A list of lore strings that will be added to the ItemStack.
     * @param skillPoint The skill points to be inserted into the lore. If -1, it will not replace the placeholder.
     * @param level The level to be inserted into the lore. If -1, it will not replace the placeholder.
     * @return The ItemStack with the added lore and formatting.
     * @throws IOException If there is an error reading from the inventory-items.json file.
     */
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