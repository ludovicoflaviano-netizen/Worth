package com.ludovicoflaviano.worth;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Intentionally does not modify ItemStacks.
 * Worth must never be stored in item lore or metadata because that changes
 * the item components and can prevent otherwise identical items from stacking.
 */
public final class WorthTooltipListener implements Listener {
    public WorthTooltipListener(WorthPlugin plugin) {
    }

    public void cleanup(Player player) {
        // No-op by design. Never mutate a player's real ItemStack for Worth.
    }
}
