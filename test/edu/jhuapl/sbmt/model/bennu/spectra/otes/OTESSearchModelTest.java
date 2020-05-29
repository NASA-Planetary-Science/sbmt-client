package edu.jhuapl.sbmt.model.bennu.spectra.otes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.jidesoft.swing.CheckBoxTree;

import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.model.structure.CircleModel;
import edu.jhuapl.saavtk.model.structure.CircleSelectionModel;
import edu.jhuapl.saavtk.model.structure.EllipseModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.model.structure.PolygonModel;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.client.SBMTModelBootstrap;
import edu.jhuapl.sbmt.client.SbmtModelFactory;
import edu.jhuapl.sbmt.client.SbmtModelManager;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentFactory;
import edu.jhuapl.sbmt.spectrum.model.core.search.HierarchicalSearchLeafNode;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchParametersModel;
import edu.jhuapl.sbmt.spectrum.model.hypertree.SpectraSearchDataCollection;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatisticsCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;

import crucible.crust.metadata.api.Metadata;

class OTESSearchModelTest
{
	static SpectrumSearchParametersModel searchParameters;
	static OTESSearchModel otesSearchModel;
	static SmallBodyViewConfig smallBodyConfig;
	static TreeModel treeModel;
	static CheckBoxTree checkBoxTree;
	static DefaultMutableTreeNode orexNode;

	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadHeadlessVtkLibraries();
        searchParameters = new SpectrumSearchParametersModel();
        Calendar.getInstance().set(2018, 12, 1);
        searchParameters.setStartDate(Calendar.getInstance().getTime());
        Calendar.getInstance().set(2018, 12, 31);
        searchParameters.setEndDate(Calendar.getInstance().getTime());
        boolean aplVersion = true;
        final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
        Configuration.setAPLVersion(aplVersion);



        SbmtMultiMissionTool.configureMission();
        Configuration.authenticate();

        SmallBodyViewConfig.initialize();
        smallBodyConfig = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190414);
        SmallBodyModel smallBodyModel = SbmtModelFactory.createSmallBodyModel(smallBodyConfig);
        SBMTModelBootstrap.initialize(smallBodyModel);

        treeModel = smallBodyConfig.hierarchicalSpectraSearchSpecification.getTreeModel();
		checkBoxTree = new CheckBoxTree(treeModel);
		orexNode = new DefaultMutableTreeNode("OREX");

		HashMap<ModelNames, Model> allModels = new HashMap<>();
        SpectraCollection collection = new SpectraCollection(smallBodyModel);

		allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
		allModels.put(ModelNames.SPECTRA_BOUNDARIES, new SpectrumBoundaryCollection(smallBodyModel, collection));

		allModels.put(ModelNames.SPECTRA_HYPERTREE_SEARCH, new SpectraSearchDataCollection(smallBodyModel));


        allModels.put(ModelNames.SPECTRA, collection);

		allModels.put(ModelNames.STATISTICS, new SpectrumStatisticsCollection());
		allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(smallBodyModel));
		allModels.put(ModelNames.POLYGON_STRUCTURES, new PolygonModel(smallBodyModel));
		allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(smallBodyModel));
		allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(smallBodyModel));
		allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(smallBodyModel));
		allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(smallBodyModel));

		SbmtModelManager modelManager = new SbmtModelManager(smallBodyModel, allModels);

        otesSearchModel = new OTESSearchModel(modelManager, SpectrumInstrumentFactory.getInstrumentForName("OTES"));

	}

	@AfterAll
	static void tearDownAfterClass() throws Exception
	{
	}

	@Test
	void testGetInstrument()
	{
		ISpectralInstrument otes = SpectrumInstrumentFactory.getInstrumentForName("OTES");
		ISpectralInstrument instrument = otesSearchModel.getInstrument();
		assertEquals(otes, instrument);
	}

	@Test
	void testGetSpectrumCollectionModelName()
	{
		ModelNames spectrumCollectionModelName = otesSearchModel.getSpectrumCollectionModelName();
		assertEquals(spectrumCollectionModelName, ModelNames.SPECTRA);
	}

	@Test
	void testGetSpectrumBoundaryCollectionModelName()
	{
		ModelNames spectrumCollectionModelName = otesSearchModel.getSpectrumBoundaryCollectionModelName();
		assertEquals(spectrumCollectionModelName, ModelNames.SPECTRA_BOUNDARIES);
	}

	@Test
	void testSaveSelectedSpectrumListToFile()
	{
		fail("Not yet implemented");
	}

	@Test
	void testSaveSpectrumListToFile()
	{
		fail("Not yet implemented");
	}

	@Test
	void testLoadSpectrumListFromFile()
	{
		fail("Not yet implemented");
	}

	@Test
	void testPerformSearch()
	{
		HierarchicalSearchLeafNode otesL2Node = new HierarchicalSearchLeafNode("OTES L2 Calibrated Radiance", 0, -1);
		TreePath[] treePath = new TreePath[] {new TreePath(new DefaultMutableTreeNode[] {orexNode, new DefaultMutableTreeNode(otesL2Node)})};
		otesSearchModel.performSearch(searchParameters, null, true, smallBodyConfig.hierarchicalSpectraSearchSpecification, treePath, null);
		List<OTESSpectrum> results = otesSearchModel.getSpectrumRawResults();
		Metadata metadata = otesSearchModel.store();
//		try
//		{
//			Serializers.serialize("SpectrumTest", metadata, new File("/Users/steelrj1/Desktop", "test.json"));
//		} catch (IOException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		System.out.println("OTESSearchModelTest: testPerformSearch: results size " + results.size());
	}

	@Test
	void testPerformHypertreeSearch()
	{
//		fail("Not yet implemented");
	}

	@Test
	void testRetrieve()
	{
		fail("Not yet implemented");
	}

	@Test
	void testStore()
	{
		fail("Not yet implemented");
	}



//	@Test
//	void testPopulateSpectrumMetadata()
//	{
//		fail("Not yet implemented");
//	}

}
