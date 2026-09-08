package me.jackcw.jcore.database;

public final class SQLiteConfiguration
{
    private final String fileName;

    public SQLiteConfiguration(String fileName)
    {
        if (fileName == null || fileName.isBlank())
            throw new IllegalArgumentException(
                    "Database file name cannot be null or blank"
            );

        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }
}
