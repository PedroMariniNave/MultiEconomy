package com.zpedroo.multieconomy.utils.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryChecker {

    public static int getFreeSpace(Player player, ItemStack item) {
        int free = 0;

        for (int slot = 0; slot < 36; ++slot) {
            ItemStack items = player.getInventory().getItem(slot);
            if (items == null || items.getType().equals(Material.AIR)) {
                free += item.getMaxStackSize();
                continue;
            }

            if (items.isSimilar(item)) free += item.getMaxStackSize() - items.getAmount();
        }

        return free;
    }
}