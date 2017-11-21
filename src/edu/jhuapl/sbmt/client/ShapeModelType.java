package edu.jhuapl.sbmt.client;

// Types of bodies
public enum ShapeModelType
{
    ASTEROID("Asteroids"),
    PLANETS_AND_SATELLITES("Planets and Satellites"),
    COMETS("Comets"),
    KBO("Kuiper Belt Objects"),
    ;

    final public String str;
    private ShapeModelType(String str)
    {
        this.str = str;
    }

    @Override
    public String toString()
    {
        return str;
    }
}