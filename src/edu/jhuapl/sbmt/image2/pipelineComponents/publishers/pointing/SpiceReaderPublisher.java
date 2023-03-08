package edu.jhuapl.sbmt.image2.pipelineComponents.publishers.pointing;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.pipeline.publisher.BasePipelinePublisher;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;

public class SpiceReaderPublisher extends BasePipelinePublisher<SpicePointingProvider>
{
	private SpicePointingProvider pointingProvider;
	private SpiceInfo spiceInfo;
	private String mkFilename;
	private String instName;

	public SpiceReaderPublisher(String mkFilename, SpiceInfo spiceInfo, String instName)
	{
		this.spiceInfo = spiceInfo;
		this.mkFilename = mkFilename;
		this.instName = instName;
		try
		{
			loadPointing();
			outputs.add(pointingProvider);
			if (spiceInfo.getBodyNamesToBind().length == 0) return;
			for (String name : spiceInfo.getBodyNamesToBind()) outputs.add(pointingProvider);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadPointing() throws Exception
	{
		Path mkPath = Paths.get(mkFilename);
		SpicePointingProvider.Builder builder =
				SpicePointingProvider.builder(ImmutableList.copyOf(new Path[] {mkPath}), spiceInfo.getBodyName(),
						spiceInfo.getBodyFrameName(), spiceInfo.getScId(), spiceInfo.getScFrameName());

		for (String bodyNameToBind : spiceInfo.getBodyNamesToBind()) builder.bindEphemeris(bodyNameToBind);
		for (String bodyFrameToBind : spiceInfo.getBodyFramesToBind()) builder.bindFrame(bodyFrameToBind);
		for (String instrumentNameToBind : spiceInfo.getInstrumentNamesToBind())
		{
			builder.addInstrumentFrame(instrumentNameToBind);
		}

        pointingProvider = builder.build();
        pointingProvider.setCurrentInstrumentName(instName);
	}
}
