package me.jackcw.jcore;

import me.jackcw.jcore.database.Database;
import me.jackcw.jcore.database.MigrationManager;
import me.jackcw.jcore.database.SQLiteDatabase;
import me.jackcw.jcore.task.TaskManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class JCore
{
    private final JavaPlugin plugin;
    private final TaskManager taskManager;
    private final Database database;
    private final MigrationManager migrationManager;

    private boolean initialized;

    private JCore(JavaPlugin plugin)
    {
        this.plugin = plugin;
        this.taskManager = new TaskManager(plugin);
        this.database = new SQLiteDatabase(
                plugin,
                taskManager,
                "database.db"
        );
        this.migrationManager = new MigrationManager(database);
    }

    public static JCore create(JavaPlugin plugin)
    {
        if (plugin == null)
            throw new IllegalArgumentException(
                    "Plugin cannot be null"
            );

        return new JCore(plugin);
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
