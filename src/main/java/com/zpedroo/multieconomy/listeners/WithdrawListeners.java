package com.zpedroo.multieconomy.listeners;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.enums.TransactionType;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.player.Transaction;
import com.zpedroo.multieconomy.utils.FileUtils;
import com.zpedroo.multieconomy.utils.config.Messages;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class WithdrawListeners implements Listener {

    private static final Map<Player, Currency> playersWithdrawing = new HashMap<>(4);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!playersWithdrawing.containsKey(event.getPlayer())) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Currency currency = playersWithdrawing.remove(player);

        BigInteger amount = NumberFormatter.getInstance().filter(event.getMessage());
        if (amount.signum() <= 0) {
            player.sendMessage(Messages.INVALID_AMOUNT);
            return;
        }

        int taxPerTransaction = currency.getTaxPerTransaction();
        if (amount.compareTo(BigInteger.valueOf(taxPerTransaction)) < 0) {
            player.sendMessage(StringUtils.replaceEach(Messages.MIN_VALUE, new String[]{
                    "{tax}"
            }, new String[]{
                    String.valueOf(taxPerTransaction)
            }));
            return;
        }

        BigInteger currencyAmount = CurrencyAPI.getCurrencyAmount(player, currency);
        if (currencyAmount.compareTo(amount) < 0) {
            player.sendMessage(StringUtils.replaceEach(Messages.INSUFFICIENT_CURRENCY, new String[]{
                    "{has}",
                    "{need}"
            }, new String[]{
                    NumberFormatter.getInstance().format(currencyAmount),
                    NumberFormatter.getInstance().format(amount)
            }));
            return;
        }

        CurrencyAPI.removeCurrencyAmount(player.getUniqueId(), currency, amount);
        BigInteger amountToGive = amount.subtract(amount.multiply(BigInteger.valueOf(taxPerTransaction)).divide(BigInteger.valueOf(100)));
        ItemStack item = currency.getItem(amountToGive);
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }

        int transactionId = FileUtils.get().getInt(FileUtils.Files.IDS, "Ids." + currency.getFileName()) + 1;
        Transaction transaction = new Transaction(player, null, amount, currency, TransactionType.WITHDRAW, System.currentTimeMillis(), transactionId);
        transaction.register(player);
    }

    public static Map<Player, Currency> getPlayersWithdrawing() {
        return playersWithdrawing;
    }
}