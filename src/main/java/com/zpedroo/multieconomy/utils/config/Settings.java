package com.zpedroo.multieconomy.utils.config;

import com.zpedroo.multieconomy.utils.FileUtils;
import org.bukkit.ChatColor;

public class Settings {

    public static final Long SAVE_INTERVAL = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.save-interval");

    public static final String VAULT_CURRENCY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.vault-currency");

    public static final String ADMIN_PERMISSION = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.admin-permission");

    public static final String PERMISSION_MESSAGE = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.permission-message"));
}