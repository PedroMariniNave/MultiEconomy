package com.zpedroo.multieconomy.hooks;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.Currency;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import java.math.BigDecimal;
import java.util.List;

public class VaultEconomy implements Economy {

    private Plugin plugin;
    private Currency currency;

    public VaultEconomy(Plugin plugin, String currencyName) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> this.currency = CurrencyAPI.getCurrency(currencyName), 4L);
        plugin.getServer().getServicesManager().register(Economy.class, this, plugin, ServicePriority.Lowest);
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return plugin.getName();
    }

    @Override
    public String format(double value) {
        return NumberFormatter.getInstance().format(BigDecimal.valueOf(value).toBigInteger());
    }

    @Override
    public String currencyNamePlural() {
        return currency.getFileName();
    }

    @Override
    public String currencyNameSingular() {
        return currency.getFileName();
    }

    @Override
    public boolean hasAccount(String playerName) {
        return DataManager.getInstance().hasAccount(Bukkit.getOfflinePlayer(playerName).getUniqueId());
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return this.hasAccount(offlinePlayer.getName());
    }

    @Override
    public boolean hasAccount(String playerName, String arg1) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String arg1) {
        return hasAccount(offlinePlayer.getName());
    }

    @Override
    public double getBalance(String playerName) {
        return CurrencyAPI.getCurrencyAmount(Bukkit.getOfflinePlayer(playerName), currency).doubleValue();
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return getBalance(offlinePlayer.getName());
    }

    @Override
    public double getBalance(String playerName, String arg1) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String arg1) {
        return this.getBalance(offlinePlayer.getName());
    }

    @Override
    public boolean has(String playerName, double value) {
        return CurrencyAPI.getCurrencyAmount(Bukkit.getOfflinePlayer(playerName), currency).doubleValue() >= value;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double value) {
        return has(offlinePlayer.getName(), value);
    }

    @Override
    public boolean has(String playerName, String arg1, double value) {
        return has(playerName, value);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String arg1, double value) {
        return has(offlinePlayer.getName(), value);
    }

    @Override
    public EconomyResponse withdrawPlayer(String name, double value) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        CurrencyAPI.removeCurrencyAmount(player, currency, BigDecimal.valueOf(value).toBigInteger());

        return new EconomyResponse(value, CurrencyAPI.getCurrencyAmount(player, currency).doubleValue(), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double value) {
        return withdrawPlayer(offlinePlayer.getName(), value);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String arg1, double value) {
        return withdrawPlayer(playerName, value);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String arg1, double value) {
        return withdrawPlayer(offlinePlayer.getName(), value);
    }

    @Override
    public EconomyResponse depositPlayer(String name, double value) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        CurrencyAPI.addCurrencyAmount(player, currency, BigDecimal.valueOf(value).toBigInteger());

        return new EconomyResponse(value, CurrencyAPI.getCurrencyAmount(player, currency).doubleValue(), EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double value) {
        return depositPlayer(offlinePlayer.getName(), value);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String arg1, double value) {
        return depositPlayer(playerName, value);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String arg1, double value) {
        return depositPlayer(offlinePlayer.getName(), value);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String name) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return true;
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }
}