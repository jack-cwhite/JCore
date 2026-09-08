package me.jackcw.jcore.database;

import java.sql.Connection;

@FunctionalInterface
public interface DatabaseFunction<T>
{
    T apply(Connection connection);
}
