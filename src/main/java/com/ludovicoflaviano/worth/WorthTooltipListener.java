package com.ludovicoflaviano.worth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class WorthTooltipListener implements Listener {
    private static final String WORTH_PREFIX = "$ Worth:";
    private final WorthPlugin plugin;

    public WorthTooltipListener(WorthPlugin plugin) {
        this.plugin = plugin;
    }

    public void update(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            updateItem(player, item);
        }
    }

    private void updateItem(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<Component> lore = meta.hasLore() && meta.lore() != null
                ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.removeIf(component -> PlainTextComponentSerializer.plainText()
                .serialize(component).startsWith(WORTH_PREFIX));

        if (plugin.getConfig().getBoolean("tooltip.enabled", true)) {
            WorthService.OptionalDoubleValue value = plugin.getWorthService().getWorth(player, item);
            if (value.present()) {
                Component line = Component.text("$", NamedTextColor.GREEN)
                        .append(Component.text(" Worth: ", NamedTextColor.GRAY))
                        .append(Component.text(plugin.formatMoney(value.value()), NamedTextColor.GREEN));
                lore.add(line);
            }
        }
        meta.lore(lore.isEmpty() ? null : lore);
        item.setItemMeta(meta);
    }

    private void scheduleUpdate(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> update(player));
    }

    @EventHandler public void join(PlayerJoinEvent event) { scheduleUpdate(event.getPlayer()); }
    @EventHandler public void click(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) scheduleUpdate(player);
    }
    @EventHandler public void drag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) scheduleUpdate(player);
    }
    @EventHandler public void held(PlayerItemHeldEvent event) { scheduleUpdate(event.getPlayer()); }
    @EventHandler public void breakItem(PlayerItemBreakEvent event) { scheduleUpdate(event.getPlayer()); }
}
