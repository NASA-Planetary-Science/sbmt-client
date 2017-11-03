package edu.jhuapl.sbmt.gui.eros;

import edu.jhuapl.sbmt.model.bennu.OTES;
import edu.jhuapl.sbmt.model.eros.SpectrumMath;

public class OTESSpectrumMath extends SpectrumMath
{

    @Override
    public int getNumberOfBandsPerRawSpectrum()
    {
        return OTES.bandCenters.length;
    }

    @Override
    public String[] getDerivedParameters()
    {
        return new String[]{};
    }

}
