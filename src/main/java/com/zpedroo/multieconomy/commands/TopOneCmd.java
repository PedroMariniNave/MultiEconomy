package com.zpedroo.multieconomy.commands;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.utils.config.Messages;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.math.BigInteger;
import java.util.UUID;

public class TopOneCmd implements CommandExecutor {

    private final Currency currency;

    public TopOneCmd(Currency currency) {
        this.currency = currency;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        UUID topOneUniqueId = currency.getTopOneUniqueId();
        OfflinePlayer player = topOneUniqueId == null ? Bukkit.getOfflinePlayer("Ninguém") : Bukkit.getOfflinePlayer(topOneUniqueId);
        BigInteger currencyAmount = topOneUniqueId == null ? BigInteger.ZERO : CurrencyAPI.getCurrencyAmount(topOneUniqueId, currency);

        sender.sendMessage(StringUtils.replaceEach(Messages.TOP_ONE_DISPLAY, new String[]{
                "{tag}",
                "{player}",
                "{amount}"
        }, new String[]{
                currency.getTopOneTag(),
                player.getName(),
                currency.getAmountDisplay(currencyAmount)
        }));
        return false;
    }
}