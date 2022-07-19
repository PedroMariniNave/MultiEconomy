package com.zpedroo.multieconomy.utils.color;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Colorize {

    public static String getColored(String str) {
        if (str == null || str.isEmpty()) return str;

        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static List<String> getColored(List<String> list) {
        List<String> colored = new ArrayList<>(list.size());
        for (String str : list) {
            colored.add(getColored(str));
        }

        return colored;
    }
}