package com.zpedroo.multieconomy.utils.config;

import com.zpedroo.multieconomy.utils.FileUtils;

public class Logs {

    public static final String PAY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Logs.pay");

    public static final String ITEM = FileUtils.get().getString(FileUtils.Files.CONFIG, "Logs.item");

    public static final String EDIT = FileUtils.get().getString(FileUtils.Files.CONFIG, "Logs.edit");

    public static final String WITHDRAW = FileUtils.get().getString(FileUtils.Files.CONFIG, "Logs.withdraw");

    public static final String DEPOSIT = FileUtils.get().getString(FileUtils.Files.CONFIG, "Logs.deposit");
}