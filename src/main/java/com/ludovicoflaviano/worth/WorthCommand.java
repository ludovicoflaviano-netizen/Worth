package com.ludovicoflaviano.worth;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class WorthCommand implements CommandExecutor, TabCompleter {
    private final WorthPlugin plugin;

    public WorthCommand(WorthPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendRichMessage(plugin.getConfig().getString("messages.player-only", "<red>This command can only be used by a player.</red>"));
            return true;
        }
        if (!player.hasPermission("worth.use")) {
            player.sendRichMessage("<red>You do not have permission to use this command.</red>");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("hand")) {
            return hand(player);
        }
        if (args[0].equalsIgnoreCase("inventory") || args[0].equalsIgnoreCase("inv")) {
            double total = plugin.getWorthService().getInventoryWorth(player);
            String template = plugin.getConfig().getString("messages.total", "<gray>Your inventory is worth <green><price></green></gray>");
            player.sendRichMessage(template.replace("<price>", plugin.formatMoney(total)));
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("worth.admin")) {
                player.sendRichMessage("<red>You do not have permission to reload Worth.</red>");
                return true;
            }
            plugin.reloadWorth();
            player.sendRichMessage(plugin.getConfig().getString("messages.reloaded", "<green>Worth configuration reloaded.</green>"));
            return true;
        }

        Material material = Material.matchMaterial(args[0]);
        if (material == null || material == Material.AIR) {
            player.sendRichMessage(plugin.getConfig().getString("messages.usage", "<gray>Usage: <white>/worth [hand|inventory|item]</white></gray>"));
            return true;
        }

        ItemStack stack = new ItemStack(material, 1);
        WorthService.OptionalDoubleValue worth = plugin.getWorthService().getWorth(player, stack);
        if (!worth.present()) {
            player.sendRichMessage(plugin.getConfig().getString("messages.no-price", "<red>This item is not sellable in EconomyShopGUI.</red>"));
            return true;
        }
        String name = material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        String template = plugin.getConfig().getString("messages.hand", "<gray><item> x<amount> is worth <green><price></green></gray>");
        player.sendRichMessage(template
                .replace("<item>", name)
                .replace("<amount>", "1")
                .replace("<price>", plugin.formatMoney(worth.value())));
        return true;
    }

    private boolean hand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendRichMessage(plugin.getConfig().getString("messages.no-price", "<red>This item is not sellable in EconomyShopGUI.</red>"));
            return true;
        }

        WorthService.OptionalDoubleValue worth = plugin.getWorthService().getWorth(player, item);
        if (!worth.present()) {
            player.sendRichMessage(plugin.getConfig().getString("messages.no-price", "<red>This item is not sellable in EconomyShopGUI.</red>"));
            return true;
        }

        String itemName = item.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ');
        String template = plugin.getConfig().getString("messages.hand", "<gray><item> x<amount> is worth <green><price></green></gray>");
        player.sendRichMessage(template
                .replace("<item>", itemName)
                .replace("<amount>", Integer.toString(item.getAmount()))
                .replace("<price>", plugin.formatMoney(worth.value())));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String value : List.of("hand", "inventory", "reload")) {
            if (value.startsWith(prefix)) {
                result.add(value);
            }
        }
        for (Material material : Material.values()) {
            String name = material.name().toLowerCase(Locale.ROOT);
            if (material.isItem() && name.startsWith(prefix)) {
                result.add(name);
            }
        }
        return result.size() > 100 ? result.subList(0, 100) : result;
    }
}
