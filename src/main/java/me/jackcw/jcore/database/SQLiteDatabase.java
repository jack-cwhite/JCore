package me.jackcw.jcore.database;

import me.jackcw.jcore.task.TaskManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class SQLiteDatabase implements Database
{
    private final File databaseFile;
    private final TaskManager taskManager;
    private DatabaseState state = DatabaseState.DISCONNECTED;

    public SQLiteDatabase(JavaPlugin plugin, TaskManager taskManager, String fileName)
    {
        this.taskManager = taskManager;
        this.databaseFile = new File(plugin.getDataFolder(), fileName
        );
    }

    @Override
    public int execute(String sql, Object... parameters)
    {
        return withConnection(connection ->
        {
            try (PreparedStatement statement = connection.prepareStatement(sql))
            {
                bindParameters(statement, parameters);
                return statement.executeUpdate();
            }
            catch (SQLException e)
            {
                throw new DatabaseException(
                        "Could not execute database statement: " + sql, e
                );
            }
        });
    }

    @Override
    public void query(String sql, Consumer<DatabaseResult> handler, Object... parameters)
    {
        useConnection(connection ->
        {
            try (PreparedStatement statement = connection.prepareStatement(sql))
            {
                bindParameters(statement, parameters);

                try (ResultSet resultSet = statement.executeQuery())
                {
                    handler.accept(new DatabaseResult(resultSet));
                }
            }
            catch (SQLException e)
            {
                throw new DatabaseException(
                        "Could not execute database query: " + sql, e
                );
            }
        });
    }

    @Override
    public void transaction(DatabaseOperation transaction)
    {
        if (!isConnected())
        {
            throw new DatabaseException(
                    "Database is not connected"
            );
        }

        try (Connection connection = createConnection())
        {
            try
            {
                connection.setAutoCommit(false);

                transaction.execute(connection);

                connection.commit();
            }
            catch (Exception e)
            {
                try
                {
                    connection.rollback();
                }
                catch (SQLException rollbackException)
                {
                    e.addSuppressed(rollbackException);
                }

                if (e instanceof DatabaseException databaseException)
                    throw databaseException;

                throw new DatabaseException(
                        "Database transaction failed", e
                );
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(
                    "Could not execute database transaction", e
            );
        }
    }


    @Override
    public CompletableFuture<Integer> executeAsync(String sql, Object... parameters)
    {
        return taskManager.submitAsync(
                () -> execute(sql, parameters)
        );
    }

    @Override
    public CompletableFuture<DatabaseResult> queryAsync(String sql, Object... parameters)
    {
        return taskManager.submitAsync(() -> withConnection((connection ->
        {
            try (PreparedStatement statement = connection.prepareStatement(sql))
            {
                bindParameters(statement, parameters);

                try (ResultSet resultSet = statement.executeQuery())
                {
                    return new DatabaseResult(resultSet);
                }
            }
            catch (SQLException e)
            {
                throw new DatabaseException(
                        "Could not execute database query: " + sql, e
                );
            }
        })));
    }

    @Override
    public CompletableFuture<Void> transactionAsync(DatabaseOperation transaction)
    {
        return taskManager.runAsyncFuture(
                () -> transaction(transaction)
        );
    }

    private <T> T executeWithConnection(DatabaseFunction<T> operation)
    {
        if (!isConnected())
        {
            throw new DatabaseException(
                    "Database not connected"
            );
        }

        try (Connection connection = createConnection())
        {
            return operation.apply(connection);
        }
        catch (SQLException e)
        {
            throw new DatabaseException(
                    "Could not execute database operation",
                    e
            );
        }
    }

    @Override
    public <T> T withConnection(DatabaseFunction<T> operation)
    {
        return executeWithConnection(operation);
    }

    @Override
    public void useConnection(Consumer<Connection> operation)
    {
        executeWithConnection((DatabaseFunction<Void>) connection ->
        {
            operation.accept(connection);
            return null;
        });
    }

    @Override
    public void connect()
    {
        if (state == DatabaseState.CONNECTED)
            return;

        if (state == DatabaseState.CLOSED)
            throw new DatabaseException(
                    "Database has been closed"
            );

        File parent = databaseFile.getParentFile();

        if (parent != null && !parent.exists() && !parent.mkdirs())
            throw new DatabaseException(
                    "Could not create database directory: " + parent
            );

        try (Connection connection = createConnection())
        {
            state = DatabaseState.CONNECTED;
        }
        catch (SQLException e)
        {
            throw new DatabaseException(
                    "Could not connect to SQLite database: " + databaseFile.getAbsolutePath(), e
            );
        }
    }

    @Override
    public void disconnect()
    {
        if (state == DatabaseState.CLOSED)
            return;

        state = DatabaseState.DISCONNECTED;
    }

    @Override
    public boolean isConnected()
    {
        return state == DatabaseState.CONNECTED;
    }

    private void bindParameters(PreparedStatement statement, Object... parameters)
    {
        try
        {
            for (int i = 0; i < parameters.length; i++)
            {
                statement.setObject(
                        i + 1,
                        parameters[i]
                );
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(
                    "Could not bind database parameters", e
            );
        }
    }

    private Connection createConnection() throws SQLException
    {
            return DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
    }

    public File getDatabaseFile()
    {
        return databaseFile;
    }

    public DatabaseState getState()
    {
        return state;
    }

    @Override
    public DatabaseType getType()
    {
        return DatabaseType.SQLITE;
    }
}
