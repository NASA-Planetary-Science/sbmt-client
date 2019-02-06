package edu.jhuapl.sbmt.client;

// Populations
public enum ShapeModelPopulation
{
    MARS("Mars System"),
    JUPITER("Jupiter System"),
    SATURN("Saturn System"),
    NEPTUNE("Neptune System"),
    NEO("Near-Earth"),
    MAIN_BELT("Main Belt"),
    PLUTO("Pluto System"),
    EARTH("Earth System"),
    COMETS("Comets");

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