package edu.jhuapl.sbmt.core.io;

public abstract class BasicFileWriter implements FileWriter
{
    protected final String filename;

    public BasicFileWriter(String filename)
    {
        this.filename=filename;
    }

    @Override
    public String getFileName()
    {
        return filename;
    }
}
