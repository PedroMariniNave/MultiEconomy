package com.zpedroo.multieconomy.objects.category;

import com.zpedroo.multieconomy.objects.general.Currency;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryItem {

    private final String permission;
    private final String permissionMessage;
    private final Currency currency;
    private final BigInteger price;
    private final BigInteger defaultAmount;
    private final ItemStack displayItem;
    private final ItemStack itemToGive;
    private final BigInteger maxStock;
    private BigInteger stockAmount;
    private final boolean inventoryLimit;
    private final boolean fixedItem;
    private final List<String> commands;

    public BigInteger getStockAmount() {
        if (stockAmount == null) stockAmount = maxStock;

        return stockAmount;
    }

    public ItemStack getDisplayItem() {
        return displayItem.clone();
    }

    public ItemStack getDisplayItemWithPlaceholdersReplaced(String[] placeholders, String[] replacers) {
        ItemStack item = displayItem.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (meta.getDisplayName() != null) {
            String displayName = meta.getDisplayName();
            meta.setDisplayName(StringUtils.replaceEach(displayName, placeholders, replacers));
        }

        if (meta.getLore() != null) {
            List<String> lore = meta.getLore();
            List<String> newLore = new ArrayList<>(lore.size());

            for (String lines : lore) {
                newLore.add(StringUtils.replaceEach(lines, placeholders, replacers));
            }

            meta.setLore(newLore);
        }

        item.setItemMeta(meta);
        return item;
    }

    public boolean isUsingStock() {
        return maxStock.signum() > 0;
    }

    public boolean hasAvailableStock(BigInteger amount) {
        if (maxStock.signum() <= 0) return true;

        return stockAmount.compareTo(amount) >= 0;
    }
}