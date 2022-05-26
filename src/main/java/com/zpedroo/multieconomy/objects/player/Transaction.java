package com.zpedroo.multieconomy.objects.player;

import com.zpedroo.multieconomy.enums.TransactionType;
import lombok.Builder;
import lombok.Data;
import org.bukkit.OfflinePlayer;

import java.math.BigInteger;

@Data
@Builder
public class Transaction {

    private final OfflinePlayer actor;
    private final OfflinePlayer target;
    private final BigInteger amount;
    private final TransactionType type;
    private final long creationDateInMillis;
    private final int id;
}