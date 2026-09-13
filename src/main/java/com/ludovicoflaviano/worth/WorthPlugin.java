package com.ludovicoflaviano.worth;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public final class WorthPlugin extends JavaPlugin {
    private Economy economy;
    private WorthService worthService;
    private Objective balanceObjective;
    private WorthTooltipListener tooltipListener;
    private boolean createdBalanceObjective;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().warning("Vault economy was not found. Balance display and Vault formatting will be unavailable until an economy provider is loaded.");
        }

        if (Bukkit.getPluginManager().getPlugin("EconomyShopGUI") == null
                && Bukkit.getPluginManager().getPlugin("EconomyShopGUI-Premium") == null) {
            getLogger().warning("EconomyShopGUI was not found. /worth will not have shop-backed values until it is installed.");
        }

        worthService = new WorthService(this);
        tooltipListener = new WorthTooltipListener(this);
        Bukkit.getPluginManager().registerEvents(tooltipListener, this);
        for (Player player : Bukkit.getOnlinePlayers()) tooltipListener.cleanup(player);

        PluginCommand command = getCommand("worth");
        if (command != null) {
            WorthCommand worthCommand = new WorthCommand(this);
            command.setExecutor(worthCommand);
            command.setTabCompleter(worthCommand);
        }

        setupBalanceObjective();
        startBalanceTask();
        getLogger().info("Worth enabled.");
    }

    @Override
    public void onDisable() {
        if (createdBalanceObjective && balanceObjective != null) {
            balanceObjective.unregister();
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> registration = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (registration == null) {
            return false;
        }
        economy = registration.getProvider();
        return economy != null;
    }

    private void setupBalanceObjective() {
        if (!getConfig().getBoolean("balance.enabled", true)) {
            return;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        balanceObjective = scoreboard.getObjective("worth_balance");
        if (balanceObjective == null) {
            balanceObjective = scoreboard.registerNewObjective(
                    "worth_balance",
                    "dummy",
                    net.kyori.adventure.text.Component.text(
                            getConfig().getString("balance.symbol", "$"),
                            net.kyori.adventure.text.format.NamedTextColor.GREEN
                    )
            );
            createdBalanceObjective = true;
        }
        balanceObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    private void startBalanceTask() {
        if (!getConfig().getBoolean("balance.enabled", true)) {
            return;
        }
        long interval = Math.max(1L, getConfig().getLong("balance.update-ticks", 20L));
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (balanceObjective == null || economy == null) {
                return;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                double balance = economy.getBalance(player);
                long rounded = getConfig().getBoolean("balance.round-decimals", true)
                        ? Math.round(balance) : (long) balance;
                int score = (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, rounded));
                balanceObjective.getScore(player.getName()).setScore(score);
            }
        }, 1L, interval);
    }

    public Economy getEconomy() {
        return economy;
    }

    public WorthService getWorthService() {
        return worthService;
    }

    public void reloadWorth() {
        reloadConfig();
        worthService.clearCache();
        if (balanceObjective != null) {
            balanceObjective.displayName(net.kyori.adventure.text.Component.text(
                    getConfig().getString("balance.symbol", "$"),
                    net.kyori.adventure.text.format.NamedTextColor.GREEN
            ));
        }
    }

    public String formatMoney(double amount) {
        if (economy != null) {
            try {
                return economy.format(amount);
            } catch (Exception ignored) {
            }
        }
        int decimals = Math.max(0, getConfig().getInt("worth.decimal-places", 2));
        return String.format(java.util.Locale.US, "%,." + decimals + "f", amount);
    }
}
