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
import java.util.List;
import java.util.Map;

public class CurrencyCmd implements CommandExecutor {

    private static Currency currency;
    private final Map<Player, TransactionConfirmation> confirmations = new HashMap<>(8);

    public CurrencyCmd(Currency currency) {
        CurrencyCmd.currency = currency;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        Player target = null;
        PlayerData targetData = null;
        BigInteger amount = null;
        CommandKeys key = getKeyByName(args.length > 0 ? args[0] : null);
        if (key != null) {
            switch (key) {
                case TOP:
                    if (player != null) Menus.getInstance().openTopMenu(player, currency);
                    return true;
                case PAY:
                    if (args.length < 3) break;
                    if (player == null) return true;

                    target = Bukkit.getPlayer(args[1]);
                    if (!target.isOnline()) {
                        player.sendMessage(Messages.OFFLINE_PLAYER);
                        return true;
                    }

                    if (isSamePlayer(player, target)) {
                        player.sendMessage(StringUtils.replaceEach(Messages.TARGET_IS_SENDER,
                                new String[]{ "{currency}", "{currency_name}" },
                                new String[]{ currency.getFileName(), currency.getCurrencyName() }));
                        return true;
                    }

                    amount = NumberFormatter.getInstance().filter(args[2]);
                    if (isInvalidAmount(amount)) {
                        player.sendMessage(Messages.INVALID_AMOUNT);
                        return true;
                    }

                    int taxPerTransaction = currency.getTaxPerTransaction();
                    if (!isBiggerOrEqualValue(amount, BigInteger.valueOf(taxPerTransaction))) {
                        player.sendMessage(StringUtils.replace(Messages.MIN_VALUE, "{tax}", String.valueOf(taxPerTransaction)));
                        return true;
                    }

                    TransactionConfirmation transactionConfirmation = confirmations.remove(player);
                    if (transactionConfirmation != null) {
                        if (isSamePlayer(transactionConfirmation.getTarget(), target) && isSameValue(transactionConfirmation.getAmount(), amount)) {
                            transactionConfirmation.confirm();
                            return true;
                        }
                    }

                    BigInteger toGive = amount.subtract(amount.multiply(BigInteger.valueOf(taxPerTransaction)).divide(BigInteger.valueOf(100)));
                    transactionConfirmation = new TransactionConfirmation(player, target, amount, toGive, currency);
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
                case ITEM:
                    if (args.length < 3) break;
                    if (!sender.hasPermission(Settings.ADMIN_PERMISSION)) {
                        sender.sendMessage(Settings.PERMISSION_MESSAGE);
                        return true;
                    }

                    target = Bukkit.getPlayer(args[1]);
                    if (!target.isOnline()) {
                        sender.sendMessage(Messages.OFFLINE_PLAYER);
                        return true;
                    }

                    amount = NumberFormatter.getInstance().filter(args[2]);
                    if (isInvalidAmount(amount)) {
                        sender.sendMessage(Messages.INVALID_AMOUNT);
                        return true;
                    }

                    ItemStack item = currency.getItem(amount);
                    givePlayerItem(target, item);
                    return true;
                case GIVE:
                case SET:
                    if (args.length < 3) break;

                    if (!sender.hasPermission(Settings.ADMIN_PERMISSION)) {
                        sender.sendMessage(Settings.PERMISSION_MESSAGE);
                        return true;
                    }

                    target = Bukkit.getPlayer(args[1]);
                    if (!target.isOnline()) {
                        sender.sendMessage(Messages.OFFLINE_PLAYER);
                        return true;
                    }

                    amount = NumberFormatter.getInstance().filter(args[2]);
                    if (isInvalidAmount(amount)) {
                        sender.sendMessage(Messages.INVALID_AMOUNT);
                        return true;
                    }

                    targetData = DataManager.getInstance().getPlayerDataByUUID(target.getUniqueId());
                    if (key.equals(CommandKeys.GIVE)) {
                        targetData.addCurrencyAmount(currency, amount);
                    } else {
                        targetData.setCurrencyAmount(currency, amount);
                    }
                    return true;
                default:
                    if (player == null) break;

                    target = Bukkit.getPlayer(args[0]);
                    if (!target.isOnline()) {
                        player.sendMessage(Messages.OFFLINE_PLAYER);
                        return true;
                    }

                    Menus.getInstance().openInfoMenu(player, target, currency);
                    return true;
            }
        }

        if (player != null) Menus.getInstance().openMainMenu(player, currency);
        return true;
    }

    private boolean isSamePlayer(Player player, Player target) {
        return player.getUniqueId().equals(target.getUniqueId());
    }

    private boolean isSameValue(BigInteger value, BigInteger toCompare) {
        return value.compareTo(toCompare) == 0;
    }

    private boolean isBiggerOrEqualValue(BigInteger value, BigInteger toCompare) {
        return value.compareTo(toCompare) >= 0;
    }

    private CommandKeys getKeyByName(String str) {
        if (str != null && !str.isEmpty()) {
            for (CommandKeys keys : CommandKeys.values()) {
                boolean contains = keys.getKeyNames().stream().anyMatch(str::equalsIgnoreCase);
                if (contains) return keys;
            }
        }

        return null;
    }

    private void givePlayerItem(Player target, ItemStack item) {
        if (target.getInventory().firstEmpty() != -1) {
            target.getInventory().addItem(item);   
        } else {
            target.getWorld().dropItemNaturally(target.getLocation(), item);
        }
    }

    private boolean isInvalidAmount(BigInteger amount) {
        return amount.signum() <= 0;
    }

    private enum CommandKeys {
        GIVE(currency.getCommandKeyNames("GIVE")),
        SET(currency.getCommandKeyNames("SET")),
        PAY(currency.getCommandKeyNames("PAY")),
        TOP(currency.getCommandKeyNames("TOP")),
        ITEM(currency.getCommandKeyNames("ITEM"));

        private final List<String> keyNames;

        CommandKeys(List<String> keyNames) {
            this.keyNames = keyNames;
        }

        public List<String> getKeyNames() {
            return keyNames;
        }
    }
}