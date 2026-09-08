package me.jackcw.jcore.command;

public final class CommandArgument<T>
{
    private final String name;
    private final ArgumentType<T> type;
    private final boolean required;

    public CommandArgument(String name, ArgumentType<T> type, boolean required)
    {
        this.name = name;
        this.type = type;
        this.required = required;
    }

    public String getName()
    {
        return name;
    }

    public ArgumentType<T> getType()
    {
        return type;
    }

    public boolean isRequired()
    {
        return required;
    }
}
