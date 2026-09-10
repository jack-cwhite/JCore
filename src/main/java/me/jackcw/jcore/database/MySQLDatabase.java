package me.jackcw.jcore.database;

import com.zaxxer.hikari.HikariConfig;
import me.jackcw.jcore.task.TaskManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MySQLDatabase extends AbstractDatabase
{
    private final MySQLConfiguration configuration;
    private final DatabaseType type;

    public MySQLDatabase(JavaPlugin plugin, TaskManager taskManager, MySQLConfiguration configuration, DatabaseType type)
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

        if (type != DatabaseType.MYSQL && type != DatabaseType.MARIADB)
            throw new IllegalArgumentException(
                    "MySQLDatabase requires MYSQL or MARIADB database type"
            );

        this.configuration = configuration;
        this.type = type;
    }

    @Override
    protected void configure(HikariConfig config)
    {
        String driver;
        String protocol;

        if (type == DatabaseType.MARIADB)
        {
            driver = "org.mariadb.jdbc.Driver";
            protocol = "mariadb";
        }
        else
        {
            driver = "com.mysql.cj.jdbc.Driver";
            protocol = "mysql";
        }

        config.setDriverClassName(driver);

        config.setJdbcUrl(
                "jdbc:" +
                        protocol +
                        "://" +
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
        return type;
    }
}
