package me.jackcw.jcore;

import me.jackcw.jcore.command.CommandManager;
import me.jackcw.jcore.database.*;
import me.jackcw.jcore.message.MessageManager;
import me.jackcw.jcore.serialization.InventorySerializer;
import me.jackcw.jcore.serialization.ItemStackSerializer;
import me.jackcw.jcore.serialization.LocationSerializer;
import me.jackcw.jcore.serialization.SerializerManager;
import me.jackcw.jcore.storage.FileManager;
import me.jackcw.jcore.storage.YamlFile;
import me.jackcw.jcore.task.TaskManager;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class JCore
{
    private static final String MESSAGES_FILE = "jcore-messages.yml";

    private final JavaPlugin plugin;
    private final TaskManager taskManager;
    private final FileManager fileManager;
    private final Database database;
    private final MigrationManager migrationManager;
    private final DatabaseConfiguration databaseConfiguration;
    private final SerializerManager serializerManager;
    private final MessageManager messageManager;
    private final CommandManager commandManager;

    private boolean initialized;

    private JCore(JavaPlugin plugin, DatabaseConfiguration databaseConfiguration)
    {
        this.plugin = plugin;
        this.taskManager = new TaskManager(plugin);
        this.database = DatabaseFactory.create(plugin, taskManager, databaseConfiguration);
        this.migrationManager = new MigrationManager(database);
        this.databaseConfiguration = databaseConfiguration;
        this.serializerManager = new SerializerManager();
        this.fileManager = new FileManager(plugin, serializers());

        serializerManager.register(ItemStack.class, new ItemStackSerializer());
        serializerManager.register(Location.class, new LocationSerializer());
        serializerManager.register(Inventory.class, new InventorySerializer());

        YamlFile messagesFile = fileManager.yaml(MESSAGES_FILE, true);
        messagesFile.updateDefaults();

        this.messageManager = new MessageManager(plugin, messagesFile);
        this.commandManager = new CommandManager(plugin, messageManager);
    }

    public static JCore create(JavaPlugin plugin)
    {
        return create(plugin, new DatabaseConfiguration(DatabaseType.SQLITE, new SQLiteConfiguration("database.db")));
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

    public FileManager files()
    {
        return fileManager;
    }

    public DatabaseConfiguration databaseConfiguration()
    {
        return databaseConfiguration;
    }

    public SerializerManager serializers()
    {
        return serializerManager;
    }

    public MessageManager messages()
    {
        return messageManager;
    }

    public CommandManager commands()
    {
        return commandManager;
    }

    public JavaPlugin plugin()
    {
        return plugin;
    }
}
