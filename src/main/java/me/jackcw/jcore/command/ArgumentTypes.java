package me.jackcw.jcore.command;

public final class ArgumentTypes
{
    private ArgumentTypes()
    {
    }

    public static ArgumentType<String> string()
    {
        return input -> input;
    }

    public static ArgumentType<Integer> integer()
    {
        return input ->
        {
            try
            {
                return Integer.parseInt(input);
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException(
                        "'" + input + "' is not a valid integer."
                );
            }
        };
    }

    public static ArgumentType<Double> decimal()
    {
        return input ->
        {
            try
            {
                return Double.parseDouble(input);
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException(
                        "'" + input + "' is not a valid number."
                );
            }
        };
    }

    public static ArgumentType<Boolean> bool()
    {
        return input ->
        {
            if (input.equalsIgnoreCase("true"))
                return true;

            if (input.equalsIgnoreCase("false"))
                return false;

            throw new IllegalArgumentException(
                    "'" + input + "' is not true or false."
            );
        };
    }
}