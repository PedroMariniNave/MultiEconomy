package com.zpedroo.multieconomy.objects;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Category {

    private FileConfiguration file;
    private String openPermission;
    private String permissionMessage;
    private List<CategoryItem> categoryItems;

    public Category(FileConfiguration file, String openPermission, String permissionMessage, List<CategoryItem> categoryItems) {
        this.file = file;
        this.openPermission = openPermission;
        this.permissionMessage = permissionMessage;
        this.categoryItems = categoryItems;
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

    public List<CategoryItem> getItems() {
        return categoryItems;
    }
}