package edu.jhuapl.sbmt.core.io;

import java.io.IOException;

public interface FileReader<E extends IOException>
{
    public void read() throws E;
    public String getFileName();
}
