package com.zpedroo.multieconomy.managers;

import com.zpedroo.multieconomy.MultiEconomy;
import com.zpedroo.multieconomy.managers.cache.DataCache;
import com.zpedroo.multieconomy.mysql.DBConnection;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.player.PlayerData;

import java.util.*;
import java.util.stream.Collectors;

public class DataManager {

    private static DataManager instance;
    public static DataManager getInstance() { return instance; }

    private final DataCache dataCache = new DataCache();

    public DataManager() {
        instance = this;
        MultiEconomy.get().getServer().getScheduler().runTaskLaterAsynchronously(MultiEconomy.get(), this::updateTopCurrencies, 0L);
    }

    public PlayerData getPlayerDataByUUID(UUID uuid) {
        PlayerData data = dataCache.getPlayersData().get(uuid);
        if (data == null) {
            data = DBConnection.getInstance().getDBManager().getPlayerData(uuid);
            dataCache.getPlayersData().put(uuid, data);
        }

        return data;
    }

    public boolean hasAccount(UUID uuid) {
        if (dataCache.getPlayersData().containsKey(uuid)) return true;

        return DBConnection.getInstance().getDBManager().contains(uuid.toString(), "uuid");
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = dataCache.getPlayersData().get(uuid);
        if (data == null || !data.isQueueUpdate()) return;

        DBConnection.getInstance().getDBManager().savePlayerData(data);
        data.setUpdate(false);
    }

    public void saveAllPlayersData() {
        dataCache.getPlayersData().keySet().forEach(this::savePlayerData);
    }

    public void updateTopCurrencies() {
        dataCache.setTopCurrencies(getTopCurrencies());
    }

    protected Map<Currency, List<PlayerData>> getTopCurrencies() {
        Map<Currency, List<PlayerData>> ret = new HashMap<>(dataCache.getCurrencies().size() * 10);
        Map<UUID, PlayerData> allPlayersData = DBConnection.getInstance().getDBManager().getAllPlayersCurrencyData();

        for (Currency currency : dataCache.getCurrencies().values()) {
            Comparator<PlayerData> comparator = Comparator.comparing((PlayerData data) -> data.getCurrencyAmount(currency)).reversed();
            List<PlayerData> topData = allPlayersData.values().stream().sorted(comparator).limit(10).collect(Collectors.toList());

            ret.put(currency, topData);
        }

        return ret;
    }

    public DataCache getCache() {
        return dataCache;
    }
}