package com.zpedroo.multieconomy.objects.general;

import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.player.PlayerData;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;

@Data
public class Currency {

    private final Map<String, String[]> titles;
    private final Map<String, String> inventoryTitles;
    private final Map<String, String> commandKeys;
    private final String fileName;
    private final String currencyName;
    private final String currencyDisplay;
    private final String currencyColor;
    private final String amountDisplay;
    private final String topOneTag;
    private final int taxPerTransaction;
    private final ItemStack item;

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

    public String getAmountDisplay(BigInteger amount) {
        return StringUtils.replace(amountDisplay, "{amount}", NumberFormatter.getInstance().format(amount));
    }

    public UUID getTopOneUniqueId() {
        List<PlayerData> topCurrency = DataManager.getInstance().getCache().getTopCurrencies().get(this);
        if (topCurrency == null || topCurrency.isEmpty()) return null;

        return topCurrency.stream().findFirst().get().getUUID();
    }

    public boolean isTopOne(@NotNull Player player) {
        return isTopOne(player.getUniqueId());
    }

    public boolean isTopOne(UUID uuid) {
        return getTopOneUniqueId().equals(uuid);
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