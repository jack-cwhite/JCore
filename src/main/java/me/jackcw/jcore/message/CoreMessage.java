package me.jackcw.jcore.message;

public enum CoreMessage implements MessageKey
{
    PREFIX("core.prefix"),
    NO_PERMISSION("core.no-permission"),
    INCORRECT_USAGE("core.incorrect-usage"),
    PLAYER_ONLY("core.player-only"),
    PLAYER_NOT_FOUND("core.player-not-found");

    private final String path;

    CoreMessage(String path)
    {
        this.path = path;
    }

    @Override
    public String getPath()
    {
        return path;
    }
}
