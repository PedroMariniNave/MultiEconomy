package com.zpedroo.multieconomy.managers;

import com.zpedroo.multieconomy.MultiEconomy;
import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.commands.CategoryCmd;
import com.zpedroo.multieconomy.commands.CurrencyCmd;
import com.zpedroo.multieconomy.commands.ShopCmd;
import com.zpedroo.multieconomy.commands.TopOneCmd;
import com.zpedroo.multieconomy.managers.cache.DataCache;
import com.zpedroo.multieconomy.mysql.DBConnection;
import com.zpedroo.multieconomy.objects.category.Category;
import com.zpedroo.multieconomy.objects.category.CategoryItem;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.general.Shop;
import com.zpedroo.multieconomy.objects.player.PlayerData;
import com.zpedroo.multieconomy.utils.builder.ItemBuilder;
import com.zpedroo.multieconomy.utils.config.Messages;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

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

    public PlayerData getPlayerDataByUUID(UUID uuid) {
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

    public void savePlayerData(UUID uuid) {
        PlayerData data = dataCache.getPlayersData().get(uuid);
        if (data == null) return;
        if (!data.isQueueUpdate()) return;

        DBConnection.getInstance().getDBManager().saveData(data);
        data.setUpdate(false);
    }

    public void saveAll() {
        new HashSet<>(dataCache.getPlayersData().keySet()).forEach(this::savePlayerData);
    }

    public void updateTopCurrencies() {
        dataCache.setTopCurrencies(getTopCurrencies());

        new BukkitRunnable() { // run a little later
            @Override
            public void run() {
                for (Currency currency : dataCache.getCurrencies().values()) {
                    if (currency.getTopOneTag().isEmpty()) continue; // disabled

                    UUID newTopOneUniqueId = currency.getTopOneUniqueId();
                    if (newTopOneUniqueId == null) continue;

                    UUID oldCachedTopOneUniqueId = dataCache.getTopsOne().get(currency);
                    if (oldCachedTopOneUniqueId != null && oldCachedTopOneUniqueId.equals(newTopOneUniqueId)) continue;

                    dataCache.getTopsOne().put(currency, newTopOneUniqueId);

                    if (oldCachedTopOneUniqueId == null) continue;

                    OfflinePlayer player = Bukkit.getOfflinePlayer(newTopOneUniqueId);
                    BigInteger currencyAmount = CurrencyAPI.getCurrencyAmount(newTopOneUniqueId, currency);

                    for (String msg : Messages.NEW_TOP_ONE) {
                        Bukkit.broadcastMessage(StringUtils.replaceEach(msg, new String[]{
                                "{player}",
                                "{tag}",
                                "{amount}",
                                "{amount_display}"
                        }, new String[]{
                                player.getName(),
                                currency.getTopOneTag(),
                                NumberFormatter.getInstance().format(currencyAmount),
                                currency.getAmountDisplay(currencyAmount)
                        }));
                    }

                    // double sound = extra thunder effect
                    if (player.isOnline()) {
                        player.getPlayer().getWorld().strikeLightningEffect(player.getPlayer().getLocation());
                        player.getPlayer().getWorld().strikeLightningEffect(player.getPlayer().getLocation());
                    } else {
                        Bukkit.getOnlinePlayers().forEach(players -> players.playSound(players.getLocation(), Sound.AMBIENCE_THUNDER, 1f, 1f));
                        Bukkit.getOnlinePlayers().forEach(players -> players.playSound(players.getLocation(), Sound.AMBIENCE_THUNDER, 1f, 1f));
                    }
                }
            }
        }.runTaskLater(MultiEconomy.get(), 20L);
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

            Shop shop = new Shop(file, openPermission, permissionMessage);
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
            String topOneTag = ChatColor.translateAlternateColorCodes('&', file.getString("Currency-Settings.top-one.tag", ""));
            String topOneCommand = file.getString("Currency-Settings.top-one.command", null);
            List<String> topOneAliases = file.getStringList("Currency-Settings.top-one.aliases");
            int taxPerTransaction = file.getInt("Currency-Settings.tax-per-transaction", 0);
            ItemStack item = ItemBuilder.build(file, "Item").build();

            Currency currency = new Currency(titles, inventoryTitles, commandKeys, fileName, currencyName, currencyDisplay, currencyColor, amountDisplay, topOneTag, taxPerTransaction, item);
            dataCache.getCurrencies().put(fileName, currency);

            MultiEconomy.get().registerCommand(command, aliases, permission, permissionMessage, new CurrencyCmd(currency));

            if (topOneCommand != null) {
                MultiEconomy.get().registerCommand(topOneCommand, topOneAliases, null, null, new TopOneCmd(currency));
            }
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

                CategoryItem categoryItem = new CategoryItem(permission, permissionMessage, currency, price, defaultAmount, display, itemToGive, requiredLevel, maxStock, inventoryLimit, fixedItem, commands);

                categoryItems.add(categoryItem);
            }

            Category category = new Category(fl, file, openPermission, openPermissionMessage, categoryItems);

            dataCache.getCategories().put(name, category);

            if (command == null || aliases == null || command.isEmpty()) continue;

            MultiEconomy.get().registerCommand(command, aliases, openPermission, openPermissionMessage, new CategoryCmd(category));
        }
    }

    protected Map<Currency, List<PlayerData>> getTopCurrencies() {
        Map<Currency, List<PlayerData>> ret = new HashMap<>(dataCache.getCurrencies().size() * 10);
        Map<UUID, PlayerData> allPlayersData = DBConnection.getInstance().getDBManager().getAllPlayersData();

        for (Currency currency : dataCache.getCurrencies().values()) {
            new Thread(() -> {
                Map<UUID, BigInteger> values = new HashMap<>(allPlayersData.size());
                allPlayersData.values().forEach(data -> values.put(data.getUUID(), data.getCurrencyAmount(currency)));

                List<PlayerData> dataList = new LinkedList<>();
                getSorted(values, 10).keySet().forEach(uuid -> dataList.add(DataManager.getInstance().getPlayerDataByUUID(uuid)));

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