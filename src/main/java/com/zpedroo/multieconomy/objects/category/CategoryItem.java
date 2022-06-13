package com.zpedroo.multieconomy.objects.category;

import com.zpedroo.multieconomy.objects.general.Currency;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.List;

@Data
public class CategoryItem {

    private final String permission;
    private final String permissionMessage;
    private final Currency currency;
    private final BigInteger price;
    private final BigInteger defaultAmount;
    private final ItemStack display;
    private final ItemStack itemToGive;
    private final int requiredLevel;
    private final BigInteger maxStock;
    private BigInteger stockAmount;
    private final boolean inventoryLimit;
    private final boolean fixedItem;
    private final List<String> commands;

    public BigInteger getStockAmount() {
        if (stockAmount == null) stockAmount = maxStock;

        return stockAmount;
    }
}