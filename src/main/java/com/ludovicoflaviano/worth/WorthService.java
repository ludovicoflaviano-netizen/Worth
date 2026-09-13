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
        final int amount = Math.max(1, item.getAmount());
        final ItemStack one = item.asOne(); // clone; original inventory stack is never changed
        Double unit = findUnitSellPrice(player, one);
        if (unit == null || !Double.isFinite(unit) || unit < 0) return OptionalDoubleValue.empty();
        double total = unit * amount;
        if (!Double.isFinite(total)) return OptionalDoubleValue.empty();
        return OptionalDoubleValue.of(total);
    }

    public OptionalDoubleValue getUnitWorth(Player player, Material material) {
        if (material == null || material.isAir()) return OptionalDoubleValue.empty();
        return getWorth(player, new ItemStack(material, 1));
    }

    private Double findUnitSellPrice(Player player, ItemStack one) {
        Double price = findPrice(player, one);
        if (price != null) return price;
        // EconomyShopGUI can have material-only entries; retry without custom components.
        if (one.getType().isItem()) return findPrice(player, new ItemStack(one.getType(), 1));
        return null;
    }

    private Double findPrice(Player player, ItemStack one) {
        try {
            ShopItem shopItem = player == null
                    ? EconomyShopGUIHook.getShopItem(one)
                    : EconomyShopGUIHook.getShopItem(player, one);
            if (shopItem == null) shopItem = EconomyShopGUIHook.getShopItem(one);
            if (shopItem == null || !EconomyShopGUIHook.isSellAble(shopItem)) return null;

            Double price = player == null
                    ? EconomyShopGUIHook.getItemSellPrice(shopItem, one, null, 1, 0)
                    : EconomyShopGUIHook.getItemSellPrice(shopItem, one, player, 1, 0);
            return price != null && Double.isFinite(price) && price >= 0 ? price : null;
        } catch (Throwable t) {
            plugin.getLogger().fine("Unable to resolve sell price for " + one.getType() + ": " + t.getClass().getSimpleName());
            return null;
        }
    }

    public void clearCache() {
        // Intentionally uncached. EconomyShopGUI prices are read fresh on each calculation.
    }

    public double getInventoryWorth(Player player) {
        if (player == null) return 0.0;
        double total = 0.0;
        for (ItemStack item : player.getInventory().getStorageContents()) total = safeAdd(total, getWorth(player, item));
        for (ItemStack item : player.getInventory().getArmorContents()) total = safeAdd(total, getWorth(player, item));
        total = safeAdd(total, getWorth(player, player.getInventory().getItemInOffHand()));
        return total;
    }

    private double safeAdd(double total, OptionalDoubleValue value) {
        if (!value.present()) return total;
        double result = total + value.value();
        return Double.isFinite(result) ? result : total;
    }

    public record OptionalDoubleValue(boolean present, double value) {
        public static OptionalDoubleValue empty() { return new OptionalDoubleValue(false, 0.0); }
        public static OptionalDoubleValue of(double value) { return new OptionalDoubleValue(true, value); }
        public double valueIfPresent() { return present ? value : 0.0; }
    }
}
