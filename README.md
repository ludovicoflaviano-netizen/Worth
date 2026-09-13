# Worth

Worth is a lightweight Paper 1.21.11 plugin for SMP servers that want a DonutSMP-style economy display.

## Features

- Reads item sell values directly from EconomyShopGUI.
- Uses the EconomyShopGUI API, so configured shop prices remain the source of truth.
- Uses Vault's Bukkit Economy service for player balances and money formatting. This works with Vault 2.0 economy providers.
- Shows a green `$` objective with the player's balance below their username using the vanilla `BELOW_NAME` scoreboard slot.
- `/worth` or `/worth hand` shows the value of the item in your main hand.
- `/worth inventory` totals the sell value of your storage, armor, and offhand contents.
- `/worth <material>` checks a specific Minecraft material.
- `/worth reload` reloads the plugin configuration for administrators.
- No custom economy database: balances remain owned by the configured Vault economy provider.

## Dependencies

Install these plugins on the server:

1. Paper 1.21.11.
2. Vault 2.0 (or another compatible Vault economy provider).
3. EconomyShopGUI 7.x for the 1.21.11/26.x server line.

The plugin uses the public EconomyShopGUI API. EconomyShopGUI documents the API as a provided/compile-only dependency and recommends a soft dependency on both the free and Premium plugin names.

## Important note about "every item"

Worth does not invent a second price list. The shop is the source of truth. If an item has a sell price in EconomyShopGUI, `/worth` can read it. If the item is not configured as a sellable shop item, Worth reports that it has no shop value.

The below-name balance uses a vanilla scoreboard score, which is an integer. The `$` is green and the displayed balance is rounded to a whole number by default.

## Build

```bash
mvn -B package
```

The resulting jar is `target/Worth.jar`.
