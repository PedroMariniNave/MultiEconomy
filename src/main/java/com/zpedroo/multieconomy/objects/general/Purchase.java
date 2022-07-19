package com.zpedroo.multieconomy.objects.general;

import com.zpedroo.multieconomy.objects.category.CategoryItem;
import lombok.Data;
import org.bukkit.entity.Player;

@Data
public class Purchase {

    private final Player player;
    private final CategoryItem item;
}