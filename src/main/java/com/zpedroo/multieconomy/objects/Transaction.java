package com.zpedroo.multieconomy.objects;

import com.zpedroo.multieconomy.enums.TransactionType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigInteger;

public class Transaction {

    private OfflinePlayer actor;
    private OfflinePlayer target;
    private BigInteger amount;
    private TransactionType type;
    private Long creationDateInMillis;
    private Integer id;

    public Transaction(OfflinePlayer actor, OfflinePlayer target, BigInteger amount, TransactionType type, Long creationDateInMillis, Integer id) {
        this.actor = actor;
        this.target = target;
        this.amount = amount;
        this.type = type;
        this.creationDateInMillis = creationDateInMillis;
        this.id = id;
    }

    public OfflinePlayer getActor() {
        if (actor == null) return Bukkit.getOfflinePlayer("???");

        return actor;
    }

    public OfflinePlayer getTarget() {
        if (target == null) return Bukkit.getOfflinePlayer("???");

        return target;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public Long getCreationDateInMillis() {
        return creationDateInMillis;
    }

    public Integer getID() {
        return id;
    }
}