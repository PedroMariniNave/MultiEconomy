package com.zpedroo.multieconomy.listeners;

import com.zpedroo.multieconomy.MultiEconomy;
import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.category.CategoryItem;
import com.zpedroo.multieconomy.objects.general.Purchase;
import com.zpedroo.multieconomy.utils.config.Messages;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import com.zpedroo.multieconomy.utils.inventory.InventoryChecker;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.*;

public class ShopListeners implements Listener {

    private static final Map<Player, Purchase> playersBuying = new HashMap<>(8);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!playersBuying.containsKey(event.getPlayer())) return;

        event.setCancelled(true);

        Purchase purchase = playersBuying.remove(event.getPlayer());
        Player player = purchase.getPlayer();
        BigInteger amount = null;

        CategoryItem item = purchase.getItem();
        Currency currency = item.getCurrency();

        BigInteger currencyAmount = CurrencyAPI.getCurrencyAmount(player.getUniqueId(), currency);
        BigInteger price = item.getPrice();

        if (StringUtils.equals(event.getMessage(), "*")) {
            amount = currencyAmount.divide(price);
            if (isInvalidValue(amount)) {
                player.sendMessage(Messages.BUY_ALL_ZERO);
                return;
            }
        } else {
            amount = NumberFormatter.getInstance().filter(event.getMessage());
        }

        if (isInvalidValue(amount)) {
            player.sendMessage(Messages.INVALID_AMOUNT);
            return;
        }

        int limit = item.getDisplayItem().getMaxStackSize() == 1 ? 36 : 2304;
        if (amount.compareTo(BigInteger.valueOf(limit)) > 0 && item.isInventoryLimit()) amount = BigInteger.valueOf(limit);

        BigInteger freeSpace = BigInteger.valueOf(InventoryChecker.getFreeSpace(player, item.getDisplayItem()));
        BigInteger toCompare = item.isInventoryLimit() ? amount : BigInteger.ONE;
        if (freeSpace.compareTo(toCompare) < 0)  {
            player.sendMessage(StringUtils.replaceEach(Messages.NEED_SPACE, new String[]{
                    "{has}",
                    "{need}"
            }, new String[]{
                    NumberFormatter.getInstance().formatThousand(freeSpace.doubleValue()),
                    NumberFormatter.getInstance().formatThousand(amount.doubleValue())
            }));
            return;
        }

        if (item.isStockEnabled() && amount.compareTo(item.getStockAmount()) > 0) amount = item.getStockAmount();
        if (!item.hasAvailableStock(amount)) {
            player.sendMessage(Messages.INSUFFICIENT_STOCK);
            return;
        }

        BigInteger finalPrice = price.multiply(amount);
        if (currencyAmount.compareTo(finalPrice) < 0) {
            player.sendMessage(StringUtils.replaceEach(Messages.INSUFFICIENT_CURRENCY, new String[]{
                    "{has}",
                    "{need}"
            }, new String[]{
                    NumberFormatter.getInstance().format(currencyAmount),
                    NumberFormatter.getInstance().format(finalPrice)
            }));
            return;
        }

        CurrencyAPI.removeCurrencyAmount(player.getUniqueId(), currency, finalPrice);
        givePurchasedItems(player, amount, item);
        executePurchaseCommands(player, amount, item);
        sendPurchasedMessages(player, amount, item, currency, finalPrice);

        if (item.isStockEnabled()) {
            item.setStockAmount(item.getStockAmount().subtract(amount));
        }

        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.5f, 10f);
    }

    private boolean isInvalidValue(BigInteger amount) {
        return amount.signum() <= 0;
    }

    private void givePurchasedItems(Player player, BigInteger amount, CategoryItem item) {
        if (item.getItemToGive() != null) {
            ItemStack itemToGive = item.getItemToGive().clone();
            if (itemToGive.getMaxStackSize() == 64) {
                final int itemAmount = itemToGive.getAmount();
                itemToGive.setAmount(itemAmount * amount.intValue());
                player.getInventory().addItem(itemToGive);
                return;
            }

            for (int i = 0; i < amount.intValue(); ++i) {
                player.getInventory().addItem(itemToGive);
            }
        }
    }

    private void executePurchaseCommands(Player player, BigInteger amount, CategoryItem item) {
        for (String cmd : item.getCommands()) {
            final BigInteger finalAmount = amount;
            MultiEconomy.get().getServer().getScheduler().runTaskLater(MultiEconomy.get(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(cmd, new String[]{
                    "{player}",
                    "{amount}"
            }, new String[]{
                    player.getName(),
                    finalAmount.multiply(item.getDefaultAmount()).toString()
            })), 0L);
        }
    }

    private void sendPurchasedMessages(Player player, BigInteger amount, CategoryItem item, Currency currency, BigInteger price) {
        for (String msg : Messages.SUCCESSFUL_PURCHASED) {
            player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                    "{item}",
                    "{amount}",
                    "{price}"
            }, new String[]{
                    item.getDisplayItem().hasItemMeta() ? item.getDisplayItem().getItemMeta().hasDisplayName() ? item.getDisplayItem().getItemMeta().getDisplayName() : item.getDisplayItem().getType().toString() : item.getDisplayItem().getType().toString(),
                    NumberFormatter.getInstance().formatThousand(amount.doubleValue()),
                    currency.getAmountDisplay(price)
            }));
        }
    }

    public static Map<Player, Purchase> getPlayersBuying() {
        return playersBuying;
    }
}