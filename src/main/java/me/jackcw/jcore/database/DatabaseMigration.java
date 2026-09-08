package me.jackcw.jcore.database;

import java.sql.Connection;

@FunctionalInterface
public interface DatabaseMigration
{
    void migrate(Connection connection) throws Exception;
}
