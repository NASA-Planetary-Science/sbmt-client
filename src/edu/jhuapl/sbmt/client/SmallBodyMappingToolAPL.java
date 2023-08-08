package edu.jhuapl.sbmt.client;

import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.sbmt.core.body.ISmallBodyModel;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESQuery;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESSearchModel;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESSpectrum;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESSpectrumMath;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSQuery;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSSearchModel;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSSpectrum;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSSpectrumMath;
import edu.jhuapl.sbmt.model.eros.nis.NIS;
import edu.jhuapl.sbmt.model.eros.nis.NISSearchModel;
import edu.jhuapl.sbmt.model.eros.nis.NISSpectrum;
import edu.jhuapl.sbmt.model.eros.nis.NISSpectrumMath;
import edu.jhuapl.sbmt.model.eros.nis.NisQuery;
import edu.jhuapl.sbmt.model.phobos.MEGANE;
import edu.jhuapl.sbmt.model.phobos.MEGANEQuery;
import edu.jhuapl.sbmt.model.phobos.MEGANESpectrumMath;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3Query;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3SearchModel;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3SpectrumMath;
import edu.jhuapl.sbmt.spectrum.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfig;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraTypeFactory;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentFactory;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.ISpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumBuilder;
import edu.jhuapl.sbmt.spectrum.model.core.search.BaseSpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.rendering.AdvancedSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.rendering.BasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.service.SBMTSpectraFactory;
import edu.jhuapl.sbmt.spectrum.service.SpectrumSearchModelBuilder;

/**
 * This class contains the "main" function called at the start of the program
 * for the APL internal version. It sets up some APL version specific
 * configuration options and then calls the public (non-APL) version's main
 * function.
 */
public class SmallBodyMappingToolAPL
{
	// TODO: This needs a new home
	static
	{
		SpectrumInstrumentFactory.registerType("NIS", new NIS());
		SpectrumInstrumentFactory.registerType("OTES", new OTES());
		SpectrumInstrumentFactory.registerType("OVIRS", new OVIRS());
		SpectrumInstrumentFactory.registerType("NIRS3", new NIRS3());
		SpectrumInstrumentFactory.registerType("MEGANE", new MEGANE());

		SBMTSpectraFactory.registerModel("NIS", new SpectrumSearchModelBuilder()
		{

			@Override
			public ISpectrumSearchModel<NISSpectrum> buildSearchModel(double diagonalLength)
			{
				return new NISSearchModel(SpectrumInstrumentFactory.getInstrumentForName("NIS"));
			}
		});

		SbmtSpectrumModelFactory.registerModel("NIS",
				new SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument>()
				{

					@Override
					public IBasicSpectrumRenderer<NISSpectrum> buildSpectrumRenderer(BasicSpectrum spectrum,
							ISmallBodyModel smallBodyModel, boolean headless) throws IOException
					{
						return new BasicSpectrumRenderer<NISSpectrum>((NISSpectrum) spectrum, smallBodyModel, headless);
					}

					@Override
					public IBasicSpectrumRenderer<NISSpectrum> buildSpectrumRenderer(String path,
							ISmallBodyModel smallBodyModel, BasicSpectrumInstrument instrument, boolean headless,
							SpectrumInstrumentConfig spectrumConfig) throws IOException
					{
						NISSpectrum spectrum = new NISSpectrum(path,
								spectrumConfig.getHierarchicalSpectraSearchSpecification(),
								smallBodyModel.getBoundingBoxDiagonalLength(), instrument);
						return new BasicSpectrumRenderer<NISSpectrum>(spectrum, smallBodyModel, headless);
					}

					@Override
					public BasicSpectrum buildSpectrum(List<String> result, ISmallBodyModel smallBodyModel,
							BasicSpectrumInstrument instrument, String timeString,
							SpectrumInstrumentConfig spectrumConfig) throws IOException
					{
						return buildSpectrum(result, smallBodyModel, instrument, spectrumConfig);
					}

					@Override
					public BasicSpectrum buildSpectrum(List<String> result, ISmallBodyModel smallBodyModel,
							BasicSpectrumInstrument instrument, SpectrumInstrumentConfig spectrumConfig)
							throws IOException
					{
						int id, year, dayOfYear;
						if (result.size() > 1)
						{
							id = Integer.parseInt(result.get(0).split("/")[3]);
							year = Integer.parseInt(result.get(1));
							dayOfYear = Integer.parseInt(result.get(2));
						} else
						{
							String[] components = result.get(0).split("/");
							id = Integer.parseInt((components[4].split("N0")[1]).split("\\.")[0]);
							year = Integer.parseInt(components[2]);
							dayOfYear = Integer.parseInt(components[3]);
						}

						String str = "/NIS/";
						str += year + "/";

						if (dayOfYear < 10)
							str += "00";
						else if (dayOfYear < 100)
							str += "0";

						str += dayOfYear + "/";

						str += "N0" + id + ".NIS";

						NISSpectrum spectrum = new NISSpectrum(str,
								spectrumConfig.getHierarchicalSpectraSearchSpecification(),
								smallBodyModel.getBoundingBoxDiagonalLength(), instrument);
//					String str = path;
//		            String strippedFileName = str.substring(str.lastIndexOf("/NIS/2000/") + 10);
//		            System.out.println(
//							"NEARSpectraFactory.initializeModels(...).new SpectrumBuilder() {...}: buildSpectrum: stripped file name " + strippedFileName);
//		            String detailedTime = NISSearchModel.nisFileToObservationTimeMap.get(strippedFileName);
//		            System.out.println(
//							"NEARSpectraFactory.initializeModels(...).new SpectrumBuilder() {...}: buildSpectrum: detailed time " + detailedTime);
//		            List<String> result = new ArrayList<String>();
//		            result.add(str);
//		            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
//		            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//		            try
//					{
//						spectrum.setDateTime(new DateTime(sdf.parse(detailedTime).getTime()));
//					}
//		            catch (ParseException e)
//					{
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}

						return spectrum;
					}
				});

		SBMTSpectraFactory.registerModel("OTES", new SpectrumSearchModelBuilder()
		{

			@Override
			public ISpectrumSearchModel buildSearchModel(double diagonalLength)
			{
				return new OTESSearchModel(SpectrumInstrumentFactory.getInstrumentForName("OTES"));
			}
		});

		SbmtSpectrumModelFactory.registerModel("OTES",
				new SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument>()
				{

					@Override
					public IBasicSpectrumRenderer buildSpectrumRenderer(BasicSpectrum spectrum,
							ISmallBodyModel smallBodyModel, boolean headless) throws IOException
					{
						return new AdvancedSpectrumRenderer(spectrum, smallBodyModel, false);
					}

					@Override
					public IBasicSpectrumRenderer buildSpectrumRenderer(String path, ISmallBodyModel smallBodyModel,
							BasicSpectrumInstrument instrument, boolean headless,
							SpectrumInstrumentConfig spectrumConfig) throws IOException
					{
						OTESSpectrum spectrum = new OTESSpectrum(path,
								spectrumConfig.getHierarchicalSpectraSearchSpecification(),
								smallBodyModel.getBoundingBoxDiagonalLength(), instrument);
						return new AdvancedSpectrumRenderer(spectrum, smallBodyModel, false);
					}

					@Override
					public BasicSpectrum buildSpectrum(List<String> result, ISmallBodyModel smallBodyModel,
							BasicSpectrumInstrument instrument, String timeString,
							SpectrumInstrumentConfig spectrumConfig) throws IOException
					{
						OTESSpectrum spectrum = new OTESSpectrum(result.get(0),
								spectrumConfig.getHierarchicalSpectraSearchSpecification(),
								smallBodyModel.getBoundingBoxDiagonalLength(), instrument);
						spectrum.setDateTime(new DateTime(Long.parseLong(timeString), DateTimeZone.UTC));
						return spectrum;
					}

					@Override
					public BasicSpectrum buildSpectrum(List<String> result, ISmallBodyModel smallBodyModel,
							BasicSpectrumInstrument instrument, SpectrumInstrumentConfig spectrumConfig)
							throws IOException
					{
						OTESSpectrum spectrum = new OTESSpectrum(result.get(0),
								spectrumConfig.getHierarchicalSpectraSearchSpecification(),
								smallBodyModel.getBoundingBoxDiagonalLength(), instrument);
						return spectrum;
					}
				});

		SBMTSpectraFactory.registerModel("OVIRS", new SpectrumSearchModelBuilder()
		{

			@Override
			public ISpectrumSearchModel buildSearchModel(double diagonalLength)
			{
				return new OVIRSSearchModel(SpectrumInstrumentFactory.getInstrumentForName("OVIRS"));
			}
		});

		SbmtSpectrumModelFactory.registerModel("OVIRS",
				new SpectrumBuilder<String, ISmallBodyModel, BasicSpectrumInstrument>()
				{

					@Override
					public IBasicSpectrumRenderer buildSpectrumRenderer(BasicSpectrum spectrum,
							ISmallBodyModel smallBodyModel, boolean headless) throws IOException
					{
						return new AdvancedSpectrumRenderer(spectrum, smallBodyModel, false);
					}

					@Override
					public IBasicSpectrumRenderer buildSpectrumRenderer(String path, ISmallBodyModel smallBodyModel,
							BasicSpectrumInstrument instrument, boolean headless,
							SpectrumInstrumentConfig spectrumConfig) throws IOException
					{
						OVIRSSpectrum spectrum = new OVIRSSpectrum(path,
								spectrumConfig.getHierarchicalSpectraSearchSpecification(),
								smallBodyModel.getBoundingBoxDiagonalLength(), instrument);
						return new AdvancedSpectrumRenderer(spectrum, smallBodyModel, false);
					}

					@Override
					public BasicSpectrum buildSpectrum(List<String> result, ISmallBodyModel smallBodyModel,
							BasicSpectrumInstrument instrument, String timeString,
							SpectrumInstrumentConfig spectrumConfig) throws IOException
					{
						OVIRSSpectrum spectrum = new OVIRSSpectrum(result.get(3),
								spectrumConfig.getHierarchicalSpectraSearchSpecification(),
								smallBodyModel.getBoundingBoxDiagonalLength(), instrument);
						spectrum.setDateTime(new DateTime(Long.parseLong(timeString), DateTimeZone.UTC));
						return spectrum;
					}

					@Override
					public BasicSpectrum buildSpectrum(List<String> result, ISmallBodyModel smallBodyModel,
							BasicSpectrumInstrument instrument, SpectrumInstrumentConfig spectrumConfig)
							throws IOException
					{
						OVIRSSpectrum spectrum = new OVIRSSpectrum(result.get(3),
								spectrumConfig.getHierarchicalSpectraSearchSpecification(),
								smallBodyModel.getBoundingBoxDiagonalLength(), instrument);
						return spectrum;
					}
				});

		SBMTSpectraFactory.registerModel("NIRS3", new SpectrumSearchModelBuilder()
		{

			@Override
			public ISpectrumSearchModel buildSearchModel(double diagonalLength)
			{
				return new NIRS3SearchModel(SpectrumInstrumentFactory.getInstrumentForName("NIRS3"));
			}
		});

		SBMTSpectraFactory.registerModel("MEGANE", new SpectrumSearchModelBuilder()
		{

			@Override
			public ISpectrumSearchModel buildSearchModel(double diagonalLength)
			{
				return new BaseSpectrumSearchModel<>(SpectrumInstrumentFactory.getInstrumentForName("MEGANE"));


//				return new MEGANESearchModel(SpectrumInstrumentFactory.getInstrumentForName("MEGANE"));
			}
		});

		SpectraTypeFactory.registerSpectraType("OTES", OTESQuery.getInstance(), OTESSpectrumMath.getInstance(), "cm^-1",
				new OTES().getBandCenters());
		SpectraTypeFactory.registerSpectraType("OVIRS", OVIRSQuery.getInstance(), OVIRSSpectrumMath.getInstance(), "um",
				new OVIRS().getBandCenters());
		SpectraTypeFactory.registerSpectraType("NIS", NisQuery.getInstance(), NISSpectrumMath.getSpectrumMath(), "nm",
				new NIS().getBandCenters());
		SpectraTypeFactory.registerSpectraType("NIRS3", NIRS3Query.getInstance(), NIRS3SpectrumMath.getInstance(), "nm",
				new NIRS3().getBandCenters());
		SpectraTypeFactory.registerSpectraType("MEGANE", MEGANEQuery.getInstance(), MEGANESpectrumMath.getInstance(),
				"cm^-1", new MEGANE().getBandCenters());

	}

	public static void main(String[] args)
	{
		String opSysName = System.getProperty("os.name").toLowerCase();
		if (opSysName.contains("mac"))
		{
			// to set the name of the app in the Mac App menu:
			System.setProperty("apple.awt.application.name", "Small Body Mapping Tool");
			// to show the menu bar at the top of the screen:
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			// // to show a more mac-like file dialog box
			// System.setProperty("apple.awt.fileDialogForDirectories", "true");
		}

		Configuration.setAPLVersion(true);

		SbmtMultiMissionTool.setEnableAuthentication(true);

		// Call the standard client main function
		SbmtMultiMissionTool.main(args);
	}
}
