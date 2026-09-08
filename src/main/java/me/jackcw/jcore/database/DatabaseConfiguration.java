package me.jackcw.jcore.database;

public final class DatabaseConfiguration
{
    private final DatabaseType type;
    private final String fileName;

    public DatabaseConfiguration(DatabaseType type, String fileName)
    {
        if (type == null)
            throw new IllegalArgumentException(
                    "Database type cannot be null"
            );

        if (fileName == null || fileName.isBlank())
            throw new IllegalArgumentException(
                    "Database file name cannot be null"
            );

        this.type = type;
        this.fileName = fileName;
    }

    public DatabaseType getType()
    {
        return type;
    }

    public String getFileName()
    {
        return fileName;
    }
}
