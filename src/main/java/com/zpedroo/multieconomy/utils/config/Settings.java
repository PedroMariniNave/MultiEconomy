package com.zpedroo.multieconomy.utils.config;

import com.zpedroo.multieconomy.utils.FileUtils;
import com.zpedroo.multieconomy.utils.color.Colorize;

import java.util.List;

public class Settings {

    public static final int DATA_UPDATE_INTERVAL_IN_SECONDS = FileUtils.get().getInt(FileUtils.Files.CONFIG, "Settings.data-update-interval");

    public static final String VAULT_CURRENCY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.vault-currency");

    public static final String ADMIN_PERMISSION = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.admin-permission");

    public static final String PERMISSION_MESSAGE = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.permission-message"));

    public static final List<String> ITEM_KEYS = FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Settings.keys.item");

    public static final List<String> TOP_KEYS = FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Settings.keys.top");

    public static final List<String> SET_KEYS = FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Settings.keys.set");

    public static final List<String> GIVE_KEYS = FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Settings.keys.give");

    public static final List<String> PAY_KEYS = FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Settings.keys.pay");
}