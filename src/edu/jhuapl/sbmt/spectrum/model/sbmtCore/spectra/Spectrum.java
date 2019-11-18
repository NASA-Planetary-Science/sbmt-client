package edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra;

import java.io.File;
import java.io.IOException;

import org.joda.time.DateTime;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;

/**
 * Abstract spectrum class that contains only required information to describe the spectrum, not how to render it (that is found in the AbstractSpectrumRenderer based classes)
 * @author steelrj1
 *
 */
public abstract class Spectrum
{
    public static final String SPECTRUM_NAMES = "SpectrumNames"; // What name to give this image for display
    public static final String SPECTRUM_FILENAMES = "SpectrumFilenames"; // Filename of image on disk
    public static final String SPECTRUM_MAP_PATHS = "SpectrumMapPaths"; // For backwards compatibility, still read this in
    public static final String SPECTRUM_TYPES = "SpectrumTypes";
    public boolean isCustomSpectra = false;

    public abstract DateTime getDateTime();
    public abstract BasicSpectrumInstrument getInstrument();
    public abstract Double[] getBandCenters();
    public abstract double[] getSpectrum();
    public abstract String getFullPath();
    public abstract String getSpectrumPathOnServer();
    public String spectrumName;

    public abstract double[] getSpacecraftPosition();
    public abstract double[] getFrustumCenter();
    public abstract double[] getFrustumCorner(int i);
    public abstract double[] getFrustumOrigin();
    public abstract double[] getToSunUnitVector();

    public abstract double evaluateDerivedParameters(int channel);

    public abstract void saveSpectrum(File file) throws IOException;

    public abstract void saveInfofile(File file) throws IOException;

    public abstract void setMetadata(SearchSpec spec);
    public abstract SearchSpec getMetadata();

    public Spectrum()
    {
    }

    public String getSpectrumName()
    {
    	return spectrumName.substring(spectrumName.lastIndexOf("/")+1);
    }
}
