package com.zpedroo.multieconomy.utils.menu;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.listeners.ShopListeners;
import com.zpedroo.multieconomy.listeners.WithdrawListeners;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.category.Category;
import com.zpedroo.multieconomy.objects.category.CategoryItem;
import com.zpedroo.multieconomy.objects.category.Task;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.general.Purchase;
import com.zpedroo.multieconomy.objects.general.Shop;
import com.zpedroo.multieconomy.objects.player.PlayerData;
import com.zpedroo.multieconomy.objects.player.Transaction;
import com.zpedroo.multieconomy.utils.FileUtils;
import com.zpedroo.multieconomy.utils.builder.InventoryBuilder;
import com.zpedroo.multieconomy.utils.builder.InventoryUtils;
import com.zpedroo.multieconomy.utils.builder.ItemBuilder;
import com.zpedroo.multieconomy.utils.config.Messages;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import com.zpedroo.multieconomy.utils.formatter.TimeFormatter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.*;

public class Menus extends InventoryUtils {

    private static Menus instance;
    public static Menus getInstance() { return instance; }

    private final ItemStack nextPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Next-Page").build();
    private final ItemStack previousPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Previous-Page").build();

    public Menus() {
        instance = this;
    }

    public void openMainMenu(Player player, Currency currency) {
        FileUtils.Files file = FileUtils.Files.MAIN;

        String title = currency.getInventoryTitles().get(file.getName().toLowerCase());
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str, new String[]{
                    "{color}",
                    "{tag}",
                    "{player}",
                    "{amount}",
                    "{amount_display}",
            }, new String[]{
                    currency.getCurrencyColor(),
                    currency.isTopOne(player) ? currency.getTopOneTag() + " " : "",
                    player.getName(),
                    NumberFormatter.getInstance().format(CurrencyAPI.getCurrencyAmount(player, currency)),
                    currency.getAmountDisplay(CurrencyAPI.getCurrencyAmount(player, currency))
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");
            String action = FileUtils.get().getString(file, "Inventory.items." + str + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action) {
                    case "WITHDRAW":
                        player.closeInventory();
                        WithdrawListeners.getPlayersWithdrawing().put(player, currency);
                        for (int i = 0; i < 25; ++i) {
                            player.sendMessage("");
                        }

                        for (String msg : Messages.WITHDRAW) {
                            player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                                    "{tax}"
                            }, new String[]{
                                    String.valueOf(currency.getTaxPerTransaction())
                            }));
                        }
                        break;
                    case "TRANSACTIONS":
                        openTransactionsMenu(player, currency);
                        break;
                    case "TOP":
                        openTopMenu(player, currency);
                        break;
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openInfoMenu(Player player, OfflinePlayer target, Currency currency) {
        FileUtils.Files file = FileUtils.Files.INFO;

        String title = currency.getInventoryTitles().get(file.getName().toLowerCase());
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str, new String[]{
                    "{color}",
                    "{tag}",
                    "{player}",
                    "{amount}",
                    "{amount_display}"
            }, new String[]{
                    currency.getCurrencyColor(),
                    currency.isTopOne(target.getUniqueId()) ? currency.getTopOneTag() + " " : "",
                    target.getName(),
                    NumberFormatter.getInstance().format(CurrencyAPI.getCurrencyAmount(target, currency)),
                    currency.getAmountDisplay(CurrencyAPI.getCurrencyAmount(target, currency))
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");

            inventory.addItem(item, slot);
        }

        inventory.open(player);
    }

    public void openTransactionsMenu(Player player, Currency currency) {
        FileUtils.Files file = FileUtils.Files.TRANSACTIONS;

        String title = currency.getInventoryTitles().get(file.getName().toLowerCase());
        int size = FileUtils.get().getInt(file, "Inventory.size");

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);
        List<Transaction> transactions = CurrencyAPI.getPlayerCurrencyTransactions(player, currency);

        if (transactions.isEmpty()) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Empty").build();
            int slot = FileUtils.get().getInt(file, "Empty.slot");

            inventory.addItem(item, slot);
        } else {
            int i = -1;
            String[] slots = FileUtils.get().getString(file, "Inventory.slots").replace(" ", "").split(",");
            for (Transaction transaction : transactions) {
                if (++i >= slots.length) i = 0;

                String type = transaction.getType().toString().toLowerCase();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + type, new String[]{
                        "{color}",
                        "{actor}",
                        "{target}",
                        "{amount}",
                        "{amount_display}",
                        "{date}",
                        "{id}"
                }, new String[]{
                        currency.getCurrencyColor(),
                        transaction.getActor().getName(),
                        transaction.getTarget() == null ? "Ninguém" : transaction.getTarget().getName(),
                        NumberFormatter.getInstance().format(transaction.getAmount()),
                        currency.getAmountDisplay(transaction.getAmount()),
                        dateFormat.format(transaction.getCreationTimestamp()),
                        String.valueOf(transaction.getId())
                }).build();
                int slot = Integer.parseInt(slots[i]);

                inventory.addItem(item, slot);
            }
        }

        inventory.open(player);
    }

    public void openShopMenu(Player player, Shop shop) {
        FileConfiguration file = shop.getFile();

        String title = ChatColor.translateAlternateColorCodes('&', file.getString("Inventory.title"));
        int size = file.getInt("Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        Collection<Currency> currencies = DataManager.getInstance().getCache().getCurrencies().values();

        List<String> placeholders = new LinkedList<>();
        List<String> replacers = new LinkedList<>();

        for (Currency currency : currencies) {
            placeholders.add("{currency:" + currency.getFileName() + "}");
            replacers.add(currency.getAmountDisplay(CurrencyAPI.getCurrencyAmount(player, currency)));
        }

        for (String str : file.getConfigurationSection("Inventory.items").getKeys(false)) {
            ItemStack item = ItemBuilder.build(file, "Inventory.items." + str,
                    placeholders.toArray(new String[0]),
                    replacers.toArray(new String[0])).build();
            int slot = file.getInt("Inventory.items." + str + ".slot");
            String action = file.getString("Inventory.items." + str + ".action", "NULL");

            inventory.addItem(item, slot, () -> {
                if (StringUtils.contains(action, ":")) {
                    String[] split = action.split(":");
                    String command = split.length > 1 ? split[1] : null;
                    if (command == null) return;

                    switch (split[0].toUpperCase()) {
                        case "OPEN":
                            Category toOpen = DataManager.getInstance().getCache().getCategories().get(split[1]);
                            if (toOpen != null) openCategoryMenu(player, toOpen);
                            break;
                        case "PLAYER":
                            player.chat("/" + command);
                            player.closeInventory();
                            break;

                        case "CONSOLE":
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(command, new String[]{
                                    "{player}"
                            }, new String[]{
                                    player.getName()
                            }));
                            player.closeInventory();
                            break;
                    }
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openCategoryMenu(Player player, Category category) {
        FileConfiguration file = category.getFileConfiguration();

        String title = ChatColor.translateAlternateColorCodes('&', file.getString("Inventory.title"));
        int size = file.getInt("Inventory.size");

        int nextPageSlot = file.getInt("Inventory.next-page-slot");
        int previousPageSlot = file.getInt("Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);

        int i = -1;
        String[] slots = file.getString("Inventory.item-slots").replace(" ", "").split(",");
        for (CategoryItem item : category.getItems()) {
            if (item == null) continue;
            if (++i >= slots.length) i = 0;

            Task task = category.getTask();
            ItemStack displayItem = item.getDisplayItemWithPlaceholdersReplaced(new String[]{
                    "{price}",
                    "{stock_amount}",
                    "{max_stock}",
                    "{next_restock}"
            }, new String[]{
                    item.getCurrency().getAmountDisplay(item.getPrice()),
                    String.valueOf(item.getStockAmount()),
                    String.valueOf(item.getMaxStock()),
                    task == null ? "-/-" : TimeFormatter.millisToFormattedTime(task.getNextFireTimeInMillis() - System.currentTimeMillis())
            });
            int slot = Integer.parseInt(slots[i]);

            Runnable action = () -> {
                if (item.getMaxStock().signum() > 0 && item.getStockAmount().signum() <= 0) {
                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
                    return;
                }

                player.closeInventory();
                ShopListeners.getPlayersBuying().put(player, new Purchase(player, item));
                for (String msg : Messages.CHOOSE_AMOUNT) {
                    player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                            "{item}",
                            "{price}"
                    }, new String[]{
                            item.getDisplayItem().hasItemMeta() ? item.getDisplayItem().getItemMeta().hasDisplayName() ? item.getDisplayItem().getItemMeta().getDisplayName() : item.getDisplayItem().getType().toString() : item.getDisplayItem().getType().toString(),
                            item.getCurrency().getAmountDisplay(item.getPrice())
                    }));
                }
            };

            if (item.isFixedItem()) {
                inventory.addDefaultItem(displayItem, slot, action, ActionType.ALL_CLICKS);
            } else {
                inventory.addItem(displayItem, slot, action, ActionType.ALL_CLICKS);
            }
        }

        if (file.contains("Inventory.displays")) {
            for (String display : file.getConfigurationSection("Inventory.displays").getKeys(false)) {
                if (display == null) continue;

                ItemStack item = ItemBuilder.build(file, "Inventory.displays." + display).build();
                int slot = file.getInt("Inventory.displays." + display + ".slot");
                String action = file.getString("Inventory.displays." + display + ".action", "NULL");

                inventory.addDefaultItem(item, slot, () -> {
                    if (StringUtils.contains(action, ":")) {
                        String[] split = action.split(":");
                        String command = split.length > 1 ? split[1] : null;
                        if (command == null) return;

                        switch (split[0].toUpperCase()) {
                            case "CATEGORY":
                                Category categoryToOpen = DataManager.getInstance().getCache().getCategories().get(split[1]);
                                if (categoryToOpen != null) openCategoryMenu(player, categoryToOpen);
                                break;
                            case "SHOP":
                                Shop shopToOpen = DataManager.getInstance().getCache().getShops().get(split[1]);
                                if (shopToOpen != null) openShopMenu(player, shopToOpen);
                                break;
                            case "PLAYER":
                                player.chat("/" + command);
                                break;
                            case "CONSOLE":
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(command, new String[]{
                                        "{player}"
                                }, new String[]{
                                        player.getName()
                                }));
                                break;
                        }
                    }
                }, ActionType.ALL_CLICKS);
            }
        }

        inventory.open(player);
    }

    public void openTopMenu(Player player, Currency currency) {
        FileUtils.Files file = FileUtils.Files.TOP;

        String title = currency.getInventoryTitles().get(file.getName().toLowerCase());
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        String[] topSlots = FileUtils.get().getString(file, "Inventory.slots").replace(" ", "").split(",");
        List<PlayerData> topCurrency = DataManager.getInstance().getCache().getTopCurrencies().get(currency);
        int pos = 0;

        for (PlayerData data : topCurrency) {
            int slot = Integer.parseInt(topSlots[pos]);
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Item", new String[]{
                    "{color}",
                    "{tag}",
                    "{player}",
                    "{pos}",
                    "{amount}",
                    "{amount_display}",
            }, new String[]{
                    currency.getCurrencyColor(),
                    currency.isTopOne(data.getUUID()) ? currency.getTopOneTag() + " " : "",
                    Bukkit.getOfflinePlayer(data.getUUID()).getName(),
                    String.valueOf(++pos),
                    NumberFormatter.getInstance().format(data.getCurrencyAmount(currency)),
                    currency.getAmountDisplay(data.getCurrencyAmount(currency))
            }).build();

            inventory.addItem(item, slot);
        }

        inventory.open(player);
    }
}