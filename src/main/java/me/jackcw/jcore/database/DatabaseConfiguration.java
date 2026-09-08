package me.jackcw.jcore.database;

public final class DatabaseConfiguration
{
    private final DatabaseType type;
    private final Object settings;

    public DatabaseConfiguration(DatabaseType type, Object settings)
    {
        if (type == null)
            throw new IllegalArgumentException(
                    "Database type cannot be null"
            );

        if (settings == null)
            throw new IllegalArgumentException(
                    "Database settings cannot be null"
            );

        this.type = type;
        this.settings = settings;
    }

    public DatabaseType getType()
    {
        return type;
    }

    public Object getSettings()
    {
        return settings;
    }
}
