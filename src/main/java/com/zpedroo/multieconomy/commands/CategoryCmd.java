package com.zpedroo.multieconomy.commands;

import com.zpedroo.multieconomy.objects.Category;
import com.zpedroo.multieconomy.utils.menu.Menus;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CategoryCmd implements CommandExecutor {

    private Category category;

    public CategoryCmd(Category category) {
        this.category = category;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (args.length > 0 && player == null) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) return true;
            if (category.getOpenPermission() != null && !target.hasPermission(category.getOpenPermission())) {
                target.sendMessage(category.getPermissionMessage());
                return true;
            }

            Menus.getInstance().openCategoryMenu(target, category);
            return true;
        }

        if (player == null) return true;
        if (category.getOpenPermission() != null && !player.hasPermission(category.getOpenPermission())) {
            player.sendMessage(category.getPermissionMessage());
            return true;
        }

        Menus.getInstance().openCategoryMenu(player, category);
        return false;
    }
}