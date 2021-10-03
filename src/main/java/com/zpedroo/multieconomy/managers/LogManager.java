package com.zpedroo.multieconomy.managers;

import com.zpedroo.multieconomy.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class LogManager {

    private static LogManager instance;
    public static LogManager getInstance() { return instance; }

    public LogManager() {
        instance = this;
    }

    public void addLog(String log) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FileUtils.get().getFile(FileUtils.Files.LOGS).getFile().getCanonicalPath(), true));
            writer.newLine();
            writer.write(log);
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}