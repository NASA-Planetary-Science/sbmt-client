package edu.jhuapl.near.app;

// Populations
public enum ShapeModelPopulation
{
    MARS("Mars"),
    JUPITER("Jupiter"),
    SATURN("Saturn"),
    NEPTUNE("Neptune"),
    NEO("Near-Earth"),
    MAIN_BELT("Main Belt"),
    PLUTO("Pluto");

    final private String str;
    private ShapeModelPopulation(String str)
    {
        this.str = str;
    }

    @Override
    public String toString()
    {
        return str;
    }
}