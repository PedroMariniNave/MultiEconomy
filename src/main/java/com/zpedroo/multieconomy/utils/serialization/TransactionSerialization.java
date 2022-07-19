package com.zpedroo.multieconomy.utils.serialization;

import com.zpedroo.multieconomy.enums.TransactionType;
import com.zpedroo.multieconomy.interfaces.ISerialization;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.player.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TransactionSerialization implements ISerialization<Map<Currency, List<Transaction>>> {

    @Override
    public String serialize(Map<Currency, List<Transaction>> toSerialize) {
        StringBuilder serialized = new StringBuilder(toSerialize.size());

        for (Map.Entry<Currency, List<Transaction>> entry : toSerialize.entrySet()) {
            Currency currency = entry.getKey();
            List<Transaction> transactions = entry.getValue();

            for (Transaction transaction : transactions) {
                serialized.append(currency.getFileName()).append("#");
                serialized.append(transaction.getActor().getName()).append("#");
                serialized.append(transaction.getTarget() == null ? "Ninguém" : transaction.getTarget().getName()).append("#");
                serialized.append(transaction.getAmount().toString()).append("#");
                serialized.append(transaction.getType().toString()).append("#");
                serialized.append(transaction.getCreationTimestamp()).append("#");
                serialized.append(transaction.getId()).append(",");
            }
        }

        return serialized.toString();
    }

    @Override
    public Map<Currency, List<Transaction>> deserialize(String serialized) {
        if (serialized == null || serialized.isEmpty()) return null;

        String[] split = serialized.split(",");
        Map<Currency, List<Transaction>> ret = new HashMap<>(split.length);

        for (String str : split) {
            String[] strSplit = str.split("#");

            Currency currency = DataManager.getInstance().getCache().getCurrencies().get(strSplit[0]);
            if (currency == null) continue;

            OfflinePlayer actor = Bukkit.getOfflinePlayer(strSplit[1]);
            OfflinePlayer target = Bukkit.getOfflinePlayer(strSplit[2]);
            BigInteger amount = new BigInteger(strSplit[3]);
            TransactionType type = TransactionType.valueOf(strSplit[4]);
            long creationTimestamp = Long.parseLong(strSplit[5]);
            int id = Integer.parseInt(strSplit[6]);

            List<Transaction> transactions = ret.containsKey(currency) ? ret.get(currency) : new LinkedList<>();
            transactions.add(new Transaction(actor, target, amount, currency, type, creationTimestamp, id));

            ret.put(currency, transactions);
        }

        return ret;
    }
}