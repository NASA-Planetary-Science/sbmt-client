package edu.jhuapl.sbmt.image2.modules.io.builtIn;

import java.io.IOException;
import java.util.ArrayList;

import vtk.vtkImageData;
import vtk.vtkPNGReader;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
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

import nom.tam.fits.FitsException;

//TODO: this is a placeholder until we get GDAL support in place - this reads data into a vtkImageData and then back to a layer so it goes with the FITS paradigm already established
//Once GDAL is in place this will go right into a Layer
public class BuiltInPNGReader extends BasePipelinePublisher<Layer>
{

	private String filename;

//	protected static final LayerDoubleFactory LayerFactory = new LayerDoubleFactory();
	protected static final PixelDoubleFactory PixelScalarFactory = new PixelDoubleFactory();
	protected static final PixelVectorDoubleFactory PixelVectorFactory = new PixelVectorDoubleFactory();
	protected static final LayerTransformFactory TransformFactory = new LayerTransformFactory();
	protected static final LayerDoubleTransformFactory DoubleTransformFactory = new LayerDoubleTransformFactory();

	protected static final double TestOOBValue = -100.0;

	int imageHeight = 0;
	int imageWidth = 0;
	private double[] fill;
	private double[][] array2D = null;

	public static void main(String[] args) throws FitsException, IOException, Exception
	{
		NativeLibraryLoader.loadVtkLibraries();
		BuiltInPNGReader reader = new BuiltInPNGReader("/Users/steelrj1/Desktop/image_map.png");
	}

	public BuiltInPNGReader(String filename) throws IOException, Exception
	{
		this.filename = filename;

        loadData();
        Layer layer;
        layer = ofScalar(imageWidth, imageHeight);
//        layer = TransformFactory.rotateCCW().apply(layer);
        // layer = DoubleTransformFactory.linearInterpolate(537,
        // 412).apply(layer);
//        displayLayer("PNG Layer", layer, 0, null);
        outputs = new ArrayList<Layer>();
        outputs.add(layer);
	}

	private void loadData() throws IOException
	{
		vtkPNGReader reader = new vtkPNGReader();
		reader.SetFileName(filename);
		reader.Update();
		vtkImageData rawImage = new vtkImageData();
		rawImage.DeepCopy(reader.GetOutput());

		imageWidth = rawImage.GetDimensions()[0];
		imageHeight = rawImage.GetDimensions()[1];
		array2D = new double[imageWidth][imageHeight];

		for (int i=0; i<imageWidth; i++)
		{
			for (int j=0; j<imageHeight; j++)
			{
				array2D[i][j] = rawImage.GetScalarComponentAsDouble(i, j, 0, 0);
			}
		}

	}

	protected Layer ofScalar(int iSize, int jSize)
	{
		// Make builders for both the layer and the range checker. Use the
        // dimensions from the fits file to set I, J sizes...
        LayerDoubleBuilder layerBuilder = new LayerDoubleBuilder();
        RangeGetterDoubleBuilder rangeBuilder = new RangeGetterDoubleBuilder();

		DoubleGetter2d doubleGetter = (i, j) ->
		{
			return array2D[i][j];
		};

		layerBuilder.doubleGetter(doubleGetter, iSize, jSize);
	    rangeBuilder.getter(doubleGetter, iSize, jSize);

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

//		return checker != null ? //
//				LayerFactory.ofScalar(doubleGetter, iSize, jSize, checker) : //
//				LayerFactory.ofScalar(doubleGetter, iSize, jSize);
	}

	protected void displayLayer(String message, Layer layer, int displayKsize, Double invalidValueSubstitute)
	{
		System.out.println("************************************************");
		System.out.println(message);
		System.out.println("PNG Loading Test");
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
