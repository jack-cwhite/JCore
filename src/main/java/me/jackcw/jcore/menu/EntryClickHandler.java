package me.jackcw.jcore.menu;

@FunctionalInterface
public interface EntryClickHandler<T>
{
    void onClick(MenuContext context, T entry);
}
