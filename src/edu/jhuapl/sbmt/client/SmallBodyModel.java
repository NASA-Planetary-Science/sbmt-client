package edu.jhuapl.sbmt.client;

import java.io.File;
import java.io.IOException;

import vtk.vtkFloatArray;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.model.ColoringInfo;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;

import nom.tam.fits.AsciiTableHDU;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;

public class SmallBodyModel extends GenericPolyhedralModel
{
    private static final String[] DEFAULT_COLORING_NAMES = {
            SlopeStr, ElevStr, GravAccStr, GravPotStr
    };
    private static final String[] DEFAULT_COLORING_UNITS = {
            SlopeUnitsStr, ElevUnitsStr, GravAccUnitsStr, GravPotUnitsStr
    };
    private static final ColoringValueType DEFAULT_COLORING_VALUE_TYPE = ColoringValueType.CELLDATA;

    public SmallBodyViewConfig getSmallBodyConfig()
    {
        return (SmallBodyViewConfig)getConfig();
    }

    /**
     * Default constructor. Must be followed by a call to setSmallBodyPolyData.
     */
    public SmallBodyModel()
    {
        super();
    }

    /**
     * Convenience method for initializing a SmallBodyModel with just a vtkPolyData.
     * @param polyData
     */
    public SmallBodyModel(vtkPolyData polyData)
    {
        super(polyData);
    }

    /**
     * Note that name is used to name this small body model as a whole including all
     * resolution levels whereas modelNames is an array of names that is specific
     * for each resolution level.
     */
    public SmallBodyModel(
            BodyViewConfig config,
            String[] modelNames,
            String[] modelFiles,
            String[] coloringFiles,
            String[] coloringNames,
            String[] coloringUnits,
            boolean[] coloringHasNulls,
            String[] imageMapNames,
            ColoringValueType coloringValueType,
            boolean lowestResolutionModelStoredInResource)
    {
        super(config, modelNames, modelFiles, coloringFiles, coloringNames, coloringUnits, coloringHasNulls, imageMapNames, coloringValueType, lowestResolutionModelStoredInResource);

//        if (lowestResolutionModelStoredInResource)
//            defaultModelFile = ConvertResourceToFile.convertResourceToRealFile(
//                    this,
//                    modelFiles[0],
//                    Configuration.getApplicationDataDir());
//        else
//            defaultModelFile = FileCache.getFileFromServer(modelFiles[0]);
//
//        initialize(defaultModelFile);
    }

    protected void initializeConfigParameters(
            String[] imageMapNames,
            boolean lowestResolutionModelStoredInResource)
    {
        SmallBodyViewConfig config = getSmallBodyConfig();
        final String[] modelFiles = {
                serverPath("shape/shape0.vtk.gz"),
                serverPath("shape/shape1.vtk.gz"),
                serverPath("shape/shape2.vtk.gz"),
                serverPath("shape/shape3.vtk.gz")
        };
        final String[] coloringFiles = {
                serverPath("coloring/Slope"),
                serverPath("coloring/Elevation"),
                serverPath("coloring/GravitationalAcceleration"),
                serverPath("coloring/GravitationalPotential")
        };
        final boolean[] coloringHasNulls = null;
        initializeConfigParameters(
                modelFiles,
                config.hasColoringData ? coloringFiles : null,
                config.hasColoringData ? DEFAULT_COLORING_NAMES : null,
                config.hasColoringData ? DEFAULT_COLORING_UNITS : null,
                coloringHasNulls,
                imageMapNames,
                config.hasColoringData ? DEFAULT_COLORING_VALUE_TYPE : null,
                lowestResolutionModelStoredInResource);
    }

    @Override
    protected void loadColoringDataFits(File file, ColoringInfo info) throws IOException
    {
        vtkFloatArray array = new vtkFloatArray();

        array.SetNumberOfComponents(1);
        if (getColoringValueType() == ColoringValueType.POINT_DATA)
            array.SetNumberOfTuples(getSmallBodyPolyData().GetNumberOfPoints());
        else
            array.SetNumberOfTuples(getSmallBodyPolyData().GetNumberOfCells());


        Fits fits = null;
        try {
            fits = new Fits(file);
            fits.read();

            BasicHDU<?> hdu = fits.getHDU(1);
            if (hdu instanceof AsciiTableHDU)
            {
                AsciiTableHDU athdu = (AsciiTableHDU)hdu;
//                int ncols = athdu.getNCols();
                int nrows = athdu.getNRows();

//                System.out.println("Reading Ancillary FITS Data");
//                System.out.println("Number of Plates: " + nrows);

                float[] scalars = (float[])athdu.getColumn(FITS_SCALAR_COLUMN_INDEX);

                if (nrows!=getSmallBodyPolyData().GetNumberOfCells())
                    throw new IOException("# rows on file ("+nrows+") != # faces ("+getSmallBodyPolyData().GetNumberOfCells()+")");

                for (int j=0; j<nrows; j++)
                {
                    float value = scalars[j];
                    array.SetTuple1(j, value);
                }
            }
            else if (hdu instanceof BinaryTableHDU)
            {
                BinaryTableHDU athdu = (BinaryTableHDU)hdu;
//                int ncols = athdu.getNCols();
                int nrows = athdu.getNRows();

//                System.out.println("Reading Ancillary FITS Data");
//                System.out.println("Number of Plates: " + nrows);

                if (nrows!=getSmallBodyPolyData().GetNumberOfCells())
                    throw new IOException("# rows on file ("+nrows+") != # faces ("+getSmallBodyPolyData().GetNumberOfCells()+")");

                float[] scalars = (float[])athdu.getColumn(FITS_SCALAR_COLUMN_INDEX);
                for (int j=0; j<nrows; j++)
                {
                    float value = scalars[j];
                    array.SetTuple1(j, value);
                }
            }

        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (fits != null) fits.close();
        }

        info.coloringValues = array;
    }


}
