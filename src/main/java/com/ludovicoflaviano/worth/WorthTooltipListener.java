package com.ludovicoflaviano.worth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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

/** Removes Worth lore written by older versions without ever adding metadata to real items. */
public final class WorthTooltipListener implements Listener {
    private static final String WORTH_PREFIX = "$ Worth:";

    public WorthTooltipListener(WorthPlugin plugin) {}

    public void cleanup(Player player) {
        for (ItemStack item : player.getInventory().getContents()) cleanupItem(item);
        for (ItemStack item : player.getInventory().getArmorContents()) cleanupItem(item);
        cleanupItem(player.getInventory().getItemInOffHand());
    }

    private void cleanupItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore() || meta.lore() == null) return;
        List<Component> oldLore = meta.lore();
        List<Component> cleaned = new ArrayList<>(oldLore);
        cleaned.removeIf(c -> PlainTextComponentSerializer.plainText().serialize(c).startsWith(WORTH_PREFIX));
        if (!cleaned.equals(oldLore)) {
            meta.lore(cleaned.isEmpty() ? null : cleaned);
            item.setItemMeta(meta);
        }
    }

    @EventHandler public void join(PlayerJoinEvent event) { cleanup(event.getPlayer()); }
    @EventHandler public void click(InventoryClickEvent event) { if (event.getWhoClicked() instanceof Player p) cleanup(p); }
    @EventHandler public void drag(InventoryDragEvent event) { if (event.getWhoClicked() instanceof Player p) cleanup(p); }
    @EventHandler public void held(PlayerItemHeldEvent event) { cleanup(event.getPlayer()); }
    @EventHandler public void breakItem(PlayerItemBreakEvent event) { cleanup(event.getPlayer()); }
}
