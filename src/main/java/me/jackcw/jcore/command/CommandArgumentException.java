package me.jackcw.jcore.command;

import me.jackcw.jcore.message.MessageKey;

public final class CommandArgumentException extends IllegalArgumentException
{
    private final MessageKey message;
    private final Object[] placeholders;

    public CommandArgumentException(MessageKey message, Object... placeholders)
    {
        this.message = message;
        this.placeholders = placeholders != null ? placeholders.clone() : new Object[0];
    }

    public MessageKey getMessageKey()
    {
        return message;
    }

    public Object[] getPlaceholders()
    {
        return placeholders.clone();
    }
}
