package com.zpedroo.multieconomy.commands;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.enums.TransactionType;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.managers.LogManager;
import com.zpedroo.multieconomy.objects.Currency;
import com.zpedroo.multieconomy.objects.PlayerData;
import com.zpedroo.multieconomy.objects.Transaction;
import com.zpedroo.multieconomy.utils.FileUtils;
import com.zpedroo.multieconomy.utils.config.Logs;
import com.zpedroo.multieconomy.utils.config.Messages;
import com.zpedroo.multieconomy.utils.config.Settings;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import com.zpedroo.multieconomy.utils.menu.Menus;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class CurrencyCmd implements CommandExecutor {

    private Currency currency;
    private Map<Player, TransactionConfirm> confirm;

    public CurrencyCmd(Currency currency) {
        this.currency = currency;
        this.confirm = new HashMap<>(16);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        OfflinePlayer target = null;

        PlayerData targetData = null;
        BigInteger amount = null;

        if (args.length == 1) {
            if (player == null) return true;

            switch (args[0].toUpperCase()) {
                case "TOP", "TOP10" -> {
                    Menus.getInstance().openTopMenu(player, currency);
                    return true;
                }
            }

            target = Bukkit.getOfflinePlayer(args[0]);

            if (!DataManager.getInstance().hasAccount(target.getUniqueId())) {
                player.sendMessage(Messages.NEVER_SEEN);
                return true;
            }

            Menus.getInstance().openInfoMenu(player, target, currency);
            return true;
        }

        if (args.length == 3) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            switch (args[0].toUpperCase()) {
                case "ITEM" -> {
                    if (!sender.hasPermission(Settings.ADMIN_PERMISSION)) {
                        sender.sendMessage(Settings.PERMISSION_MESSAGE);
                        return true;
                    }

                    target = Bukkit.getOfflinePlayer(args[1]);
                    if (target.getPlayer() == null) return true;

                    amount = NumberFormatter.getInstance().filter(args[2]);
                    if (amount.signum() <= 0) {
                        sender.sendMessage(Messages.INVALID_AMOUNT);
                        return true;
                    }

                    ItemStack item = currency.getItem(amount);
                    target.getPlayer().getInventory().addItem(item);

                    if (player == null) return true;

                    LogManager.getInstance().addLog(StringUtils.replaceEach(Logs.ITEM, new String[]{
                            "{date}",
                            "{currency_name}",
                            "{actor}",
                            "{actor_ip}",
                            "{target}",
                            "{target_ip}",
                            "{amount}"
                    }, new String[]{
                            formatter.format(System.currentTimeMillis()),
                            currency.getName(),
                            player.getName(),
                            player.getAddress().getAddress().getHostAddress(),
                            target.getName(),
                            target.getPlayer() == null ? "???" : target.getPlayer().getAddress().getAddress().getHostAddress(),
                            NumberFormatter.getInstance().format(amount)
                    }));
                    return true;
                }

                case "SEND", "PAY", "ENVIAR" -> {
                    if (player == null) return true;

                    target = Bukkit.getOfflinePlayer(args[1]);
                    if (StringUtils.equals(player.getName(), target.getName())) {
                        player.sendMessage(StringUtils.replaceEach(Messages.TARGET_IS_SENDER, new String[]{
                                "{currency}",
                                "{currency_name}"
                        }, new String[]{
                                currency.getFileName(),
                                currency.getName()
                        }));
                        return true;
                    }

                    if (!DataManager.getInstance().hasAccount(target.getUniqueId())) {
                        player.sendMessage(Messages.NEVER_SEEN);
                        return true;
                    }

                    amount = NumberFormatter.getInstance().filter(args[2]);
                    if (amount.signum() <= 0) {
                        player.sendMessage(Messages.INVALID_AMOUNT);
                        return true;
                    }

                    Integer taxPerTransaction = currency.getTaxPerTransaction();
                    if (amount.compareTo(BigInteger.valueOf(taxPerTransaction)) < 0) {
                        player.sendMessage(StringUtils.replaceEach(Messages.MIN_VALUE, new String[]{
                                "{tax}"
                        }, new String[]{
                                taxPerTransaction.toString()
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
                        if (msg == null) continue;

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
                                currency.getName(),
                                currency.getDisplay(),
                                NumberFormatter.getInstance().format(amount),
                                StringUtils.replaceEach(currency.getAmountDisplay(), new String[]{
                                        "{amount}"
                                }, new String[]{
                                        NumberFormatter.getInstance().format(amount)
                                }),
                                NumberFormatter.getInstance().format(toGive),
                                StringUtils.replaceEach(currency.getAmountDisplay(), new String[]{
                                        "{amount}"
                                }, new String[]{
                                        NumberFormatter.getInstance().format(toGive)
                                }),
                                currency.getTaxPerTransaction().toString()
                        }));
                    }
                    return true;
                }

                case "GIVE", "ADD", "SET" -> {
                    if (!sender.hasPermission(Settings.ADMIN_PERMISSION)) {
                        sender.sendMessage(Settings.PERMISSION_MESSAGE);
                        return true;
                    }

                    target = Bukkit.getOfflinePlayer(args[1]);
                    if (!DataManager.getInstance().hasAccount(target.getUniqueId())) {
                        sender.sendMessage(Messages.NEVER_SEEN);
                        return true;
                    }

                    amount = NumberFormatter.getInstance().filter(args[2]);
                    if (amount.signum() <= 0) {
                        sender.sendMessage(Messages.INVALID_AMOUNT);
                        return true;
                    }

                    targetData = DataManager.getInstance().load(target.getUniqueId());
                    String action = null;
                    if (StringUtils.equals(args[0].toUpperCase(), "GIVE")) {
                        targetData.addCurrencyAmount(currency, amount);
                        action = "GIVE";
                    } else {
                        targetData.setCurrencyAmount(currency, amount);
                        action = "SET";
                    }

                    if (player == null) break;

                    LogManager.getInstance().addLog(StringUtils.replaceEach(Logs.EDIT, new String[]{
                            "{date}",
                            "{currency_name}",
                            "{action}",
                            "{actor}",
                            "{actor_ip}",
                            "{target}",
                            "{target_ip}",
                            "{amount}"
                    }, new String[]{
                            formatter.format(System.currentTimeMillis()),
                            currency.getName(),
                            action,
                            player.getName(),
                            player.getAddress().getAddress().getHostAddress(),
                            target.getName(),
                            target.getPlayer() == null ? "???" : target.getPlayer().getAddress().getAddress().getHostAddress(),
                            NumberFormatter.getInstance().format(amount)
                    }));
                    return true;
                }
            }
        }

        if (player == null) return true;

        Menus.getInstance().openMainMenu(player, currency);
        return true;
    }

    private class TransactionConfirm {

        private Player player;
        private OfflinePlayer target;
        private BigInteger amount;
        private BigInteger toGive;

        public TransactionConfirm(Player player, OfflinePlayer target, BigInteger amount, BigInteger toGive) {
            this.player = player;
            this.target = target;
            this.amount = amount;
            this.toGive = toGive;
        }

        public Player getPlayer() {
            return player;
        }

        public OfflinePlayer getTarget() {
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

            Integer id = FileUtils.get().getInt(FileUtils.Files.IDS, "IDs." + currency.getName()) + 1;

            CurrencyAPI.removeCurrencyAmount(player, currency, amount);
            CurrencyAPI.addCurrencyAmount(player, currency, toGive);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

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
                        currency.getDisplay(),
                        target.getName(),
                        NumberFormatter.getInstance().format(amount),
                        StringUtils.replaceEach(currency.getAmountDisplay(), new String[]{
                                "{amount}"
                        }, new String[]{
                                NumberFormatter.getInstance().format(amount)
                        }),
                        NumberFormatter.getInstance().format(toGive),
                        StringUtils.replaceEach(currency.getAmountDisplay(), new String[]{
                                "{amount}"
                        }, new String[]{
                                NumberFormatter.getInstance().format(toGive)
                        }),
                        id.toString(),
                        currency.getTaxPerTransaction().toString()
                }));
            }

            Player targetPlayer = target.getPlayer();
            if (targetPlayer != null) {
                targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
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
                            currency.getDisplay(),
                            player.getName(),
                            NumberFormatter.getInstance().format(amount),
                            StringUtils.replaceEach(currency.getAmountDisplay(), new String[]{
                                    "{amount}"
                            }, new String[]{
                                    NumberFormatter.getInstance().format(amount)
                            }),
                            NumberFormatter.getInstance().format(toGive),
                            StringUtils.replaceEach(currency.getAmountDisplay(), new String[]{
                                    "{amount}"
                            }, new String[]{
                                    NumberFormatter.getInstance().format(toGive)
                            }),
                            id.toString(),
                            currency.getTaxPerTransaction().toString()
                    }));
                }
            }

            DataManager.getInstance().load(player.getUniqueId()).addTransaction(currency, new Transaction(player, target, amount, TransactionType.REMOVE, System.currentTimeMillis(), id));
            DataManager.getInstance().load(target.getUniqueId()).addTransaction(currency, new Transaction(player, target, toGive, TransactionType.ADD, System.currentTimeMillis(), id));
        }
    }
}