package com.zpedroo.multieconomy.utils.serialization;

import com.zpedroo.multieconomy.interfaces.ISerialization;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class CurrencySerialization implements ISerialization<Map<Currency, BigInteger>> {

    @Override
    public String serialize(Map<Currency, BigInteger> toSerialize) {
        StringBuilder builder = new StringBuilder(toSerialize.size());

        for (Map.Entry<Currency, BigInteger> entry : toSerialize.entrySet()) {
            Currency currency = entry.getKey();
            BigInteger amount = entry.getValue();

            builder.append(currency.getFileName()).append("#");
            builder.append(amount.toString()).append(",");
        }

        return builder.toString();
    }

    @Override
    public Map<Currency, BigInteger> deserialize(String serialized) {
        if (serialized == null || serialized.isEmpty()) return null;

        String[] currenciesSplit = serialized.split(",");
        Map<Currency, BigInteger> ret = new HashMap<>(currenciesSplit.length);

        for (String str : currenciesSplit) {
            String[] currencySplit = str.split("#");

            Currency currency = DataManager.getInstance().getCache().getCurrencies().get(currencySplit[0]);
            if (currency == null) continue;

            BigInteger amount = NumberFormatter.getInstance().filter(currencySplit[1]);
            if (amount.signum() <= 0) continue;

            ret.put(currency, amount);
        }

        return ret;
    }
}