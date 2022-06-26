package com.zpedroo.multieconomy.managers.cache;

import com.zpedroo.multieconomy.MultiEconomy;
import com.zpedroo.multieconomy.commands.CategoryCmd;
import com.zpedroo.multieconomy.commands.CurrencyCmd;
import com.zpedroo.multieconomy.commands.ShopCmd;
import com.zpedroo.multieconomy.commands.TopOneCmd;
import com.zpedroo.multieconomy.mysql.DBConnection;
import com.zpedroo.multieconomy.objects.category.Category;
import com.zpedroo.multieconomy.objects.category.CategoryItem;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.general.Shop;
import com.zpedroo.multieconomy.objects.player.PlayerData;
import com.zpedroo.multieconomy.utils.builder.ItemBuilder;
import com.zpedroo.multieconomy.utils.color.Colorize;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigInteger;
import java.util.*;

@Getter
public class DataCache {

    private final Map<UUID, PlayerData> playersData = DBConnection.getInstance().getDBManager().getAllPlayersData();
    private final Map<String, Currency> currencies = getCurrenciesFromFolder();
    private final Map<String, Category> categories = getCategoriesFromFolder();
    private final Map<String, Shop> shops = getShopsFromFolder();
    private final Map<Currency, UUID> topsOne = new HashMap<>(4);
    private Map<Currency, List<PlayerData>> topCurrencies = new HashMap<>(0);

    public void setTopCurrencies(Map<Currency, List<PlayerData>> topCurrencies) {
        this.topCurrencies = topCurrencies;
    }

    private Map<String, Currency> getCurrenciesFromFolder() {
        Map<String, Currency> ret = new HashMap<>(4);
        File folder = new File(MultiEconomy.get().getDataFolder(), "/currencies");
        File[] files = folder.listFiles((file, name) -> name.endsWith(".yml"));
        if (files == null) return ret;

        for (File fl : files) {
            FileConfiguration file = YamlConfiguration.loadConfiguration(fl);

            String command = file.getString("Currency-Settings.command");
            List<String> aliases = file.getStringList("Currency-Settings.aliases");
            String permission = file.getString("Currency-Settings.permission", null);
            String permissionMessage = Colorize.getColored(file.getString("Currency-Settings.permission-message", null));

            Map<String, String[]> titles = new HashMap<>(1);
            for (String str : file.getConfigurationSection("Titles").getKeys(false)) {
                String title = Colorize.getColored(file.getString("Titles." + str + ".title"));
                String subtitle = Colorize.getColored(file.getString("Titles." + str + ".subtitle"));

                titles.put(str, new String[] { title, subtitle });
            }

            Map<String, String> inventoryTitles = new HashMap<>(4);
            for (String inventory : file.getConfigurationSection("Inventory-Titles").getKeys(false)) {
                String title = Colorize.getColored(file.getString("Inventory-Titles." + inventory));

                inventoryTitles.put(inventory, title);
            }

            Map<String, String> commandKeys = new HashMap<>(4);
            for (String keyName : file.getConfigurationSection("Currency-Settings.keys").getKeys(false)) {
                String keys = file.getString("Currency-Settings.keys." + keyName);

                commandKeys.put(keyName, keys);
            }

            String fileName = fl.getName().replace(".yml", "");
            String currencyName = file.getString("Currency-Settings.currency-name");
            String currencyDisplay = ChatColor.translateAlternateColorCodes('&', file.getString("Currency-Settings.currency-display"));
            String currencyColor = ChatColor.translateAlternateColorCodes('&', file.getString("Currency-Settings.currency-color"));
            String amountDisplay = ChatColor.translateAlternateColorCodes('&', file.getString("Currency-Settings.amount-display"));
            String topOneTag = ChatColor.translateAlternateColorCodes('&', file.getString("Currency-Settings.top-one.tag", ""));
            String topOneCommand = file.getString("Currency-Settings.top-one.command", null);
            List<String> topOneAliases = file.getStringList("Currency-Settings.top-one.aliases");
            int taxPerTransaction = file.getInt("Currency-Settings.tax-per-transaction", 0);
            ItemStack item = ItemBuilder.build(file, "Item").build();

            Currency currency = new Currency(titles, inventoryTitles, commandKeys, fileName, currencyName, currencyDisplay, currencyColor, amountDisplay, topOneTag, taxPerTransaction, item);
            ret.put(fileName, currency);

            MultiEconomy.get().registerCommand(command, aliases, permission, permissionMessage, new CurrencyCmd(currency));

            if (topOneCommand != null) {
                MultiEconomy.get().registerCommand(topOneCommand, topOneAliases, null, null, new TopOneCmd(currency));
            }
        }

        return ret;
    }

    private Map<String, Category> getCategoriesFromFolder() {
        Map<String, Category> ret = new HashMap<>(4);
        File folder = new File(MultiEconomy.get().getDataFolder(), "/categories");
        File[] files = folder.listFiles((file, name) -> name.endsWith(".yml"));
        if (files == null) return ret;

        for (File fl : files) {
            FileConfiguration file = YamlConfiguration.loadConfiguration(fl);

            String name = fl.getName().replace(".yml", "");
            String command = file.getString("Settings.command", null);
            List<String> aliases = file.contains("Settings.aliases") ? file.getStringList("Settings.aliases") : null;
            String openPermission = file.getString("Settings.open-permission", null);
            String openPermissionMessage = Colorize.getColored(file.getString("Settings.permission-message", null));

            List<CategoryItem> categoryItems = getCategoryItemsFromFile(file);

            Category category = new Category(fl, file, openPermission, openPermissionMessage, categoryItems);
            ret.put(name, category);

            if (command != null && aliases != null && !command.isEmpty()) {
                MultiEconomy.get().registerCommand(command, aliases, openPermission, openPermissionMessage, new CategoryCmd(category));
            }
        }

        return ret;
    }

    private List<CategoryItem> getCategoryItemsFromFile(FileConfiguration file) {
        List<CategoryItem> ret = new ArrayList<>(8);

        for (String str : file.getConfigurationSection("Inventory.items").getKeys(false)) {
            String permission = file.contains("Inventory.items." + str + ".permission") ? file.getString("Inventory.items." + str + ".permission") : null;
            String permissionMessage = Colorize.getColored(file.getString("Inventory.items." + str + ".permission-message", null));

            Currency currency = currencies.get(file.getString("Inventory.items." + str + ".currency"));
            if (currency == null) continue;

            BigInteger price = NumberFormatter.getInstance().filter(file.getString("Inventory.items." + str + ".price", "0"));
            BigInteger defaultAmount = NumberFormatter.getInstance().filter(file.getString("Inventory.items." + str + ".default-amount", "1"));
            ItemStack display = ItemBuilder.build(file, "Inventory.items." + str + ".display").build();
            ItemStack itemToGive = file.contains("Inventory.items." + str + ".item-to-give") ?
                    ItemBuilder.build(file, "Inventory.items." + str + ".item-to-give").build() : null;
            BigInteger maxStock = NumberFormatter.getInstance().filter(file.getString("Inventory.items." + str + ".max-stock", "0"));
            boolean inventoryLimit = file.getBoolean("Inventory.items." + str + ".inventory-limit", true);
            boolean fixedItem = file.getBoolean("Inventory.items." + str + ".fixed-item", false);
            List<String> commands = file.getStringList("Inventory.items." + str + ".commands");

            CategoryItem categoryItem = new CategoryItem(permission, permissionMessage, currency, price, defaultAmount, display, itemToGive, maxStock, inventoryLimit, fixedItem, commands);
            ret.add(categoryItem);
        }

        return ret;
    }

    private Map<String, Shop> getShopsFromFolder() {
        Map<String, Shop> ret = new HashMap<>(4);
        File folder = new File(MultiEconomy.get().getDataFolder(), "/shops");
        File[] files = folder.listFiles((file, name) -> name.endsWith(".yml"));
        if (files == null) return ret;

        for (File fl : files) {
            FileConfiguration file = YamlConfiguration.loadConfiguration(fl);

            String name = fl.getName().replace(".yml", "");
            String command = file.getString("Settings.command", null);
            List<String> aliases = file.contains("Settings.aliases") ? file.getStringList("Settings.aliases") : null;
            String openPermission = file.getString("Settings.open-permission", null);
            String permissionMessage = Colorize.getColored(file.getString("Settings.permission-message", null));

            Shop shop = new Shop(file, openPermission, permissionMessage);
            ret.put(name, shop);

            if (command != null && aliases != null && !command.isEmpty()) {
                MultiEconomy.get().registerCommand(command, aliases, openPermission, permissionMessage, new ShopCmd(shop));
            }
        }

        return ret;
    }
}