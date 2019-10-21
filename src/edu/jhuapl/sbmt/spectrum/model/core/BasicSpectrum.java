package edu.jhuapl.sbmt.spectrum.model.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.joda.time.DateTime;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.Spectrum;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;


public abstract class BasicSpectrum extends Spectrum
{
    protected BasicSpectrumInstrument instrument;

    protected String fullpath; // The actual path of the spectrum stored on the
                               // local disk (after downloading from the server)
    protected String serverpath; // The path of the spectrum as passed into the
                                 // constructor. This is not the
    // same as fullpath but instead corresponds to the name needed to download
    // the file from the server (excluding the hostname).

    protected Vector3D toSunUnitVector;

    protected double[] frustumCenter;

    protected double toSunVectorLength;

    protected double[] spacecraftPosition = new double[3];
    protected double[] frustum1 = new double[3];
    protected double[] frustum2 = new double[3];
    protected double[] frustum3 = new double[3];
    protected double[] frustum4 = new double[3];

    protected int[] channelsToColorBy = { 0, 0, 0 };
    protected double[] channelsColoringMinValue = { 0.0, 0.0, 0.0 };
    protected double[] channelsColoringMaxValue = { 0.05, 0.05, 0.05 };

    protected DateTime dateTime;
    protected double duration;
    protected short polygon_type_flag;
    protected double range;
    protected List<LatLon> latLons = new ArrayList<LatLon>();
    protected double[] spectrum;
    protected Double[] xData;
    protected double minIncidence;
    protected double maxIncidence;
    protected double minEmission;
    protected double maxEmission;
    protected double minPhase;
    protected double maxPhase;

    protected String dataName;
    protected String xAxisUnits;
    protected String yAxisUnits;
    protected SearchSpec spec;


    protected SpectrumColoringStyle coloringStyle = SpectrumColoringStyle.RGB;

    public BasicSpectrum(String filename,
            BasicSpectrumInstrument instrument) throws IOException
    {
        this(filename, instrument, false);
    }

    public BasicSpectrum(String filename,
            BasicSpectrumInstrument instrument, boolean isCustom) throws IOException
    {
        this.serverpath = filename; // path on server relative to data
                                    // repository root (e.g. relative to
                                    // /project/nearsdc/data/)
        this.instrument = instrument; //

        spectrum=new double[getNumberOfBands()];
        this.isCustomSpectra = isCustom;
        spectrumName = filename;
    }

    public abstract int getNumberOfBands();
    public abstract void readPointingFromInfoFile();
    public abstract void readSpectrumFromFile();

    /**
     * Evaluates the derived parameters for a given channel (custom color definition)
     */
    @Override
    public double evaluateDerivedParameters(int channel)
    {
        switch (channel)
        {
        case 0:
             return spectrum[35] - spectrum[4];
        case 1:
             return spectrum[0] - spectrum[4];
        case 2:
             return spectrum[51] - spectrum[35];
        default:
            return 0.0;
        }
    }

    //GETTERS/SETTERS

    public double[] getSpectrum()
    {
        return spectrum;
    }

    @Override
    public BasicSpectrumInstrument getInstrument()
    {
        return instrument;
    }

    @Override
    public Double[] getBandCenters()
    {
        return instrument.getBandCenters();
    }

    public double[] getToSunUnitVector()
    {
        return toSunUnitVector.toArray();
    }

    public void setChannelColoring(int[] channels, double[] mins, double[] maxs)
    {
        for (int i = 0; i < 3; ++i)
        {
            channelsToColorBy[i] = channels[i];
            channelsColoringMinValue[i] = mins[i];
            channelsColoringMaxValue[i] = maxs[i];
        }
    }

    @Override
    public DateTime getDateTime()
    {
        return dateTime;
    }

    @Override
    public String getFullPath()
    {
    	File file = FileCache.getFileFromServer(serverpath);
    	this.fullpath = file.getAbsolutePath();
        return fullpath;
    }

	public double[] getSpacecraftPosition()
    {
        return spacecraftPosition;
    }

    public double[] getFrustumCenter()
    {
        return frustumCenter;
    }

    public double[] getFrustumCorner(int i)
    {
        switch (i)
        {
        case 0:
            return frustum1;
        case 1:
            return frustum2;
        case 2:
            return frustum3;
        case 3:
            return frustum4;
        }
        return null;
    }

    public double[] getFrustumOrigin()
    {
        return spacecraftPosition;
    }

    @Override
    public String getSpectrumPathOnServer()
    {
        return serverpath;
    }

    public String getDataName()
    {
        return dataName;
    }

    public void setDataName(String dataName)
    {
        this.dataName = dataName;
    }

    public String getxAxisUnits()
    {
        return xAxisUnits;
    }

    public void setxAxisUnits(String xAxisUnits)
    {
        this.xAxisUnits = xAxisUnits;
    }

    public String getyAxisUnits()
    {
        return yAxisUnits;
    }

    public void setyAxisUnits(String yAxisUnits)
    {
        this.yAxisUnits = yAxisUnits;
    }

    public Double[] getxData()
    {
        return xData;
    }

    public SpectrumColoringStyle getColoringStyle()
    {
        return coloringStyle;
    }

    public void setColoringStyle(SpectrumColoringStyle coloringStyle)
    {
        this.coloringStyle = coloringStyle;
    }

    public void setMetadata(SearchSpec spec)
    {
        this.spec = spec;
    }

    public SearchSpec getMetadata()
    {
        return this.spec;
    }

	public double getToSunVectorLength()
	{
		return toSunVectorLength;
	}

	public void setToSunVectorLength(double toSunVectorLength)
	{
		this.toSunVectorLength = toSunVectorLength;
	}

	public String getServerpath()
	{
		return serverpath;
	}

	public double[] getFrustum1()
	{
		return frustum1;
	}

	public double[] getFrustum2()
	{
		return frustum2;
	}

	public double[] getFrustum3()
	{
		return frustum3;
	}

	public double[] getFrustum4()
	{
		return frustum4;
	}

	public int[] getChannelsToColorBy()
	{
		return channelsToColorBy;
	}

	public double[] getChannelsColoringMinValue()
	{
		return channelsColoringMinValue;
	}

	public double[] getChannelsColoringMaxValue()
	{
		return channelsColoringMaxValue;
	}

	public double getDuration()
	{
		return duration;
	}

	public short getPolygon_type_flag()
	{
		return polygon_type_flag;
	}

	public double getRange()
	{
		return range;
	}

	public List<LatLon> getLatLons()
	{
		return latLons;
	}

	public double getMinIncidence()
	{
		return minIncidence;
	}

	public double getMaxIncidence()
	{
		return maxIncidence;
	}

	public double getMinEmission()
	{
		return minEmission;
	}

	public double getMaxEmission()
	{
		return maxEmission;
	}

	public double getMinPhase()
	{
		return minPhase;
	}

	public double getMaxPhase()
	{
		return maxPhase;
	}

	public SearchSpec getSpec()
	{
		return spec;
	}

	public void setDateTime(DateTime dateTime)
	{
		this.dateTime = dateTime;
	}
}
