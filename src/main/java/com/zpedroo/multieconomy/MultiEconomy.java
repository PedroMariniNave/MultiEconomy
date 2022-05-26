package com.zpedroo.multieconomy;

import com.zpedroo.multieconomy.hooks.PlaceholderAPIHook;
import com.zpedroo.multieconomy.hooks.VaultEconomy;
import com.zpedroo.multieconomy.listeners.PlayerGeneralListeners;
import com.zpedroo.multieconomy.listeners.ShopListeners;
import com.zpedroo.multieconomy.listeners.WithdrawListeners;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.managers.InventoryManager;
import com.zpedroo.multieconomy.managers.TransactionManager;
import com.zpedroo.multieconomy.mysql.DBConnection;
import com.zpedroo.multieconomy.scheduler.SchedulerLoader;
import com.zpedroo.multieconomy.tasks.SaveTask;
import com.zpedroo.multieconomy.utils.FileUtils;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import com.zpedroo.multieconomy.utils.menu.Menus;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.quartz.SchedulerException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

import static com.zpedroo.multieconomy.utils.config.Settings.*;

public class MultiEconomy extends JavaPlugin {

    private static MultiEconomy instance;
    public static MultiEconomy get() { return instance; }

    public void onEnable() {
        instance = this;
        new FileUtils(this);

        if (!isMySQLEnabled(getConfig())) {
            getLogger().log(Level.SEVERE, "MySQL are disabled! You need to enable it.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new DBConnection(getConfig());
        new NumberFormatter(getConfig());
        new DataManager();
        new TransactionManager();
        new InventoryManager();
        new Menus();
        new SaveTask(this);
        new SchedulerLoader();

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            new VaultEconomy(this, VAULT_CURRENCY);
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this);
        }

        registerListeners();
        startSchedulers();
    }

    public void onDisable() {
        if (!isMySQLEnabled(getConfig())) return;

        try {
            DataManager.getInstance().saveAll();
            DBConnection.getInstance().closeConnection();
            SchedulerLoader.getInstance().stopAllSchedulers();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "An error occurred while trying to save data!");
            ex.printStackTrace();
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerGeneralListeners(), this);
        getServer().getPluginManager().registerEvents(new ShopListeners(), this);
        getServer().getPluginManager().registerEvents(new WithdrawListeners(), this);
    }

    public void registerCommand(String command, List<String> aliases, String permission, String permissionMessage, CommandExecutor executor) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            PluginCommand pluginCmd = constructor.newInstance(command, this);
            pluginCmd.setAliases(aliases);
            pluginCmd.setExecutor(executor);
            if (permission != null) pluginCmd.setPermission(permission);
            if (permissionMessage != null) pluginCmd.setPermissionMessage(permissionMessage);

            Field field = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getPluginManager());
            commandMap.register(getName().toLowerCase(), pluginCmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void startSchedulers() {
        try {
            SchedulerLoader.getInstance().startAllCategoriesScheduler();
        } catch (SchedulerException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isMySQLEnabled(FileConfiguration file) {
        if (!file.contains("MySQL.enabled")) return false;

        return file.getBoolean("MySQL.enabled");
    }
}