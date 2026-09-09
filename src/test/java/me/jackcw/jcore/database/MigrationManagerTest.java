package me.jackcw.jcore.database;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.task.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MigrationManagerTest
{
    private TestPlugin plugin;
    private TaskManager taskManager;
    private SQLiteDatabase database;
    private MigrationManager migrationManager;

    @BeforeEach
    void setup() throws Exception
    {
        MockBukkit.mock();

        plugin = MockBukkit.load(TestPlugin.class);
        taskManager = new TaskManager(plugin);

        File file = Files.createTempFile("jcore-test-", ".db").toFile();
        database = new SQLiteDatabase(plugin, taskManager, file.getName());

        database.connect();
        migrationManager = new MigrationManager(database);
    }

    @AfterEach
    void cleanup()
    {
        MockBukkit.unmock();
    }

    @Test
    void appliesMigration()
    {
        migrationManager.add(
                new Migration(
                        1,
                        connection ->
                        {
                            try (var statement = connection.createStatement())
                            {
                                statement.executeUpdate(
                                        """
                                        CREATE TABLE test (
                                            value INTEGER
                                        )
                                        """
                                );
                            }
                        }
                )
        );

        migrationManager.migrate();

        assertEquals(1, migrationManager.getCurrentVersion());

        database.query(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'test'",
                result ->
                {
                    assertTrue(result.next());
                    assertEquals("test", result.getString("name"));
                }
        );
    }

    @Test
    void doesNotApplyMigrationTwice()
    {
        AtomicInteger executions = new AtomicInteger();

        migrationManager.add(
                new Migration(
                        1,
                        connection ->
                        {
                            executions.incrementAndGet();
                        }
                )
        );

        migrationManager.migrate();
        migrationManager.migrate();

        assertEquals(1, executions.get());
        assertEquals(1, migrationManager.getCurrentVersion());
    }

    @Test
    void appliesMigrationsInVersionOrder()
    {
        StringBuilder order = new StringBuilder();

        migrationManager.add(
                new Migration(
                        2,
                        connection -> order.append("2")
                )
        );

        migrationManager.add(
                new Migration(
                        1,
                        connection -> order.append("1")
                )
        );

        migrationManager.add(
                new Migration(
                        3,
                        connection -> order.append("3")
                )
        );

        migrationManager.migrate();

        assertEquals("123", order.toString());
        assertEquals(3, migrationManager.getCurrentVersion());
    }

    @Test
    void rejectsDuplicateMigrationVersion()
    {
        migrationManager.add(
                new Migration(
                        1,
                        connection ->
                        {
                        }
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> migrationManager.add(
                        new Migration(
                                1,
                                connection ->
                                {
                                }
                        )
                )
        );
    }

    @Test
    void failedMigrationRollsBack()
    {
        migrationManager.add(
                new Migration(
                        1,
                        connection ->
                        {
                            try (var statement = connection.createStatement())
                            {
                                statement.executeUpdate(
                                        """
                                        CREATE TABLE test (
                                            value INTEGER
                                        )
                                        """
                                );
                            }

                            throw new Exception("Force migration failure");
                        }
                )
        );

        assertThrows(
                DatabaseException.class,
                () -> migrationManager.migrate()
        );

        assertEquals(0, migrationManager.getCurrentVersion());

        database.query(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'test'",
                result ->
                {
                    assertFalse(result.next());
                }
        );
    }

    @Test
    void returnsMigrationsInVersionOrder()
    {
        Migration migrationTwo = new Migration(
                2,
                connection ->
                {
                }
        );

        Migration migrationOne = new Migration(
                1,
                connection ->
                {
                }
        );

        migrationManager.add(migrationTwo);
        migrationManager.add(migrationOne);

        assertEquals(
                2,
                migrationManager.getMigrations().size()
        );

        assertEquals(
                1,
                migrationManager.getMigrations().get(0).getVersion()
        );

        assertEquals(
                2,
                migrationManager.getMigrations().get(1).getVersion()
        );
    }

    @Test
    void rejectsInvalidMigration()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Migration(
                        0,
                        connection ->
                        {
                        }
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Migration(
                        1,
                        null
                )
        );
    }
}