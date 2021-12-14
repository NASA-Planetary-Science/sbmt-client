package edu.jhuapl.sbmt.image2.modules.io.builtIn;

import java.io.IOException;
import java.util.ArrayList;

import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.api.Pixel;
import edu.jhuapl.sbmt.image2.impl.DoubleBuilderBase.DoubleGetter2d;
import edu.jhuapl.sbmt.image2.impl.DoubleBuilderBase.ScalarValidityChecker;
import edu.jhuapl.sbmt.image2.impl.LayerDoubleBuilder;
import edu.jhuapl.sbmt.image2.impl.LayerDoubleTransformFactory;
import edu.jhuapl.sbmt.image2.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.image2.impl.PixelDoubleFactory;
import edu.jhuapl.sbmt.image2.impl.PixelVectorDoubleFactory;
import edu.jhuapl.sbmt.image2.impl.RangeGetterDoubleBuilder;
import edu.jhuapl.sbmt.image2.impl.ValidityCheckerDoubleFactory;
import edu.jhuapl.sbmt.image2.pipeline.publisher.BasePipelinePublisher;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.header.Standard;

public class BuiltInFitsReader extends BasePipelinePublisher<Layer>
{

    public static void main(String[] args) throws FitsException, IOException
    {
        BuiltInFitsReader reader = new BuiltInFitsReader("/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.FIT", new double[] {});
    }

    private String filename;
    private double[] fill;

    protected static final PixelDoubleFactory PixelScalarFactory = new PixelDoubleFactory();
    protected static final PixelVectorDoubleFactory PixelVectorFactory = new PixelVectorDoubleFactory();
    protected static final LayerTransformFactory TransformFactory = new LayerTransformFactory();
    protected static final LayerDoubleTransformFactory DoubleTransformFactory = new LayerDoubleTransformFactory();

    protected static final double TestOOBValue = -100.0;

    private float[][] array2D = null;
    // height is axis 0
    private int fitsHeight = 0;
    // for 2D pixel arrays, width is axis 1, for 3D pixel arrays, width axis is
    // 2
    private int fitsWidth = 0;
    // for 2D pixel arrays, depth is 0, for 3D pixel arrays, depth axis is 1
    private int fitsDepth = 0;

    private Double dataMin = null;
    private Double dataMax = null;

    public BuiltInFitsReader(String filename, double[] fill) throws FitsException, IOException
    {
        this.filename = filename;
        this.fill = fill;
        loadData();

        Layer layer = ofScalar();

        layer = TransformFactory.rotateCCW().apply(layer);
        // layer = DoubleTransformFactory.linearInterpolate(537,
        // 412).apply(layer);
//         displayLayer("FITS 1D Layer", layer, 0, null);
        outputs = new ArrayList<Layer>();
        outputs.add(layer);
    }

    private void loadData() throws IOException, FitsException
    {
        int[] fitsAxes = null;
        int fitsNAxes = 0;

        // single file images (e.g. LORRI and LEISA)
        try (Fits f = new Fits(filename))
        {
            BasicHDU<?> hdu = f.getHDU(0);

            fitsAxes = hdu.getAxes();
            fitsNAxes = fitsAxes.length;
            fitsHeight = fitsAxes[0];
            fitsWidth = fitsNAxes == 3 ? fitsAxes[2] : fitsAxes[1];
            fitsDepth = fitsNAxes == 3 ? fitsAxes[1] : 1;

            // Do not use BasicHDU to get these optional keywords. BasicHDU
            // would return a value of 0. for missing keywords. We need to SKIP
            // missing DATAMIN/DATAMAX. Use the Header interface instead.
            Header h = hdu.getHeader();
            dataMin = h.findCard(Standard.DATAMIN) != null ? h.getDoubleValue(Standard.DATAMIN) : null;
            dataMax = h.findCard(Standard.DATAMAX) != null ? h.getDoubleValue(Standard.DATAMAX) : null;

            Object data = hdu.getData().getData();

            if (data instanceof float[][])
            {
                array2D = (float[][]) data;
            }
            else if (data instanceof short[][])
            {
                short[][] arrayS = (short[][]) data;
                array2D = new float[fitsHeight][fitsWidth];

                for (int i = 0; i < fitsHeight; ++i)
                    for (int j = 0; j < fitsWidth; ++j)
                    {
                        array2D[i][j] = arrayS[i][j];
                    }
            }
            else if (data instanceof double[][])
            {
                double[][] arrayDouble = (double[][]) data;
                array2D = new float[fitsHeight][fitsWidth];

                for (int i = 0; i < fitsHeight; ++i)
                    for (int j = 0; j < fitsWidth; ++j)
                    {
                        array2D[i][j] = (float) arrayDouble[i][j];
                    }
            }
            else if (data instanceof byte[][])
            {
                byte[][] arrayB = (byte[][]) data;
                array2D = new float[fitsHeight][fitsWidth];

                for (int i = 0; i < fitsHeight; ++i)
                    for (int j = 0; j < fitsWidth; ++j)
                    {
                        array2D[i][j] = arrayB[i][j] & 0xFF;
                    }
            }
            // WARNING: THIS IS A TOTAL HACK TO SUPPORT DART LUKE TEST IMAGES:
            else if (data instanceof byte[][][])
            {
                // DART LUKE images are color: 3-d slab with the 3rd
                // dimension being RGB, but the first test images are
                // monochrome. Thus, in order to process the images, making
                // this temporary hack.
                byte[][][] arrayB = (byte[][][]) data;

                // Override the default setup used for other 3-d images.
                fitsDepth = 1;
                fitsHeight = arrayB[0].length;
                fitsWidth = arrayB[0][0].length;

                array2D = new float[fitsHeight][fitsWidth];

                for (int i = 0; i < fitsHeight; ++i)
                    for (int j = 0; j < fitsWidth; ++j)
                    {
                        array2D[i][j] = arrayB[0][i][j] & 0xFF;
                    }
            }
            else
            {
                System.out.println("Data type not supported: " + data.getClass().getCanonicalName());
                return;
            }

            // load in calibration info
            // loadImageCalibrationData(f);
        }

    }

    protected Layer ofScalar()
    {
        // Make builders for both the layer and the range checker. Use the
        // dimensions from the fits file to set I, J sizes...
        LayerDoubleBuilder layerBuilder = new LayerDoubleBuilder();
        RangeGetterDoubleBuilder rangeBuilder = new RangeGetterDoubleBuilder();

        // Adapt the array to the appropriate getter interface. Both builders
        // need this.
        DoubleGetter2d doubleGetter = (i, j) -> {
            return array2D[i][j];
        };

        layerBuilder.doubleGetter(doubleGetter, fitsHeight, fitsWidth);
        rangeBuilder.getter(doubleGetter, fitsHeight, fitsWidth);

        // Also tell the range builder (only) any min or max values that were
        // defined
        // by keywords.
        if (dataMin != null)
        {
            rangeBuilder.min(dataMin);
        }
        if (dataMax != null)
        {
            rangeBuilder.max(dataMax);
        }

        // Both builders need to know how to check for validity as well.
        if (fill != null && fill.length > 0)
        {
            ScalarValidityChecker checker = new ValidityCheckerDoubleFactory().scalar(fill);

            layerBuilder.checker(checker);
            rangeBuilder.checker(checker);
        }

        // Here's the trick: build the range getter first and inject it into the
        // layer builder just before building the layer.
        layerBuilder.rangeGetter(rangeBuilder.build());

        return layerBuilder.build();
    }

    protected void displayLayer(String message, Layer layer, int displayKsize, Double invalidValueSubstitute)
    {
        System.out.println("************************************************");
        System.out.println(message);
        System.out.println("1D Fits Test");
        System.out.println("************************************************");

        Pixel pixel = displayKsize == 0 ? //
                PixelScalarFactory.of(0.0, TestOOBValue, invalidValueSubstitute) : //
                PixelVectorFactory.of(displayKsize, TestOOBValue, invalidValueSubstitute);
        display("Loaded layer:", layer, pixel);
        System.out.println();
    }

    /**
     * Display the state of a {@link Layer}, prefaced by a message, and using
     * the specified {@link Pixel} instance to retrieve data from the layer.
     *
     * @param message the message used as a preface
     * @param layer the layer whose state to display
     * @param pixel the pixel used to get data from the layer
     */
    protected final void display(String message, Layer layer, Pixel pixel)
    {
        System.out.println(message);
        for (int row = -1; row <= layer.jSize(); ++row)
        {
            StringBuilder builder = new StringBuilder();
            String delim = "";
            for (int column = -1; column <= layer.iSize(); ++column)
            {
                layer.get(column, row, pixel);
                builder.append(delim);
                builder.append(pixel);
                delim = ", ";
            }
            System.out.println(builder.toString());
        }
    }

}
