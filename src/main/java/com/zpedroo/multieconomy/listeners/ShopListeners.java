package com.zpedroo.multieconomy.listeners;

import com.zpedroo.multieconomy.MultiEconomy;
import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.managers.InventoryManager;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.category.CategoryItem;
import com.zpedroo.multieconomy.utils.config.Messages;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import com.zpedroo.onlinetime.api.OnlineTimeAPI;
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

    private static final Map<Player, PlayerChat> playerChat = new HashMap<>(8);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!getPlayerChat().containsKey(event.getPlayer())) return;

        event.setCancelled(true);

        PlayerChat playerChat = getPlayerChat().remove(event.getPlayer());
        Player player = playerChat.getPlayer();
        BigInteger amount = null;

        CategoryItem item = playerChat.getItem();
        Currency currency = item.getCurrency();

        BigInteger currencyAmount = CurrencyAPI.getCurrencyAmount(player, currency);
        BigInteger price = item.getPrice();

        if (StringUtils.equals(event.getMessage(), "*")) {
            amount = currencyAmount.divide(price);
            if (amount.signum() <= 0) {
                player.sendMessage(Messages.BUY_ALL_ZERO);
                return;
            }
        } else {
            amount = NumberFormatter.getInstance().filter(event.getMessage());
        }

        if (amount.signum() <= 0) {
            player.sendMessage(Messages.INVALID_AMOUNT);
            return;
        }

        int limit = item.getDisplay().getMaxStackSize() == 1 ? 36 : 2304;
        if (amount.compareTo(BigInteger.valueOf(limit)) > 0 && item.isInventoryLimit()) amount = BigInteger.valueOf(limit);

        BigInteger freeSpace = BigInteger.valueOf(InventoryManager.getInstance().getFreeSpace(player, item.getDisplay()));
        BigInteger toCompare = item.isInventoryLimit() ? amount : BigInteger.ONE;
        if (freeSpace.compareTo(toCompare) < 0)  {
            player.sendMessage(StringUtils.replaceEach(Messages.NEED_SPACE, new String[]{
                    "{has}",
                    "{need}"
            }, new String[]{
                    NumberFormatter.getInstance().formatDecimal(freeSpace.doubleValue()),
                    NumberFormatter.getInstance().formatDecimal(amount.doubleValue())
            }));
            return;
        }

        if (item.getMaxStock().signum() > 0) {
            if (amount.compareTo(item.getStockAmount()) > 0) amount = item.getStockAmount();
            if (amount.signum() <= 0) {
                player.sendMessage(Messages.INSUFFICIENT_STOCK);
                return;
            }
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

        long playerLevel = OnlineTimeAPI.getLevel(player);
        int requiredLevel = item.getRequiredLevel();
        if (playerLevel < requiredLevel) {
            player.sendMessage(StringUtils.replaceEach(Messages.INSUFFICIENT_LEVEL, new String[]{
                    "{has}",
                    "{need}"
            }, new String[]{
                    NumberFormatter.getInstance().formatDecimal(playerLevel),
                    NumberFormatter.getInstance().formatDecimal(requiredLevel)
            }));
            return;
        }

        CurrencyAPI.removeCurrencyAmount(player, currency, finalPrice);
        if (item.getItemToGive() != null) {
            ItemStack toGive = item.getItemToGive().clone();
            if (toGive.getMaxStackSize() == 64) {
                toGive.setAmount(amount.intValue());
                player.getInventory().addItem(toGive);
                return;
            }

            for (int i = 0; i < amount.intValue(); ++i) {
                player.getInventory().addItem(toGive);
            }
        }

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

        for (String msg : Messages.SUCCESSFUL_PURCHASED) {
            player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                    "{item}",
                    "{amount}",
                    "{price}"
            }, new String[]{
                    item.getDisplay().hasItemMeta() ? item.getDisplay().getItemMeta().hasDisplayName() ? item.getDisplay().getItemMeta().getDisplayName() : item.getDisplay().getType().toString() : item.getDisplay().getType().toString(),
                    NumberFormatter.getInstance().formatDecimal(amount.doubleValue()),
                    StringUtils.replaceEach(currency.getAmountDisplay(), new String[]{
                            "{amount}"
                    }, new String[]{
                            NumberFormatter.getInstance().format(finalPrice)
                    })
            }));
        }

        if (item.getMaxStock().signum() > 0) {
            item.setStockAmount(item.getStockAmount().subtract(amount));
        }

        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.5f, 10f);
    }

    public static Map<Player, PlayerChat> getPlayerChat() {
        return playerChat;
    }

    public static class PlayerChat {

        private final Player player;
        private final CategoryItem item;

        public PlayerChat(Player player, CategoryItem item) {
            this.player = player;
            this.item = item;
        }

        public Player getPlayer() {
            return player;
        }

        public CategoryItem getItem() {
            return item;
        }
    }
}