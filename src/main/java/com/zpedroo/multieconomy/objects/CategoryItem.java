package com.zpedroo.multieconomy.objects;

import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.List;

public class CategoryItem {

    private String permission;
    private String permissionMessage;
    private Currency currency;
    private BigInteger price;
    private BigInteger defaultAmount;
    private ItemStack display;
    private ItemStack shopItem;
    private Boolean inventoryLimit;
    private List<String> commands;

    public CategoryItem(String permission, String permissionMessage, Currency currency, BigInteger price, BigInteger defaultAmount, ItemStack display, ItemStack shopItem, Boolean inventoryLimit, List<String> commands) {
        this.permission = permission;
        this.permissionMessage = permissionMessage;
        this.currency = currency;
        this.price = price;
        this.defaultAmount = defaultAmount;
        this.display = display;
        this.shopItem = shopItem;
        this.inventoryLimit = inventoryLimit;
        this.commands = commands;
    }

    public String getPermission() {
        return permission;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigInteger getPrice() {
        return price;
    }

    public BigInteger getDefaultAmount() {
        return defaultAmount;
    }

    public ItemStack getDisplay() {
        return display;
    }

    public ItemStack getShopItem() {
        return shopItem;
    }

    public Boolean hasInventoryLimit() {
        return inventoryLimit;
    }

    public List<String> getCommands() {
        return commands;
    }
}