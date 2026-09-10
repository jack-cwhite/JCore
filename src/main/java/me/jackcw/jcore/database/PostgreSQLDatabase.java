package me.jackcw.jcore.database;

import com.zaxxer.hikari.HikariConfig;
import me.jackcw.jcore.task.TaskManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PostgreSQLDatabase extends AbstractDatabase
{
    private final PostgreSQLConfiguration configuration;

    public PostgreSQLDatabase(JavaPlugin plugin, TaskManager taskManager, PostgreSQLConfiguration configuration)
    {
        super(taskManager);

        if (plugin == null)
            throw new IllegalArgumentException(
                    "Plugin cannot be null"
            );

        if (configuration == null)
            throw new IllegalArgumentException(
                    "Database configuration cannot be null"
            );

        this.configuration = configuration;
    }

    @Override
    protected void configure(HikariConfig config)
    {
        config.setDriverClassName("org.postgresql.Driver");

        config.setJdbcUrl(
                "jdbc:postgresql://" +
                        configuration.getHost() +
                        ":" +
                        configuration.getPort() +
                        "/" +
                        configuration.getDatabase()
        );

        config.setUsername(configuration.getUsername());
        config.setPassword(configuration.getPassword());
    }

    @Override
    public DatabaseType getType()
    {
        return DatabaseType.POSTGRESQL;
    }
}
