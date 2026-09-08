package me.jackcw.jcore.database;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Database
{
    void connect();

    void disconnect();

    boolean isConnected();

    DatabaseType getType();

    <T> T withConnection(DatabaseFunction<T> operation);

    void useConnection(Consumer<Connection> operation);

    int execute(String sql, Object... parameters);

    void query(String sql, Consumer<DatabaseResult> handler, Object... parameters
    );

    void transaction(DatabaseOperation transaction);

    CompletableFuture<Integer> executeAsync(String sql, Object... parameters
    );

    CompletableFuture<DatabaseResult> queryAsync(String sql, Object... parameters
    );

    CompletableFuture<Void> transactionAsync(DatabaseOperation transaction
    );
}
