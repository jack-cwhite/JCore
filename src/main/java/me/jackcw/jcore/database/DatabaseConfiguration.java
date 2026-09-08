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

        switch (type)
        {
            case SQLITE:
                if (!(settings instanceof SQLiteConfiguration))
                    throw new IllegalArgumentException(
                            "SQLite database requires SQLiteConfiguration"
                    );
                break;
            case MYSQL, MARIADB:
                if (!(settings instanceof MySQLConfiguration))
                    throw new IllegalArgumentException(
                            "MySQL/MariaDB database required MySQLConfiguration"
                    );
            case POSTGRESQL:
                if (!(settings instanceof PostgreSQLConfiguration))
                    throw new IllegalArgumentException(
                            "PostgreSQL database requires PostgreSQLConfiguration"
                    );
                break;
        }

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
