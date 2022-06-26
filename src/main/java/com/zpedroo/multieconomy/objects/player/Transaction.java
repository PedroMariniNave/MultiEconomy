package com.zpedroo.multieconomy.objects.player;

import com.zpedroo.multieconomy.enums.TransactionType;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.utils.FileUtils;
import lombok.Data;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigInteger;

@Data
public class Transaction {

    private final OfflinePlayer actor;
    private final OfflinePlayer target;
    private final BigInteger amount;
    private final Currency currency;
    private final TransactionType type;
    private final long creationTimestamp;
    private final int id;

    public void register(Player player) {
        PlayerData data = DataManager.getInstance().getPlayerDataByUUID(player.getUniqueId());
        data.addTransaction(currency, this);

        FileUtils.get().getFile(FileUtils.Files.IDS).get().set("IDs." + currency.getFileName(), id);
        FileUtils.get().getFile(FileUtils.Files.IDS).save();
    }
}