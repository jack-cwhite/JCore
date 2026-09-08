package me.jackcw.jcore.command;

public interface ArgumentType<T>
{
    T parse(String input) throws IllegalArgumentException;
}
