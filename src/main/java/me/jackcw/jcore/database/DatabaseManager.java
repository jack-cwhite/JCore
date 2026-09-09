package me.jackcw.jcore.database;

public final class DatabaseManager
{
    private final Database database;
    private final MigrationManager migrationManager;

    public DatabaseManager(Database database)
    {
        if (database == null)
            throw new IllegalArgumentException(
                    "Database cannot be null"
            );

        this.database = database;
        this.migrationManager = new MigrationManager(database);
    }

    public void connect()
    {
        database.connect();
    }

    public void disconnect()
    {
        database.disconnect();
    }

    public boolean isConnected()
    {
        return database.isConnected();
    }

    public Database getDatabase()
    {
        return database;
    }

    public MigrationManager getMigrationManager()
    {
        return migrationManager;
    }

    public void migrate()
    {
        migrationManager.migrate();
    }
}