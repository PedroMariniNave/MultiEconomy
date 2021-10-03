package com.zpedroo.multieconomy.objects;

import org.bukkit.configuration.file.FileConfiguration;

public class Shop {

    private FileConfiguration file;
    private String openPermission;
    private String permissionMessage;

    public Shop(FileConfiguration file, String openPermission, String permissionMessage) {
        this.file = file;
        this.openPermission = openPermission;
        this.permissionMessage = permissionMessage;
    }

    public FileConfiguration getFile() {
        return file;
    }

    public String getOpenPermission() {
        return openPermission;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }
}