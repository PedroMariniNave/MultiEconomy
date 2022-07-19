package com.zpedroo.multieconomy.tasks;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.managers.cache.DataCache;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.utils.config.Messages;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigInteger;
import java.util.UUID;

import static com.zpedroo.multieconomy.utils.config.Settings.DATA_UPDATE_INTERVAL_IN_SECONDS;

public class TopUpdateTask extends BukkitRunnable {

    private final DataCache dataCache = DataManager.getInstance().getCache();

    public TopUpdateTask(Plugin plugin) {
        this.runTaskTimerAsynchronously(plugin, DATA_UPDATE_INTERVAL_IN_SECONDS * 20L, DATA_UPDATE_INTERVAL_IN_SECONDS * 20L);
    }

    @Override
    public void run() {
        for (Currency currency : dataCache.getCurrencies().values()) {
            if (currency.getTopOneTag().isEmpty()) continue; // disabled

            UUID newTopOneUniqueId = currency.getTopOneUniqueId();
            UUID oldCachedTopOneUniqueId = dataCache.getTopsOne().get(currency);
            if (isSamePlayer(newTopOneUniqueId, oldCachedTopOneUniqueId)) continue;

            dataCache.getTopsOne().put(currency, newTopOneUniqueId);
            if (oldCachedTopOneUniqueId == null) continue;

            OfflinePlayer newTopPlayer = Bukkit.getOfflinePlayer(newTopOneUniqueId);
            BigInteger currencyAmount = CurrencyAPI.getCurrencyAmount(newTopOneUniqueId, currency);

            for (String msg : Messages.NEW_TOP_ONE) {
                Bukkit.broadcastMessage(StringUtils.replaceEach(msg, new String[]{
                        "{player}",
                        "{tag}",
                        "{amount}",
                        "{amount_display}"
                }, new String[]{
                        newTopPlayer.getName(),
                        currency.getTopOneTag(),
                        NumberFormatter.getInstance().format(currencyAmount),
                        currency.getAmountDisplay(currencyAmount)
                }));
            }

            playThunder(newTopPlayer);
        }
    }

    private void playThunder(OfflinePlayer newTopPlayer) {
        // double sound = extra thunder effect
        if (newTopPlayer.isOnline()) {
            newTopPlayer.getPlayer().getWorld().strikeLightningEffect(newTopPlayer.getPlayer().getLocation());
            newTopPlayer.getPlayer().getWorld().strikeLightningEffect(newTopPlayer.getPlayer().getLocation());
        } else {
            Bukkit.getOnlinePlayers().forEach(players -> {
                players.playSound(players.getLocation(), Sound.AMBIENCE_THUNDER, 1f, 1f);
                players.playSound(players.getLocation(), Sound.AMBIENCE_THUNDER, 1f, 1f);
            });
        }
    }

    private boolean isSamePlayer(UUID firstUniqueId, UUID secondUniqueId) {
        if (firstUniqueId == null || secondUniqueId == null) return false;

        return firstUniqueId.equals(secondUniqueId);
    }
}