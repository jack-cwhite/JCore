package me.jackcw.jcore.database;

import me.jackcw.jcore.task.TaskManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class DatabaseFactory
{
    private DatabaseFactory()
    {
    }

    public static Database create(JavaPlugin plugin, TaskManager taskManager, DatabaseConfiguration configuration)
    {
        if (plugin == null)
            throw new IllegalArgumentException(
                    "Plugin cannot be null"
            );

        if (taskManager == null)
            throw new IllegalArgumentException(
                    "Task manager cannot be null"
            );

        if (configuration == null)
            throw new IllegalArgumentException(
                    "Database configuration cannot be null"
            );

        return switch(configuration.getType())
        {
            case SQLITE -> new SQLiteDatabase(plugin, taskManager, ((SQLiteConfiguration) configuration.getSettings()).getFileName());

            case MYSQL, MARIADB -> new MySQLDatabase(plugin, taskManager, (MySQLConfiguration) configuration.getSettings(), configuration.getType());

            case POSTGRESQL -> new PostgreSQLDatabase(plugin, taskManager, (PostgreSQLConfiguration) configuration.getSettings());
        };
    }
}
