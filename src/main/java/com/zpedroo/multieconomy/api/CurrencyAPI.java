package com.zpedroo.multieconomy.api;

import com.zpedroo.multieconomy.hooks.VaultEconomy;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.Currency;
import com.zpedroo.multieconomy.objects.Transaction;
import org.bukkit.OfflinePlayer;

import java.math.BigInteger;
import java.util.List;

public class CurrencyAPI {

    public static Currency getVaultCurrency() {
        return VaultEconomy.getInstance().getCurrency();
    }

    public static Currency getCurrency(String currencyName) {
        return DataManager.getInstance().getCache().getCurrencies().get(currencyName);
    }

    public static BigInteger getCurrencyAmount(OfflinePlayer player, Currency currency) {
        return DataManager.getInstance().load(player.getUniqueId()).getCurrencyAmount(currency);
    }

    public static List<Transaction> getCurrencyTransactions(OfflinePlayer player, Currency currency) {
        return DataManager.getInstance().load(player.getUniqueId()).getCurrencyTransactions(currency);
    }

    public static void setCurrencyAmount(OfflinePlayer player, Currency currency, BigInteger amount) {
        DataManager.getInstance().load(player.getUniqueId()).setCurrencyAmount(currency, amount);
    }

    public static void addCurrencyAmount(OfflinePlayer player, Currency currency, BigInteger amount) {
        DataManager.getInstance().load(player.getUniqueId()).addCurrencyAmount(currency, amount);
    }

    public static void removeCurrencyAmount(OfflinePlayer player, Currency currency, BigInteger amount) {
        DataManager.getInstance().load(player.getUniqueId()).removeCurrencyAmount(currency, amount);
    }
}