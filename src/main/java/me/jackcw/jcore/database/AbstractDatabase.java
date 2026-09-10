package me.jackcw.jcore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.jackcw.jcore.task.TaskManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class AbstractDatabase implements Database
{
    private final TaskManager taskManager;
    private final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();

    private volatile DatabaseState state = DatabaseState.DISCONNECTED;
    private volatile HikariDataSource dataSource;

    protected AbstractDatabase(TaskManager taskManager)
    {
        if (taskManager == null)
            throw new IllegalArgumentException(
                    "Task manager cannot be null"
            );

        this.taskManager = taskManager;
    }

    protected abstract void configure(HikariConfig config);

    protected void beforeConnect()
    {
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

        try (Connection connection = dataSource.getConnection())
        {
            try
            {
                connection.setAutoCommit(false);

                transactionConnection.set(connection);

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

                throw new DatabaseException(
                        "Database transaction failed", e
                );
            }
            finally
            {
                transactionConnection.remove();
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
    public CompletableFuture<Void> transactionAsync(DatabaseOperation transaction)
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

        Connection existingConnection = transactionConnection.get();

        if (existingConnection != null)
        {
            return operation.apply(existingConnection);
        }

        try (Connection connection = dataSource.getConnection())
        {
            return operation.apply(connection);
        }
        catch (SQLException e)
        {
            throw new DatabaseException(
                    "Could not execute database operation", e
            );
        }
    }

    @Override
    public void useConnection(Consumer<Connection> operation)
    {
        withConnection((DatabaseFunction<Void>) connection ->
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

        beforeConnect();

        HikariConfig config = new HikariConfig();
        configure(config);

        try
        {
            dataSource = new HikariDataSource(config);
        }
        catch (RuntimeException e)
        {
            throw new DatabaseException(
                    "Could not connect to database", e
            );
        }

        state = DatabaseState.CONNECTED;
    }

    @Override
    public void disconnect()
    {
        if (state == DatabaseState.CLOSED)
            return;

        if (dataSource != null)
        {
            dataSource.close();
            dataSource = null;
        }

        state = DatabaseState.DISCONNECTED;
    }

    @Override
    public boolean isConnected()
    {
        return state == DatabaseState.CONNECTED;
    }

    public DatabaseState getState()
    {
        return state;
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
}
