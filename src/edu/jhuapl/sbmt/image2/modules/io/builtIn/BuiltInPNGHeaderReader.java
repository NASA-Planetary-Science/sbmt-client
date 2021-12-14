package edu.jhuapl.sbmt.image2.modules.io.builtIn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.sbmt.image2.pipeline.publisher.BasePipelinePublisher;

import nom.tam.fits.FitsException;

public class BuiltInPNGHeaderReader extends BasePipelinePublisher<HashMap<String, String>>
{

	public BuiltInPNGHeaderReader(String filename)
	{
		outputs = new ArrayList<HashMap<String, String>>();
		try
		{
			outputs.add(loadHeaders(filename));
		}
		catch (IOException | FitsException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private HashMap<String, String> loadHeaders(String filename) throws IOException, FitsException
	{
		HashMap<String, String> properties = new HashMap<String, String>();
		return properties;
	}

}
