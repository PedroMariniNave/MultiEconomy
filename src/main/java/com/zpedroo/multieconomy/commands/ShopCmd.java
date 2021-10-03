package com.zpedroo.multieconomy.commands;

import com.zpedroo.multieconomy.objects.Shop;
import com.zpedroo.multieconomy.utils.menu.Menus;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCmd implements CommandExecutor {

    private Shop shop;

    public ShopCmd(Shop shop) {
        this.shop = shop;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (args.length > 0 && player == null) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) return true;
            if (shop.getOpenPermission() != null && !target.hasPermission(shop.getOpenPermission())) {
                target.sendMessage(shop.getPermissionMessage());
                return true;
            }

            Menus.getInstance().openShopMenu(target, shop);
            return true;
        }

        if (player == null) return true;
        if (shop.getOpenPermission() != null && !player.hasPermission(shop.getOpenPermission())) {
            player.sendMessage(shop.getPermissionMessage());
            return true;
        }

        Menus.getInstance().openShopMenu(player, shop);
        return false;
    }
}