package me.jackcw.jcore;

import me.jackcw.jcore.database.*;
import me.jackcw.jcore.task.TaskManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class JCore
{
    private final JavaPlugin plugin;
    private final TaskManager taskManager;
    private final Database database;
    private final MigrationManager migrationManager;

    private boolean initialized;

    private JCore(JavaPlugin plugin, DatabaseConfiguration databaseConfiguration)
    {
        this.plugin = plugin;
        this.taskManager = new TaskManager(plugin);
        this.database = DatabaseFactory.create(plugin, taskManager, databaseConfiguration);
        this.migrationManager = new MigrationManager(database);
    }

    public static JCore create(JavaPlugin plugin)
    {
        return create(plugin, new DatabaseConfiguration(DatabaseType.SQLITE, "database.db"));
    }

    public static JCore create(JavaPlugin plugin, DatabaseConfiguration databaseConfiguration)
    {
        if (plugin == null)
            throw new IllegalArgumentException(
                    "Plugin cannot be null"
            );

        if (databaseConfiguration == null)
            throw new IllegalArgumentException(
                    "Database configuration cannot be null"
            );

        return new JCore(plugin, databaseConfiguration);
    }

    public void initialize()
    {
        if (initialized)
            return;

        database.connect();
        migrationManager.migrate();

        initialized = true;
    }

    public void shutdown()
    {
        if (!initialized)
            return;

        database.disconnect();
        taskManager.shutdown();

        initialized = false;
    }

    public boolean isInitialized()
    {
        return initialized;
    }

    public Database database()
    {
        return database;
    }

    public MigrationManager migrations()
    {
        return migrationManager;
    }

    public TaskManager tasks()
    {
        return taskManager;
    }

    public JavaPlugin plugin()
    {
        return plugin;
    }
}
