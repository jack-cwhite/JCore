package me.jackcw.jcore.menu;

final class MenuNavigationSlots
{
    private MenuNavigationSlots()
    {
    }

    static int previousPage(int rows)
    {
        return navigationStart(rows);
    }

    static int pageIndicator(int rows)
    {
        return navigationStart(rows) + 2;
    }

    static int backButton(int rows)
    {
        return navigationStart(rows) + 4;
    }

    static int nextPage(int rows)
    {
        return navigationStart(rows) + 8;
    }

    private static int navigationStart(int rows)
    {
        return (rows - 1) * 9;
    }
}
