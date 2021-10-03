package com.zpedroo.multieconomy.listeners;

import com.zpedroo.multieconomy.enums.TransactionType;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.Currency;
import com.zpedroo.multieconomy.objects.PlayerData;
import com.zpedroo.multieconomy.objects.Transaction;
import com.zpedroo.multieconomy.utils.FileUtils;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;

public class PlayerGeneralListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        Player player = event.getPlayer();
        ItemStack item = event.getHand() == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
        if (item.getType().equals(Material.AIR)) return;

        NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey("CurrencyName")) return;

        event.setCancelled(true);

        Currency currency = DataManager.getInstance().getCache().getCurrencies().get(nbt.getString("CurrencyName"));
        if (currency == null) return;

        BigInteger amount = new BigInteger(nbt.getString("CurrencyAmount"));
        if (amount.signum() <= 0) return;

        PlayerData data = DataManager.getInstance().load(player.getUniqueId());
        if (data == null) return;

        item.setAmount(item.getAmount() - 1);

        data.addCurrencyAmount(currency, amount);

        String[] titles = currency.getTitles().get("item-activated");
        String title = StringUtils.replaceEach(titles[0], new String[] { "{amount}" }, new String[] { NumberFormatter.getInstance().format(amount) });
        String subtitle = StringUtils.replaceEach(titles[1], new String[] { "{amount}" }, new String[] { NumberFormatter.getInstance().format(amount) });

        player.sendTitle(title, subtitle);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 10f);

        Integer id = FileUtils.get().getInt(FileUtils.Files.IDS, "IDs." + currency.getFileName()) + 1;
        data.addTransaction(currency, new Transaction(player, null, amount, TransactionType.DEPOSIT, System.currentTimeMillis(), id));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        DataManager.getInstance().save(event.getPlayer().getUniqueId());
    }
}