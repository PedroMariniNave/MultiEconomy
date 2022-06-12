package com.zpedroo.multieconomy.utils.config;

import com.zpedroo.multieconomy.utils.FileUtils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Messages {

    public static final String BUY_ALL_ZERO = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.buy-all-zero"));

    public static final String OFFLINE_PLAYER = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.offline-player"));

    public static final String INVALID_AMOUNT = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-amount"));

    public static final String TARGET_IS_SENDER = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.send-to-yourself"));

    public static final String INSUFFICIENT_STOCK = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.insufficient-stock"));

    public static final String INSUFFICIENT_CURRENCY = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.insufficient-currency"));

    public static final String INSUFFICIENT_LEVEL = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.insufficient-level"));

    public static final String NEED_SPACE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.need-space"));

    public static final String MIN_VALUE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.min-value"));

    public static final String TOP_ONE_DISPLAY = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.top-one-display"));

    public static final List<String> SENT = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.sent"));

    public static final List<String> RECEIVED = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.received"));

    public static final List<String> WITHDRAW = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.withdraw"));

    public static final List<String> CHOOSE_AMOUNT = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.choose-amount"));

    public static final List<String> SUCCESSFUL_PURCHASED = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.successful-purchased"));

    public static final List<String> CONFIRM = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.confirm"));

    public static final List<String> NEW_TOP_ONE = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.new-top-one"));

    private static String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private static List<String> getColored(List<String> list) {
        List<String> colored = new ArrayList<>(list.size());
        for (String str : list) {
            colored.add(getColored(str));
        }

        return colored;
    }
}