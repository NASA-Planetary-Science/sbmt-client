package edu.jhuapl.sbmt.core.image;

public class NoOverlapException extends Exception
{
    public NoOverlapException()
    {
        super("No overlap in 3 images");
    }
}