package me.jackcw.jcore.menu;

public final class PageContext
{
    private final PaginatedMenu<?> menu;

    PageContext(PaginatedMenu<?> menu)
    {
        this.menu = menu;
    }

    public int index()
    {
        return menu.getCurrentPage();
    }

    public int total()
    {
        return menu.getTotalPages();
    }

    public void goTo(int page)
    {
        menu.goToPage(page);
    }

    public void next()
    {
        menu.nextPage();
    }

    public void previous()
    {
        menu.previousPage();
    }
}
