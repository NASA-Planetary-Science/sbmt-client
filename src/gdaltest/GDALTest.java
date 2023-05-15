package gdaltest;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

import edu.jhuapl.sbmt.util.SBMTNativeLibraryLoader;

public class GDALTest
{
	public GDALTest()
	{
		SBMTNativeLibraryLoader.loadGDALLibraries();
		gdal.AllRegister();
		for (int i=0; i<gdal.GetDriverCount(); i++)
		{
			System.out.println("GDALTest: GDALTest: driver " + gdal.GetDriver(i).getLongName());
		}
//		loadAndDetail("/Users/steelrj1/Desktop/SBMT Example Data files/M0125956777F4_2P_IOF_DBL.FIT");
//		loadAndDetail("/Users/steelrj1/Desktop/SBMT Example Data files/Global_20181213_20181201_Shape14_NatureEd.png");
//		loadAndDetail("/Users/steelrj1/Desktop/SBMT Example Data files/M0145810520F6_2P_CIF_DBL.FIT");
//		loadAndDetail("/Users/steelrj1/Downloads/m0153784905f4_2p_iof_dbl_bp.fit");
		loadAndDetailVector("/Users/steelrj1/Desktop/SBMT Example Data files/bulkunitlines_titan2000.shp");

//		loadAndDetail("/Users/steelrj1/Desktop/SBMT Example Data files/EN1064441328M.nomap.cub");
//		loadAndDetail("/Users/steelrj1/Desktop/SBMT Example Data files/EN1064441328M.map.cub");

	}

	private void loadAndDetailVector(String filename)
	{
		DataSource dataset = ogr.Open(filename);
		System.out.println("---------------------------------------");
		System.out.println("GDALTest: loadAndDetailVector: desc " + dataset.GetDescription());
		System.out.println("GDALTest: loadAndDetailVector: layer count " + dataset.GetLayerCount());
		System.out.println("GDALTest: loadAndDetailVector: name " + dataset.GetName());
		System.out.println("GDALTest: loadAndDetailVector: driver name " + dataset.GetDriver().getName());
		System.out.println("GDALTest: loadAndDetailVector: layer 0 desc " + dataset.GetLayer(0).GetDescription());
		System.out.println("GDALTest: loadAndDetailVector: feature count layer 0: " + dataset.GetLayer(0).GetFeatureCount());
//		System.out.println("GDALTest: loadAndDetail: raster y size " + dataset.GetRasterYSize());
//		System.out.println("GDALTest: loadAndDetail: driver " + dataset.GetDriver().GetDescription());
//		System.out.println("GDALTest: loadAndDetail: filelist 0 " + dataset.GetFileList().get(0));
//		System.out.println("GDALTest: loadAndDetail: layer 0 " + dataset.GetLayer(0));
//		System.out.println("GDALTest: loadAndDetail: band 1 description " + dataset.GetRasterBand(1).GetDescription());

		System.out.println("GDALTest: loadAndDetailVector: number of metadata items " + dataset.GetLayer(0).GetMetadataDomainList().size());
		for (int layerIdx = 0; layerIdx < dataset.GetLayerCount(); layerIdx++)
		{
			Layer layer = dataset.GetLayer(layerIdx);
			for (int i=0; i < layer.GetMetadataDomainList().size(); i++)
			{
				int size = layer.GetMetadata_List(""+layer.GetMetadataDomainList().get(i)).size();
				for (int j=0; j < size; j++)
				{
					System.out.println("GDALTest: loadAndDetailVector: metadata for " + layer.GetMetadataDomainList().get(i) + " : " + layer.GetMetadata_List(""+layer.GetMetadataDomainList().get(i)).get(j));
				}
			}

			for (int i=0; i < layer.GetFeatureCount(); i++)
			{
				int fieldCount = layer.GetFeature(i).GetFieldCount();
				for (int j=0; j<fieldCount; j++)
				{
					System.out.println("GDALTest: loadAndDetailVector: feature " + i + " " + layer.GetFeature(i).GetFieldDefnRef(j).GetName());
					System.out.println("GDALTest: loadAndDetailVector: feature " + i + " " + layer.GetFeature(i).GetFieldAsDouble(j));
				}
			}

			System.out.println("GDALTest: loadAndDetailVector: geom field count for layer 0, feature 0: " + layer.GetFeature(0).GetGeomFieldCount());
			System.out.println("GDALTest: loadAndDetailVector: geom field count for layer 0, feature 0, area: " + layer.GetFeature(0).GetGeomFieldRef(0).Area());
		}
	}

	private void loadAndDetail(String filename)
	{
		Dataset dataset = gdal.Open(filename);
		System.out.println("---------------------------------------");
		System.out.println("GDALTest: loadAndDetail: desc " + dataset.GetDescription());
		System.out.println("GDALTest: loadAndDetail: layer count " + dataset.GetLayerCount());
		System.out.println("GDALTest: loadAndDetail: projection " + dataset.GetProjection());
		System.out.println("GDALTest: loadAndDetail: raster/band count " + dataset.GetRasterCount());
		System.out.println("GDALTest: loadAndDetail: raster x size " + dataset.GetRasterXSize());
		System.out.println("GDALTest: loadAndDetail: raster y size " + dataset.GetRasterYSize());
		System.out.println("GDALTest: loadAndDetail: driver " + dataset.GetDriver().GetDescription());
		System.out.println("GDALTest: loadAndDetail: filelist 0 " + dataset.GetFileList().get(0));
		System.out.println("GDALTest: loadAndDetail: layer 0 " + dataset.GetLayer(0));
		System.out.println("GDALTest: loadAndDetail: band 1 description " + dataset.GetRasterBand(1).GetDescription());


		for (int i=0; i<dataset.GetMetadataDomainList().size(); i++)
		{
			int size = dataset.GetMetadata_List(""+dataset.GetMetadataDomainList().get(i)).size();
			for (int j=0; j<size; j++)
			{
				System.out.println("GDALTest: loadAndDetail: metadata for " + dataset.GetMetadataDomainList().get(i) + " : " + dataset.GetMetadata_List(""+dataset.GetMetadataDomainList().get(i)).get(j));
			}
		}
//		for (int i=0; i<dataset.GetMetadata_List().size(); i++)
//		{
//			System.out.println("GDALTest: loadAndDetail: metadata list " + i + " " + dataset.GetMetadata_List().get(i));
//
//		}
//		while(dataset.GetMetadata_Dict().keys().hasMoreElements())
//		{
//			Object obj = dataset.GetMetadata_Dict().keys().nextElement();
//			System.out.println("GDALTest: GDALTest: metadata list " + obj + " " + dataset.GetMetadata_Dict().get(obj));
//		}
//		System.out.println("GDALTest: GDALTest: band 0 " + dataset.GetRasterBand(1).
		double[] fitsData = new double[dataset.getRasterXSize() * dataset.getRasterYSize()];
//		double[][] fitsData = new double[dataset.getRasterXSize()][dataset.getRasterYSize()];
		dataset.GetRasterBand(1).ReadRaster(0, 0, dataset.getRasterXSize(), dataset.getRasterYSize(), fitsData);
		System.out.println("GDALTest: loadAndDetail: NEEDS TO HAVE THE OPTIONS TO BE TRANSPOSED For FITS");
		for (int jj=0; jj<dataset.getRasterXSize(); jj++)
			System.out.println("GDALTest: loadAndDetail: values at " + fitsData[jj]);
//		for (int j=0; j<dataset.getRasterXSize(); j++)
//		{


//			for (int k=0; k<dataset.getRasterYSize(); k++)
//			{
//				System.out.println("GDALTest: loadAndDetail: index into main array " + (j*dataset.getRasterYSize() + k));
//				System.out.println("GDALTest: loadAndDetail: values at " + j + " " + k + " "+ fitsData[j*dataset.getRasterYSize() + k]);
//			}
//		}

		//This block can be used to export the layer a PNG - the Translate method can do this, we'll have to figure out which helper methods to create.
//		Vector<String> options = new Vector<String>();
//		options.add("-ot");
//		options.add("Byte"); //was UInt16
//		options.add("-scale");
//		gdal.Translate("/Users/steelrj1/Desktop/test1.png", dataset, new TranslateOptions(options));
	}

	public static void main(String[] args)
	{
		new GDALTest();
	}
}
