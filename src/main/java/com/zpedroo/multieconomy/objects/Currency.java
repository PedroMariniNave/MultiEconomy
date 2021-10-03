package com.zpedroo.multieconomy.objects;

import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Currency {

    private Map<String, String[]> titles;
    private Map<String, String> inventoryTitles;
    private Map<String, String> commandKeys;
    private String fileName;
    private String currencyName;
    private String currencyDisplay;
    private String currencyColor;
    private String amountDisplay;
    private Integer taxPerTransaction;
    private ItemStack item;

    public Currency(Map<String, String[]> titles, Map<String, String> inventoryTitles, Map<String, String> commandKeys, String fileName, String currencyName, String currencyDisplay, String currencyColor, String amountDisplay, Integer taxPerTransaction, ItemStack item) {
        this.titles = titles;
        this.inventoryTitles = inventoryTitles;
        this.commandKeys = commandKeys;
        this.fileName = fileName;
        this.currencyName = currencyName;
        this.currencyDisplay = currencyDisplay;
        this.currencyColor = currencyColor;
        this.amountDisplay = amountDisplay;
        this.taxPerTransaction = taxPerTransaction;
        this.item = item;
    }

    public Map<String, String[]> getTitles() {
        return titles;
    }

    public Map<String, String> getInventoryTitles() {
        return inventoryTitles;
    }

    public Map<String, String> getCommandKeys() {
        return commandKeys;
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return currencyName;
    }

    public String getDisplay() {
        return currencyDisplay;
    }

    public String getColor() {
        return currencyColor;
    }

    public String getAmountDisplay() {
        return amountDisplay;
    }

    public Integer getTaxPerTransaction() {
        return taxPerTransaction;
    }

    public ItemStack getItem(BigInteger amount) {
        NBTItem nbt = new NBTItem(item.clone());
        nbt.setString("CurrencyName", fileName);
        nbt.setString("CurrencyAmount", amount.toString());

        ItemStack item = nbt.getItem();
        if (item.getItemMeta() != null) {
            String displayName = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null;
            List<String> lore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : null;
            ItemMeta meta = item.getItemMeta();

            if (displayName != null) meta.setDisplayName(StringUtils.replaceEach(displayName, new String[] {
                    "{amount}"
            }, new String[] {
                    NumberFormatter.getInstance().format(amount)
            }));

            if (lore != null) {
                List<String> newLore = new ArrayList<>(lore.size());

                for (String str : lore) {
                    newLore.add(StringUtils.replaceEach(str, new String[] {
                            "{amount}"
                    }, new String[] {
                            NumberFormatter.getInstance().format(amount)
                    }));
                }

                meta.setLore(newLore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /*
     * We need to create a unique hashcode
     * to fix problems when using this class
     * as HashMap key.
     */
    @Override
    public int hashCode() {
        StringBuilder builder = new StringBuilder(6);

        for (Character character : fileName.toCharArray()) {
            if (builder.length() > 5) break;

            builder.append((int) character);
        }

        return Integer.parseInt(builder.toString());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        Currency toCompare = (Currency) object;

        return toCompare.getFileName().equals(fileName);
    }
}