package com.zpedroo.multieconomy.listeners;

import com.zpedroo.multieconomy.MultiEconomy;
import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.managers.InventoryManager;
import com.zpedroo.multieconomy.objects.Currency;
import com.zpedroo.multieconomy.objects.CategoryItem;
import com.zpedroo.multieconomy.utils.config.Messages;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
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
import java.util.HashMap;

public class ShopListeners implements Listener {

    private static HashMap<Player, PlayerChat> playerChat;

    static {
        playerChat = new HashMap<>(32);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!getPlayerChat().containsKey(event.getPlayer())) return;

        event.setCancelled(true);

        PlayerChat playerChat = getPlayerChat().remove(event.getPlayer());
        Player player = playerChat.getPlayer();
        BigInteger amount = NumberFormatter.getInstance().filter(event.getMessage());

        if (amount.signum() <= 0) {
            player.sendMessage(Messages.INVALID_AMOUNT);
            return;
        }

        CategoryItem item = playerChat.getItem();
        Integer limit = item.getDisplay().getMaxStackSize() == 1 ? 36 : 2304;
        if (amount.compareTo(BigInteger.valueOf(limit)) > 0) amount = BigInteger.valueOf(limit);

        Integer freeSpace = InventoryManager.getInstance().getFreeSpace(player, item.getDisplay());
        if (freeSpace < amount.intValue()) {
            player.sendMessage(StringUtils.replaceEach(Messages.NEED_SPACE, new String[]{
                    "{has}",
                    "{need}"
            }, new String[]{
                    NumberFormatter.getInstance().formatDecimal(freeSpace.doubleValue()),
                    NumberFormatter.getInstance().formatDecimal(amount.doubleValue())
            }));
            return;
        }

        Currency currency = item.getCurrency();

        BigInteger currencyAmount = CurrencyAPI.getCurrencyAmount(player, currency);
        BigInteger price = item.getPrice().multiply(amount);

        if (currencyAmount.compareTo(price) < 0) {
            player.sendMessage(StringUtils.replaceEach(Messages.INSUFFICIENT_CURRENCY, new String[]{
                    "{has}",
                    "{need}"
            }, new String[]{
                    NumberFormatter.getInstance().format(currencyAmount),
                    NumberFormatter.getInstance().format(price)
            }));
            return;
        }

        CurrencyAPI.removeCurrencyAmount(player, currency, price);
        if (item.getShopItem() != null) {
            ItemStack toGive = item.getShopItem().clone();
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
            if (cmd == null) continue;

            final Integer finalAmount = amount.intValue();
            MultiEconomy.get().getServer().getScheduler().runTaskLater(MultiEconomy.get(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(cmd, new String[]{
                    "{player}",
                    "{amount}"
            }, new String[]{
                    player.getName(),
                    String.valueOf(finalAmount * item.getDefaultAmount())
            })), 0L);
        }

        for (String msg : Messages.SUCCESSFUL_PURCHASED) {
            if (msg == null) continue;

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
                            NumberFormatter.getInstance().format(price)
                    })
            }));
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 10f);
    }

    public static HashMap<Player, PlayerChat> getPlayerChat() {
        return playerChat;
    }

    public static class PlayerChat {

        private Player player;
        private CategoryItem item;

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