package edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra;

import java.io.File;
import java.io.IOException;

import org.joda.time.DateTime;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;

public abstract class Spectrum //extends AbstractModel implements PropertyChangeListener
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

//    public abstract void addPropertyChangeListener(PropertyChangeListener l);
//    public abstract void removePropertyChangeListener(PropertyChangeListener l);
//
//    public abstract void shiftFootprintToHeight(double d);
//    public abstract vtkPolyData getUnshiftedFootprint();
//    public abstract vtkPolyData getShiftedFootprint();
//    public abstract vtkPolyData getSelectionPolyData();
//    protected vtkActor outlineActor = new vtkActor();

//    public abstract void setShowFrustum(boolean show);
//    public abstract void setShowOutline(boolean show);
//    public abstract void setShowToSunVector(boolean show);

//    public abstract boolean isFrustumShowing();
//    public abstract boolean isOutlineShowing();
//    public abstract boolean isToSunVectorShowing();

    public abstract double[] getSpacecraftPosition();
    public abstract double[] getFrustumCenter();
    public abstract double[] getFrustumCorner(int i);
    public abstract double[] getFrustumOrigin();
    public abstract double[] getToSunUnitVector();

    public abstract void setChannelColoring(int[] channels, double[] mins, double[] maxs);
//    public abstract void updateChannelColoring();
    public abstract double evaluateDerivedParameters(int channel);
//    public abstract double[] getChannelColor();

//    public abstract void setSelected();
//    public abstract void setUnselected();
//    public abstract boolean isSelected();

    public abstract void saveSpectrum(File file) throws IOException;

    public abstract void setColoringStyle(SpectrumColoringStyle coloringStyle);
    public abstract void setMetadata(SearchSpec spec);
    public abstract SearchSpec getMetadata();

    protected SpectrumKeyInterface key;

    public Spectrum()
    {
        this.key = null;
    }

    public Spectrum(SpectrumKeyInterface key)
    {
        this.key = key;
    }

    public SpectrumKeyInterface getKey()
    {
        return key;
    }

    public void setKey(SpectrumKeyInterface key)
    {
        this.key = key;
    }

    public String getSpectrumName()
    {
        return new File(key.getName()).getName();
    }
}
