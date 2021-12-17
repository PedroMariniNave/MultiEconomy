package com.zpedroo.multieconomy.managers;

import com.zpedroo.multieconomy.MultiEconomy;
import com.zpedroo.multieconomy.commands.CategoryCmd;
import com.zpedroo.multieconomy.commands.CurrencyCmd;
import com.zpedroo.multieconomy.commands.ShopCmd;
import com.zpedroo.multieconomy.managers.cache.DataCache;
import com.zpedroo.multieconomy.mysql.DBConnection;
import com.zpedroo.multieconomy.objects.Currency;
import com.zpedroo.multieconomy.objects.*;
import com.zpedroo.multieconomy.utils.builder.ItemBuilder;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {

    private static DataManager instance;
    public static DataManager getInstance() { return instance; }

    private DataCache dataCache;

    public DataManager(Plugin plugin) {
        instance = this;
        this.dataCache = new DataCache();
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, this::loadShops, 0L);
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, this::loadCurrencies, 0L);
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, this::loadCategories, 10L);
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, this::loadDataFromDB, 20L);
    }

    public PlayerData load(UUID uuid) {
        PlayerData data = dataCache.getPlayerData().get(uuid);
        if (data == null) {
            data = DBConnection.getInstance().getDBManager().loadData(uuid);
            dataCache.getPlayerData().put(uuid, data);
        }

        return data;
    }

    public Boolean hasAccount(UUID uuid) {
        if (dataCache.getPlayerData().containsKey(uuid)) return true;

        return DBConnection.getInstance().getDBManager().contains(uuid.toString(), "uuid");
    }

    public void save(UUID uuid) {
        PlayerData data = dataCache.getPlayerData().get(uuid);
        if (data == null) return;
        if (!data.isQueueUpdate()) return;

        DBConnection.getInstance().getDBManager().saveData(data);
        data.setUpdate(false);
    }

    public void saveAll() {
        new HashSet<>(dataCache.getPlayerData().keySet()).forEach(this::save);
    }

    public void loadDataFromDB() {
        dataCache.setPlayerData(DBConnection.getInstance().getDBManager().getPlayerData());
        dataCache.setTopCurrencies(getTopCurrencies());
    }

    private void loadShops() {
        File folder = new File(MultiEconomy.get().getDataFolder(), "/shops");
        File[] files = folder.listFiles((file, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File fl : files) {
            if (fl == null) continue;

            FileConfiguration file = YamlConfiguration.loadConfiguration(fl);

            String name = fl.getName().replace(".yml", "");
            String command = file.contains("Settings.command") ? file.getString("Settings.command") : null;
            List<String> aliases = file.contains("Settings.aliases") ? file.getStringList("Settings.aliases") : null;
            String openPermission = file.contains("Settings.open-permission") ? file.getString("Settings.open-permission") : null;
            String openPermissionMessage = file.contains("Settings.permission-message") ?
                    ChatColor.translateAlternateColorCodes('&', file.getString("Settings.permission-message")) : null;

            Shop shop = new Shop(file, openPermission, openPermissionMessage);
            dataCache.getShops().put(name, shop);

            if (command == null || aliases == null || command.isEmpty()) continue;

            MultiEconomy.get().registerCommand(command, aliases, openPermission, openPermissionMessage, new ShopCmd(shop));
        }
    }

    private void loadCurrencies() {
        File folder = new File(MultiEconomy.get().getDataFolder(), "/currencies");
        File[] files = folder.listFiles((file, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File fl : files) {
            if (fl == null) continue;

            FileConfiguration file = YamlConfiguration.loadConfiguration(fl);

            String command = file.getString("Currency-Settings.command");
            List<String> aliases = file.getStringList("Currency-Settings.aliases");
            String permission = file.contains("Currency-Settings.permission") ? file.getString("Currency-Settings.permission") : null;
            String permissionMessage = file.contains("Currency-Settings.permission-message") ?
                    file.getString("Currency-Settings.permission-message") : null;

            Map<String, String[]> titles = new HashMap<>(1);
            for (String str : file.getConfigurationSection("Titles").getKeys(false)) {
                String title = ChatColor.translateAlternateColorCodes('&', file.getString("Titles." + str + ".title"));
                String subtitle = ChatColor.translateAlternateColorCodes('&', file.getString("Titles." + str + ".subtitle"));

                titles.put(str, new String[] { title, subtitle });
            }

            Map<String, String> inventoryTitles = new HashMap<>(4);
            for (String inventory : file.getConfigurationSection("Inventory-Titles").getKeys(false)) {
                String title = ChatColor.translateAlternateColorCodes('&', file.getString("Inventory-Titles." + inventory));

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
            Integer taxPerTransaction = file.getInt("Currency-Settings.tax-per-transaction", 0);
            ItemStack item = ItemBuilder.build(file, "Item").build();

            Currency currency = new Currency(titles, inventoryTitles, commandKeys, fileName, currencyName, currencyDisplay, currencyColor, amountDisplay, taxPerTransaction, item);
            dataCache.getCurrencies().put(fileName, currency);

            MultiEconomy.get().registerCommand(command, aliases, permission, permissionMessage, new CurrencyCmd(currency));
        }
    }

    private void loadCategories() {
        File folder = new File(MultiEconomy.get().getDataFolder(), "/categories");
        File[] files = folder.listFiles((file, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File fl : files) {
            if (fl == null) continue;

            FileConfiguration file = YamlConfiguration.loadConfiguration(fl);

            String name = fl.getName().replace(".yml", "");
            String command = file.contains("Settings.command") ? file.getString("Settings.command") : null;
            List<String> aliases = file.contains("Settings.aliases") ? file.getStringList("Settings.aliases") : null;
            String openPermission = file.contains("Settings.open-permission") ? file.getString("Settings.open-permission") : null;
            String openPermissionMessage = file.contains("Settings.permission-message") ?
                    ChatColor.translateAlternateColorCodes('&', file.getString("Settings.permission-message")) : null;

            List<CategoryItem> categoryItems = new ArrayList<>(8);

            for (String str : file.getConfigurationSection("Inventory.items").getKeys(false)) {
                String permission = file.contains("Inventory.items." + str + ".permission") ?
                        file.getString("Inventory.items." + str + ".permission") : null;
                String permissionMessage = file.contains("Inventory.items." + str + ".permission-message") ?
                        ChatColor.translateAlternateColorCodes('&', file.getString("Inventory.items." + str + ".permission-message")) : null;

                Currency currency = dataCache.getCurrencies().get(file.getString("Inventory.items." + str + ".currency"));
                if (currency == null) continue;

                BigInteger price = NumberFormatter.getInstance().filter(file.getString("Inventory.items." + str + ".price", "0"));
                BigInteger defaultAmount = NumberFormatter.getInstance().filter(file.getString("Inventory.items." + str + ".default-amount", "1"));
                ItemStack display = ItemBuilder.build(file, "Inventory.items." + str + ".display", new String[]{
                        "{price}"
                }, new String[]{
                        StringUtils.replaceEach(currency.getAmountDisplay(), new String[]{
                                "{amount}"
                        }, new String[]{
                                NumberFormatter.getInstance().format(price)
                        })
                }).build();
                ItemStack shopItem = file.contains("Inventory.items." + str + ".shop-item") ?
                        ItemBuilder.build(file, "Inventory.items." + str + ".shop-item").build() : null;
                Boolean inventoryLimit = file.getBoolean("Inventory.items." + str + ".inventory-limit", true);
                List<String> commands = file.getStringList("Inventory.items." + str + ".commands");

                categoryItems.add(new CategoryItem(permission, permissionMessage, currency, price, defaultAmount, display, shopItem, inventoryLimit, commands));
            }

            Category category = new Category(file, openPermission, openPermissionMessage, categoryItems);
            dataCache.getCategories().put(name, category);

            if (command == null || aliases == null || command.isEmpty()) continue;

            MultiEconomy.get().registerCommand(command, aliases, openPermission, openPermissionMessage, new CategoryCmd(category));
        }
    }

    private Map<Currency, List<PlayerData>> getTopCurrencies() {
        Map<Currency, List<PlayerData>> ret = new HashMap<>(dataCache.getCurrencies().size() * 10);

        new HashSet<>(dataCache.getCurrencies().values()).forEach(currency -> {
            Map<UUID, BigInteger> values = new HashMap<>(dataCache.getPlayerData().size());
            new HashSet<>(dataCache.getPlayerData().values()).forEach(data -> {
                values.put(data.getUUID(), data.getCurrencyAmount(currency));
            });

            List<PlayerData> dataList = new LinkedList<>();

            getSorted(values, 10).keySet().forEach(uuid -> {
                dataList.add(DataManager.getInstance().load(uuid));
            });

            ret.put(currency, dataList);
        });

        return ret;
    }

    private Map<UUID, BigInteger> getSorted(Map<UUID, BigInteger> map, Integer limit) {
        return map.entrySet().stream().sorted((value1, value2) -> value2.getValue().compareTo(value1.getValue())).limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (value1, value2) -> value1, LinkedHashMap::new));
    }

    public DataCache getCache() {
        return dataCache;
    }
}