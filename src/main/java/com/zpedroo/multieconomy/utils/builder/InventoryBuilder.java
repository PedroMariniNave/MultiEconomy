package com.zpedroo.multieconomy.utils.builder;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.zpedroo.multieconomy.utils.builder.InventoryUtils.*;

public class InventoryBuilder {

    private Inventory inventory;
    private String title;
    private Integer size;
    private ItemStack previousPageItem;
    private Integer previousPageSlot;
    private InventoryBuilder previousPageInventory;
    private ItemStack nextPageItem;
    private Integer nextPageSlot;
    private InventoryBuilder nextPageInventory;
    private Table<Integer, ItemStack, List<Action>> defaultItems;
    private Table<Integer, ActionType, Action> actions;

    public InventoryBuilder(String title, Integer size) {
        this(title, size, null, null, null, null, null, null);
    }

    public InventoryBuilder(String title, Integer size, ItemStack previousPageItem, Integer previousPageSlot, ItemStack nextPageItem, Integer nextPageSlot) {
        this(title, size, previousPageItem, previousPageSlot, null, nextPageItem, nextPageSlot, null);
    }

    public InventoryBuilder(String title, Integer size, ItemStack previousPageItem, Integer previousPageSlot, InventoryBuilder previousPageInventory, ItemStack nextPageItem, Integer nextPageSlot, Table<Integer, ItemStack, List<Action>> defaultItems) {
        this.inventory = Bukkit.createInventory(null, size, title);
        this.title = title;
        this.size = size;
        this.previousPageItem = previousPageItem;
        this.previousPageSlot = previousPageSlot;
        this.previousPageInventory = previousPageInventory;
        this.nextPageItem = nextPageItem;
        this.nextPageSlot = nextPageSlot;
        this.defaultItems = defaultItems;
        this.actions = HashBasedTable.create();
    }

    public void open(Player player) {
        if (previousPageInventory != null) {
            if (previousPageItem != null && previousPageSlot != null && inventory.getItem(previousPageSlot) == null) {
                addItem(previousPageItem, previousPageSlot, () -> {
                    previousPageInventory.open(player);
                }, ActionType.ALL_CLICKS);
            }
        }

        if (nextPageInventory != null) {
            if (nextPageItem != null && nextPageSlot != null && inventory.getItem(nextPageSlot) == null) {
                addItem(nextPageItem, nextPageSlot, () -> {
                    nextPageInventory.open(player);
                }, ActionType.ALL_CLICKS);
            }
        }

        if (defaultItems != null) {
            for (Integer slot : defaultItems.rowKeySet()) {
                if (inventory.getItem(slot) != null) continue;
                for (ItemStack item : defaultItems.row(slot).keySet()) {
                    List<Action> actions = defaultItems.get(slot, item);
                    addItem(item, slot);
                    actions.forEach(action -> addAction(slot, action.getAction(), action.getActionType()));
                }
            }
        }

        player.openInventory(inventory);
        InventoryUtils.get().getViewers().put(player, this);
    }

    public void close(Player player) {
        player.closeInventory();
    }

    public void addItem(@NotNull ItemStack item, @NotNull Integer slot) {
        addItem(item, slot, null, null);
    }

    public void addItem(@NotNull ItemStack item, @NotNull Integer slot, Runnable action, ActionType actionType) {
        if (inventory.getItem(slot) != null) {
            if (nextPageInventory == null) {
                if (nextPageItem == null || nextPageSlot == null) return;

                nextPageInventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, this, nextPageItem, nextPageSlot, defaultItems);
            }

            nextPageInventory.addItem(item, slot, action, actionType);
            return;
        }

        inventory.setItem(slot, item);

        if (action == null || actionType == null) return;

        addAction(slot, action, actionType);
    }

    public void addAction(@NotNull Integer slot, Runnable action, ActionType actionType) {
        actions.put(slot, actionType, new Action(action, actionType));
    }

    public void addDefaultItem(@NotNull ItemStack item, @NotNull Integer slot) {
        addDefaultItem(item, slot, null, null);
    }

    public void addDefaultItem(@NotNull ItemStack item, @NotNull Integer slot, Runnable action, ActionType actionType) {
        addDefaultAction(item, slot, action, actionType);
    }

    public void addDefaultAction(@NotNull ItemStack item, @NotNull Integer slot, Runnable action, ActionType actionType) {
        if (defaultItems == null) defaultItems = HashBasedTable.create();

        List<Action> defaultActions = defaultItems.contains(slot, item) ? defaultItems.get(slot, item) : new ArrayList<>(1);
        if (action != null && actionType != null) defaultActions.add(new Action(action, actionType));

        defaultItems.put(slot, item, defaultActions);

        if (nextPageInventory != null) nextPageInventory.addDefaultAction(item, slot, action, actionType);
    }

    protected Inventory getInventory() {
        return inventory;
    }

    protected Action getAction(@NotNull Integer slot, ActionType actionType) {
        return actions.get(slot, actionType);
    }
}