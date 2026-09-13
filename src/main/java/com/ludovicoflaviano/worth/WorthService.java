package com.ludovicoflaviano.worth;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.objects.ShopItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class WorthService {
    private final WorthPlugin plugin;
    private final Map<String, Double> unitCache = new ConcurrentHashMap<>();

    public WorthService(WorthPlugin plugin) {
        this.plugin = plugin;
    }

    public OptionalDoubleValue getWorth(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return OptionalDoubleValue.empty();
        }

        ItemStack one = item.clone();
        one.setAmount(1);

        ShopItem shopItem = EconomyShopGUIHook.getShopItem(player, one);
        if (shopItem == null) {
            shopItem = EconomyShopGUIHook.getShopItem(one);
        }
        if (shopItem == null || !EconomyShopGUIHook.isSellAble(shopItem)) {
            return OptionalDoubleValue.empty();
        }

        double unitPrice;
        try {
            Double price = EconomyShopGUIHook.getItemSellPrice(shopItem, one, player, 1, 0);
            if (price == null || !Double.isFinite(price) || price < 0) {
                return OptionalDoubleValue.empty();
            }
            unitPrice = price;
        } catch (Throwable throwable) {
            plugin.getLogger().fine("Could not read EconomyShopGUI price for " + item.getType() + ": " + throwable.getMessage());
            return OptionalDoubleValue.empty();
        }

        return OptionalDoubleValue.of(unitPrice * Math.max(1, item.getAmount()));
    }

    public OptionalDoubleValue getUnitWorth(Player player, Material material) {
        if (material == Material.AIR) {
            return OptionalDoubleValue.empty();
        }
        ItemStack stack = new ItemStack(material, 1);
        return getWorth(player, stack);
    }

    public double getInventoryWorth(Player player) {
        double total = 0.0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            OptionalDoubleValue worth = getWorth(player, item);
            if (worth.present()) {
                total += worth.value();
            }
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            OptionalDoubleValue worth = getWorth(player, item);
            if (worth.present()) {
                total += worth.value();
            }
        }
        OptionalDoubleValue offhand = getWorth(player, player.getInventory().getItemInOffHand());
        if (offhand.present()) {
            total += offhand.value();
        }
        return total;
    }

    public void clearCache() {
        unitCache.clear();
    }

    public record OptionalDoubleValue(boolean present, double value) {
        public static OptionalDoubleValue empty() {
            return new OptionalDoubleValue(false, 0.0);
        }

        public static OptionalDoubleValue of(double value) {
            return new OptionalDoubleValue(true, value);
        }
    }
}
