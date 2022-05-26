package com.zpedroo.multieconomy.managers;

import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.player.Transaction;
import com.zpedroo.multieconomy.utils.FileUtils;

public class TransactionManager {

    private static TransactionManager instance;
    public static TransactionManager getInstance() { return instance; }

    public TransactionManager() {
        instance = this;
    }

    public void registerTransaction(Currency currency, Transaction transaction) {
        FileUtils.get().getFile(FileUtils.Files.IDS).get().set("IDs." + currency.getFileName(), transaction.getId());
        FileUtils.get().getFile(FileUtils.Files.IDS).save();
    }
}