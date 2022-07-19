package com.zpedroo.multieconomy.managers;

import com.zpedroo.multieconomy.utils.serialization.CurrencySerialization;
import com.zpedroo.multieconomy.utils.serialization.TransactionSerialization;

public class SerializationManager {

    public final CurrencySerialization currencySerialization = new CurrencySerialization();
    public final TransactionSerialization transactionSerialization = new TransactionSerialization();
}