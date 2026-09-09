package me.jackcw.jcore.database;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
import me.jackcw.jcore.task.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseFactoryTest
{
    @AfterEach
    void cleanup()
    {
        TestUtils.cleanup();
    }

    @Test
    void createsSQLiteDatabase()
    {
        TestPlugin plugin = TestUtils.mockPlugin();
        TaskManager taskManager = new TaskManager(plugin);

        Database database = DatabaseFactory.create(
                plugin,
                taskManager,
                new DatabaseConfiguration(
                        DatabaseType.SQLITE,
                        new SQLiteConfiguration("test.db")
                )
        );

        assertInstanceOf(SQLiteDatabase.class, database);
        assertEquals(DatabaseType.SQLITE, database.getType());
    }

    @Test
    void createsMySQLDatabase()
    {
        TestPlugin plugin = TestUtils.mockPlugin();
        TaskManager taskManager = new TaskManager(plugin);

        Database database = DatabaseFactory.create(
                plugin,
                taskManager,
                new DatabaseConfiguration(
                        DatabaseType.MYSQL,
                        new MySQLConfiguration(
                                "localhost",
                                3306,
                                "test",
                                "root",
                                "password"
                        )
                )
        );

        assertInstanceOf(MySQLDatabase.class, database);
        assertEquals(DatabaseType.MYSQL, database.getType());
    }

    @Test
    void createsMariaDBDatabase()
    {
        TestPlugin plugin = TestUtils.mockPlugin();
        TaskManager taskManager = new TaskManager(plugin);

        Database database = DatabaseFactory.create(
                plugin,
                taskManager,
                new DatabaseConfiguration(
                        DatabaseType.MARIADB,
                        new MySQLConfiguration(
                                "localhost",
                                3306,
                                "test",
                                "root",
                                "password"
                        )
                )
        );

        assertInstanceOf(MySQLDatabase.class, database);
        assertEquals(DatabaseType.MARIADB, database.getType());
    }

    @Test
    void createsPostgreSQLDatabase()
    {
        TestPlugin plugin = TestUtils.mockPlugin();
        TaskManager taskManager = new TaskManager(plugin);

        Database database = DatabaseFactory.create(
                plugin,
                taskManager,
                new DatabaseConfiguration(
                        DatabaseType.POSTGRESQL,
                        new PostgreSQLConfiguration(
                                "localhost",
                                5432,
                                "test",
                                "postgres",
                                "password"
                        )
                )
        );

        assertInstanceOf(PostgreSQLDatabase.class, database);
        assertEquals(DatabaseType.POSTGRESQL, database.getType());
    }

    @Test
    void rejectsNullPlugin()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> DatabaseFactory.create(
                        null,
                        null,
                        new DatabaseConfiguration(
                                DatabaseType.SQLITE,
                                new SQLiteConfiguration("test.db")
                        )
                )
        );
    }

    @Test
    void rejectsNullTaskManager()
    {
        TestPlugin plugin = TestUtils.mockPlugin();

        assertThrows(
                IllegalArgumentException.class,
                () -> DatabaseFactory.create(
                        plugin,
                        null,
                        new DatabaseConfiguration(
                                DatabaseType.SQLITE,
                                new SQLiteConfiguration("test.db")
                        )
                )
        );
    }

    @Test
    void rejectsNullConfiguration()
    {
        TestPlugin plugin = TestUtils.mockPlugin();
        TaskManager taskManager = new TaskManager(plugin);

        assertThrows(
                IllegalArgumentException.class,
                () -> DatabaseFactory.create(
                        plugin,
                        taskManager,
                        null
                )
        );
    }
}