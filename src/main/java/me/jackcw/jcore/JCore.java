package me.jackcw.jcore;

import me.jackcw.jcore.command.CommandManager;
import me.jackcw.jcore.database.*;
import me.jackcw.jcore.menu.MenuManager;
import me.jackcw.jcore.message.MessageManager;
import me.jackcw.jcore.serialization.*;
import me.jackcw.jcore.storage.FileManager;
import me.jackcw.jcore.storage.YamlFile;
import me.jackcw.jcore.task.TaskManager;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class JCore
{
    private static final String MESSAGES_FILE = "messages.yml";
    private static final String CONFIG_FILE = "config.yml";

    // Merged into the consuming plugin's own messages.yml rather than shipped
    // as a bundled resource, so there's nothing for Maven Shade to collide
    // with when it merges JCore's resources into the consumer's jar - only
    // the consumer's own messages.yml exists at that path.
    private static final String CORE_MESSAGE_DEFAULTS = """
            core:
              prefix: ""
              no-permission: "&cYou do not have permission to use this command."
              incorrect-usage: "&cUsage: {usage}"
              player-only: "&cThis command can only be used by a player."
              player-not-found: "&cThat player could not be found."
            """;

    private final JavaPlugin plugin;
    private final TaskManager taskManager;
    private final FileManager fileManager;
    private final Database database;
    private final MigrationManager migrationManager;
    private final DatabaseConfiguration databaseConfiguration;
    private final SerializerManager serializerManager;
    private final MenuManager menuManager;
    private MessageManager messageManager;
    private CommandManager commandManager;
    private YamlFile configFile;

    private boolean initialized;
    private boolean databaseReady;

    private JCore(JavaPlugin plugin, DatabaseConfiguration databaseConfiguration)
    {
        this.plugin = plugin;
        this.taskManager = new TaskManager(plugin);
        this.database = DatabaseFactory.create(plugin, taskManager, databaseConfiguration);
        this.migrationManager = new MigrationManager(database, migrationTableName(plugin));
        this.databaseConfiguration = databaseConfiguration;
        this.serializerManager = new SerializerManager();
        this.fileManager = new FileManager(plugin, serializers());
        this.menuManager = new MenuManager(plugin, taskManager);

        serializerManager.register(ItemStack.class, new ItemStackSerializer());
        serializerManager.register(Location.class, new LocationSerializer());
        serializerManager.register(Inventory.class, new InventorySerializer());
        serializerManager.register(PlayerState.class, new PlayerStateSerializer(serializerManager));
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

        initialized = true;
    }

    public void shutdown()
    {
        if (!initialized)
            return;

        // Close menus first while Bukkit can still dispatch their close
        // callbacks. Editable menus may use those callbacks to restore a
        // player's real inventory.
        menuManager.shutdown();

        // Let queued database work finish while its connection pool is still
        // available, then close the pool.
        taskManager.shutdown();
        database.disconnect();
        databaseReady = false;

        initialized = false;
    }

    public boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Checks the database's connection state without triggering the lazy
     * connect that {@link #database()} would (useful for diagnostics/tests
     * that need to observe state without forcing a connection).
     */
    public boolean isDatabaseConnected()
    {
        return database.isConnected();
    }

    /**
     * Connects and runs migrations on first access, rather than JCore
     * eagerly connecting to a database every plugin never actually uses.
     */
    public Database database()
    {
        ensureDatabase();
        return database;
    }

    /**
     * Registers migrations only - deliberately does not connect. Consuming
     * plugins call this during their own onEnable to add every migration
     * they need before anything ever touches {@link #database()}; the first
     * real use of the database then connects and runs the full, by-then-
     * complete list. Triggering a connect here too would run migrate() with
     * whatever had been registered so far - on a plugin's very first ever
     * migrations() call that's nothing, so its own migration would silently
     * never apply.
     */
    public MigrationManager migrations()
    {
        return migrationManager;
    }

    private synchronized void ensureDatabase()
    {
        if (databaseReady)
            return;

        try
        {
            if (!database.isConnected())
                database.connect();

            migrationManager.migrate();
            databaseReady = true;
        }
        catch (RuntimeException e)
        {
            database.disconnect();
            throw e;
        }
    }

    private static String migrationTableName(JavaPlugin plugin)
    {
        String pluginName = plugin.getName().toLowerCase().replaceAll("[^a-z0-9_]", "_");

        if (pluginName.length() > 48)
            pluginName = pluginName.substring(0, 48);

        return pluginName + "_migrations";
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

    public MenuManager menus()
    {
        return menuManager;
    }

    public YamlFile config()
    {
        ensureConfig();
        return configFile;
    }

    private void ensureConfig()
    {
        if (configFile != null)
            return;

        configFile = fileManager.yaml(CONFIG_FILE, true);
        configFile.updateDefaults();
    }

    public MessageManager messages()
    {
        ensureMessages();
        return messageManager;
    }

    public CommandManager commands()
    {
        ensureMessages();
        return commandManager;
    }

    private void ensureMessages()
    {
        if (messageManager != null)
            return;

        YamlFile messagesFile = fileManager.yaml(MESSAGES_FILE, true);
        messagesFile.mergeDefaults(CORE_MESSAGE_DEFAULTS);

        messageManager = new MessageManager(plugin, messagesFile);
        commandManager = new CommandManager(plugin, messageManager);
    }

    public JavaPlugin plugin()
    {
        return plugin;
    }
}
