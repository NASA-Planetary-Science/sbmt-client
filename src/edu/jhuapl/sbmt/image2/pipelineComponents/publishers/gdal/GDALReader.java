package edu.jhuapl.sbmt.image2.pipelineComponents.publishers.gdal;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.function.Function;

import org.apache.commons.io.FilenameUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.ogr;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.PixelVector;
import edu.jhuapl.sbmt.layer.gdal.LayerLoaderBuilder;
import edu.jhuapl.sbmt.layer.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.layer.impl.PixelVectorDoubleFactory;
import edu.jhuapl.sbmt.layer.impl.ValidityChecker;
import edu.jhuapl.sbmt.layer.impl.ValidityChecker2d;
import edu.jhuapl.sbmt.pipeline.publisher.BasePipelinePublisher;

public class GDALReader extends BasePipelinePublisher<Layer>
{
	private String filename;
	private boolean isVectorFile;

	public GDALReader(String filename, boolean isVectorFile, ValidityChecker checker, double oobValue) throws InvalidGDALFileTypeException
	{
		this.filename = filename;
		this.isVectorFile = isVectorFile;
		if (FilenameUtils.getExtension(filename).equals("pgm")) throw new InvalidGDALFileTypeException("SBMT does not currently support PGM files.");
		outputs = new ArrayList<Layer>();
		loadData(checker, oobValue);
	}

	private void loadData(ValidityChecker checker, double oobValue)
	{
		if (!isVectorFile)
		{
			Dataset dataset = gdal.Open(filename);
			Layer layer = new LayerLoaderBuilder()
					.dataSet(dataset)
					.checker(checker)
					.build()
					.load();
//			 try
//				{
//					VTKDebug.previewLayer(layer, "Layer after translation");
//				}
//				catch (Exception e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			int numLayers = layer.dataSizes().get(0);
			PixelVector factory = new PixelVectorDoubleFactory().of(numLayers, oobValue);
			Hashtable<String, String> metadata = (Hashtable<String, String>)dataset.GetMetadata_Dict();

			for (int i=0; i < numLayers; i++)
			{
				 Function<Layer, Layer> transform = new LayerTransformFactory().slice(factory, i);
				 Layer singleLayer = transform.apply(layer);
//				 singleLayer = new LayerTransformFactory().swapIJ().apply(singleLayer);
//				 try
//				{
//					VTKDebug.previewLayer(singleLayer, "Single Layer on load");
//				}
//				catch (Exception e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				 outputs.add(singleLayer);
			}

			//insert a composite RGB(A) layer as the 0th layer
			if (dataset.GetDriver().getShortName().equals("PNG") || dataset.GetDriver().getShortName().equals("JPEG") || dataset.GetDriver().getShortName().equals("JPG"))
			{
				outputs.add(0, layer);
			}
		}
		else
		{
			DataSource datasource = ogr.Open(filename);
		}
	}

	public static void main(String[] args) throws InvalidGDALFileTypeException
	{
		NativeLibraryLoader.loadAllVtkLibraries();
        gdal.AllRegister();
        // This is a DART/LICIA/LUKE test image, which is a 3-band UNSIGNED byte
        // image that has both Didymos and Dimorphos visible and fairly large.
        // For a given (i, j), all 3 k-bands have the same pixel value.
        String sampleFile = Paths.get(System.getProperty("user.home"), //
                "Downloads", //
                "liciacube_luke_l0_717506291_294_01.fits").toString();
        String sampleFile2 = Paths.get(System.getProperty("user.home"), //
                "Desktop/SBMT Example Data files/", //
                "Global_20181213_20181201_Shape14_NatureEd.png").toString();
        ValidityChecker2d vc = (i, j, value) -> {
            return !(i == 2047 && j == 0);
        };
        GDALReader reader = new GDALReader(sampleFile2, false, vc, Double.NaN);

	}
}
