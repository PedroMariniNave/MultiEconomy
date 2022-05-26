package com.zpedroo.multieconomy.objects.category;

import lombok.Builder;
import lombok.Data;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

@Data
@Builder
public class Category {

    private final File file;
    private final FileConfiguration fileConfiguration;
    private final String openPermission;
    private final String permissionMessage;
    private final List<CategoryItem> items;
    private Task task;
}