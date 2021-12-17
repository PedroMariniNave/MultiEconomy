package com.zpedroo.multieconomy.managers;

import com.zpedroo.multieconomy.objects.Currency;
import com.zpedroo.multieconomy.objects.Transaction;
import com.zpedroo.multieconomy.utils.FileUtils;
import com.zpedroo.multieconomy.utils.config.Logs;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;

public class TransactionManager {

    private static TransactionManager instance;
    public static TransactionManager getInstance() { return instance; }

    public TransactionManager() {
        instance = this;
    }

    public void registerTransaction(Currency currency, Transaction transaction) {
        FileUtils.get().getFile(FileUtils.Files.IDS).get().set("IDs." + currency.getFileName(), transaction.getID());
        FileUtils.get().getFile(FileUtils.Files.IDS).save();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        String formattedDate = format.format(transaction.getCreationDateInMillis());
        String currencyFileName = currency.getFileName();
        String currencyName = currency.getName();
        String actorName = transaction.getActor() == null ? "???" : transaction.getActor().getName();
        String actorIP = transaction.getActor() == null ? "???" : (transaction.getActor().getPlayer() == null ? "???" : transaction.getActor().getPlayer().getAddress().getAddress().getHostAddress());
        String targetName = transaction.getTarget() == null ? "???" : transaction.getTarget().getName();
        String targetIP = transaction.getTarget() == null ? "???" : (transaction.getTarget().getPlayer() == null ? "???" : transaction.getTarget().getPlayer().getAddress().getAddress().getHostAddress());
        String amount = NumberFormatter.getInstance().format(transaction.getAmount());
        String id = transaction.getID().toString();

        String log = null;

        String[] placeholders = new String[]{
                "{date}",
                "{currency}",
                "{currency_name}",
                "{actor}",
                "{actor_ip}",
                "{target}",
                "{target_ip}",
                "{amount}",
                "{id}"
        };
        String[] replaces = new String[]{
                formattedDate,
                currencyFileName,
                currencyName,
                actorName,
                actorIP,
                targetName,
                targetIP,
                amount,
                id
        };

        switch (transaction.getType()) {
            case ADD:
                log = StringUtils.replaceEach(Logs.PAY, placeholders, replaces);
                break;
            case DEPOSIT:
                log = StringUtils.replaceEach(Logs.DEPOSIT, placeholders, replaces);
                break;
            case WITHDRAW:
                log = StringUtils.replaceEach(Logs.WITHDRAW, placeholders, replaces);
                break;
        }

        if (log != null) LogManager.getInstance().addLog(log);
    }
}