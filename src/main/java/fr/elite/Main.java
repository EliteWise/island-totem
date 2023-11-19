package fr.elite;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.net.http.WebSocket;
import java.util.HashMap;

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

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        utils.craftTotem(totemMaterials, totemCraftPattern, textureValue);
    }

    @Override
    public void onDisable() {

    }
}
