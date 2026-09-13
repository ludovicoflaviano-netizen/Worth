package com.ludovicoflaviano.worth;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.objects.ShopItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class WorthService {
    private final WorthPlugin plugin;

    public WorthService(WorthPlugin plugin) { this.plugin = plugin; }

    public OptionalDoubleValue getWorth(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) return OptionalDoubleValue.empty();
        int amount = Math.max(1, item.getAmount());
        ItemStack one = item.clone();
        one.setAmount(1);
        Double unitPrice = findUnitSellPrice(player, one);
        return unitPrice == null ? OptionalDoubleValue.empty() : OptionalDoubleValue.of(unitPrice * amount);
    }

    public OptionalDoubleValue getUnitWorth(Player player, Material material) {
        if (material == null || material.isAir()) return OptionalDoubleValue.empty();
        return getWorth(player, new ItemStack(material, 1));
    }

    private Double findUnitSellPrice(Player player, ItemStack one) {
        Double price = findPrice(player, one);
        if (price != null) return price;
        return findPrice(player, new ItemStack(one.getType(), 1));
    }

    private Double findPrice(Player player, ItemStack one) {
        ShopItem shopItem;
        try {
            shopItem = EconomyShopGUIHook.getShopItem(player, one);
            if (shopItem == null) shopItem = EconomyShopGUIHook.getShopItem(one);
        } catch (Throwable t) {
            plugin.getLogger().fine("Could not resolve EconomyShopGUI item for " + one.getType() + ": " + t.getMessage());
            return null;
        }
        if (shopItem == null || !EconomyShopGUIHook.isSellAble(shopItem)) return null;
        try {
            Double price = EconomyShopGUIHook.getItemSellPrice(shopItem, one, player, 1, 0);
            return price != null && Double.isFinite(price) && price >= 0 ? price : null;
        } catch (Throwable t) {
            plugin.getLogger().fine("Could not read EconomyShopGUI sell price for " + one.getType() + ": " + t.getMessage());
            return null;
        }
    }

    public double getInventoryWorth(Player player) {
        double total = 0.0;
        for (ItemStack item : player.getInventory().getStorageContents()) total += getWorth(player, item).valueIfPresent();
        for (ItemStack item : player.getInventory().getArmorContents()) total += getWorth(player, item).valueIfPresent();
        total += getWorth(player, player.getInventory().getItemInOffHand()).valueIfPresent();
        return total;
    }

    public record OptionalDoubleValue(boolean present, double value) {
        public static OptionalDoubleValue empty() { return new OptionalDoubleValue(false, 0.0); }
        public static OptionalDoubleValue of(double value) { return new OptionalDoubleValue(true, value); }
        public double valueIfPresent() { return present ? value : 0.0; }
    }
}
