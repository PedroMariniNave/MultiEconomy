package com.zpedroo.multieconomy.managers;

import com.zpedroo.multieconomy.MultiEconomy;
import com.zpedroo.multieconomy.managers.cache.DataCache;
import com.zpedroo.multieconomy.mysql.DBConnection;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.player.PlayerData;

import java.math.BigInteger;
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
            data = DBConnection.getInstance().getDBManager().loadData(uuid);
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

        DBConnection.getInstance().getDBManager().saveData(data);
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
        Map<UUID, PlayerData> allPlayersData = DBConnection.getInstance().getDBManager().getAllPlayersData();

        for (Currency currency : dataCache.getCurrencies().values()) {
            Map<UUID, BigInteger> values = new HashMap<>(allPlayersData.size());
            allPlayersData.values().forEach(data -> values.put(data.getUUID(), data.getCurrencyAmount(currency)));

            List<PlayerData> dataList = new LinkedList<>();
            getSorted(values).keySet().forEach(uuid -> dataList.add(DataManager.getInstance().getPlayerDataByUUID(uuid)));

            ret.put(currency, dataList);
        }

        return ret;
    }

    private Map<UUID, BigInteger> getSorted(Map<UUID, BigInteger> map) {
        return map.entrySet().stream().sorted((value1, value2) -> value2.getValue().compareTo(value1.getValue())).limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (value1, value2) -> value1, LinkedHashMap::new));
    }

    public DataCache getCache() {
        return dataCache;
    }
}