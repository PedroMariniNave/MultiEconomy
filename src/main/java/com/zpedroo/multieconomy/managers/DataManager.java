package com.zpedroo.multieconomy.managers;

import com.zpedroo.multieconomy.MultiEconomy;
import com.zpedroo.multieconomy.commands.CategoryCmd;
import com.zpedroo.multieconomy.commands.CurrencyCmd;
import com.zpedroo.multieconomy.commands.ShopCmd;
import com.zpedroo.multieconomy.managers.cache.DataCache;
import com.zpedroo.multieconomy.mysql.DBConnection;
import com.zpedroo.multieconomy.objects.category.Category;
import com.zpedroo.multieconomy.objects.category.CategoryItem;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.general.Shop;
import com.zpedroo.multieconomy.objects.player.PlayerData;
import com.zpedroo.multieconomy.utils.builder.ItemBuilder;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {

    private static DataManager instance;
    public static DataManager getInstance() { return instance; }

    private final DataCache dataCache = new DataCache();

    public DataManager() {
        instance = this;
        this.loadShops();
        this.loadCurrencies();
        this.loadCategories();
        MultiEconomy.get().getServer().getScheduler().runTaskLaterAsynchronously(MultiEconomy.get(), this::updateTopCurrencies, 0L);
    }

    public PlayerData load(UUID uuid) {
        PlayerData data = dataCache.getPlayersData().get(uuid);
        if (data == null) {
            data = DBConnection.getInstance().getDBManager().loadData(uuid);
            dataCache.getPlayersData().put(uuid, data);
        }

        return data;
    }

    public boolean hasAccount(UUID uuid) {
        if (dataCache.getPlayersData().containsKey(uuid)) return true;

        return DBConnection.getInstance().getDBManager().contains(uuid.toString(), "uuid");
    }

    public void save(UUID uuid) {
        PlayerData data = dataCache.getPlayersData().get(uuid);
        if (data == null) return;
        if (!data.isQueueUpdate()) return;

        DBConnection.getInstance().getDBManager().saveData(data);
        data.setUpdate(false);
    }

    public void saveAll() {
        new HashSet<>(dataCache.getPlayersData().keySet()).forEach(this::save);
    }

    public void updateTopCurrencies() {
        dataCache.setTopCurrencies(getTopCurrencies());
    }

    private void loadShops() {
        File folder = new File(MultiEconomy.get().getDataFolder(), "/shops");
        File[] files = folder.listFiles((file, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File fl : files) {
            FileConfiguration file = YamlConfiguration.loadConfiguration(fl);

            String name = fl.getName().replace(".yml", "");
            String command = file.contains("Settings.command") ? file.getString("Settings.command") : null;
            List<String> aliases = file.contains("Settings.aliases") ? file.getStringList("Settings.aliases") : null;
            String openPermission = file.contains("Settings.open-permission") ? file.getString("Settings.open-permission") : null;
            String permissionMessage = file.contains("Settings.permission-message") ?
                    ChatColor.translateAlternateColorCodes('&', file.getString("Settings.permission-message")) : null;

            Shop shop = Shop.builder().file(file).openPermission(openPermission).permissionMessage(permissionMessage).build();
            dataCache.getShops().put(name, shop);

            if (command == null || aliases == null || command.isEmpty()) continue;

            MultiEconomy.get().registerCommand(command, aliases, openPermission, permissionMessage, new ShopCmd(shop));
        }
    }

    private void loadCurrencies() {
        File folder = new File(MultiEconomy.get().getDataFolder(), "/currencies");
        File[] files = folder.listFiles((file, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File fl : files) {
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
            int taxPerTransaction = file.getInt("Currency-Settings.tax-per-transaction", 0);
            ItemStack item = ItemBuilder.build(file, "Item").build();

            Currency currency = Currency.builder().titles(titles).inventoryTitles(inventoryTitles).commandKeys(commandKeys)
                    .fileName(fileName).currencyName(currencyName).currencyDisplay(currencyDisplay).currencyColor(currencyColor)
                    .amountDisplay(amountDisplay).taxPerTransaction(taxPerTransaction).item(item).build();
            dataCache.getCurrencies().put(fileName, currency);

            MultiEconomy.get().registerCommand(command, aliases, permission, permissionMessage, new CurrencyCmd(currency));
        }
    }

    private void loadCategories() {
        File folder = new File(MultiEconomy.get().getDataFolder(), "/categories");
        File[] files = folder.listFiles((file, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File fl : files) {
            FileConfiguration file = YamlConfiguration.loadConfiguration(fl);

            String name = fl.getName().replace(".yml", "");
            String command = file.contains("Settings.command") ? file.getString("Settings.command") : null;
            List<String> aliases = file.contains("Settings.aliases") ? file.getStringList("Settings.aliases") : null;
            String openPermission = file.contains("Settings.open-permission") ? file.getString("Settings.open-permission") : null;
            String openPermissionMessage = file.contains("Settings.permission-message") ?
                    ChatColor.translateAlternateColorCodes('&', file.getString("Settings.permission-message")) : null;

            List<CategoryItem> categoryItems = new ArrayList<>(8);

            for (String str : file.getConfigurationSection("Inventory.items").getKeys(false)) {
                String permission = file.contains("Inventory.items." + str + ".permission") ? file.getString("Inventory.items." + str + ".permission") : null;
                String permissionMessage = file.contains("Inventory.items." + str + ".permission-message") ?
                        ChatColor.translateAlternateColorCodes('&', file.getString("Inventory.items." + str + ".permission-message")) : null;

                Currency currency = dataCache.getCurrencies().get(file.getString("Inventory.items." + str + ".currency"));
                if (currency == null) continue;

                BigInteger price = NumberFormatter.getInstance().filter(file.getString("Inventory.items." + str + ".price", "0"));
                BigInteger defaultAmount = NumberFormatter.getInstance().filter(file.getString("Inventory.items." + str + ".default-amount", "1"));
                ItemStack display = ItemBuilder.build(file, "Inventory.items." + str + ".display", new String[]{
                        "{price}"
                }, new String[]{
                        currency.getAmountDisplay(price)
                }).build();
                ItemStack itemToGive = file.contains("Inventory.items." + str + ".item-to-give") ?
                        ItemBuilder.build(file, "Inventory.items." + str + ".item-to-give").build() : null;
                int requiredLevel = file.getInt("Inventory.items." + str + ".required-level", 0);
                BigInteger maxStock = NumberFormatter.getInstance().filter(file.getString("Inventory.items." + str + ".max-stock", "0"));
                boolean inventoryLimit = file.getBoolean("Inventory.items." + str + ".inventory-limit", true);
                boolean fixedItem = file.getBoolean("Inventory.items." + str + ".fixed-item", false);
                List<String> commands = file.getStringList("Inventory.items." + str + ".commands");

                CategoryItem categoryItem = CategoryItem.builder().permission(permission).permissionMessage(permissionMessage).currency(currency)
                        .price(price).defaultAmount(defaultAmount).display(display).itemToGive(itemToGive).requiredLevel(requiredLevel)
                        .maxStock(maxStock).stockAmount(maxStock).inventoryLimit(inventoryLimit).fixedItem(fixedItem).commands(commands).build();

                categoryItems.add(categoryItem);
            }

            Category category = Category.builder().file(fl).fileConfiguration(file).openPermission(openPermission)
                    .permissionMessage(openPermissionMessage).items(categoryItems).build();

            dataCache.getCategories().put(name, category);

            if (command == null || aliases == null || command.isEmpty()) continue;

            MultiEconomy.get().registerCommand(command, aliases, openPermission, openPermissionMessage, new CategoryCmd(category));
        }
    }

    public Map<Currency, List<PlayerData>> getTopCurrencies() {
        Map<Currency, List<PlayerData>> ret = new HashMap<>(dataCache.getCurrencies().size() * 10);
        Map<UUID, PlayerData> allPlayersData = DBConnection.getInstance().getDBManager().getAllPlayersData();

        for (Currency currency : dataCache.getCurrencies().values()) {
            new Thread(() -> {
                Map<UUID, BigInteger> values = new HashMap<>(allPlayersData.size());
                allPlayersData.values().forEach(data -> {
                    values.put(data.getUUID(), data.getCurrencyAmount(currency));
                });

                List<PlayerData> dataList = new LinkedList<>();

                getSorted(values, 10).keySet().forEach(uuid -> {
                    dataList.add(DataManager.getInstance().load(uuid));
                });

                ret.put(currency, dataList);
            }).start();
        }

        return ret;
    }

    private Map<UUID, BigInteger> getSorted(Map<UUID, BigInteger> map, int limit) {
        return map.entrySet().stream().sorted((value1, value2) -> value2.getValue().compareTo(value1.getValue())).limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (value1, value2) -> value1, LinkedHashMap::new));
    }

    public DataCache getCache() {
        return dataCache;
    }
}