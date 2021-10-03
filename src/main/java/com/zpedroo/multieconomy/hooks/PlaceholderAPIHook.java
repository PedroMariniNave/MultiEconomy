package com.zpedroo.multieconomy.hooks;

import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.Currency;
import com.zpedroo.multieconomy.objects.PlayerData;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private Plugin plugin;

    public PlaceholderAPIHook(Plugin plugin) {
        this.plugin = plugin;
        this.register();
    }

    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    public String getIdentifier() {
        return "economy";
    }

    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        Currency currency = DataManager.getInstance().getCache().getCurrencies().get(identifier);
        if (currency == null) return "0";

        PlayerData data = DataManager.getInstance().load(player.getUniqueId());

        return NumberFormatter.getInstance().format(data.getCurrencyAmount(currency));
    }

    private String format(Integer value) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        formatter.setDecimalFormatSymbols(symbols);

        return formatter.format(value);
    }
}