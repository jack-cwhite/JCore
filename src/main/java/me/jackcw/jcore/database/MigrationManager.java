package me.jackcw.jcore.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MigrationManager
{
    private static final String MIGRATION_TABLE = "jcore_migrations";

    private final Database database;
    private final List<Migration> migrations = new ArrayList<>();

    public MigrationManager(Database database)
    {
        this.database = database;
    }

    public void add(Migration migration)
    {
        if (migrations.stream()
                .anyMatch(existing ->
                        existing.getVersion() == migration.getVersion()))
        {
            throw new IllegalArgumentException(
                    "Migration version already exists: " +
                            migration.getVersion()
            );
        }

        migrations.add(migration);

        migrations.sort(
                Comparator.comparingInt(Migration::getVersion)
        );
    }

    public void migrate()
    {
        createMigrationTable();

        int currentVersion = getCurrentVersion();

        for (Migration migration : migrations)
        {
            if (migration.getVersion() <= getCurrentVersion())
                continue;

            applyMigration(migration);
            currentVersion = migration.getVersion();
        }
    }


    private void createMigrationTable()
    {
        database.useConnection(connection ->
        {
            try (Statement statement = connection.createStatement())
            {
                statement.executeUpdate(
                        """
                        CREATE TABLE IF NOT EXISTS jcore_migrations (
                            version INTEGER PRIMARY KEY,
                            applied_at INTEGER NOT NULL
                        )
                        """
                );
            }
            catch (SQLException e)
            {
                throw new DatabaseException(
                        "Could not create migration table",
                        e
                );
            }
        });
    }


    private int getCurrentVersion(Connection connection)
    {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT MAX(version) FROM jcore_migrations"
             ))
        {
            if (resultSet.next())
            {
                int version = resultSet.getInt(1);

                if (!resultSet.wasNull())
                    return version;
            }

            return 0;
        }
        catch (SQLException e)
        {
            throw new DatabaseException(
                    "Could not determine database version",
                    e
            );
        }
    }

    private void applyMigration(Migration migration)
    {
        database.transaction(connection ->
        {
            try
            {
                migration.getMigration().migrate(connection);

                try (var statement = connection.prepareStatement(
                        """
                        INSERT INTO jcore_migrations
                            (version, applied_at)
                        VALUES (?, ?)
                        """
                ))
                {
                    statement.setInt(
                            1,
                            migration.getVersion()
                    );

                    statement.setLong(
                            2,
                            System.currentTimeMillis()
                    );

                    statement.executeUpdate();
                }
            }
            catch (Exception e)
            {
                if (e instanceof DatabaseException databaseException)
                    throw databaseException;

                throw new DatabaseException(
                        "Could not apply migration " +
                                migration.getVersion(), e
                );
            }
        });
    }

    public int getCurrentVersion()
    {
        return database.withConnection(
                this::getCurrentVersion
        );
    }

    public List<Migration> getMigrations()
    {
        return List.copyOf(migrations);
    }
}
