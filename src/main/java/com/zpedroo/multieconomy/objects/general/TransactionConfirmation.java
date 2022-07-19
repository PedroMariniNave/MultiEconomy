package com.zpedroo.multieconomy.objects.general;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.enums.TransactionType;
import com.zpedroo.multieconomy.objects.player.Transaction;
import com.zpedroo.multieconomy.utils.FileUtils;
import com.zpedroo.multieconomy.utils.config.Messages;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.math.BigInteger;

@Data
public class TransactionConfirmation {

    private final Player player;
    private final Player target;
    private final BigInteger amount;
    private final BigInteger toGive;
    private final Currency currency;

    public void confirm() {
        BigInteger currencyAmount = CurrencyAPI.getCurrencyAmount(player, currency);
        if (currencyAmount.compareTo(amount) < 0) {
            player.sendMessage(Messages.INSUFFICIENT_CURRENCY);
            return;
        }

        int id = FileUtils.get().getInt(FileUtils.Files.IDS, "IDs." + currency.getCurrencyName()) + 1;

        CurrencyAPI.removeCurrencyAmount(player, currency, amount);
        CurrencyAPI.addCurrencyAmount(target, currency, toGive);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);

        String[] placeholders = new String[]{
                "{actor}",
                "{target}",
                "{currency}",
                "{amount}",
                "{received}",
                "{id}",
                "{tax}"
        };
        String[] replacers = new String[]{
                player.getName(),
                target.getName(),
                currency.getCurrencyDisplay(),
                currency.getAmountDisplay(amount),
                currency.getAmountDisplay(toGive),
                String.valueOf(id),
                String.valueOf(currency.getTaxPerTransaction())
        };

        for (String msg : Messages.SENT) {
            player.sendMessage(StringUtils.replaceEach(msg, placeholders, replacers));
        }

        target.playSound(target.getLocation(), Sound.LEVEL_UP, 1f, 1f);
        for (String msg : Messages.RECEIVED) {
            target.sendMessage(StringUtils.replaceEach(msg, placeholders, replacers));
        }

        Transaction removeTransaction = new Transaction(player, target, amount, currency, TransactionType.REMOVE, System.currentTimeMillis(), id);
        Transaction addTransaction = new Transaction(player, target, amount, currency, TransactionType.ADD, System.currentTimeMillis(), id);

        removeTransaction.register(player);
        addTransaction.register(target);
    }
}