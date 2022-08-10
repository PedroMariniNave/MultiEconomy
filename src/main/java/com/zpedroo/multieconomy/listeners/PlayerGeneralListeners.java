package com.zpedroo.multieconomy.listeners;

import com.zpedroo.multieconomy.enums.TransactionType;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.player.PlayerData;
import com.zpedroo.multieconomy.objects.player.Transaction;
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
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;

import static com.zpedroo.multieconomy.utils.number.NumberUtils.isInvalidValue;

public class PlayerGeneralListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (event.getItem() == null || event.getItem().getType().equals(Material.AIR)) return;

        ItemStack item = event.getItem().clone();
        NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey("CurrencyName")) return;

        event.setCancelled(true);

        Currency currency = DataManager.getInstance().getCache().getCurrencies().get(nbt.getString("CurrencyName"));
        if (currency == null) return;

        BigInteger amount = new BigInteger(nbt.getString("CurrencyAmount"));
        if (isInvalidValue(amount)) return;

        Player player = event.getPlayer();
        PlayerData data = DataManager.getInstance().getPlayerDataByUUID(player.getUniqueId());
        if (data == null) return;

        int amountToActivate = player.isSneaking() ? item.getAmount() : 1;
        item.setAmount(amountToActivate);
        player.getInventory().removeItem(item);

        BigInteger finalAmount = amount.multiply(BigInteger.valueOf(amountToActivate));
        data.addCurrencyAmount(currency, finalAmount);

        String[] titles = currency.getTitles().get("item-activated");
        String title = StringUtils.replace(titles[0], "{amount}", NumberFormatter.getInstance().format(finalAmount));
        String subtitle = StringUtils.replace(titles[1], "{amount}", NumberFormatter.getInstance().format(finalAmount));

        player.sendTitle(title, subtitle);
        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.5f, 10f);

        int id = FileUtils.get().getInt(FileUtils.Files.IDS, "IDs." + currency.getFileName()) + 1;
        Transaction transaction = new Transaction(player, null, finalAmount, currency, TransactionType.DEPOSIT, System.currentTimeMillis(), id);
        transaction.register(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        DataManager.getInstance().savePlayerData(event.getPlayer().getUniqueId());
    }
}