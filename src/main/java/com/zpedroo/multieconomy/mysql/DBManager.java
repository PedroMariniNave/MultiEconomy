package com.zpedroo.multieconomy.mysql;

import com.zpedroo.multieconomy.enums.TransactionType;
import com.zpedroo.multieconomy.managers.DataManager;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.player.PlayerData;
import com.zpedroo.multieconomy.objects.player.Transaction;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class DBManager {

    public void saveData(PlayerData data) {
        executeUpdate("REPLACE INTO `" + DBConnection.TABLE + "` (`uuid`, `currencies`, `transactions`) VALUES " +
                "('" + data.getUUID().toString() + "', " +
                "'" + serializeCurrencies(data.getCurrencies()) + "', " +
                "'" + serializeTransactions(data.getTransactions()) + "');");
    }

    public PlayerData loadData(UUID uuid) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "` WHERE `uuid`='" + uuid.toString() + "';";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            if (result.next()) {
                Map<Currency, BigInteger> currencies = deserializeCurrencies(result.getString(2));
                Map<Currency, List<Transaction>> transactions = deserializeTransactions(result.getString(3));

                return new PlayerData(uuid, currencies, transactions);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return new PlayerData(uuid, null, null);
    }

    public Map<UUID, PlayerData> getAllPlayersData() {
        Map<UUID, PlayerData> data = new HashMap<>(1280);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "`;";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                Map<Currency, BigInteger> currencies = deserializeCurrencies(result.getString(2));
                Map<Currency, List<Transaction>> transactions = deserializeTransactions(result.getString(3));

                data.put(uuid, new PlayerData(uuid, currencies, transactions));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return data;
    }

    private String serializeCurrencies(Map<Currency, BigInteger> toSerialize) {
        StringBuilder builder = new StringBuilder(toSerialize.size());

        for (Map.Entry<Currency, BigInteger> entry : toSerialize.entrySet()) {
            Currency currency = entry.getKey();
            BigInteger amount = entry.getValue();

            builder.append(currency.getFileName()).append("#");
            builder.append(amount.toString()).append(",");
        }

        return builder.toString();
    }

    private Map<Currency, BigInteger> deserializeCurrencies(String serialized) {
        if (serialized == null || serialized.isEmpty()) return null;

        String[] currenciesSplit = serialized.split(",");

        Map<Currency, BigInteger> ret = new HashMap<>(currenciesSplit.length);

        for (String str : currenciesSplit) {
            String[] currencySplit = str.split("#");

            Currency currency = DataManager.getInstance().getCache().getCurrencies().get(currencySplit[0]);
            if (currency == null) continue;

            BigInteger amount = NumberFormatter.getInstance().filter(currencySplit[1]);
            if (amount.signum() <= 0) continue;

            ret.put(currency, amount);
        }

        return ret;
    }

    private String serializeTransactions(Map<Currency, List<Transaction>> toSerialize) {
        StringBuilder serialized = new StringBuilder(toSerialize.size());

        for (Map.Entry<Currency, List<Transaction>> entry : toSerialize.entrySet()) {
            Currency currency = entry.getKey();
            List<Transaction> transactions = entry.getValue();

            for (Transaction transaction : transactions) {
                serialized.append(currency.getFileName()).append("#");
                serialized.append(transaction.getActor().getName()).append("#");
                serialized.append(transaction.getTarget() == null ? "Ninguém" : transaction.getTarget().getName()).append("#");
                serialized.append(transaction.getAmount().toString()).append("#");
                serialized.append(transaction.getType().toString()).append("#");
                serialized.append(transaction.getCreationTimestamp()).append("#");
                serialized.append(transaction.getId()).append(",");
            }
        }

        return serialized.toString();
    }

    private Map<Currency, List<Transaction>> deserializeTransactions(String serialized) {
        if (serialized == null || serialized.isEmpty()) return null;

        String[] split = serialized.split(",");

        Map<Currency, List<Transaction>> ret = new HashMap<>(split.length);

        for (String str : split) {
            String[] strSplit = str.split("#");

            Currency currency = DataManager.getInstance().getCache().getCurrencies().get(strSplit[0]);
            OfflinePlayer actor = Bukkit.getOfflinePlayer(strSplit[1]);
            OfflinePlayer target = Bukkit.getOfflinePlayer(strSplit[2]);
            BigInteger amount = new BigInteger(strSplit[3]);
            TransactionType type = TransactionType.valueOf(strSplit[4]);
            long creationTimestamp = Long.parseLong(strSplit[5]);
            int id = Integer.parseInt(strSplit[6]);

            List<Transaction> transactions = ret.containsKey(currency) ? ret.get(currency) : new LinkedList<>();
            transactions.add(new Transaction(actor, target, amount, type, creationTimestamp, id));

            ret.put(currency, transactions);
        }

        return ret;
    }

    public boolean contains(String value, String column) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT `" + column + "` FROM `" + DBConnection.TABLE + "` WHERE `" + column + "`='" + value + "';";
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();
            return result.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return false;
    }

    private void executeUpdate(String query) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, null, null, statement);
        }
    }

    private void closeConnections(Connection connection, ResultSet resultSet, PreparedStatement preparedStatement, Statement statement) {
        try {
            if (connection != null) connection.close();
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void createTable() {
        executeUpdate("CREATE TABLE IF NOT EXISTS `" + DBConnection.TABLE + "` (`uuid` VARCHAR(255) NOT NULL, `currencies` LONGTEXT NOT NULL, `transactions` LONGTEXT NOT NULL, PRIMARY KEY(`uuid`));");
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }
}