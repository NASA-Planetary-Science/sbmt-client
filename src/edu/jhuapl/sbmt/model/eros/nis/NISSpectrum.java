package edu.jhuapl.sbmt.model.eros.nis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.gui.eros.NISSearchPanel;
import edu.jhuapl.sbmt.model.image.InfoFileReader;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.InstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;

public class NISSpectrum extends BasicSpectrum
{

    static public final int DATE_TIME_OFFSET = 0;
    static public final int MET_OFFSET = 1;
    static public final int CURRENT_SEQUENCE_NUM_OFFSET = 1;
    static public final int DURATION_OFFSET = 3+2;
    static public final int MET_OFFSET_TO_MIDDLE_OFFSET = 4+2;
    static public final int CALIBRATED_GE_DATA_OFFSET = 96+2;
    static public final int CALIBRATED_GE_NOISE_OFFSET = 160+2;
    static public final int SPACECRAFT_POSITION_OFFSET = 224+2;
    static public final int FRUSTUM_OFFSET = 230+2;
    static public final int INCIDENCE_OFFSET = 242+2;
    static public final int EMISSION_OFFSET = 245+2;
    static public final int PHASE_OFFSET = 248+2;
    static public final int RANGE_OFFSET = 252+2;
    static public final int POLYGON_TYPE_FLAG_OFFSET = 258+2;
    static public final int NUMBER_OF_VERTICES_OFFSET = 259+2;
    static public final int POLYGON_START_COORDINATES_OFFSET = 260+2;
    String extension = "";
    private SpectrumInstrumentMetadataIO specIO;
    private InstrumentMetadata<SpectrumSearchSpec> instrumentMetadata;
    private File infoFile, spectrumFile;

    double[] spectrumErrors=new double[NIS.bandCentersLength];

    ISmallBodyModel smallBodyModel;

    public NISSpectrum(String filename, SpectrumInstrumentMetadataIO specIO, ISmallBodyModel smallBodyModel,
    		BasicSpectrumInstrument instrument) throws IOException
    {
        this(filename, specIO, smallBodyModel, instrument, false);
        double dx = MathUtil.vnorm(spacecraftPosition) + smallBodyModel.getBoundingBoxDiagonalLength();
        toSunVectorLength=dx;
    }

    public NISSpectrum(String filename, SpectrumInstrumentMetadataIO specIO, ISmallBodyModel smallBodyModel, BasicSpectrumInstrument instrument, boolean isCustom) throws IOException
    {
        super(filename, instrument, isCustom);
        this.smallBodyModel = smallBodyModel;
        xData = getBandCenters();
        extension = FilenameUtils.getExtension(serverpath.toString());
//        this.specIO = specIO;
//        instrumentMetadata = specIO.getInstrumentMetadata("NIS");
    }

    protected String getLocalInfoFilePathOnServer()
    {
    	String normalpath = SafeURLPaths.instance().getString(serverpath); //.substring(7);
    	return FilenameUtils.removeExtension(normalpath) + ".INFO";
    }

    protected String getLocalSpectrumFilePathOnServer()
    {
        return SafeURLPaths.instance().getString(serverpath); //.substring(7);
    }

    protected String getInfoFilePathOnServer()
    {
        if (isCustomSpectra)
        {
            return getLocalInfoFilePathOnServer();
        }
        else
        {
            String spectrumPath = getSpectrumPathOnServer().substring(0, getSpectrumPathOnServer().lastIndexOf("/"));
            return Paths.get(spectrumPath).getParent()
                    .resolveSibling("infofiles-corrected")
                    .resolve(FilenameUtils.getBaseName(getSpectrumPathOnServer()) + ".INFO")
                    .toString();
        }
    }

    public String getSpectrumPathOnServer()
    {
//  		spec = instrumentMetadata.getSpecs().get(0);


        if (isCustomSpectra)
        {
            return serverpath;
        }
        else
        {
            return Paths.get(serverpath).getParent()
                    .resolve(FilenameUtils.getBaseName(serverpath) + "." + extension)
                    .toString();
        }
    }

    public double getRange()
    {
        return range;
    }

    public double getDuration()
    {
        return duration;
    }

    public short getPolygonTypeFlag()
    {
        return polygon_type_flag;
    }

    public double[] getSpectrumErrors()
    {
        return spectrumErrors;
    }

  /*  public static String[] getDerivedParameters()
    {
        return derivedParameters;
    }*/


    public HashMap<String, String> getProperties() throws IOException
    {
        HashMap<String, String> properties = new LinkedHashMap<String, String>();

        String name = new File(this.fullpath).getName();
        properties.put("Name", name.substring(0, name.length()-4));

        properties.put("Date", dateTime.toString());

        properties.put("Day of Year", (new File(this.fullpath)).getParentFile().getName());

        //properties.put("Year", (new File(this.fullpath)).getParentFile().getParentFile().getName());

        properties.put("MET", (new File(this.fullpath)).getName().substring(2,11));

        properties.put("Duration", Double.toString(duration) + " seconds");

        String polygonTypeStr = "Missing value";
        switch(this.polygon_type_flag)
        {
        case 0:
            polygonTypeStr = "Full (all vertices on shape)";
            break;
        case 1:
            polygonTypeStr = "Partial (single contiguous set of vertices on shape)";
            break;
        case 2:
            polygonTypeStr = "Degenerate (multiple contiguous sets of vertices on shape)";
            break;
        case 3:
            polygonTypeStr = "Empty (no vertices on shape)";
            break;
        }
        properties.put("Polygon Type", polygonTypeStr);

        // Note \u00B0 is the unicode degree symbol
        String deg = "\u00B0";
        properties.put("Minimum Incidence", Double.toString(minIncidence)+deg);
        properties.put("Maximum Incidence", Double.toString(maxIncidence)+deg);
        properties.put("Minimum Emission", Double.toString(minEmission)+deg);
        properties.put("Maximum Emission", Double.toString(maxIncidence)+deg);
        properties.put("Minimum Phase", Double.toString(minPhase)+deg);
        properties.put("Maximum Phase", Double.toString(maxPhase)+deg);

        properties.put("Range", this.range + " km");
        properties.put("Spacecraft Position (km)",
                spacecraftPosition[0] + " " + spacecraftPosition[1] + " " + spacecraftPosition[2]);

        return properties;
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


    @Override
    public void saveSpectrum(File file) throws IOException
    {
        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

        String nl = System.getProperty("line.separator");

        HashMap<String,String> properties = getProperties();
        for (String key : properties.keySet())
        {
            String value = properties.get(key);

            // Replace unicode degrees symbol (\u00B0) with text ' deg'
            value = value.replace("\u00B0", " deg");

            out.write(key + " = " + value + nl);
        }

        out.write(nl + nl + "Band Wavelength(nm) Reflectance" + nl);
        for (int i=0; i<instrument.getBandCenters().length; ++i)
        {
            out.write((i+1) + " " + instrument.getBandCenters()[i] + " " + spectrum[i] + nl);
        }

        NISSpectrumMath spectrumMath = NISSpectrumMath.getSpectrumMath();

        out.write(nl + nl + "Derived Values" + nl);
        for (int i=0; i<spectrumMath.getDerivedParameters().length; ++i)
        {
            out.write(spectrumMath.getDerivedParameters()[i] + " = " + evaluateDerivedParameters(i) + nl);
        }

        for (int i=0; i<spectrumMath.getAllUserDefinedDerivedParameters().size(); ++i)
        {
            out.write(spectrumMath.getAllUserDefinedDerivedParameters().get(i).GetFunction() + " = " + spectrumMath.evaluateUserDefinedDerivedParameters(i, spectrum) + nl);
        }

        out.close();
    }


//    @Override
//    public double[] getChannelColor()
//    {
//        double[] color = new double[3];
//        for (int i=0; i<3; ++i)
//        {
//            double val = 0.0;
//            if (channelsToColorBy[i] < instrument.getBandCenters().length)
//            {
//                val = spectrum[channelsToColorBy[i]];
//            }
//            else if (channelsToColorBy[i] < instrument.getBandCenters().length + instrument.getSpectrumMath().getDerivedParameters().length)
//                val = evaluateDerivedParameters(channelsToColorBy[i]-instrument.getBandCenters().length);
//            else
//                val = instrument.getSpectrumMath().evaluateUserDefinedDerivedParameters(channelsToColorBy[i]-instrument.getBandCenters().length-instrument.getSpectrumMath().getDerivedParameters().length, spectrum);
//
//            if (val < 0.0)
//                val = 0.0;
//            else if (val > 1.0)
//                val = 1.0;
//
//            double slope = 1.0 / (channelsColoringMaxValue[i] - channelsColoringMinValue[i]);
//            color[i] = slope * (val - channelsColoringMinValue[i]);
//        }
//
//        return color;
//    }

    @Override
    public double evaluateDerivedParameters(int channel)
    {
        switch(channel)
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



    @Override
    public int getNumberOfBands()
    {
        return NIS.bandCentersLength;
    }

    @Override
    public String getxAxisUnits()
    {
        return "";
    }

    @Override
    public String getyAxisUnits()
    {
        return "";
    }

    @Override
    public String getDataName()
    {
        return serverpath;
    }

	@Override
	public void readPointingFromInfoFile()
	{
		InfoFileReader reader = null;
        if (!isCustomSpectra)
        {
            infoFile = FileCache.getFileFromServer(getInfoFilePathOnServer());
            reader = new InfoFileReader(infoFile.getAbsolutePath());
        }
        else
        {
            infoFile = new File(getInfoFilePathOnServer());
            reader = new InfoFileReader(infoFile.toString());
        }
        reader.read();

        Vector3D origin = new Vector3D(reader.getSpacecraftPosition());
        Vector3D fovUnit = new Vector3D(reader.getFrustum2()).normalize(); // for whatever
                                                               // reason,
                                                               // frustum2
                                                               // contains the
                                                               // vector along
                                                               // the field of
                                                               // view cone
        Vector3D boresightUnit = new Vector3D(reader.getBoresightDirection()).normalize();
        Vector3D lookTarget = origin
                .add(boresightUnit.scalarMultiply(origin.getNorm()));

        double fovDeg = Math
                .toDegrees(Vector3D.angle(fovUnit, boresightUnit) * 2.);
        toSunUnitVector = new Vector3D(reader.getSunPosition()).normalize();
        Frustum frustum = new Frustum(origin.toArray(), lookTarget.toArray(),
                boresightUnit.orthogonal().toArray(), fovDeg, fovDeg);
        frustum1 = frustum.ul;
        frustum2 = frustum.ur;
        frustum3 = frustum.lr;
        frustum4 = frustum.ll;
        spacecraftPosition = frustum.origin;
	}

	@Override
	public void readSpectrumFromFile()
	{
		if (!isCustomSpectra)
        {
			if (fullpath == null) getFullPath();
        }
        else
        {
            fullpath = getLocalSpectrumFilePathOnServer();
        }
//		if (fullpath == null) getFullPath();
		List<String> values = null;
		try
		{
			values = FileUtil.getFileWordsAsStringList(fullpath);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        dateTime = new DateTime(values.get(DATE_TIME_OFFSET), DateTimeZone.UTC);

        double metOffsetToMiddle = Double.parseDouble(values.get(MET_OFFSET_TO_MIDDLE_OFFSET));
        dateTime = dateTime.plusMillis((int)metOffsetToMiddle);

        duration = Double.parseDouble(values.get(DURATION_OFFSET));
        minIncidence = Double.parseDouble(values.get(INCIDENCE_OFFSET+1));
        maxIncidence = Double.parseDouble(values.get(INCIDENCE_OFFSET+2));
        minEmission= Double.parseDouble(values.get(EMISSION_OFFSET+1));
        maxEmission = Double.parseDouble(values.get(EMISSION_OFFSET+2));
        minPhase = Double.parseDouble(values.get(PHASE_OFFSET+1));
        maxPhase= Double.parseDouble(values.get(PHASE_OFFSET+2));
        range = Double.parseDouble(values.get(RANGE_OFFSET));
        polygon_type_flag = Short.parseShort(values.get(POLYGON_TYPE_FLAG_OFFSET));

        int footprintSize = Integer.parseInt(values.get(NUMBER_OF_VERTICES_OFFSET));
        for (int i=0; i<footprintSize; ++i)
        {
            int latIdx = POLYGON_START_COORDINATES_OFFSET + i*2;
            int lonIdx = POLYGON_START_COORDINATES_OFFSET + i*2 + 1;

            latLons.add(new LatLon(Double.parseDouble(values.get(latIdx)) * Math.PI / 180.0,
                                   (360.0-Double.parseDouble(values.get(lonIdx))) * Math.PI / 180.0));
        }


        for (int i=0; i<getNumberOfBands(); ++i)
        {
            // The following min and max clamps the value between 0 and 1.
            spectrum[i] = Math.min(1.0, Math.max(0.0, Double.parseDouble(values.get(CALIBRATED_GE_DATA_OFFSET + i))));
            spectrumErrors[i] = Double.parseDouble(values.get(CALIBRATED_GE_NOISE_OFFSET + i));
        }

        for (int i=0; i<3; ++i)
            spacecraftPosition[i] = Double.parseDouble(values.get(SPACECRAFT_POSITION_OFFSET + i));
        for (int i=0; i<3; ++i)
            frustum1[i] = Double.parseDouble(values.get(FRUSTUM_OFFSET + i));
        for (int i=0; i<3; ++i)
            frustum2[i] = Double.parseDouble(values.get(FRUSTUM_OFFSET + 3 + i));
        for (int i=0; i<3; ++i)
            frustum3[i] = Double.parseDouble(values.get(FRUSTUM_OFFSET + 6 + i));
        for (int i=0; i<3; ++i)
            frustum4[i] = Double.parseDouble(values.get(FRUSTUM_OFFSET + 9 + i));
        MathUtil.vhat(frustum1, frustum1);
        MathUtil.vhat(frustum2, frustum2);
        MathUtil.vhat(frustum3, frustum3);
        MathUtil.vhat(frustum4, frustum4);

        frustumCenter=new double[3];
        for (int i=0; i<3; i++)
            frustumCenter[i]=frustum1[i]+frustum2[i]+frustum3[i]+frustum4[i];


        double dx = MathUtil.vnorm(spacecraftPosition) + smallBodyModel.getBoundingBoxDiagonalLength();
        toSunVectorLength=dx;
        toSunUnitVector=NISSearchPanel.getToSunUnitVector(serverpath.replace("/NIS/2000/", ""));
	}

}
