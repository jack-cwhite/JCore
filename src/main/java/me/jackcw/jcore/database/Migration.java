package me.jackcw.jcore.database;

public final class Migration
{
    private final int version;
    private final DatabaseMigration migration;

    public Migration(int version, DatabaseMigration migration)
    {
        if (version <= 0)
            throw new IllegalArgumentException(
                    "Migration version must be greater than 0"
            );

        if (migration == null)
            throw new IllegalArgumentException(
                    "Migration cannot be null"
            );

        this.version = version;
        this.migration = migration;
    }

    public int getVersion()
    {
        return version;
    }

    public DatabaseMigration getMigration()
    {
        return migration;
    }
}
