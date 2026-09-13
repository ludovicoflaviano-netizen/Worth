package com.ludovicoflaviano.worth;

import io.papermc.paper.event.player.PlayerItemHeldEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class WorthTooltipListener implements Listener {
    private static final String MARKER = "worth-tooltip-line";
    private final WorthPlugin plugin;

    public WorthTooltipListener(WorthPlugin plugin) { this.plugin = plugin; }

    public void update(Player player) {
        for (ItemStack item : player.getInventory().getContents()) updateItem(player, item);
    }

    private void updateItem(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        WorthService.OptionalDoubleValue value = plugin.getWorthService().getWorth(player, item);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.removeIf(c -> c.toString().contains(MARKER));
        if (value.present() && plugin.getConfig().getBoolean("tooltip.enabled", true)) {
            String prefix = plugin.getConfig().getString("tooltip.prefix", "💵");
            String text = plugin.getConfig().getString("tooltip.format", "%s Worth: %s");
            String line = String.format(text, prefix, plugin.formatMoney(value.value()));
            lore.add(Component.text(line).color(NamedTextColor.GREEN));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    @EventHandler public void join(PlayerJoinEvent e) { Bukkit.getScheduler().runTask(plugin, () -> update(e.getPlayer())); }
    @EventHandler public void click(InventoryClickEvent e) { if (e.getWhoClicked() instanceof Player p) Bukkit.getScheduler().runTask(plugin, () -> update(p)); }
    @EventHandler public void drag(InventoryDragEvent e) { if (e.getWhoClicked() instanceof Player p) Bukkit.getScheduler().runTask(plugin, () -> update(p)); }
    @EventHandler public void held(PlayerItemHeldEvent e) { Bukkit.getScheduler().runTask(plugin, () -> update(e.getPlayer())); }
    @EventHandler public void breakItem(PlayerItemBreakEvent e) { Bukkit.getScheduler().runTask(plugin, () -> update(e.getPlayer())); }
    @EventHandler public void quit(PlayerQuitEvent e) { }
}
