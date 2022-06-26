package com.zpedroo.multieconomy.tasks;

import com.zpedroo.multieconomy.managers.DataManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static com.zpedroo.multieconomy.utils.config.Settings.DATA_UPDATE_INTERVAL_IN_SECONDS;

public class SaveTask extends BukkitRunnable {

    public SaveTask(Plugin plugin) {
        this.runTaskTimerAsynchronously(plugin, DATA_UPDATE_INTERVAL_IN_SECONDS * 20L, DATA_UPDATE_INTERVAL_IN_SECONDS * 20L);
    }

    @Override
    public void run() {
        DataManager.getInstance().saveAllPlayersData();
        DataManager.getInstance().updateTopCurrencies();
    }
}