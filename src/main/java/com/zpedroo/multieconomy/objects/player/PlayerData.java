package com.zpedroo.multieconomy.objects.player;

import com.zpedroo.multieconomy.objects.general.Currency;

import java.math.BigInteger;
import java.util.*;

public class PlayerData {

    private final UUID uuid;
    private final Map<Currency, BigInteger> currencies;
    private final Map<Currency, List<Transaction>> transactions;
    private boolean update;

    public PlayerData(UUID uuid, Map<Currency, BigInteger> currencies, Map<Currency, List<Transaction>> transactions) {
        this.uuid = uuid;
        this.currencies = currencies == null ? new HashMap<>(4) : currencies;
        this.transactions = transactions == null ? new HashMap<>(2) : transactions;
        this.update = false;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Map<Currency, BigInteger> getCurrencies() {
        return currencies;
    }

    public Map<Currency, List<Transaction>> getTransactions() {
        return transactions;
    }

    public boolean isQueueUpdate() {
        return update;
    }

    public BigInteger getCurrencyAmount(Currency currency) {
        return currencies.getOrDefault(currency, BigInteger.ZERO);
    }

    public List<Transaction> getCurrencyTransactions(Currency currency) {
        return transactions.getOrDefault(currency, new LinkedList<>());
    }

    public void setCurrencyAmount(Currency currency, BigInteger amount) {
        if (amount.signum() < 0) amount = BigInteger.ZERO;

        this.currencies.put(currency, amount);
        this.update = true;
    }

    public void addCurrencyAmount(Currency currency, BigInteger amount) {
        setCurrencyAmount(currency, getCurrencyAmount(currency).add(amount));
    }

    public void removeCurrencyAmount(Currency currency, BigInteger amount) {
        setCurrencyAmount(currency, getCurrencyAmount(currency).subtract(amount));
    }

    public void addTransaction(Currency currency, Transaction transaction) {
        if (getCurrencyTransactions(currency).size() == 225) getCurrencyTransactions(currency).remove(0);

        List<Transaction> currencyTransactions = getCurrencyTransactions(currency);
        currencyTransactions.add(transaction);

        transactions.put(currency, currencyTransactions);
        setUpdate(true);
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }
}