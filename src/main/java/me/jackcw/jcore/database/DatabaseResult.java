package me.jackcw.jcore.database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public final class DatabaseResult
{
    private final List<Map<String, Object>> rows;
    private int currentRow = -1;

    DatabaseResult(ResultSet resultSet)
    {
        this.rows = readRows(resultSet);
    }

    public boolean next()
    {
        if (currentRow + 1 >= rows.size())
            return false;

        currentRow++;
        return true;
    }

    public boolean isNull(String column)
    {
        return getValue(column) == null;
    }

    public String getString(String column)
    {
        return (String) getValue(column);
    }

    public int getInt(String column)
    {
        Object value = getValue(column);

        if (value == null)
            throw new DatabaseException(
                    "Column '" + column + "' contains NULL"
            );

        if (!(value instanceof Number number))
            throw new DatabaseException(
                    "Column '" + column + "' does not contain a numeric value"
            );

        return number.intValue();
    }

    public long getLong(String column)
    {
        Object value = getValue(column);

        if (value == null)
            throw new DatabaseException(
                    "Column '" + column + "' contains NULL"
            );

        if (!(value instanceof Number number))
            throw new DatabaseException(
                    "Column '" + column + "' does not contain a numeric value"
            );

        return number.longValue();
    }

    public short getShort(String column)
    {
        Object value = getValue(column);

        if (value == null)
            throw new DatabaseException(
                    "Column '" + column + "' contains NULL"
            );

        if (!(value instanceof Number number))
            throw new DatabaseException(
                    "Column '" + column + "' does not contain a numeric value"
            );

        return number.shortValue();
    }

    public byte getByte(String column)
    {
        Object value = getValue(column);

        if (value == null)
            throw new DatabaseException(
                    "Column '" + column + "' contains NULL"
            );

        if (!(value instanceof Number number))
            throw new DatabaseException(
                    "Column '" + column + "' does not contain a numeric value"
            );

        return number.byteValue();
    }

    public float getFloat(String column)
    {
        Object value = getValue(column);

        if (value == null)
            throw new DatabaseException(
                    "Column '" + column + "' contains NULL"
            );

        if (!(value instanceof Number number))
            throw new DatabaseException(
                    "Column '" + column + "' does not contain a numeric value"
            );

        return number.floatValue();
    }

    public double getDouble(String column)
    {
        Object value = getValue(column);

        if (value == null)
            throw new DatabaseException(
                    "Column '" + column + "' contains NULL"
            );

        if (!(value instanceof Number number))
            throw new DatabaseException(
                    "Column '" + column + "' does not contain a numeric value"
            );

        return number.doubleValue();
    }

    public boolean getBoolean(String column)
    {
        Object value = getValue(column);

        if (value instanceof Boolean booleanValue)
            return booleanValue;

        if (value instanceof Number number)
            return number.intValue() != 0;

        if (value instanceof String string)
            return Boolean.parseBoolean(string);

        throw new DatabaseException(
                "Could not convert column '" + column + "' to boolean"
        );
    }

    public UUID getUUID(String column)
    {
        Object value = getValue(column);

        if (value == null)
            return null;

        if (value instanceof UUID uuid)
            return uuid;

        try
        {
            return UUID.fromString(value.toString());
        }
        catch (IllegalArgumentException e)
        {
            throw new DatabaseException(
                    "Invalid UUID in column: " + column, e
            );
        }
    }

    public <T extends Enum<T>> T getEnum(String column, Class<T> enumType)
    {
        Object value = getValue(column);

        if (value == null)
            return null;

        try
        {
            return Enum.valueOf(enumType, value.toString());
        }
        catch (IllegalArgumentException e)
        {
            throw new DatabaseException(
                    "Invalid enum value '" + value + "' in column: " + column, e
            );
        }
    }

    public Object getObject(String column)
    {
        return getValue(column);
    }

    public int size()
    {
        return rows.size();
    }

    public boolean isEmpty()
    {
        return rows.isEmpty();
    }

    private Object getValue(String column)
    {
        if (currentRow < 0)
            throw new DatabaseException(
                    "No current database row. Call next() before retrieving values"
            );

        if (currentRow >= rows.size())
            throw new DatabaseException(
                    "No current database row"
            );

        Map<String, Object> row = rows.get(currentRow);

        if (!row.containsKey(column))
            throw new DatabaseException(
                    "Column does not exist: " + column
            );

        return row.get(column);
    }

    private List<Map<String, Object>> readRows(ResultSet resultSet)
    {
        try
        {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            List<Map<String, Object>> rows = new ArrayList<>();

            while (resultSet.next())
            {
                Map<String, Object> row = new HashMap<>();

                for (int i = 1; i <= columnCount; i++)
                {
                    String column = metaData.getColumnLabel(i);
                    Object value = resultSet.getObject(i);

                    row.put(column, value);
                }

                rows.add(row);
            }

            return rows;
        }
        catch (SQLException e)
        {
            throw new DatabaseException(
                    "Could not read database result", e
            );
        }
    }
}