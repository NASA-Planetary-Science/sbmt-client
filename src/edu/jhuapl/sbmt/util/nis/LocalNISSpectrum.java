package edu.jhuapl.sbmt.util.nis;

import java.io.File;
import java.io.IOException;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.eros.NIS;
import edu.jhuapl.sbmt.model.eros.NISSpectrum;

public class LocalNISSpectrum extends NISSpectrum
{
    private static NIS nis=new NIS();

    public LocalNISSpectrum(File nisFile, SmallBodyModel eros)
            throws IOException
    {
        super(nisFile.getAbsolutePath(), eros, nis);
        serverpath=nisFile.toString();
    }

}
