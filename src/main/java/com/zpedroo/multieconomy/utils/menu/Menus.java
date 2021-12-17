package com.zpedroo.multieconomy.utils.menu;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.listeners.ShopListeners;
import com.zpedroo.multieconomy.listeners.WithdrawListeners;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.*;
import com.zpedroo.multieconomy.utils.FileUtils;
import com.zpedroo.multieconomy.utils.builder.InventoryBuilder;
import com.zpedroo.multieconomy.utils.builder.InventoryUtils;
import com.zpedroo.multieconomy.utils.builder.ItemBuilder;
import com.zpedroo.multieconomy.utils.config.Messages;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Menus extends InventoryUtils {

    private static Menus instance;
    public static Menus getInstance() { return instance; }

    private ItemStack nextPageItem;
    private ItemStack previousPageItem;

    public Menus() {
        instance = this;
        this.nextPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Next-Page").build();
        this.previousPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Previous-Page").build();
    }

    public void openMainMenu(Player player, Currency currency) {
        FileUtils.Files file = FileUtils.Files.MAIN;

        String title = currency.getInventoryTitles().get(file.getName().toLowerCase());
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str, new String[]{
                    "{color}",
                    "{player}",
                    "{amount}",
                    "{amount_display}",
            }, new String[]{
                    currency.getColor(),
                    player.getName(),
                    NumberFormatter.getInstance().format(CurrencyAPI.getCurrencyAmount(player, currency)),
                    StringUtils.replaceEach(currency.getAmountDisplay(), new String[]{
                            "{amount}"
                    }, new String[]{
                            NumberFormatter.getInstance().format(CurrencyAPI.getCurrencyAmount(player, currency))
                    })
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");
            String action = FileUtils.get().getString(file, "Inventory.items." + str + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action) {
                    case "WITHDRAW":
                        player.closeInventory();
                        WithdrawListeners.getWithdrawing().put(player, currency);
                        for (int i = 0; i < 25; ++i) {
                            player.sendMessage("");
                        }

                        for (String msg : Messages.WITHDRAW) {
                            if (msg == null) continue;

                            player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                                    "{tax}"
                            }, new String[]{
                                    currency.getTaxPerTransaction().toString()
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
                    "{player}",
                    "{amount}",
                    "{amount_display}"
            }, new String[]{
                    currency.getColor(),
                    target.getName(),
                    NumberFormatter.getInstance().format(CurrencyAPI.getCurrencyAmount(target, currency)),
                    StringUtils.replaceEach(currency.getAmountDisplay(), new String[]{
                            "{amount}"
                    }, new String[]{
                            NumberFormatter.getInstance().format(CurrencyAPI.getCurrencyAmount(target, currency))
                    })
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
        List<Transaction> transactions = CurrencyAPI.getCurrencyTransactions(player, currency);

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
                        currency.getColor(),
                        transaction.getActor().getName(),
                        transaction.getTarget().getName(),
                        NumberFormatter.getInstance().format(transaction.getAmount()),
                        StringUtils.replaceEach(currency.getAmountDisplay(), new String[]{
                                "{amount}"
                        }, new String[]{
                                NumberFormatter.getInstance().format(transaction.getAmount())
                        }),
                        dateFormat.format(transaction.getCreationDateInMillis()),
                        transaction.getID().toString()
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
        List<String> replaces = new LinkedList<>();

        for (Currency currency : currencies) {
            placeholders.add("{currency:" + currency.getFileName() + "}");
            replaces.add(StringUtils.replaceEach(currency.getAmountDisplay(),
                    new String[] { "{amount}" },
                    new String[]{ NumberFormatter.getInstance().format(CurrencyAPI.getCurrencyAmount(player, currency)) }));
        }

        for (String str : file.getConfigurationSection("Inventory.items").getKeys(false)) {
            ItemStack item = ItemBuilder.build(file, "Inventory.items." + str,
                    placeholders.toArray(new String[placeholders.size()]),
                    replaces.toArray(new String[replaces.size()])).build();
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
        FileConfiguration file = category.getFile();

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

            ItemStack display = item.getDisplay().clone();
            int slot = Integer.parseInt(slots[i]);

            inventory.addItem(display, slot, () -> {
                player.closeInventory();
                ShopListeners.getPlayerChat().put(player, new ShopListeners.PlayerChat(player, item));
                for (String msg : Messages.CHOOSE_AMOUNT) {
                    player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                            "{item}",
                            "{price}"
                    }, new String[]{
                            item.getDisplay().hasItemMeta() ? item.getDisplay().getItemMeta().hasDisplayName() ? item.getDisplay().getItemMeta().getDisplayName() : item.getDisplay().getType().toString() : item.getDisplay().getType().toString(),
                            StringUtils.replaceEach(item.getCurrency().getAmountDisplay(), new String[]{
                                    "{amount}"
                            }, new String[]{
                                    NumberFormatter.getInstance().format(item.getPrice())
                            })
                    }));
                }
            }, ActionType.ALL_CLICKS);
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

        int pos = 0;

        List<PlayerData> topCurrency = DataManager.getInstance().getCache().getTop().get(currency);

        for (PlayerData data : topCurrency) {
            int slot = Integer.parseInt(topSlots[pos]);
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Item", new String[]{
                    "{color}",
                    "{player}",
                    "{pos}",
                    "{amount}",
                    "{amount_display}",
            }, new String[]{
                    currency.getColor(),
                    Bukkit.getOfflinePlayer(data.getUUID()).getName(),
                    String.valueOf(++pos),
                    NumberFormatter.getInstance().format(data.getCurrencyAmount(currency)),
                    StringUtils.replaceEach(currency.getAmountDisplay(), new String[]{
                            "{amount}"
                    }, new String[]{
                            NumberFormatter.getInstance().format(data.getCurrencyAmount(currency))
                    })
            }).build();

            inventory.addItem(item, slot);
        }

        inventory.open(player);
    }
}