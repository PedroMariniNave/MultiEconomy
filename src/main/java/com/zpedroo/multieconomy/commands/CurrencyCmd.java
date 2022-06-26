package com.zpedroo.multieconomy.commands;

import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.general.TransactionConfirmation;
import com.zpedroo.multieconomy.objects.player.PlayerData;
import com.zpedroo.multieconomy.utils.config.Messages;
import com.zpedroo.multieconomy.utils.config.Settings;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import com.zpedroo.multieconomy.utils.menu.Menus;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class CurrencyCmd implements CommandExecutor {

    private final Currency currency;
    private final Map<Player, TransactionConfirmation> confirmations = new HashMap<>(8);

    public CurrencyCmd(Currency currency) {
        this.currency = currency;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        Player target = null;
        PlayerData targetData = null;
        BigInteger amount = null;

        if (args.length == 1) {
            if (player == null) return true;

            switch (args[0].toUpperCase()) {
                case "TOP":
                    Menus.getInstance().openTopMenu(player, currency);
                    return true;
            }

            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(Messages.OFFLINE_PLAYER);
                return true;
            }

            Menus.getInstance().openInfoMenu(player, target, currency);
            return true;
        }

        if (args.length == 3) {
            switch (args[0].toUpperCase()) {
                case "ITEM":
                    if (!sender.hasPermission(Settings.ADMIN_PERMISSION)) {
                        sender.sendMessage(Settings.PERMISSION_MESSAGE);
                        return true;
                    }

                    target = Bukkit.getPlayer(args[1]);
                    if (target.getPlayer() == null) return true;

                    amount = NumberFormatter.getInstance().filter(args[2]);
                    if (amount.signum() <= 0) {
                        sender.sendMessage(Messages.INVALID_AMOUNT);
                        return true;
                    }

                    ItemStack item = currency.getItem(amount);
                    target.getPlayer().getInventory().addItem(item);
                    return true;
                case "SEND":
                case "PAY":
                case "ENVIAR":
                    if (player == null) return true;

                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(Messages.OFFLINE_PLAYER);
                        return true;
                    }
                    if (StringUtils.equals(player.getName(), target.getName())) {
                        player.sendMessage(StringUtils.replaceEach(Messages.TARGET_IS_SENDER, new String[]{
                                "{currency}",
                                "{currency_name}"
                        }, new String[]{
                                currency.getFileName(),
                                currency.getCurrencyName()
                        }));
                        return true;
                    }

                    amount = NumberFormatter.getInstance().filter(args[2]);
                    if (amount.signum() <= 0) {
                        player.sendMessage(Messages.INVALID_AMOUNT);
                        return true;
                    }

                    int taxPerTransaction = currency.getTaxPerTransaction();
                    if (amount.compareTo(BigInteger.valueOf(taxPerTransaction)) < 0) {
                        player.sendMessage(StringUtils.replaceEach(Messages.MIN_VALUE, new String[]{
                                "{tax}"
                        }, new String[]{
                                String.valueOf(taxPerTransaction)
                        }));
                        return true;
                    }

                    if (confirmations.containsKey(player)) {
                        TransactionConfirmation transactionConfirmation = confirmations.remove(player);
                        if (StringUtils.equals(transactionConfirmation.getTarget().getUniqueId().toString(), target.getUniqueId().toString())) {
                            if (transactionConfirmation.getAmount().compareTo(amount) == 0) {
                                transactionConfirmation.confirm();
                                return true;
                            }
                        }
                    }

                    BigInteger toGive = amount.subtract(amount.multiply(BigInteger.valueOf(taxPerTransaction)).divide(BigInteger.valueOf(100)));
                    TransactionConfirmation transactionConfirmation = new TransactionConfirmation(player, target, amount, toGive, currency);
                    confirmations.put(player, transactionConfirmation);

                    for (String msg : Messages.CONFIRM) {
                        player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                                "{target}",
                                "{currency}",
                                "{amount}",
                                "{received}",
                                "{tax}"
                        }, new String[]{
                                target.getName(),
                                currency.getCurrencyDisplay(),
                                currency.getAmountDisplay(amount),
                                currency.getAmountDisplay(toGive),
                                String.valueOf(currency.getTaxPerTransaction())
                        }));
                    }
                    return true;

                case "GIVE":
                case "ADD":
                case "SET":
                    if (!sender.hasPermission(Settings.ADMIN_PERMISSION)) {
                        sender.sendMessage(Settings.PERMISSION_MESSAGE);
                        return true;
                    }

                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(Messages.OFFLINE_PLAYER);
                        return true;
                    }

                    amount = NumberFormatter.getInstance().filter(args[2]);
                    if (amount.signum() <= 0) {
                        sender.sendMessage(Messages.INVALID_AMOUNT);
                        return true;
                    }

                    targetData = DataManager.getInstance().getPlayerDataByUUID(target.getUniqueId());
                    if (StringUtils.equals(args[0].toUpperCase(), "GIVE")) {
                        targetData.addCurrencyAmount(currency, amount);
                    } else {
                        targetData.setCurrencyAmount(currency, amount);
                    }
                    return true;
            }
        }

        if (player != null) Menus.getInstance().openMainMenu(player, currency);
        return true;
    }
}