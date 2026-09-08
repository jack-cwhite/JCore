package me.jackcw.jcore.database;

import java.sql.Connection;

@FunctionalInterface
public interface DatabaseOperation
{
    void execute(Connection connection) throws Exception;
}
