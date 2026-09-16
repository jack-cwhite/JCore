package me.jackcw.jcore.database;

import com.zaxxer.hikari.HikariConfig;
import me.jackcw.jcore.task.TaskManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class SQLiteDatabase extends AbstractDatabase
{
    private final File databaseFile;

    public SQLiteDatabase(JavaPlugin plugin, TaskManager taskManager, String fileName)
    {
        super(taskManager);

        if (plugin == null)
            throw new IllegalArgumentException("Plugin cannot be null");

        this.databaseFile = new File(plugin.getDataFolder(), fileName);
    }

    @Override
    protected void beforeConnect()
    {
        File parent = databaseFile.getParentFile();

        if (parent != null && !parent.exists() && !parent.mkdirs())
            throw new DatabaseException("Could not create database directory: " + parent);
    }

    @Override
    protected void configure(HikariConfig config)
    {
        config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");

        config.setMaximumPoolSize(1);
        config.setConnectionInitSql("PRAGMA journal_mode=WAL");

        config.setConnectionTimeout(5000);
    }

    @Override
    public DatabaseType getType()
    {
        return DatabaseType.SQLITE;
    }

    public File getDatabaseFile()
    {
        return databaseFile;
    }
}
