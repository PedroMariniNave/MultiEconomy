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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Menus {

    private static Menus instance;
    public static Menus getInstance() { return instance; }

    private InventoryUtils inventoryUtils;

    private ItemStack nextPageItem;
    private ItemStack previousPageItem;

    public Menus() {
        instance = this;
        this.inventoryUtils = new InventoryUtils();
        this.nextPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Next-Page").build();
        this.previousPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Previous-Page").build();
    }

    public void openMainMenu(Player player, Currency currency) {
        FileUtils.Files file = FileUtils.Files.MAIN;

        int size = FileUtils.get().getInt(file, "Inventory.size");
        String title = currency.getInventoryTitles().get(file.getName().toLowerCase());

        Inventory inventory = Bukkit.createInventory(null, size, title);

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

            switch (action) {
                case "WITHDRAW":
                    inventoryUtils.addAction(inventory, item, () -> {
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
                    }, InventoryUtils.ActionType.ALL_CLICKS);
                    break;
                case "TRANSACTIONS":
                    inventoryUtils.addAction(inventory, item, () -> {
                        openTransactionsMenu(player, currency);
                    }, InventoryUtils.ActionType.ALL_CLICKS);
                    break;
                case "TOP":
                    inventoryUtils.addAction(inventory, item, () -> {
                        openTopMenu(player, currency);
                    }, InventoryUtils.ActionType.ALL_CLICKS);
                    break;
            }

            inventory.setItem(slot, item);
        }

        player.openInventory(inventory);
    }

    public void openInfoMenu(Player player, OfflinePlayer target, Currency currency) {
        FileUtils.Files file = FileUtils.Files.INFO;

        int size = FileUtils.get().getInt(file, "Inventory.size");
        String title = currency.getInventoryTitles().get(file.getName().toLowerCase());

        Inventory inventory = Bukkit.createInventory(null, size, title);

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

            inventoryUtils.addAction(inventory, item, null, InventoryUtils.ActionType.ALL_CLICKS);

            inventory.setItem(slot, item);
        }

        player.openInventory(inventory);
    }

    public void openTransactionsMenu(Player player, Currency currency) {
        FileUtils.Files file = FileUtils.Files.TRANSACTIONS;

        int size = FileUtils.get().getInt(file, "Inventory.size");
        String title = currency.getInventoryTitles().get(file.getName().toLowerCase());

        Inventory inventory = Bukkit.createInventory(null, size, title);
        List<Transaction> transactions = CurrencyAPI.getCurrencyTransactions(player, currency);
        List<ItemBuilder> builders = new ArrayList<>(transactions.size());

        if (transactions.isEmpty()) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Empty").build();
            int slot = FileUtils.get().getInt(file, "Empty.slot");

            builders.add(ItemBuilder.build(item, slot, null));
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

                builders.add(ItemBuilder.build(item, slot, null));
            }
        }

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder.build(player, inventory, title, builders, nextPageSlot, previousPageSlot, nextPageItem, previousPageItem);
    }

    public void openShopMenu(Player player, Shop shop) {
        FileConfiguration file = shop.getFile();

        int size = file.getInt("Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', file.getString("Inventory.title"));

        Inventory inventory = Bukkit.createInventory(null, size, title);

        Collection<Currency> currencies = DataManager.getInstance().getCache().getCurrencies().values();

        List<String> placeholders = new LinkedList<>();
        List<String> replaces = new LinkedList<>();

        for (Currency currency : currencies) {
            placeholders.add("{currency:" + currency.getFileName() + "}");
            replaces.add(StringUtils.replaceEach(currency.getAmountDisplay(),
                    new String[] { "{amount}" },
                    new String[]{ NumberFormatter.getInstance().format(CurrencyAPI.getCurrencyAmount(player, currency)) }));
        }

        for (String str :file.getConfigurationSection("Inventory.items").getKeys(false)) {
            ItemStack item = ItemBuilder.build(file, "Inventory.items." + str,
                    placeholders.toArray(new String[placeholders.size()]),
                    replaces.toArray(new String[replaces.size()])).build();
            int slot = file.getInt("Inventory.items." + str + ".slot");
            String action = file.getString("Inventory.items." + str + ".action", "NULL");

            if (StringUtils.contains(action, ":")) {
                String[] split = action.split(":");
                String command = split.length > 1 ? split[1] : null;
                if (command == null) continue;

                switch (split[0].toUpperCase()) {
                    case "OPEN" -> inventoryUtils.addAction(inventory, item, () -> {
                        Category toOpen = DataManager.getInstance().getCache().getCategories().get(split[1]);

                        if (toOpen != null) openCategoryMenu(player, toOpen);
                    }, InventoryUtils.ActionType.ALL_CLICKS);

                    case "PLAYER" -> inventoryUtils.addAction(inventory, item, () -> {
                        player.chat("/" + command);
                        player.closeInventory();
                    }, InventoryUtils.ActionType.ALL_CLICKS);

                    case "CONSOLE" -> inventoryUtils.addAction(inventory, item, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(command, new String[]{
                                "{player}"
                        }, new String[]{
                                player.getName()
                        }));
                        player.closeInventory();
                    }, InventoryUtils.ActionType.ALL_CLICKS);
                }
            }

            inventory.setItem(slot, item);
        }

        player.openInventory(inventory);
    }

    public void openCategoryMenu(Player player, Category category) {
        FileConfiguration file = category.getFile();

        int size = file.getInt("Inventory.size");
        String title = ChatColor.translateAlternateColorCodes('&', file.getString("Inventory.title"));

        Inventory inventory = Bukkit.createInventory(null, size, title);

        int i = -1;
        String[] slots = file.getString("Inventory.item-slots").replace(" ", "").split(",");
        List<ItemBuilder> builders = new ArrayList<>(64);
        for (CategoryItem item : category.getItems()) {
            if (item == null) continue;
            if (++i >= slots.length) i = 0;

            ItemStack display = item.getDisplay().clone();
            int slot = Integer.parseInt(slots[i]);
            List<InventoryUtils.Action> actions = new ArrayList<>();

            actions.add(new InventoryUtils.Action(InventoryUtils.ActionType.ALL_CLICKS, display, () -> {
                player.closeInventory();
                ShopListeners.getPlayerChat().put(player, new ShopListeners.PlayerChat(player, item));
                for (String msg : Messages.CHOOSE_AMOUNT) {
                    if (msg == null) continue;

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
            }));

            builders.add(ItemBuilder.build(display, slot, actions));
        }

        if (file.contains("Inventory.displays")) {
            for (String display : file.getConfigurationSection("Inventory.displays").getKeys(false)) {
                if (display == null) continue;

                ItemStack item = ItemBuilder.build(file, "Inventory.displays." + display).build();
                int slot = file.getInt("Inventory.displays." + display + ".slot");
                String action = file.getString("Inventory.displays." + display + ".action", "NULL");

                if (StringUtils.contains(action, ":")) {
                    String[] split = action.split(":");
                    String command = split.length > 1 ? split[1] : null;
                    if (command == null) continue;

                    switch (split[0].toUpperCase()) {
                        case "CATEGORY" -> inventoryUtils.addAction(inventory, item, () -> {
                            Category toOpen = DataManager.getInstance().getCache().getCategories().get(split[1]);

                            if (toOpen != null) openCategoryMenu(player, toOpen);
                        }, InventoryUtils.ActionType.ALL_CLICKS);

                        case "SHOP" -> inventoryUtils.addAction(inventory, item, () -> {
                            Shop toOpen = DataManager.getInstance().getCache().getShops().get(split[1]);

                            if (toOpen != null) openShopMenu(player, toOpen);
                        }, InventoryUtils.ActionType.ALL_CLICKS);

                        case "PLAYER" -> inventoryUtils.addAction(inventory, item, () -> {
                            player.chat("/" + command);
                        }, InventoryUtils.ActionType.ALL_CLICKS);

                        case "CONSOLE" -> inventoryUtils.addAction(inventory, item, () -> {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(command, new String[]{
                                    "{player}"
                            }, new String[]{
                                    player.getName()
                            }));
                        }, InventoryUtils.ActionType.ALL_CLICKS);
                    }
                }

                inventory.setItem(slot, item);
            }
        }

        int nextPageSlot = file.getInt("Inventory.next-page-slot");
        int previousPageSlot = file.getInt("Inventory.previous-page-slot");

        InventoryBuilder.build(player, inventory, title, builders, nextPageSlot, previousPageSlot, nextPageItem, previousPageItem);
    }

    public void openTopMenu(Player player, Currency currency) {
        FileUtils.Files file = FileUtils.Files.TOP;

        int size = FileUtils.get().getInt(file, "Inventory.size");
        String title = currency.getInventoryTitles().get(file.getName().toLowerCase());

        Inventory inventory = Bukkit.createInventory(null, size, title);

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

            inventoryUtils.addAction(inventory, item, null, InventoryUtils.ActionType.ALL_CLICKS);

            inventory.setItem(slot, item);
        }

        player.openInventory(inventory);
    }
}