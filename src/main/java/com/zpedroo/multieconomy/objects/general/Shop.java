package com.zpedroo.multieconomy.objects.general;

import lombok.Builder;
import lombok.Data;
import org.bukkit.configuration.file.FileConfiguration;

@Data
@Builder
public class Shop {

    private final FileConfiguration file;
    private final String openPermission;
    private final String permissionMessage;
}