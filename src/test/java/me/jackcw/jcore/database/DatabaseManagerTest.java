package me.jackcw.jcore.database;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest
{
    @Test
    void rejectsNullDatabase()
    {
        assertThrows(IllegalArgumentException.class, () -> new DatabaseManager(null));
    }

    @Test
    void exposesDatabase()
    {
        FakeDatabase database = new FakeDatabase();
        DatabaseManager manager = new DatabaseManager(database);

        assertSame(database, manager.getDatabase());
    }

    @Test
    void exposesMigrationManager()
    {
        FakeDatabase database = new FakeDatabase();
        DatabaseManager manager = new DatabaseManager(database);

        assertNotNull(manager.getMigrationManager());
    }

    @Test
    void connectDelegates()
    {
        FakeDatabase database = new FakeDatabase();
        DatabaseManager manager = new DatabaseManager(database);

        manager.connect();

        assertTrue(database.connected);
    }

    @Test
    void disconnectDelegates()
    {
        FakeDatabase database = new FakeDatabase();
        DatabaseManager manager = new DatabaseManager(database);

        manager.connect();
        manager.disconnect();

        assertFalse(database.connected);
    }

    @Test
    void isConnectedReflectsDatabase()
    {
        FakeDatabase database = new FakeDatabase();
        DatabaseManager manager = new DatabaseManager(database);

        assertFalse(manager.isConnected());

        manager.connect();

        assertTrue(manager.isConnected());
    }

    private static class FakeDatabase implements Database
    {
        private boolean connected;

        @Override
        public void connect()
        {
            connected = true;
        }

        @Override
        public void disconnect()
        {
            connected = false;
        }

        @Override
        public boolean isConnected()
        {
            return connected;
        }

        @Override
        public DatabaseType getType()
        {
            return DatabaseType.SQLITE;
        }

        @Override
        public <T> T withConnection(DatabaseFunction<T> operation)
        {
            return null;
        }

        @Override
        public void useConnection(Consumer<Connection> operation)
        {
        }

        @Override
        public int execute(String sql, Object... parameters)
        {
            return 0;
        }

        @Override
        public void query(String sql, Consumer<DatabaseResult> handler, Object... parameters)
        {
        }

        @Override
        public void transaction(DatabaseOperation transaction)
        {
        }

        @Override
        public CompletableFuture<Integer> executeAsync(String sql, Object... parameters)
        {
            return null;
        }

        @Override
        public CompletableFuture<DatabaseResult> queryAsync(String sql, Object... parameters)
        {
            return null;
        }

        @Override
        public CompletableFuture<Void> transactionAsync(DatabaseOperation transaction)
        {
            return null;
        }
    }
}