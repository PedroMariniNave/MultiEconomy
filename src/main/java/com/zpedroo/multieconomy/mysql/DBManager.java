package com.zpedroo.multieconomy.mysql;

import com.zpedroo.multieconomy.managers.SerializationManager;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.objects.player.PlayerData;
import com.zpedroo.multieconomy.objects.player.Transaction;

import java.math.BigInteger;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DBManager extends SerializationManager {

    public void savePlayerData(PlayerData data) {
        executeUpdate("REPLACE INTO `" + DBConnection.TABLE + "` (`uuid`, `currencies`, `transactions`) VALUES " +
                "('" + data.getUniqueId().toString() + "', " +
                "'" + currencySerialization.serialize(data.getCurrencies()) + "', " +
                "'" + transactionSerialization.serialize(data.getTransactions()) + "');");
    }

    public PlayerData getPlayerData(UUID uuid) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "` WHERE `uuid`='" + uuid.toString() + "';";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            if (result.next()) {
                Map<Currency, BigInteger> currencies = currencySerialization.deserialize(result.getString(2));
                Map<Currency, List<Transaction>> transactions = transactionSerialization.deserialize(result.getString(3));

                return new PlayerData(uuid, currencies, transactions);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return new PlayerData(uuid, null, null);
    }

    public Map<UUID, PlayerData> getAllPlayersCurrencyData() {
        Map<UUID, PlayerData> data = new HashMap<>(1024);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT uuid, currencies FROM `" + DBConnection.TABLE + "`;";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                Map<Currency, BigInteger> currencies = currencySerialization.deserialize(result.getString(2));

                data.put(uuid, new PlayerData(uuid, currencies, null));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return data;
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