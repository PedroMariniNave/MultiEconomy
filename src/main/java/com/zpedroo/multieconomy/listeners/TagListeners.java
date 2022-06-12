package com.zpedroo.multieconomy.listeners;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TagListeners implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(ChatMessageEvent event) {
        Player player = event.getSender();
        for (Map.Entry<Currency, List<PlayerData>> entry : DataManager.getInstance().getCache().getTopCurrencies().entrySet()) {
            List<PlayerData> playersData = entry.getValue();
            if (playersData.isEmpty()) continue;

            UUID topOneUniqueId = playersData.stream().findFirst().get().getUUID();
            if (!player.getUniqueId().equals(topOneUniqueId)) continue;

            Currency currency = entry.getKey();
            event.setTagValue(currency.getCurrencyName(), currency.getTopOneTag());
        }
    }
}
