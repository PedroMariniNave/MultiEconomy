package com.zpedroo.multieconomy.commands;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.enums.TransactionType;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.player.PlayerData;
import com.zpedroo.multieconomy.objects.player.Transaction;
import com.zpedroo.multieconomy.utils.FileUtils;
import com.zpedroo.multieconomy.utils.config.Messages;
import com.zpedroo.multieconomy.utils.config.Settings;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import com.zpedroo.multieconomy.utils.menu.Menus;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
    private final Map<Player, TransactionConfirm> confirm = new HashMap<>(8);

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

                    if (confirm.containsKey(player)) {
                        TransactionConfirm transactionConfirm = confirm.remove(player);
                        if (StringUtils.equals(transactionConfirm.getTarget().getUniqueId().toString(), target.getUniqueId().toString())) {
                            if (transactionConfirm.getAmount().compareTo(amount) == 0) {
                                transactionConfirm.confirm();
                                return true;
                            }
                        }
                    }

                    BigInteger toGive = amount.subtract(amount.multiply(BigInteger.valueOf(taxPerTransaction)).divide(BigInteger.valueOf(100)));
                    TransactionConfirm transactionConfirm = new TransactionConfirm(player, target, amount, toGive);
                    confirm.put(player, transactionConfirm);

                    for (String msg : Messages.CONFIRM) {
                        player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                                "{target}",
                                "{currency}",
                                "{currency_display}",
                                "{amount}",
                                "{amount_display}",
                                "{received}",
                                "{received_display}",
                                "{tax}"
                        }, new String[]{
                                target.getName(),
                                currency.getCurrencyName(),
                                currency.getCurrencyDisplay(),
                                NumberFormatter.getInstance().format(amount),
                                currency.getAmountDisplay(amount),
                                NumberFormatter.getInstance().format(toGive),
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

                    targetData = DataManager.getInstance().load(target.getUniqueId());
                    if (StringUtils.equals(args[0].toUpperCase(), "GIVE")) {
                        targetData.addCurrencyAmount(currency, amount);
                    } else {
                        targetData.setCurrencyAmount(currency, amount);
                    }
                    return true;
            }
        }

        if (player == null) return true;

        Menus.getInstance().openMainMenu(player, currency);
        return true;
    }

    private class TransactionConfirm {

        private final Player player;
        private final Player target;
        private final BigInteger amount;
        private final BigInteger toGive;

        public TransactionConfirm(Player player, Player target, BigInteger amount, BigInteger toGive) {
            this.player = player;
            this.target = target;
            this.amount = amount;
            this.toGive = toGive;
        }

        public Player getPlayer() {
            return player;
        }

        public Player getTarget() {
            return target;
        }

        public BigInteger getAmount() {
            return amount;
        }

        public BigInteger getToGive() {
            return toGive;
        }

        public void confirm() {
            BigInteger currencyAmount = CurrencyAPI.getCurrencyAmount(player, currency);
            if (currencyAmount.compareTo(amount) < 0) {
                player.sendMessage(Messages.INSUFFICIENT_CURRENCY);
                return;
            }

            int id = FileUtils.get().getInt(FileUtils.Files.IDS, "IDs." + currency.getCurrencyName()) + 1;

            CurrencyAPI.removeCurrencyAmount(player, currency, amount);
            CurrencyAPI.addCurrencyAmount(target, currency, toGive);
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);

            for (String msg : Messages.SENT) {
                player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                        "{currency}",
                        "{currency_display}",
                        "{target}",
                        "{amount}",
                        "{amount_display}",
                        "{received}",
                        "{received_display}",
                        "{id}",
                        "{tax}"
                }, new String[]{
                        currency.getFileName(),
                        currency.getCurrencyDisplay(),
                        target.getName(),
                        NumberFormatter.getInstance().format(amount),
                        currency.getAmountDisplay(amount),
                        NumberFormatter.getInstance().format(toGive),
                        currency.getAmountDisplay(toGive),
                        String.valueOf(id),
                        String.valueOf(currency.getTaxPerTransaction())
                }));
            }

            Player targetPlayer = target.getPlayer();
            if (targetPlayer != null) {
                targetPlayer.playSound(targetPlayer.getLocation(), Sound.LEVEL_UP, 1f, 1f);
                for (String msg : Messages.RECEIVED) {
                    targetPlayer.sendMessage(StringUtils.replaceEach(msg, new String[]{
                            "{currency}",
                            "{currency_display}",
                            "{actor}",
                            "{amount}",
                            "{amount_display}",
                            "{received}",
                            "{received_display}",
                            "{id}",
                            "{tax}"
                    }, new String[]{
                            currency.getFileName(),
                            currency.getCurrencyDisplay(),
                            player.getName(),
                            NumberFormatter.getInstance().format(amount),
                            currency.getAmountDisplay(amount),
                            NumberFormatter.getInstance().format(toGive),
                            currency.getAmountDisplay(toGive),
                            String.valueOf(id),
                            String.valueOf(currency.getTaxPerTransaction())
                    }));
                }
            }

            DataManager.getInstance().load(player.getUniqueId()).addTransaction(currency,Transaction.builder().actor(player)
                    .target(target).amount(amount).type(TransactionType.REMOVE).creationDateInMillis(System.currentTimeMillis()).id(id).build());
            DataManager.getInstance().load(target.getUniqueId()).addTransaction(currency, Transaction.builder().actor(player)
                    .target(target).amount(amount).type(TransactionType.ADD).creationDateInMillis(System.currentTimeMillis()).id(id).build());
        }
    }
}