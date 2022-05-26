package com.zpedroo.multieconomy.managers.cache;

import com.zpedroo.multieconomy.objects.category.Category;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.player.PlayerData;
import com.zpedroo.multieconomy.objects.general.Shop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataCache {

    private final Map<UUID, PlayerData> playersData = new HashMap<>(128);
    private final Map<String, Currency> currencies = new HashMap<>(4);
    private final Map<String, Category> categories = new HashMap<>(8);
    private final Map<String, Shop> shops = new HashMap<>(4);
    private Map<Currency, List<PlayerData>> topCurrencies;

    public Map<UUID, PlayerData> getPlayersData() {
        return playersData;
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

    public void setTopCurrencies(Map<Currency, List<PlayerData>> topCurrencies) {
        this.topCurrencies = topCurrencies;
    }
}