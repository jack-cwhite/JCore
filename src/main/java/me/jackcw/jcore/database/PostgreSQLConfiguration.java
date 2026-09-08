package me.jackcw.jcore.database;

public final class PostgreSQLConfiguration
{
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public PostgreSQLConfiguration(String host, int port, String database, String username, String password
    )
    {
        if (host == null || host.isBlank())
            throw new IllegalArgumentException(
                    "Database host cannot be null or blank"
            );

        if (port <= 0 || port > 65535)
            throw new IllegalArgumentException(
                    "Database port must be between 1 and 65535"
            );

        if (database == null || database.isBlank())
            throw new IllegalArgumentException(
                    "Database name cannot be null or blank"
            );

        if (username == null || username.isBlank())
            throw new IllegalArgumentException(
                    "Database username cannot be null or blank"
            );

        if (password == null)
            throw new IllegalArgumentException(
                    "Database password cannot be null"
            );

        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public String getDatabase()
    {
        return database;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }
}