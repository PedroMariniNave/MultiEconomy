package com.zpedroo.multieconomy.api;

import com.zpedroo.multieconomy.hooks.VaultEconomy;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.player.Transaction;
import org.bukkit.OfflinePlayer;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class CurrencyAPI {

    public static Currency getVaultCurrency() {
        return VaultEconomy.getInstance().getCurrency();
    }

    public static Currency getCurrency(String currencyName) {
        return DataManager.getInstance().getCache().getCurrencies().get(currencyName);
    }

    @Deprecated
    public static BigInteger getCurrencyAmount(OfflinePlayer player, Currency currency) {
        return getCurrencyAmount(player.getUniqueId(), currency);
    }

    public static BigInteger getCurrencyAmount(UUID uuid, Currency currency) {
        return DataManager.getInstance().getPlayerDataByUUID(uuid).getCurrencyAmount(currency);
    }

    @Deprecated
    public static List<Transaction> getPlayerCurrencyTransactions(OfflinePlayer player, Currency currency) {
        return getPlayerCurrencyTransactions(player.getUniqueId(), currency);
    }

    public static List<Transaction> getPlayerCurrencyTransactions(UUID uuid, Currency currency) {
        return DataManager.getInstance().getPlayerDataByUUID(uuid).getCurrencyTransactions(currency);
    }

    @Deprecated
    public static void setCurrencyAmount(OfflinePlayer player, Currency currency, BigInteger amount) {
        setCurrencyAmount(player.getUniqueId(), currency, amount);
    }

    public static void setCurrencyAmount(UUID uuid, Currency currency, BigInteger amount) {
        DataManager.getInstance().getPlayerDataByUUID(uuid).setCurrencyAmount(currency, amount);
    }

    @Deprecated
    public static void addCurrencyAmount(OfflinePlayer player, Currency currency, BigInteger amount) {
        addCurrencyAmount(player.getUniqueId(), currency, amount);
    }

    public static void addCurrencyAmount(UUID uuid, Currency currency, BigInteger amount) {
        DataManager.getInstance().getPlayerDataByUUID(uuid).addCurrencyAmount(currency, amount);
    }

    @Deprecated
    public static void removeCurrencyAmount(OfflinePlayer player, Currency currency, BigInteger amount) {
        removeCurrencyAmount(player.getUniqueId(), currency, amount);
    }

    public static void removeCurrencyAmount(UUID uuid, Currency currency, BigInteger amount) {
        DataManager.getInstance().getPlayerDataByUUID(uuid).removeCurrencyAmount(currency, amount);
    }
}