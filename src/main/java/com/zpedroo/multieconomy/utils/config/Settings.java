package com.zpedroo.multieconomy.utils.config;

import com.zpedroo.multieconomy.utils.FileUtils;
import com.zpedroo.multieconomy.utils.color.Colorize;

public class Settings {

    public static final int DATA_UPDATE_INTERVAL_IN_SECONDS = FileUtils.get().getInt(FileUtils.Files.CONFIG, "Settings.data-update-interval");

    public static final String VAULT_CURRENCY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.vault-currency");

    public static final String ADMIN_PERMISSION = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.admin-permission");

    public static final String PERMISSION_MESSAGE = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.permission-message"));
}