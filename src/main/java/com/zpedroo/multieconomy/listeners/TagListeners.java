package com.zpedroo.multieconomy.listeners;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.general.Currency;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;

public class TagListeners implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(ChatMessageEvent event) {
        Player player = event.getSender();
        for (Map.Entry<Currency, UUID> entry : DataManager.getInstance().getCache().getTopsOne().entrySet()) {
            UUID topOneUniqueId = entry.getValue();
            if (!player.getUniqueId().equals(topOneUniqueId)) continue;

            Currency currency = entry.getKey();
            event.setTagValue(currency.getCurrencyName(), currency.getTopOneTag());
        }
    }
}