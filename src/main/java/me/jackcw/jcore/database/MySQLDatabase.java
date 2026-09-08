package me.jackcw.jcore.database;

import me.jackcw.jcore.task.TaskManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class MySQLDatabase implements Database
{
    private final MySQLConfiguration configuration;
    private final TaskManager taskManager;

    private DatabaseState state = DatabaseState.DISCONNECTED;

    public MySQLDatabase(JavaPlugin plugin, TaskManager taskManager, MySQLConfiguration configuration)
    {
        if (plugin == null)
            throw new IllegalArgumentException(
                    "Plugin cannot be null"
            );

        if (taskManager == null)
            throw new IllegalArgumentException(
                    "Task manager cannot be null"
            );

        if (configuration == null)
            throw new IllegalArgumentException(
                    "Database configuration cannot be null"
            );

        this.taskManager = taskManager;
        this.configuration = configuration;
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
        return taskManager.submitAsync(
                () -> withConnection(connection ->
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
                })
        );
    }

    @Override
    public CompletableFuture<Void> transactionAsync(
            DatabaseOperation transaction
    )
    {
        return taskManager.runAsyncFuture(
                () -> transaction(transaction)
        );
    }

    @Override
    public <T> T withConnection(DatabaseFunction<T> operation)
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
    public void useConnection(Consumer<Connection> operation)
    {
        withConnection(connection ->
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

        try (Connection connection = createConnection())
        {
            state = DatabaseState.CONNECTED;
        }
        catch (SQLException e)
        {
            throw new DatabaseException(
                    "Could not connect to database", e
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

    @Override
    public DatabaseType getType()
    {
        return DatabaseType.MYSQL;
    }

    private void bindParameters(PreparedStatement statement, Object... parameters)
    {
        try
        {
            for (int i = 0; i < parameters.length; i++)
            {
                statement.setObject(i + 1, parameters[i]);
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
        return DriverManager.getConnection(
                "jdbc:mariadb://" +
                        configuration.getHost() +
                        ":" +
                        configuration.getPort() +
                        "/" +
                        configuration.getDatabase(),
                configuration.getUsername(),
                configuration.getPassword()
        );
    }
}