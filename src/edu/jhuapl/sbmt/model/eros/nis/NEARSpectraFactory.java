package edu.jhuapl.sbmt.model.eros.nis;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.joda.time.DateTime;

import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraTypeFactory;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumBuilder;
import edu.jhuapl.sbmt.spectrum.rendering.BasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

public class NEARSpectraFactory
{
	public static void initializeModels(ISmallBodyModel smallBodyModel)
	{
		SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument> nisSpectra = new SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument>()
		{

			@Override
			public BasicSpectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel,
					BasicSpectrumInstrument instrument) throws IOException
			{
				NISSpectrum spectrum = new NISSpectrum(path, smallBodyModel, instrument);
				String str = path;
	            String strippedFileName = str.substring(str.lastIndexOf("/NIS/2000/") + 10);
	            String detailedTime = NISSearchModel.nisFileToObservationTimeMap.get(strippedFileName);
	            List<String> result = new ArrayList<String>();
	            result.add(str);
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	            try
				{
					spectrum.setDateTime(new DateTime(sdf.parse(detailedTime).getTime()));
				}
	            catch (ParseException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return spectrum;
			}

			@Override
			public BasicSpectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel,
					BasicSpectrumInstrument instrument, String timeString) throws IOException
			{
				return buildSpectrum(path, smallBodyModel, instrument);
			}

			@Override
			public IBasicSpectrumRenderer buildSpectrumRenderer(BasicSpectrum spectrum, ISmallBodyModel smallBodyModel) throws IOException
			{
				return new BasicSpectrumRenderer(spectrum, smallBodyModel, false);
			}

			@Override
			public IBasicSpectrumRenderer buildSpectrumRenderer(String path, ISmallBodyModel smallBodyModel, BasicSpectrumInstrument instrument) throws IOException
			{
				BasicSpectrum spectrum = buildSpectrum(path, smallBodyModel, instrument);
				return new BasicSpectrumRenderer(spectrum, smallBodyModel, false);
			}


		};
		SbmtSpectrumModelFactory.registerModel("NIS", nisSpectra, smallBodyModel);

		SpectraTypeFactory.registerSpectraType("NIS", NisQuery.getInstance(), NISSpectrumMath.getSpectrumMath(), "cm^-1", new NIS().getBandCenters());
	}
}
