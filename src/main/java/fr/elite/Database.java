package fr.elite;

import org.bukkit.entity.Player;

import java.sql.*;

public class Database {

    private final Connection connection;

    /**
     * Connects to the SQLite database and creates a new table for players if it doesn't exist.
     * @param path The file path to the SQLite database.
     * @throws SQLException If a database access error occurs or the URL is null.
     */
    public Database(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "username TEXT NOT NULL, " +
                    "totem_levels INTEGER NOT NULL DEFAULT 0, " +
                    "crops_speed_level INTEGER NOT NULL DEFAULT 0, " +
                    "crops_quantity_level INTEGER NOT NULL DEFAULT 0, " +
                    "ores_quantity_level INTEGER NOT NULL DEFAULT 0)");
        }
    }

    /**
     * Closes the database connection if it's open.
     * @throws SQLException If a database access error occurs.
     */
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Adds a new player to the database with default attribute values.
     * @param player The player to add.
     * @throws SQLException If a database access error occurs or the generated SQL statement is not valid.
     */
    public void addPlayer(Player player) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO players (uuid, username) VALUES (?, ?)")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.setString(2, player.getName());
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Checks if a player exists in the database.
     * @param player The player to check.
     * @return true if the player exists, false otherwise.
     * @throws SQLException If a database access error occurs or the generated SQL statement is not valid.
     */
    public boolean playerExists(Player player) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    /**
     * Updates the value of a specified attribute for a given player.
     * @param player The player whose attribute is to be updated.
     * @param attributeName The name of the attribute to update.
     * @param attributeValue The new value for the attribute.
     * @throws SQLException If a database access error occurs or the generated SQL statement is not valid.
     */
    public void updatePlayerAttribute(Player player, String attributeName, int attributeValue) throws SQLException {
        if (!playerExists(player)) {
            addPlayer(player);
        }

        String query = "UPDATE players SET " + attributeName + " = ? WHERE uuid = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, attributeValue);
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Retrieves the value of a specified attribute for a given player.
     * @param player The player whose attribute value is to be retrieved.
     * @param attributeName The name of the attribute to retrieve.
     * @return The value of the attribute, or 0 if the player does not exist or the attribute is not set.
     * @throws SQLException If a database access error occurs or the generated SQL statement is not valid.
     */
    public int getPlayerAttribute(Player player, String attributeName) throws SQLException {
        String query = "SELECT " + attributeName + " FROM players WHERE uuid = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(attributeName);
            } else {
                return 0; // Return 0 if the player has no value set for this attribute
            }
        }
    }

}
