package com.zpedroo.multieconomy.managers.cache;

import com.zpedroo.multieconomy.objects.Currency;
import com.zpedroo.multieconomy.objects.PlayerData;
import com.zpedroo.multieconomy.objects.Category;
import com.zpedroo.multieconomy.objects.Shop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataCache {

    private Map<UUID, PlayerData> playerData;
    private Map<String, Currency> currencies;
    private Map<Currency, List<PlayerData>> topCurrencies;
    private Map<String, Category> categories;
    private Map<String, Shop> shops;

    public DataCache() {
        this.playerData = new HashMap<>(64);
        this.currencies = new HashMap<>(4);
        this.categories = new HashMap<>(8);
        this.shops = new HashMap<>(4);
    }

    public Map<UUID, PlayerData> getPlayerData() {
        return playerData;
    }

    public Map<Currency, List<PlayerData>> getTop() {
        return topCurrencies;
    }

    public Map<String, Currency> getCurrencies() {
        return currencies;
    }

    public Map<String, Category> getCategories() {
        return categories;
    }

    public Map<String, Shop> getShops() {
        return shops;
    }

    public void setPlayerData(Map<UUID, PlayerData> playerData) {
        this.playerData = playerData;
    }

    public void setTopCurrencies(Map<Currency, List<PlayerData>> topCurrencies) {
        this.topCurrencies = topCurrencies;
    }
}