package me.jackcw.jcore.database;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.task.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteDatabaseTest
{
    private TestPlugin plugin;
    private TaskManager taskManager;
    private SQLiteDatabase database;

    @BeforeEach
    void setup() throws Exception
    {
        MockBukkit.mock();

        plugin = MockBukkit.load(TestPlugin.class);
        taskManager = new TaskManager(plugin);

        File file = Files.createTempFile("jcore-test-", ".db").toFile();
        database = new SQLiteDatabase(plugin, taskManager, file.getName());
    }

    @AfterEach
    void cleanup()
    {
        MockBukkit.unmock();
    }

    @Test
    void connectsSuccessfully()
    {
        database.connect();

        assertTrue(database.isConnected());
        assertEquals(DatabaseType.SQLITE, database.getType());
    }

    @Test
    void createsAndQueriesTable()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE players (
                    uuid TEXT PRIMARY KEY,
                    name TEXT
                )
                """
        );

        database.execute(
                "INSERT INTO players VALUES (?, ?)",
                "1234",
                "Jack"
        );

        database.query(
                "SELECT * FROM players",
                result ->
                {
                    assertTrue(result.next());
                    assertEquals("1234", result.getString("uuid"));
                    assertEquals("Jack", result.getString("name"));
                }
        );
    }

    @Test
    void transactionsCommit()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        database.transaction(connection ->
        {
            database.execute(
                    "INSERT INTO test VALUES (?)",
                    5
            );
        });

        database.query(
                "SELECT value FROM test",
                result ->
                {
                    assertTrue(result.next());
                    assertEquals(5, result.getInt("value"));
                }
        );
    }

    @Test
    void transactionsRollback()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        assertThrows(
                DatabaseException.class,
                () ->
                {
                    database.transaction(connection ->
                    {
                        database.execute(
                                "INSERT INTO test VALUES (?)",
                                5
                        );

                        throw new Exception("Force rollback");
                    });
                }
        );

        database.query(
                "SELECT value FROM test",
                result ->
                {
                    assertFalse(result.next());
                }
        );
    }

    @Test
    void executeReturnsAffectedRows()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        assertEquals(
                1,
                database.execute(
                        "INSERT INTO test VALUES (?)",
                        5
                )
        );

        assertEquals(
                1,
                database.execute(
                        "UPDATE test SET value = ?",
                        10
                )
        );

        assertEquals(
                1,
                database.execute(
                        "DELETE FROM test"
                )
        );
    }

    @Test
    void executeSupportsParameters()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE players (
                    name TEXT,
                    age INTEGER
                )
                """
        );

        database.execute(
                "INSERT INTO players VALUES (?, ?)",
                "Jack",
                25
        );

        database.query(
                "SELECT name, age FROM players",
                result ->
                {
                    assertTrue(result.next());
                    assertEquals("Jack", result.getString("name"));
                    assertEquals(25, result.getInt("age"));
                }
        );
    }

    @Test
    void executeFailsWhenDisconnected()
    {
        assertThrows(DatabaseException.class,
                () -> database.execute(
                        "CREATE TABLE test (value INTEGER)"
                )
        );
    }

    @Test
    void readsNumericValues()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    int_value INTEGER,
                    long_value INTEGER,
                    float_value REAL,
                    double_value REAL
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?, ?, ?, ?)",
                10,
                20L,
                2.5f,
                5.75
        );

        database.query(
                "SELECT * FROM test",
                result ->
                {
                    assertTrue(result.next());
                    assertEquals(10, result.getInt("int_value"));
                    assertEquals(20L, result.getLong("long_value"));
                    assertEquals(2.5f, result.getFloat("float_value"));
                    assertEquals(5.75, result.getDouble("double_value"));
                }
        );
    }

    @Test
    void readsBooleanValues()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?)",
                1
        );

        database.query(
                "SELECT value FROM test",
                result ->
                {
                    assertTrue(result.next());
                    assertTrue(result.getBoolean("value"));
                }
        );
    }

    @Test
    void readsNullValues()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value TEXT
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?)",
                (Object) null
        );

        database.query(
                "SELECT value FROM test",
                result ->
                {
                    assertTrue(result.next());
                    assertTrue(result.isNull("value"));
                    assertNull(result.getObject("value"));
                }
        );
    }

    @Test
    void readsMultipleRows()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?)",
                1
        );

        database.execute(
                "INSERT INTO test VALUES (?)",
                2
        );

        database.query(
                "SELECT value FROM test ORDER BY value",
                result ->
                {
                    assertEquals(2, result.size());
                    assertFalse(result.isEmpty());

                    assertTrue(result.next());
                    assertEquals(1, result.getInt("value"));

                    assertTrue(result.next());
                    assertEquals(2, result.getInt("value"));

                    assertFalse(result.next());
                }
        );
    }

    @Test
    void failsWhenReadingBeforeNext()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?)",
                5
        );

        database.query(
                "SELECT value FROM test",
                result ->
                {
                    assertThrows(
                            DatabaseException.class,
                            () -> result.getInt("value")
                    );
                }
        );
    }

    @Test
    void failsWhenReadingMissingColumn()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?)",
                5
        );

        database.query(
                "SELECT value FROM test",
                result ->
                {
                    assertTrue(result.next());

                    assertThrows(
                            DatabaseException.class,
                            () -> result.getInt("missing")
                    );
                }
        );
    }

    @Test
    void failsWhenReadingNullNumericValue()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?)",
                (Object) null
        );

        database.query(
                "SELECT value FROM test",
                result ->
                {
                    assertTrue(result.next());

                    assertThrows(
                            DatabaseException.class,
                            () -> result.getInt("value")
                    );
                }
        );
    }

    @Test
    void readsUuidAndEnumValues()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    uuid TEXT,
                    type TEXT
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?, ?)",
                "550e8400-e29b-41d4-a716-446655440000",
                TestType.EXAMPLE.name()
        );

        database.query(
                "SELECT uuid, type FROM test",
                result ->
                {
                    assertTrue(result.next());

                    assertEquals(
                            java.util.UUID.fromString(
                                    "550e8400-e29b-41d4-a716-446655440000"
                            ),
                            result.getUUID("uuid")
                    );

                    assertEquals(
                            TestType.EXAMPLE,
                            result.getEnum("type", TestType.class)
                    );
                }
        );
    }

    @Test
    void transactionUsesSameConnection()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        database.transaction(connection ->
        {
            database.execute(
                    "INSERT INTO test VALUES (?)",
                    5
            );

            database.query(
                    "SELECT value FROM test",
                    result ->
                    {
                        assertTrue(result.next());
                        assertEquals(5, result.getInt("value"));
                    }
            );
        });
    }

    @Test
    void transactionRollbackDoesNotAffectExistingData()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?)",
                1
        );

        assertThrows(
                DatabaseException.class,
                () ->
                {
                    database.transaction(connection ->
                    {
                        database.execute(
                                "INSERT INTO test VALUES (?)",
                                2
                        );

                        throw new Exception("Force rollback");
                    });
                }
        );

        database.query(
                "SELECT value FROM test ORDER BY value",
                result ->
                {
                    assertEquals(1, result.size());

                    assertTrue(result.next());
                    assertEquals(1, result.getInt("value"));
                }
        );
    }

    @Test
    void executeAsyncExecutesStatement()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        int affectedRows = database.executeAsync(
                "INSERT INTO test VALUES (?)",
                5
        ).join();

        assertEquals(1, affectedRows);
    }

    @Test
    void queryAsyncReturnsResult()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?)",
                5
        );

        DatabaseResult result = database.queryAsync(
                "SELECT value FROM test"
        ).join();

        assertTrue(result.next());
        assertEquals(5, result.getInt("value"));
    }

    @Test
    void transactionAsyncCommits()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        database.transactionAsync(connection ->
        {
            database.execute(
                    "INSERT INTO test VALUES (?)",
                    5
            );
        }).join();

        database.query(
                "SELECT value FROM test",
                result ->
                {
                    assertTrue(result.next());
                    assertEquals(5, result.getInt("value"));
                }
        );
    }

    @Test
    void transactionAsyncRollsBack()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        database.transactionAsync(connection ->
        {
            database.execute(
                    "INSERT INTO test VALUES (?)",
                    5
            );

            throw new Exception("Force rollback");
        }).exceptionally(throwable ->
        {
            assertNotNull(throwable);
            return null;
        }).join();

        database.query(
                "SELECT value FROM test",
                result ->
                {
                    assertFalse(result.next());
                }
        );
    }

    @Test
    void readsShortAndByteValues()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    short_value INTEGER,
                    byte_value INTEGER
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?, ?)",
                12,
                7
        );

        database.query(
                "SELECT * FROM test",
                result ->
                {
                    assertTrue(result.next());
                    assertEquals((short) 12, result.getShort("short_value"));
                    assertEquals((byte) 7, result.getByte("byte_value"));
                }
        );
    }

    @Test
    void readsBooleanFromString()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value TEXT
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?)",
                "true"
        );

        database.query(
                "SELECT value FROM test",
                result ->
                {
                    assertTrue(result.next());
                    assertTrue(result.getBoolean("value"));
                }
        );
    }

    @Test
    void readsNullUuidAndEnum()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    uuid TEXT,
                    type TEXT
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?, ?)",
                (Object) null,
                null
        );

        database.query(
                "SELECT uuid, type FROM test",
                result ->
                {
                    assertTrue(result.next());
                    assertNull(result.getUUID("uuid"));
                    assertNull(result.getEnum("type", TestType.class));
                }
        );
    }

    @Test
    void rejectsInvalidUuid()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    uuid TEXT
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?)",
                "not-a-uuid"
        );

        database.query(
                "SELECT uuid FROM test",
                result ->
                {
                    assertTrue(result.next());

                    assertThrows(
                            DatabaseException.class,
                            () -> result.getUUID("uuid")
                    );
                }
        );
    }

    @Test
    void rejectsInvalidEnum()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    type TEXT
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?)",
                "INVALID"
        );

        database.query(
                "SELECT type FROM test",
                result ->
                {
                    assertTrue(result.next());

                    assertThrows(
                            DatabaseException.class,
                            () -> result.getEnum("type", TestType.class)
                    );
                }
        );
    }

    @Test
    void reconnectsSuccessfully()
    {
        database.connect();

        assertTrue(database.isConnected());

        database.disconnect();

        assertFalse(database.isConnected());

        database.connect();

        assertTrue(database.isConnected());

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        database.execute(
                "INSERT INTO test VALUES (?)",
                5
        );

        database.query(
                "SELECT value FROM test",
                result ->
                {
                    assertTrue(result.next());
                    assertEquals(5, result.getInt("value"));
                }
        );
    }

    @Test
    void executeFailsAfterDisconnect()
    {
        database.connect();

        database.execute(
                """
                CREATE TABLE test (
                    value INTEGER
                )
                """
        );

        database.disconnect();

        assertThrows(
                DatabaseException.class,
                () -> database.execute(
                        "INSERT INTO test VALUES (?)",
                        5
                )
        );
    }

    @Test
    void withConnectionReturnsValue()
    {
        database.connect();

        String value = database.withConnection(connection ->
        {
            try
            {
                return connection.getMetaData().getDatabaseProductName();
            }
            catch (SQLException e)
            {
                throw new DatabaseException(
                        "Could not read database metadata", e
                );
            }
        });

        assertEquals("SQLite", value);
    }

    @Test
    void useConnectionProvidesConnection()
    {
        database.connect();

        database.useConnection(connection ->
        {
            try
            {
                assertFalse(connection.isClosed());
            }
            catch (SQLException e)
            {
                throw new DatabaseException(
                        "Could not inspect database connection", e
                );
            }
        });
    }

    private enum TestType
    {
        EXAMPLE
    }
}