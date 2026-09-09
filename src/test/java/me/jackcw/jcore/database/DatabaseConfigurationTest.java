package me.jackcw.jcore.database;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigurationTest
{
    @Test
    void acceptsSQLiteConfiguration()
    {
        SQLiteConfiguration settings =
                new SQLiteConfiguration("test.db");

        DatabaseConfiguration configuration =
                new DatabaseConfiguration(
                        DatabaseType.SQLITE,
                        settings
                );

        assertEquals(
                DatabaseType.SQLITE,
                configuration.getType()
        );

        assertSame(
                settings,
                configuration.getSettings()
        );
    }

    @Test
    void acceptsMySQLConfiguration()
    {
        MySQLConfiguration settings =
                new MySQLConfiguration(
                        "localhost",
                        3306,
                        "test",
                        "root",
                        "password"
                );

        DatabaseConfiguration configuration =
                new DatabaseConfiguration(
                        DatabaseType.MYSQL,
                        settings
                );

        assertEquals(
                DatabaseType.MYSQL,
                configuration.getType()
        );

        assertSame(
                settings,
                configuration.getSettings()
        );
    }

    @Test
    void acceptsMariaDBConfiguration()
    {
        MySQLConfiguration settings =
                new MySQLConfiguration(
                        "localhost",
                        3306,
                        "test",
                        "root",
                        "password"
                );

        DatabaseConfiguration configuration =
                new DatabaseConfiguration(
                        DatabaseType.MARIADB,
                        settings
                );

        assertEquals(
                DatabaseType.MARIADB,
                configuration.getType()
        );

        assertSame(
                settings,
                configuration.getSettings()
        );
    }

    @Test
    void acceptsPostgreSQLConfiguration()
    {
        PostgreSQLConfiguration settings =
                new PostgreSQLConfiguration(
                        "localhost",
                        5432,
                        "test",
                        "postgres",
                        "password"
                );

        DatabaseConfiguration configuration =
                new DatabaseConfiguration(
                        DatabaseType.POSTGRESQL,
                        settings
                );

        assertEquals(
                DatabaseType.POSTGRESQL,
                configuration.getType()
        );

        assertSame(
                settings,
                configuration.getSettings()
        );
    }

    @Test
    void rejectsNullType()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DatabaseConfiguration(
                        null,
                        new SQLiteConfiguration("test.db")
                )
        );
    }

    @Test
    void rejectsNullSettings()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DatabaseConfiguration(
                        DatabaseType.SQLITE,
                        null
                )
        );
    }

    @Test
    void rejectsIncorrectConfigurationType()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DatabaseConfiguration(
                        DatabaseType.SQLITE,
                        new MySQLConfiguration(
                                "localhost",
                                3306,
                                "test",
                                "root",
                                "password"
                        )
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new DatabaseConfiguration(
                        DatabaseType.POSTGRESQL,
                        new SQLiteConfiguration("test.db")
                )
        );
    }

    @Test
    void validatesSQLiteConfiguration()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SQLiteConfiguration(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new SQLiteConfiguration("")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new SQLiteConfiguration("   ")
        );

        SQLiteConfiguration configuration =
                new SQLiteConfiguration("test.db");

        assertEquals(
                "test.db",
                configuration.getFileName()
        );
    }

    @Test
    void validatesMySQLConfiguration()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MySQLConfiguration(
                        null,
                        3306,
                        "test",
                        "root",
                        "password"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new MySQLConfiguration(
                        "",
                        3306,
                        "test",
                        "root",
                        "password"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new MySQLConfiguration(
                        "localhost",
                        0,
                        "test",
                        "root",
                        "password"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new MySQLConfiguration(
                        "localhost",
                        65536,
                        "test",
                        "root",
                        "password"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new MySQLConfiguration(
                        "localhost",
                        3306,
                        null,
                        "root",
                        "password"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new MySQLConfiguration(
                        "localhost",
                        3306,
                        "test",
                        null,
                        "password"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new MySQLConfiguration(
                        "localhost",
                        3306,
                        "test",
                        "root",
                        null
                )
        );

        MySQLConfiguration configuration =
                new MySQLConfiguration(
                        "localhost",
                        3306,
                        "test",
                        "root",
                        "password"
                );

        assertEquals("localhost", configuration.getHost());
        assertEquals(3306, configuration.getPort());
        assertEquals("test", configuration.getDatabase());
        assertEquals("root", configuration.getUsername());
        assertEquals("password", configuration.getPassword());
    }

    @Test
    void validatesPostgreSQLConfiguration()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PostgreSQLConfiguration(
                        null,
                        5432,
                        "test",
                        "postgres",
                        "password"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PostgreSQLConfiguration(
                        "",
                        5432,
                        "test",
                        "postgres",
                        "password"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PostgreSQLConfiguration(
                        "localhost",
                        0,
                        "test",
                        "postgres",
                        "password"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PostgreSQLConfiguration(
                        "localhost",
                        65536,
                        "test",
                        "postgres",
                        "password"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PostgreSQLConfiguration(
                        "localhost",
                        5432,
                        null,
                        "postgres",
                        "password"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PostgreSQLConfiguration(
                        "localhost",
                        5432,
                        "test",
                        null,
                        "password"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PostgreSQLConfiguration(
                        "localhost",
                        5432,
                        "test",
                        "postgres",
                        null
                )
        );

        PostgreSQLConfiguration configuration =
                new PostgreSQLConfiguration(
                        "localhost",
                        5432,
                        "test",
                        "postgres",
                        "password"
                );

        assertEquals("localhost", configuration.getHost());
        assertEquals(5432, configuration.getPort());
        assertEquals("test", configuration.getDatabase());
        assertEquals("postgres", configuration.getUsername());
        assertEquals("password", configuration.getPassword());
    }

    @Test
    void rejectsAllIncorrectConfigurationTypes()
    {
        MySQLConfiguration mySQL =
                new MySQLConfiguration(
                        "localhost",
                        3306,
                        "test",
                        "root",
                        "password"
                );

        PostgreSQLConfiguration postgreSQL =
                new PostgreSQLConfiguration(
                        "localhost",
                        5432,
                        "test",
                        "postgres",
                        "password"
                );

        SQLiteConfiguration sqlite =
                new SQLiteConfiguration("test.db");

        assertThrows(
                IllegalArgumentException.class,
                () -> new DatabaseConfiguration(
                        DatabaseType.SQLITE,
                        mySQL
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new DatabaseConfiguration(
                        DatabaseType.MYSQL,
                        postgreSQL
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new DatabaseConfiguration(
                        DatabaseType.MARIADB,
                        postgreSQL
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new DatabaseConfiguration(
                        DatabaseType.POSTGRESQL,
                        sqlite
                )
        );
    }
}