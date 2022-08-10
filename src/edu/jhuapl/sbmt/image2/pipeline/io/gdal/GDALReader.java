package edu.jhuapl.sbmt.image2.pipeline.io.gdal;

import java.util.ArrayList;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.ogr;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.BasePipelinePublisher;

public class GDALReader extends BasePipelinePublisher<Layer>
{
	private String filename;
	private boolean isVectorFile;

	public GDALReader(String filename, boolean isVectorFile)
	{
		this.filename = filename;
		this.isVectorFile = isVectorFile;

		outputs = new ArrayList<Layer>();
		loadData();
	}

	private void loadData()
	{
		if (!isVectorFile)
		{
			Dataset dataset = gdal.Open(filename);
			org.gdal.ogr.Layer getLayer = dataset.GetLayer(0);
		}
		else
		{
			DataSource datasource = ogr.Open(filename);
		}


	}
}
